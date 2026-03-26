package com.asadi.havenly_stays.controller;

import com.asadi.havenly_stays.dto.RoomTypeCreateRequest;
import com.asadi.havenly_stays.dto.RoomTypeResponse;
import com.asadi.havenly_stays.dto.RoomTypeUpdateRequest;
import com.asadi.havenly_stays.exception.ResourceNotFoundException;
import com.asadi.havenly_stays.service.RoomTypeService;
import com.asadi.havenly_stays.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/hotels/{hotelId}/rooms")
@Tag(name = "Admin Room Types", description = "Admin operations for room types")
public class AdminRoomTypeController {

    private final RoomTypeService roomTypeService;

    public AdminRoomTypeController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    @PostMapping
    @Operation(summary = "Create a room type")
    public ResponseEntity<ApiResponse<RoomTypeResponse>> createRoomType(@PathVariable Long hotelId,
                                                                        @Valid @RequestBody RoomTypeCreateRequest request) {
        RoomTypeResponse response = roomTypeService.createRoomType(hotelId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<RoomTypeResponse>builder()
                        .success(true)
                        .message("Room type created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping
    @Operation(summary = "Get room types by hotel")
    public ResponseEntity<ApiResponse<List<RoomTypeResponse>>> getRoomTypes(@PathVariable Long hotelId) {
        List<RoomTypeResponse> response = roomTypeService.getRoomTypesByHotel(hotelId);
        return ResponseEntity.ok(ApiResponse.<List<RoomTypeResponse>>builder()
                .success(true)
                .message("Room types fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{roomTypeId}")
    @Operation(summary = "Get room type by ID")
    public ResponseEntity<ApiResponse<RoomTypeResponse>> getRoomType(@PathVariable Long hotelId,
                                                                     @PathVariable Long roomTypeId) {
        RoomTypeResponse response = roomTypeService.getRoomTypeById(roomTypeId);
        validateHotelOwnership(hotelId, response);
        return ResponseEntity.ok(ApiResponse.<RoomTypeResponse>builder()
                .success(true)
                .message("Room type fetched successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{roomTypeId}")
    @Operation(summary = "Update room type")
    public ResponseEntity<ApiResponse<RoomTypeResponse>> updateRoomType(@PathVariable Long hotelId,
                                                                        @PathVariable Long roomTypeId,
                                                                        @Valid @RequestBody RoomTypeUpdateRequest request) {
        RoomTypeResponse existing = roomTypeService.getRoomTypeById(roomTypeId);
        validateHotelOwnership(hotelId, existing);
        RoomTypeResponse response = roomTypeService.updateRoomType(roomTypeId, request);
        return ResponseEntity.ok(ApiResponse.<RoomTypeResponse>builder()
                .success(true)
                .message("Room type updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{roomTypeId}")
    @Operation(summary = "Delete room type")
    public ResponseEntity<ApiResponse<Void>> deleteRoomType(@PathVariable Long hotelId,
                                                            @PathVariable Long roomTypeId) {
        RoomTypeResponse existing = roomTypeService.getRoomTypeById(roomTypeId);
        validateHotelOwnership(hotelId, existing);
        roomTypeService.deleteRoomType(roomTypeId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Room type deleted successfully")
                .data(null)
                .build());
    }

    private void validateHotelOwnership(Long hotelId, RoomTypeResponse roomType) {
        if (!hotelId.equals(roomType.getHotelId())) {
            throw new ResourceNotFoundException("Room type not found: " + roomType.getId());
        }
    }
}
