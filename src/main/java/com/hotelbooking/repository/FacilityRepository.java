package com.hotelbooking.repository;

import com.hotelbooking.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
    boolean existsByNameIgnoreCase(String name);
}
