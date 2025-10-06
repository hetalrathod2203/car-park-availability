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
        ORDER BY (
            6371 * acos(
                cos(radians(:latitude)) * cos(radians(cp.latitude)) *
                cos(radians(cp.longitude) - radians(:longitude)) +
                sin(radians(:latitude)) * sin(radians(cp.latitude))
            )
        )
        """, nativeQuery = true)
    Page<CarPark> findNearestWithAvailability(
        @Param("latitude") Double latitude, 
        @Param("longitude") Double longitude, 
        Pageable pageable
    );
}