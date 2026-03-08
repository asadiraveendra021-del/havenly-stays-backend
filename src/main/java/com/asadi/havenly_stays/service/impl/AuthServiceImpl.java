package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.dto.LoginRequest;
import com.asadi.havenly_stays.dto.RefreshTokenRequest;
import com.asadi.havenly_stays.dto.SignupRequest;
import com.asadi.havenly_stays.dto.TokenRefreshResponse;
import com.asadi.havenly_stays.dto.UserResponse;
import com.asadi.havenly_stays.entity.RefreshToken;
import com.asadi.havenly_stays.entity.Role;
import com.asadi.havenly_stays.entity.RoleName;
import com.asadi.havenly_stays.entity.User;
import com.asadi.havenly_stays.exception.InvalidCredentialsException;
import com.asadi.havenly_stays.exception.InvalidRefreshTokenException;
import com.asadi.havenly_stays.exception.UserAlreadyExistsException;
import com.asadi.havenly_stays.repository.RoleRepository;
import com.asadi.havenly_stays.repository.UserRepository;
import com.asadi.havenly_stays.security.JwtTokenProvider;
import com.asadi.havenly_stays.service.AuthService;
import com.asadi.havenly_stays.service.BlacklistedTokenService;
import com.asadi.havenly_stays.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final BlacklistedTokenService blacklistedTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build()));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .build();

        User savedUser = userRepository.save(user);
        log.info("security_event=signup_success email={}", savedUser.getEmail());

        return UserResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .build();
    }

    @Override
    public TokenRefreshResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("security_event=login_failed reason=user_not_found email={}", request.getEmail());
                    return new InvalidCredentialsException("Invalid credentials");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("security_event=login_failed reason=bad_password email={}", request.getEmail());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String accessToken = refreshTokenService.generateNewAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        log.info("security_event=login_success email={}", user.getEmail());

        return TokenRefreshResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessExpirationSeconds())
                .build();
    }

    @Override
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token is invalid"));

        RefreshToken validToken = refreshTokenService.verifyExpiration(refreshToken);
        User user = validToken.getUser();
        String newAccessToken = refreshTokenService.generateNewAccessToken(user);
        log.info("security_event=token_refreshed email={}", user.getEmail());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(validToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessExpirationSeconds())
                .build();
    }

    @Override
    public void logout(RefreshTokenRequest request, String accessToken) {
        if (accessToken != null && jwtTokenProvider.isTokenValid(accessToken)) {
            blacklistedTokenService.blacklistToken(accessToken, jwtTokenProvider.extractExpiration(accessToken));
        }

        refreshTokenService.findByToken(request.getRefreshToken())
                .ifPresent(token -> refreshTokenService.deleteByUser(token.getUser()));
        log.info("security_event=logout_success");
    }
}
