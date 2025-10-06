package com.wego.car_park_availability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Car park information with availability")
public class CarParkResponse {
    @Schema(description = "Car park address", example = "BLK 401-413, 460-463 HOUGANG AVENUE 10")
    private String address;
    
    @Schema(description = "Car park latitude", example = "1.37429")
    private Double latitude;
    
    @Schema(description = "Car park longitude", example = "103.896")
    private Double longitude;
    
    @Schema(description = "Total parking lots", example = "693")
    private Integer totalLots;
    
    @Schema(description = "Available parking lots", example = "182")
    private Integer availableLots;
}