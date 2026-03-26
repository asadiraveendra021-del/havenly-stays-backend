package com.asadi.havenly_stays.service;

import com.asadi.havenly_stays.dto.RoomFacilityResponse;
import java.util.List;

public interface RoomFacilityService {
    List<RoomFacilityResponse> setFacilities(Long roomTypeId, List<Long> facilityIds);
    List<RoomFacilityResponse> getFacilities(Long roomTypeId);
}
