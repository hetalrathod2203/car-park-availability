package com.wego.car_park_availability.controller;

import com.wego.car_park_availability.service.DataImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative operations for data management")
public class DataImportController {
    
    private final DataImportService dataImportService;
    
    @PostMapping(value = "/import-carparks", consumes = "multipart/form-data")
    @Operation(summary = "Import car park data", description = "Imports car park information from uploaded CSV file")
    @ApiResponse(responseCode = "200", description = "Import initiated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid file or file upload error")
    public ResponseEntity<String> importCarParks(
            @Parameter(description = "CSV file containing car park data", required = true,
                      content = @Content(mediaType = "multipart/form-data",
                                       schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a CSV file to upload");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("File must have a valid filename");
        }
        
        if (!filename.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest().body("Please upload a CSV file");
        }
        
        dataImportService.importCarParkData(file);
        return ResponseEntity.ok("Car park data import initiated");
    }
    
    @PostMapping("/update-availability")
    @Operation(summary = "Update availability data", description = "Fetches latest availability data from Update API")
    @ApiResponse(responseCode = "200", description = "Update initiated successfully")
    public ResponseEntity<String> updateAvailability() {
        dataImportService.updateAvailabilityData();
        return ResponseEntity.ok("Availability data update initiated");
    }
}