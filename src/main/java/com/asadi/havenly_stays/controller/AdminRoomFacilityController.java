package com.asadi.havenly_stays.controller;

import com.asadi.havenly_stays.dto.FacilitySetRequest;
import com.asadi.havenly_stays.dto.RoomFacilityResponse;
import com.asadi.havenly_stays.service.RoomFacilityService;
import com.asadi.havenly_stays.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/rooms/{roomTypeId}/facilities")
@Tag(name = "Admin Room Facilities", description = "Admin operations for room facilities")
public class AdminRoomFacilityController {

    private final RoomFacilityService roomFacilityService;

    public AdminRoomFacilityController(RoomFacilityService roomFacilityService) {
        this.roomFacilityService = roomFacilityService;
    }

    @PutMapping
    @Operation(summary = "Set room facilities")
    public ResponseEntity<ApiResponse<List<RoomFacilityResponse>>> setFacilities(@PathVariable Long roomTypeId,
                                                                                 @Valid @RequestBody FacilitySetRequest request) {
        List<RoomFacilityResponse> response = roomFacilityService.setFacilities(roomTypeId, request.getFacilityIds());
        return ResponseEntity.ok(ApiResponse.<List<RoomFacilityResponse>>builder()
                .success(true)
                .message("Facilities updated successfully")
                .data(response)
                .build());
    }
}
