package com.asadi.havenly_stays.mapper;

import com.asadi.havenly_stays.dto.RoomFacilityResponse;
import com.asadi.havenly_stays.entity.RoomFacility;
import org.springframework.stereotype.Component;

@Component
public class RoomFacilityMapper {

    public RoomFacilityResponse toResponse(RoomFacility facility) {
        if (facility == null) {
            return null;
        }
        return RoomFacilityResponse.builder()
                .id(facility.getId())
                .name(facility.getName())
                .iconCode(facility.getIconCode())
                .category(facility.getCategory())
                .build();
    }
}
