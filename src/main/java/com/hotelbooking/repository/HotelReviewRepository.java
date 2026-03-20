package com.hotelbooking.repository;

import com.hotelbooking.entity.HotelReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelReviewRepository extends JpaRepository<HotelReview, Long> {
    List<HotelReview> findByHotelId(Long hotelId);

    long countByHotelId(Long hotelId);

    @Query("select avg(r.rating) from HotelReview r where r.hotel.id = :hotelId")
    Double findAverageRatingByHotelId(@Param("hotelId") Long hotelId);
}
