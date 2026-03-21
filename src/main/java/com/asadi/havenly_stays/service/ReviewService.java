package com.asadi.havenly_stays.service;

import com.asadi.havenly_stays.dto.HotelReviewRequest;
import com.asadi.havenly_stays.dto.HotelReviewResponse;
import java.util.List;

public interface ReviewService {
    HotelReviewResponse addReview(Long hotelId, HotelReviewRequest request);
    List<HotelReviewResponse> getReviewsByHotel(Long hotelId);
}
