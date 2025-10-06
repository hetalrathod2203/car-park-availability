package com.wego.car_park_availability.service;

import com.wego.car_park_availability.dto.CarParkResponse;
import com.wego.car_park_availability.entity.CarPark;
import com.wego.car_park_availability.entity.CarParkAvailability;
import com.wego.car_park_availability.repository.CarParkRepository;
import com.wego.car_park_availability.repository.CarParkAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarParkService {
    
    private final CarParkRepository carParkRepository;
    private final CarParkAvailabilityRepository availabilityRepository;
    
    @Transactional(readOnly = true)
    public List<CarParkResponse> findNearestCarParks(Double latitude, Double longitude, int page, int perPage) {
        Pageable pageable = PageRequest.of(page - 1, perPage);
        Page<CarPark> carParks = carParkRepository.findNearestWithAvailability(latitude, longitude, pageable);
        
        return carParks.getContent().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    private CarParkResponse mapToResponse(CarPark carPark) {
        List<CarParkAvailability> availabilities = availabilityRepository.findLatestByCarParkNo(carPark.getCarParkNo());
        
        int totalLots = availabilities.stream().mapToInt(CarParkAvailability::getTotalLots).sum();
        int availableLots = availabilities.stream().mapToInt(CarParkAvailability::getAvailableLots).sum();
        
        return new CarParkResponse(
            carPark.getAddress(),
            carPark.getLatitude(),
            carPark.getLongitude(),
            totalLots,
            availableLots
        );
    }
}