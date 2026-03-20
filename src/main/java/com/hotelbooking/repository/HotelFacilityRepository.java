package com.hotelbooking.repository;

import com.hotelbooking.entity.HotelFacility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelFacilityRepository extends JpaRepository<HotelFacility, Long> {
}
