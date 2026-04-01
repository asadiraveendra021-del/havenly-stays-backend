package com.asadi.havenly_stays.controller;

import com.asadi.havenly_stays.dto.HotelAvailabilityResponse;
import com.asadi.havenly_stays.service.HotelAvailabilityService;
import com.asadi.havenly_stays.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hotels")
@Tag(name = "Hotel Availability", description = "Hotel availability details APIs")
public class HotelAvailabilityController {

    private final HotelAvailabilityService hotelAvailabilityService;

    public HotelAvailabilityController(HotelAvailabilityService hotelAvailabilityService) {
        this.hotelAvailabilityService = hotelAvailabilityService;
    }

    @GetMapping("/{hotelId}/availability-details")
    @Operation(summary = "Get availability details for a hotel")
    public ResponseEntity<ApiResponse<HotelAvailabilityResponse>> getAvailabilityDetails(@PathVariable Long hotelId) {
        HotelAvailabilityResponse response = hotelAvailabilityService.getAvailabilityDetails(hotelId);
        return ResponseEntity.ok(ApiResponse.<HotelAvailabilityResponse>builder()
                .success(true)
                .message("Hotel availability details fetched successfully")
                .data(response)
                .build());
    }
}
