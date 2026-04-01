package com.asadi.havenly_stays.service;

import com.asadi.havenly_stays.dto.SearchAvailabilityRequest;
import com.asadi.havenly_stays.dto.SearchAvailabilityResponse;
import java.util.List;

public interface SearchService {
    List<SearchAvailabilityResponse> searchAvailability(SearchAvailabilityRequest request);
}
