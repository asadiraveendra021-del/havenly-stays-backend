package com.hotelbooking.repository;

import com.hotelbooking.entity.HotelImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelImageRepository extends JpaRepository<HotelImage, Long> {
}
