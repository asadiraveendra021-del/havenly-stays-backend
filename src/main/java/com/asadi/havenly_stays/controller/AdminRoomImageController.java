package com.asadi.havenly_stays.controller;

import com.asadi.havenly_stays.dto.RoomImageResponse;
import com.asadi.havenly_stays.service.RoomImageService;
import com.asadi.havenly_stays.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/rooms/{roomTypeId}/images")
@Tag(name = "Admin Room Images", description = "Admin operations for room images")
public class AdminRoomImageController {

    private final RoomImageService roomImageService;

    public AdminRoomImageController(RoomImageService roomImageService) {
        this.roomImageService = roomImageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload room image")
    public ResponseEntity<ApiResponse<RoomImageResponse>> uploadRoomImage(@PathVariable Long roomTypeId,
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
        RoomImageResponse response = roomImageService.uploadRoomImage(roomTypeId, file, title, description, isMainImage, displayOrder);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<RoomImageResponse>builder()
                        .success(true)
                        .message("Room image uploaded successfully")
                        .data(response)
                        .build());
    }

    @PutMapping(value = "/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update room image")
    public ResponseEntity<ApiResponse<RoomImageResponse>> updateRoomImage(@PathVariable Long roomTypeId,
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
        RoomImageResponse response = roomImageService.updateRoomImage(roomTypeId, imageId, file, title, description, isMainImage, displayOrder);
        return ResponseEntity.ok(ApiResponse.<RoomImageResponse>builder()
                .success(true)
                .message("Room image updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "Delete room image")
    public ResponseEntity<ApiResponse<Void>> deleteRoomImage(@PathVariable Long roomTypeId,
                                                             @PathVariable Long imageId) {
        roomImageService.deleteRoomImage(roomTypeId, imageId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Room image deleted successfully")
                .data(null)
                .build());
    }
}
