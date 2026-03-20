package com.asadi.havenly_stays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.asadi.havenly_stays", "com.hotelbooking"})
@EnableJpaRepositories(basePackages = {"com.asadi.havenly_stays.repository", "com.hotelbooking.repository"})
@EntityScan(basePackages = {"com.asadi.havenly_stays.entity", "com.hotelbooking.entity"})
@EnableScheduling
public class HavenlyStaysApplication {

	public static void main(String[] args) {
		SpringApplication.run(HavenlyStaysApplication.class, args);
	}

}
