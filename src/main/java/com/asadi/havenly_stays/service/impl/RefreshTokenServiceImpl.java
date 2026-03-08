package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.entity.RefreshToken;
import com.asadi.havenly_stays.entity.User;
import com.asadi.havenly_stays.exception.TokenExpiredException;
import com.asadi.havenly_stays.repository.RefreshTokenRepository;
import com.asadi.havenly_stays.security.JwtTokenProvider;
import com.asadi.havenly_stays.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.refresh-expiration-seconds:604800}")
    private long refreshTokenDurationSeconds;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .expiryDate(Instant.now().plusSeconds(refreshTokenDurationSeconds))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenExpiredException("Refresh token has expired. Please login again.");
        }
        return token;
    }

    @Override
    public String generateNewAccessToken(User user) {
        return jwtTokenProvider.generateToken(user);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 */30 * * * *")
    public void removeExpiredRefreshTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
        log.debug("security_event=refresh_tokens_pruned");
    }
}
