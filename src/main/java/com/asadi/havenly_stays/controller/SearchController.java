package com.asadi.havenly_stays.controller;

import com.asadi.havenly_stays.dto.SearchAvailabilityRequest;
import com.asadi.havenly_stays.dto.SearchAvailabilityResponse;
import com.asadi.havenly_stays.service.SearchService;
import com.asadi.havenly_stays.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Availability search APIs")
@Validated
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/availability")
    @Operation(summary = "Search availability by date range")
    public ResponseEntity<ApiResponse<List<SearchAvailabilityResponse>>> searchAvailability(
            @Valid @ModelAttribute SearchAvailabilityRequest request) {
        List<SearchAvailabilityResponse> response = searchService.searchAvailability(request);
        return ResponseEntity.ok(ApiResponse.<List<SearchAvailabilityResponse>>builder()
                .success(true)
                .message("Available rooms fetched successfully")
                .data(response)
                .build());
    }
}
