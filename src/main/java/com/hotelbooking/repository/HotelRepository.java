package com.hotelbooking.repository;

import com.hotelbooking.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    Page<Hotel> findByIsDeletedFalse(Pageable pageable);
    Page<Hotel> findByIsDeletedFalseAndNameContainingIgnoreCase(String name, Pageable pageable);
}
