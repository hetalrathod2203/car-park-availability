package com.wego.car_park_availability.service;

import com.opencsv.CSVReader;
import com.wego.car_park_availability.dto.CoordinateConversionResponse;
import com.wego.car_park_availability.dto.OneMapAuthResponse;
import com.wego.car_park_availability.entity.CarPark;
import com.wego.car_park_availability.entity.CarParkAvailability;
import com.wego.car_park_availability.repository.CarParkRepository;
import com.wego.car_park_availability.repository.CarParkAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataImportService {
    
    private final CarParkRepository carParkRepository;
    private final CarParkAvailabilityRepository availabilityRepository;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${carpark.onemap.api.url}")
    private String oneMapApiUrl;
    
    @Value("${carpark.onemap.api.auth-url}")
    private String oneMapAuthUrl;
    
    @Value("${carpark.onemap.api.email}")
    private String oneMapEmail;
    
    @Value("${carpark.onemap.api.password}")
    private String oneMapPassword;
    
    @Value("${carpark.api.availability-url}")
    private String carparkApiUrl;
    
    @Value("${app.threading.csv-import.pool-size:1}")
    private int threadPoolSize;
    
    public void importCarParkData(MultipartFile csvFile) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(csvFile.getInputStream()))) {
            
            String[] headers = reader.readNext();
            log.info("CSV Processing Started : {}", String.join(", ", headers));
            
            List<String[]> allRows = new ArrayList<>();
            String[] line;
            while ((line = reader.readNext()) != null) {
                allRows.add(line);
            }
            
            int totalRows = allRows.size();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            
            ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
            log.info("Using {} threads for CSV processing", threadPoolSize);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (int i = 0; i < allRows.size(); i++) {
                final String[] rowData = allRows.get(i);
                final int rowNumber = i + 1;
                
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        CarPark carPark = parseCarParkFromCsv(rowData);
                        convertAndSetCoordinates(carPark);
                        saveCarPark(carPark);
                        
                        int current = successCount.incrementAndGet();
                        if (current % 100 == 0) {
                            log.info("Processed {} car parks", current);
                        }
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        log.warn("Failed to process row {}: {}", rowNumber, e.getMessage());
                    }
                }, executor);
                
                futures.add(future);
            }
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();
            
            log.info("Car park data import completed: {}/{} records processed successfully", 
                    successCount.get(), totalRows);
        } catch (Exception e) {
            log.error("Error importing car park data", e);
            throw new RuntimeException("Failed to import car park data: " + e.getMessage());
        }
    }
    
    @Transactional
    public void saveCarPark(CarPark carPark) {
        carParkRepository.save(carPark);
    }
    
    private CarPark parseCarParkFromCsv(String[] data) {
        if (data.length < 12) {
            throw new IllegalArgumentException("Invalid CSV row: expected 12 columns, got " + data.length);
        }
        
        CarPark carPark = new CarPark();
        carPark.setCarParkNo(data[0].trim());
        carPark.setAddress(data[1].trim());
        carPark.setXCoord(Double.parseDouble(data[2].trim()));
        carPark.setYCoord(Double.parseDouble(data[3].trim()));
        carPark.setCarParkType(data[4].trim());
        carPark.setTypeOfParkingSystem(data[5].trim());
        carPark.setShortTermParking(data[6].trim());
        carPark.setFreeParking(data[7].trim());
        carPark.setNightParking(data[8].trim());
        carPark.setCarParkDecks(Integer.parseInt(data[9].trim()));
        carPark.setGantryHeight(Double.parseDouble(data[10].trim()));
        carPark.setCarParkBasement(data[11].trim());
        return carPark;
    }
    
    private void convertAndSetCoordinates(CarPark carPark) {
        String token = getOneMapToken();
        if (token == null) {
            throw new RuntimeException("Failed to get OneMap token for car park: " + carPark.getCarParkNo());
        }
        
        WebClient webClient = webClientBuilder.build();
        String url = String.format("%s?X=%.6f&Y=%.6f", oneMapApiUrl, carPark.getXCoord(), carPark.getYCoord());
        
        CoordinateConversionResponse response = webClient.get()
            .uri(url)
            .header("Authorization", token)
            .retrieve()
            .bodyToMono(CoordinateConversionResponse.class)
            .block();
        
        if (response != null) {
            carPark.setLatitude(response.getLatitude());
            carPark.setLongitude(response.getLongitude());
            log.debug("Converted coordinates for {}: {}, {}", carPark.getCarParkNo(), response.getLatitude(), response.getLongitude());
        } else {
            throw new RuntimeException("No response from OneMap API for car park: " + carPark.getCarParkNo());
        }
    }
    
    private String getOneMapToken() {
        try {
            WebClient webClient = webClientBuilder.build();
            
            String requestBody = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", oneMapEmail, oneMapPassword);
            log.debug("OneMap auth request: {}", requestBody);
            
            OneMapAuthResponse response = webClient.post()
                .uri(oneMapAuthUrl)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), clientResponse -> {
                    log.error("OneMap auth failed with status: {}", clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class)
                        .doOnNext(body -> log.error("OneMap auth error response: {}", body))
                        .then(Mono.error(new RuntimeException("OneMap authentication failed")));
                })
                .bodyToMono(OneMapAuthResponse.class)
                .block();
            
            if (response != null && response.getAccessToken() != null) {
                log.debug("OneMap token obtained successfully");
                return response.getAccessToken();
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get OneMap authentication token: {}", e.getMessage());
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Transactional
    public void updateAvailabilityData() {
        try {
            WebClient webClient = webClientBuilder.build();
            
            Mono<Map> response = webClient.get()
                .uri(carparkApiUrl)
                .retrieve()
                .bodyToMono(Map.class);
            
            Map<String, Object> data = response.block();
            if (data != null && data.containsKey("items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
                int processedCount = 0;
                int skippedCount = 0;
                Set<String> skippedCarParks = new HashSet<>();
                
                for (Map<String, Object> item : items) {
                    String timestamp = (String) item.get("timestamp");
                    LocalDateTime updateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    
                    List<Map<String, Object>> carparkData = (List<Map<String, Object>>) item.get("carpark_data");
                    
                    for (Map<String, Object> carpark : carparkData) {
                        String carparkNo = (String) carpark.get("carpark_number");
                        
                        if (carParkRepository.existsById(carparkNo)) {
                            List<Map<String, Object>> carparkInfo = (List<Map<String, Object>>) carpark.get("carpark_info");
                            
                            for (Map<String, Object> info : carparkInfo) {
                                String lotType = (String) info.get("lot_type");
                                int totalLots = Integer.parseInt((String) info.get("total_lots"));
                                int availableLots = Integer.parseInt((String) info.get("lots_available"));
                                
                                CarParkAvailability availability = availabilityRepository
                                    .findByCarParkNoAndLotType(carparkNo, lotType)
                                    .orElse(new CarParkAvailability());
                                
                                availability.setCarParkNo(carparkNo);
                                availability.setTotalLots(totalLots);
                                availability.setAvailableLots(availableLots);
                                availability.setLotType(lotType);
                                availability.setUpdateDatetime(updateTime);
                                
                                availabilityRepository.save(availability);
                            }
                        } else {
                            skippedCarParks.add(carparkNo);
                            skippedCount++;
                        }
                        processedCount++;
                    }
                }
                if (!skippedCarParks.isEmpty()) {
                    log.warn("Skipped {} unknown car parks: {}", skippedCount, skippedCarParks);
                }
                log.info("Availability data update completed: {} processed, {} skipped", processedCount, skippedCount);
            }
        } catch (Exception e) {
            log.error("Error updating availability data", e);
        }
    }
}