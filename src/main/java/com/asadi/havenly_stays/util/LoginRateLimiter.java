package com.asadi.havenly_stays.util;

import com.asadi.havenly_stays.exception.RateLimitExceededException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, Deque<Long>> attemptsByIp = new ConcurrentHashMap<>();

    public void validateRequest(String ipAddress) {
        long now = Instant.now().getEpochSecond();
        Deque<Long> attempts = attemptsByIp.computeIfAbsent(ipAddress, key -> new ArrayDeque<>());

        synchronized (attempts) {
            while (!attempts.isEmpty() && now - attempts.peekFirst() >= WINDOW_SECONDS) {
                attempts.pollFirst();
            }
            if (attempts.size() >= MAX_ATTEMPTS) {
                throw new RateLimitExceededException("Too many login attempts. Please try again later.");
            }
            attempts.addLast(now);
        }
    }
}
