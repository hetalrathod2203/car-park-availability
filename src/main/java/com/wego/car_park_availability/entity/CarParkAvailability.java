package com.wego.car_park_availability.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "car_park_availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarParkAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "car_park_no")
    private String carParkNo;
    
    @Column(name = "total_lots")
    private Integer totalLots;
    
    @Column(name = "available_lots")
    private Integer availableLots;
    
    @Column(name = "lot_type")
    private String lotType;
    
    @Column(name = "update_datetime")
    private LocalDateTime updateDatetime;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}