package com.wego.car_park_availability.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "car_parks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarPark {
    
    @Id
    @Column(name = "car_park_no")
    private String carParkNo;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "x_coord")
    private Double xCoord;
    
    @Column(name = "y_coord")
    private Double yCoord;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "car_park_type")
    private String carParkType;
    
    @Column(name = "type_of_parking_system")
    private String typeOfParkingSystem;
    
    @Column(name = "short_term_parking")
    private String shortTermParking;
    
    @Column(name = "free_parking")
    private String freeParking;
    
    @Column(name = "night_parking")
    private String nightParking;
    
    @Column(name = "car_park_decks")
    private Integer carParkDecks;
    
    @Column(name = "gantry_height")
    private Double gantryHeight;
    
    @Column(name = "car_park_basement")
    private String carParkBasement;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}