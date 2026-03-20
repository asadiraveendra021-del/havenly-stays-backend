package com.hotelbooking.controller;

import com.hotelbooking.dto.HotelResponse;
import com.hotelbooking.service.HotelService;
import com.hotelbooking.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hotels")
@Tag(name = "Hotels", description = "Hotel browse APIs")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    @Operation(summary = "Get all hotels")
    public ResponseEntity<ApiResponse<Page<HotelResponse>>> getAllHotels(@ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        Page<HotelResponse> response = hotelService.getAllHotels(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<HotelResponse>>builder()
                .success(true)
                .message("Hotels fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hotel by ID")
    public ResponseEntity<ApiResponse<HotelResponse>> getHotelById(@PathVariable Long id) {
        HotelResponse response = hotelService.getHotelById(id);
        return ResponseEntity.ok(ApiResponse.<HotelResponse>builder()
                .success(true)
                .message("Hotel fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search hotels by name")
    public ResponseEntity<ApiResponse<Page<HotelResponse>>> searchHotels(
            @Parameter(description = "Search keyword") @RequestParam(value = "q", required = false) String query,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        Page<HotelResponse> response = hotelService.searchHotels(query, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<HotelResponse>>builder()
                .success(true)
                .message("Hotels fetched successfully")
                .data(response)
                .build());
    }
}
