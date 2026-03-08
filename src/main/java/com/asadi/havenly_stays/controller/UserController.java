package com.asadi.havenly_stays.controller;

import com.asadi.havenly_stays.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "User endpoint", description = "Accessible by ROLE_USER or ROLE_ADMIN.")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK.value())
                .message("User API access granted")
                .data(Map.of("scope", "user"))
                .build());
    }
}
