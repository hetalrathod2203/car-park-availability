package com.wego.car_park_availability.repository;

import com.wego.car_park_availability.entity.CarPark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarParkRepository extends JpaRepository<CarPark, String> {
    
    @Query(value = """
        SELECT cp.* FROM car_parks cp
        JOIN (
            SELECT car_park_no, SUM(available_lots) as total_available
            FROM car_park_availability 
            WHERE (car_park_no, update_datetime) IN (
                SELECT car_park_no, MAX(update_datetime)
                FROM car_park_availability
                GROUP BY car_park_no
            )
            GROUP BY car_park_no
            HAVING SUM(available_lots) > 0
        ) av ON cp.car_park_no = av.car_park_no
        WHERE cp.latitude BETWEEN :latitude - 0.05 AND :latitude + 0.05
        AND cp.longitude BETWEEN :longitude - 0.05 AND :longitude + 0.05
        ORDER BY (
            POWER(cp.latitude - :latitude, 2) + POWER(cp.longitude - :longitude, 2)
        )
        """, nativeQuery = true)
    Page<CarPark> findNearestWithAvailability(
        @Param("latitude") Double latitude, 
        @Param("longitude") Double longitude, 
        Pageable pageable
    );
}