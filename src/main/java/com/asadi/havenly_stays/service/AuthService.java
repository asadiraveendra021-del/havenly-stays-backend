package com.asadi.havenly_stays.service;

import com.asadi.havenly_stays.dto.LoginRequest;
import com.asadi.havenly_stays.dto.RefreshTokenRequest;
import com.asadi.havenly_stays.dto.SignupRequest;
import com.asadi.havenly_stays.dto.TokenRefreshResponse;
import com.asadi.havenly_stays.dto.UserResponse;

public interface AuthService {

    UserResponse signup(SignupRequest request);

    TokenRefreshResponse login(LoginRequest request);

    TokenRefreshResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request, String accessToken);

}
