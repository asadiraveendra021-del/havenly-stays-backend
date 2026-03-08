package com.asadi.havenly_stays.controller;

import com.asadi.havenly_stays.dto.ApiResponse;
import com.asadi.havenly_stays.dto.LoginRequest;
import com.asadi.havenly_stays.dto.RefreshTokenRequest;
import com.asadi.havenly_stays.dto.SignupRequest;
import com.asadi.havenly_stays.dto.TokenRefreshApiResponse;
import com.asadi.havenly_stays.dto.TokenRefreshResponse;
import com.asadi.havenly_stays.dto.UserApiResponse;
import com.asadi.havenly_stays.dto.UserResponse;
import com.asadi.havenly_stays.service.AuthService;
import com.asadi.havenly_stays.util.LoginRateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter loginRateLimiter;

    @PostMapping("/signup")
    @Operation(summary = "Sign up", description = "Register a new user account.", security = {})
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse userResponse = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<UserResponse>builder()
                .timestamp(Instant.now())
                .status(HttpStatus.CREATED.value())
                .message("User registered successfully")
                .data(userResponse)
                .build());
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate and return access + refresh tokens.", security = {})
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(
                            schema = @Schema(implementation = TokenRefreshApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-03-08T12:00:00Z",
                                      "status": 200,
                                      "message": "Authentication successful",
                                      "data": {
                                        "accessToken": "eyJ...",
                                        "refreshToken": "6b6ebf1a-21d9-4022-bb5a-8a4f9e31e986",
                                        "tokenType": "Bearer",
                                        "expiresIn": 900
                                      }
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        loginRateLimiter.validateRequest(resolveClientIp(httpServletRequest));
        TokenRefreshResponse tokens = authService.login(request);
        return ResponseEntity.ok(ApiResponse.<TokenRefreshResponse>builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK.value())
                .message("Authentication successful")
                .data(tokens)
                .build());
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh token", description = "Create a new access token using refresh token.", security = {})
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(
                            schema = @Schema(implementation = TokenRefreshApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-03-08T12:00:00Z",
                                      "status": 200,
                                      "message": "Token refreshed successfully",
                                      "data": {
                                        "accessToken": "eyJ...",
                                        "refreshToken": "6b6ebf1a-21d9-4022-bb5a-8a4f9e31e986",
                                        "tokenType": "Bearer",
                                        "expiresIn": 900
                                      }
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.<TokenRefreshResponse>builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK.value())
                .message("Token refreshed successfully")
                .data(response)
                .build());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Blacklist current access token and remove refresh token.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-03-08T12:00:00Z",
                                      "status": 200,
                                      "message": "Logout successful",
                                      "data": null
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String accessToken = extractBearerToken(authorizationHeader);
        authService.logout(request, accessToken);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK.value())
                .message("Logout successful")
                .data(null)
                .build());
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7);
    }
}
