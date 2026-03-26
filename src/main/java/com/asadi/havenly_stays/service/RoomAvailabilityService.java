package com.asadi.havenly_stays.service;

import com.asadi.havenly_stays.dto.RoomAvailabilityBulkRequest;
import com.asadi.havenly_stays.dto.RoomAvailabilityResponse;
import java.util.List;

public interface RoomAvailabilityService {
    List<RoomAvailabilityResponse> createOrUpdateAvailabilities(Long roomTypeId, RoomAvailabilityBulkRequest request);
    List<RoomAvailabilityResponse> getAvailabilities(Long roomTypeId);
}
