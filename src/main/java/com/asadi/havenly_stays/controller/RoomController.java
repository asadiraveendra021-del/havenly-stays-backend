package com.asadi.havenly_stays.controller;

import com.asadi.havenly_stays.dto.RoomTypeResponse;
import com.asadi.havenly_stays.service.RoomTypeService;
import com.asadi.havenly_stays.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Rooms", description = "Public room APIs")
public class RoomController {

    private final RoomTypeService roomTypeService;

    public RoomController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    @GetMapping("/hotels/{hotelId}/rooms")
    @Operation(summary = "Get room types by hotel")
    public ResponseEntity<ApiResponse<List<RoomTypeResponse>>> getRoomsByHotel(@PathVariable Long hotelId) {
        List<RoomTypeResponse> response = roomTypeService.getRoomTypesByHotel(hotelId);
        return ResponseEntity.ok(ApiResponse.<List<RoomTypeResponse>>builder()
                .success(true)
                .message("Room types fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/rooms/{roomTypeId}")
    @Operation(summary = "Get room type by ID")
    public ResponseEntity<ApiResponse<RoomTypeResponse>> getRoomById(@PathVariable Long roomTypeId) {
        RoomTypeResponse response = roomTypeService.getRoomTypeById(roomTypeId);
        return ResponseEntity.ok(ApiResponse.<RoomTypeResponse>builder()
                .success(true)
                .message("Room type fetched successfully")
                .data(response)
                .build());
    }
}
