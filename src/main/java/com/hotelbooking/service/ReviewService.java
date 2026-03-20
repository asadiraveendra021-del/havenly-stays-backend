package com.hotelbooking.service;

import com.hotelbooking.dto.HotelReviewRequest;
import com.hotelbooking.dto.HotelReviewResponse;
import java.util.List;

public interface ReviewService {
    HotelReviewResponse addReview(Long hotelId, HotelReviewRequest request);
    List<HotelReviewResponse> getReviewsByHotel(Long hotelId);
}
