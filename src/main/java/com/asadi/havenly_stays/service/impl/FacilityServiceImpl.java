package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.dto.FacilityResponse;
import com.asadi.havenly_stays.mapper.FacilityMapper;
import com.asadi.havenly_stays.repository.FacilityRepository;
import com.asadi.havenly_stays.service.FacilityService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilityMapper facilityMapper;

    public FacilityServiceImpl(FacilityRepository facilityRepository, FacilityMapper facilityMapper) {
        this.facilityRepository = facilityRepository;
        this.facilityMapper = facilityMapper;
    }

    @Override
    public List<FacilityResponse> getAllFacilities() {
        return facilityRepository.findAll().stream()
                .map(facilityMapper::toResponse)
                .collect(Collectors.toList());
    }
}
