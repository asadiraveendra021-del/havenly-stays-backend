package com.asadi.havenly_stays.controller;

import com.asadi.havenly_stays.dto.FacilitySetRequest;
import com.asadi.havenly_stays.dto.HotelCreateRequest;
import com.asadi.havenly_stays.dto.HotelImageResponse;
import com.asadi.havenly_stays.dto.HotelPetPolicyRequest;
import com.asadi.havenly_stays.dto.HotelPetPolicyResponse;
import com.asadi.havenly_stays.dto.HotelResponse;
import com.asadi.havenly_stays.dto.HotelUpdateRequest;
import com.asadi.havenly_stays.service.HotelService;
import com.asadi.havenly_stays.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/hotels")
@Tag(name = "Admin Hotels", description = "Admin operations for hotels")
public class AdminHotelController {

    private final HotelService hotelService;

    public AdminHotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @PostMapping
    @Operation(summary = "Create a hotel")
    public ResponseEntity<ApiResponse<HotelResponse>> createHotel(@Valid @RequestBody HotelCreateRequest request) {
        HotelResponse response = hotelService.createHotel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<HotelResponse>builder()
                        .success(true)
                        .message("Hotel created successfully")
                        .data(response)
                        .build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a hotel")
    public ResponseEntity<ApiResponse<HotelResponse>> updateHotel(@PathVariable Long id,
                                                                  @Valid @RequestBody HotelUpdateRequest request) {
        HotelResponse response = hotelService.updateHotel(id, request);
        return ResponseEntity.ok(ApiResponse.<HotelResponse>builder()
                .success(true)
                .message("Hotel updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a hotel")
    public ResponseEntity<ApiResponse<Void>> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Hotel deleted successfully")
                .data(null)
                .build());
    }

    @PostMapping(value = "/{hotelId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload hotel image")
    public ResponseEntity<ApiResponse<HotelImageResponse>> uploadHotelImage(@PathVariable Long hotelId,
                                                                            @Parameter(
                                                                                    description = "Image file",
                                                                                    required = true,
                                                                                    content = @io.swagger.v3.oas.annotations.media.Content(
                                                                                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                                                                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                                                                                    type = "string",
                                                                                                    format = "binary"
                                                                                            )
                                                                                    )
                                                                            )
                                                                            @RequestPart("file") MultipartFile file,
                                                                            @RequestParam(value = "title", required = false) String title,
                                                                            @RequestParam(value = "description", required = false) String description,
                                                                            @RequestParam(value = "isMainImage", required = false) Boolean isMainImage,
                                                                            @RequestParam(value = "displayOrder", required = false) Integer displayOrder) {
        HotelImageResponse response = hotelService.uploadHotelImage(hotelId, file, title, description, isMainImage, displayOrder);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<HotelImageResponse>builder()
                        .success(true)
                        .message("Hotel image uploaded successfully")
                        .data(response)
                        .build());
    }

    @PutMapping(value = "/{hotelId}/images/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update hotel image")
    public ResponseEntity<ApiResponse<HotelImageResponse>> updateHotelImage(@PathVariable Long hotelId,
                                                                            @PathVariable Long imageId,
                                                                            @Parameter(
                                                                                    description = "Image file (optional)",
                                                                                    required = false,
                                                                                    content = @io.swagger.v3.oas.annotations.media.Content(
                                                                                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                                                                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                                                                                    type = "string",
                                                                                                    format = "binary"
                                                                                            )
                                                                                    )
                                                                            )
                                                                            @RequestPart(value = "file", required = false) MultipartFile file,
                                                                            @RequestParam(value = "title", required = false) String title,
                                                                            @RequestParam(value = "description", required = false) String description,
                                                                            @RequestParam(value = "isMainImage", required = false) Boolean isMainImage,
                                                                            @RequestParam(value = "displayOrder", required = false) Integer displayOrder) {
        HotelImageResponse response = hotelService.updateHotelImage(hotelId, imageId, file, title, description, isMainImage, displayOrder);
        return ResponseEntity.ok(ApiResponse.<HotelImageResponse>builder()
                .success(true)
                .message("Hotel image updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{hotelId}/images/{imageId}")
    @Operation(summary = "Delete hotel image")
    public ResponseEntity<ApiResponse<Void>> deleteHotelImage(@PathVariable Long hotelId, @PathVariable Long imageId) {
        hotelService.deleteHotelImage(hotelId, imageId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Hotel image deleted successfully")
                .data(null)
                .build());
    }

    @PutMapping("/{id}/facilities")
    @Operation(summary = "Set hotel facilities")
    public ResponseEntity<ApiResponse<HotelResponse>> setFacilities(@PathVariable Long id,
                                                                    @Valid @RequestBody FacilitySetRequest request) {
        HotelResponse response = hotelService.setFacilities(id, request.getFacilityIds());
        return ResponseEntity.ok(ApiResponse.<HotelResponse>builder()
                .success(true)
                .message("Facilities updated successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/pet-policy")
    @Operation(summary = "Set hotel pet policy")
    public ResponseEntity<ApiResponse<HotelPetPolicyResponse>> setPetPolicy(@PathVariable Long id,
                                                                            @Valid @RequestBody HotelPetPolicyRequest request) {
        HotelPetPolicyResponse response = hotelService.setPetPolicy(id, request);
        return ResponseEntity.ok(ApiResponse.<HotelPetPolicyResponse>builder()
                .success(true)
                .message("Pet policy updated successfully")
                .data(response)
                .build());
    }
}
