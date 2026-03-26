package com.asadi.havenly_stays.controller;

import com.asadi.havenly_stays.dto.RoomAvailabilityBulkRequest;
import com.asadi.havenly_stays.dto.RoomAvailabilityResponse;
import com.asadi.havenly_stays.service.RoomAvailabilityService;
import com.asadi.havenly_stays.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/rooms/{roomTypeId}/availabilities")
@Tag(name = "Admin Room Availability", description = "Admin operations for room availability")
public class AdminRoomAvailabilityController {

    private final RoomAvailabilityService roomAvailabilityService;

    public AdminRoomAvailabilityController(RoomAvailabilityService roomAvailabilityService) {
        this.roomAvailabilityService = roomAvailabilityService;
    }

    @PostMapping
    @Operation(summary = "Create or update room availability ranges")
    public ResponseEntity<ApiResponse<List<RoomAvailabilityResponse>>> createOrUpdate(@PathVariable Long roomTypeId,
                                                                                      @Valid @RequestBody RoomAvailabilityBulkRequest request) {
        List<RoomAvailabilityResponse> response = roomAvailabilityService.createOrUpdateAvailabilities(roomTypeId, request);
        return ResponseEntity.ok(ApiResponse.<List<RoomAvailabilityResponse>>builder()
                .success(true)
                .message("Room availability updated successfully")
                .data(response)
                .build());
    }

    @GetMapping
    @Operation(summary = "Get room availabilities")
    public ResponseEntity<ApiResponse<List<RoomAvailabilityResponse>>> getAvailabilities(@PathVariable Long roomTypeId) {
        List<RoomAvailabilityResponse> response = roomAvailabilityService.getAvailabilities(roomTypeId);
        return ResponseEntity.ok(ApiResponse.<List<RoomAvailabilityResponse>>builder()
                .success(true)
                .message("Room availabilities fetched successfully")
                .data(response)
                .build());
    }
}
