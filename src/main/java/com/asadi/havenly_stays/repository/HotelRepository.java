package com.asadi.havenly_stays.repository;

import com.asadi.havenly_stays.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    Page<Hotel> findByIsDeletedFalse(Pageable pageable);
    Page<Hotel> findByIsDeletedFalseAndNameContainingIgnoreCase(String name, Pageable pageable);
}
