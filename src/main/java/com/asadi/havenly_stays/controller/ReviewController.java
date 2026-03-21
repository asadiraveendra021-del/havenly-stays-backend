package com.asadi.havenly_stays.controller;

import com.asadi.havenly_stays.dto.HotelReviewRequest;
import com.asadi.havenly_stays.dto.HotelReviewResponse;
import com.asadi.havenly_stays.service.ReviewService;
import com.asadi.havenly_stays.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hotels/{hotelId}/reviews")
@Tag(name = "Hotel Reviews", description = "Hotel review APIs")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @Operation(summary = "Add a review")
    public ResponseEntity<ApiResponse<HotelReviewResponse>> addReview(@PathVariable Long hotelId,
                                                                      @Valid @RequestBody HotelReviewRequest request) {
        HotelReviewResponse response = reviewService.addReview(hotelId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<HotelReviewResponse>builder()
                        .success(true)
                        .message("Review added successfully")
                        .data(response)
                        .build());
    }

    @GetMapping
    @Operation(summary = "Get reviews for a hotel")
    public ResponseEntity<ApiResponse<List<HotelReviewResponse>>> getReviewsByHotel(@PathVariable Long hotelId) {
        List<HotelReviewResponse> response = reviewService.getReviewsByHotel(hotelId);
        return ResponseEntity.ok(ApiResponse.<List<HotelReviewResponse>>builder()
                .success(true)
                .message("Reviews fetched successfully")
                .data(response)
                .build());
    }
}
