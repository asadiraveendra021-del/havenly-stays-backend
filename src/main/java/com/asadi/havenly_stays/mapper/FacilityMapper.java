package com.asadi.havenly_stays.mapper;

import com.asadi.havenly_stays.dto.FacilityResponse;
import com.asadi.havenly_stays.entity.Facility;
import org.springframework.stereotype.Component;

@Component
public class FacilityMapper {

    public FacilityResponse toResponse(Facility facility) {
        if (facility == null) {
            return null;
        }
        return FacilityResponse.builder()
                .id(facility.getId())
                .name(facility.getName())
                .iconCode(facility.getIconCode())
                .category(facility.getCategory())
                .build();
    }
}
