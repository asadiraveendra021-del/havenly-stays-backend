package com.asadi.havenly_stays.repository;

import com.asadi.havenly_stays.entity.Hotel;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    Page<Hotel> findByIsDeletedFalse(Pageable pageable);
    Page<Hotel> findByIsDeletedFalseAndNameContainingIgnoreCase(String name, Pageable pageable);

    List<Hotel> findByIsDeletedFalse();

    @Query("select h from Hotel h " +
            "where h.isDeleted = false " +
            "and (lower(h.name) like lower(concat('%', :location, '%')) " +
            "or lower(h.city) like lower(concat('%', :location, '%')))")
    List<Hotel> searchByLocation(@Param("location") String location);
}
