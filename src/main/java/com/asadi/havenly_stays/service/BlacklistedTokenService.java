package com.asadi.havenly_stays.service;

import java.time.Instant;

public interface BlacklistedTokenService {

    void blacklistToken(String token, Instant expiresAt);

    boolean isBlacklisted(String token);

    void removeExpiredTokens();
}
