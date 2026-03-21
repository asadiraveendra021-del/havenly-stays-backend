package com.asadi.havenly_stays.repository;

import com.asadi.havenly_stays.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
    boolean existsByNameIgnoreCase(String name);
}
