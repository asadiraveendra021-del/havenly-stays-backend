package com.hotelbooking.service;

import com.hotelbooking.dto.FacilityResponse;
import java.util.List;

public interface FacilityService {
    List<FacilityResponse> getAllFacilities();
}
