package com.wego.car_park_availability.controller;

import com.wego.car_park_availability.dto.CarParkResponse;
import com.wego.car_park_availability.service.CarParkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/carparks")
@RequiredArgsConstructor
@Validated
@Tag(name = "Car Parks", description = "Car park availability API")
public class CarParkController {
    
    private final CarParkService carParkService;
    
    @GetMapping("/nearest")
    @Operation(summary = "Get nearest car parks", description = "Returns car parks sorted by distance with availability information")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved car parks")
    @ApiResponse(responseCode = "400", description = "Invalid parameters")
    public ResponseEntity<List<CarParkResponse>> getNearestCarParks(
            @Parameter(description = "User latitude (-90 to 90)", required = true, example = "1.37326")
            @RequestParam @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
            @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90") Double latitude,
            
            @Parameter(description = "User longitude (-180 to 180)", required = true, example = "103.897")
            @RequestParam @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
            @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180") Double longitude,
            
            @Parameter(description = "Page number", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than 0") Integer page,
            
            @Parameter(description = "Results per page", example = "10")
            @RequestParam(name = "per_page", defaultValue = "10") 
            @Min(value = 1, message = "Per page must be greater than 0") Integer perPage) {
        
        List<CarParkResponse> carParks = carParkService.findNearestCarParks(latitude, longitude, page, perPage);
        return ResponseEntity.ok(carParks);
    }
}