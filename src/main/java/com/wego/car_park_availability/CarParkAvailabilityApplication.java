package com.wego.car_park_availability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class CarParkAvailabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarParkAvailabilityApplication.class, args);
	}

}
