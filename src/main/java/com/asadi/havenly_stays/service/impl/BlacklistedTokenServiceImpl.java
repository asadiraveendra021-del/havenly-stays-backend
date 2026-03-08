package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.entity.BlacklistedToken;
import com.asadi.havenly_stays.repository.BlacklistedTokenRepository;
import com.asadi.havenly_stays.service.BlacklistedTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlacklistedTokenServiceImpl implements BlacklistedTokenService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    @Transactional
    public void blacklistToken(String token, Instant expiresAt) {
        if (token == null || token.isBlank() || blacklistedTokenRepository.existsByToken(token)) {
            return;
        }
        blacklistedTokenRepository.save(BlacklistedToken.builder()
                .token(token)
                .blacklistedAt(Instant.now())
                .expiresAt(expiresAt)
                .build());
        log.info("security_event=token_blacklisted expiresAt={}", expiresAt);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 */30 * * * *")
    public void removeExpiredTokens() {
        blacklistedTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
