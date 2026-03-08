package com.asadi.havenly_stays.service;

import com.asadi.havenly_stays.entity.RefreshToken;
import com.asadi.havenly_stays.entity.User;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyExpiration(RefreshToken token);

    String generateNewAccessToken(User user);

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    void removeExpiredRefreshTokens();
}
