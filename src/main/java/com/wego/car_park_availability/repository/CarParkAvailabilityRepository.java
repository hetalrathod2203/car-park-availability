package com.wego.car_park_availability.repository;

import com.wego.car_park_availability.entity.CarParkAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarParkAvailabilityRepository extends JpaRepository<CarParkAvailability, Long> {
    
    @Query("""
        SELECT ca FROM CarParkAvailability ca 
        WHERE ca.carParkNo = :carParkNo 
        AND ca.updateDatetime = (
            SELECT MAX(ca2.updateDatetime) 
            FROM CarParkAvailability ca2 
            WHERE ca2.carParkNo = :carParkNo
        )
        """)
    List<CarParkAvailability> findLatestByCarParkNo(@Param("carParkNo") String carParkNo);
    
    Optional<CarParkAvailability> findByCarParkNoAndLotType(String carParkNo, String lotType);
}