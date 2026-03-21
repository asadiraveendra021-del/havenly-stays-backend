package com.asadi.havenly_stays.config;

import com.asadi.havenly_stays.entity.Facility;
import com.asadi.havenly_stays.repository.FacilityRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FacilitySeedConfig implements CommandLineRunner {

    private final FacilityRepository facilityRepository;

    public FacilitySeedConfig(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    @Override
    public void run(String... args) {
        if (facilityRepository.count() > 0) {
            return;
        }

        List<Facility> facilities = List.of(
                Facility.builder().name("Free WiFi").iconCode("wifi").category("Connectivity").build(),
                Facility.builder().name("Swimming Pool").iconCode("pool").category("Leisure").build(),
                Facility.builder().name("Parking").iconCode("parking").category("Transport").build(),
                Facility.builder().name("Gym").iconCode("gym").category("Fitness").build(),
                Facility.builder().name("Spa").iconCode("spa").category("Wellness").build()
        );

        facilityRepository.saveAll(facilities);
    }
}
