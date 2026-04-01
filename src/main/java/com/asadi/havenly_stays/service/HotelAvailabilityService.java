package com.asadi.havenly_stays.service;

import com.asadi.havenly_stays.dto.HotelAvailabilityResponse;

public interface HotelAvailabilityService {
    HotelAvailabilityResponse getAvailabilityDetails(Long hotelId);
}
