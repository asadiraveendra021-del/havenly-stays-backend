package com.hotelbooking.service.impl;

import com.hotelbooking.dto.FacilityResponse;
import com.hotelbooking.mapper.FacilityMapper;
import com.hotelbooking.repository.FacilityRepository;
import com.hotelbooking.service.FacilityService;
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
