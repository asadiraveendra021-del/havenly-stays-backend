package com.asadi.havenly_stays.security;

import com.asadi.havenly_stays.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final String HMAC_ALGO = "HmacSHA256";

    private final String secret;
    private final long accessExpirationSeconds;
    private final ObjectMapper objectMapper;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:change-this-secret-key-to-a-very-long-random-value}") String secret,
            @Value("${app.jwt.expiration-seconds:900}") long accessExpirationSeconds,
            ObjectMapper objectMapper
    ) {
        this.secret = secret;
        this.accessExpirationSeconds = accessExpirationSeconds;
        this.objectMapper = objectMapper;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessExpirationSeconds);

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getEmail());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiry.getEpochSecond());

        String header = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String body = base64UrlEncode(toJsonBytes(payload));
        String signingInput = header + "." + body;
        String signature = sign(signingInput);

        return signingInput + "." + signature;
    }

    public String extractUsername(String token) {
        Map<String, Object> claims = parseClaims(token);
        Object subject = claims.get("sub");
        return subject == null ? null : String.valueOf(subject);
    }

    public Instant extractExpiration(String token) {
        Map<String, Object> claims = parseClaims(token);
        Object exp = claims.get("exp");
        if (exp == null) {
            return Instant.EPOCH;
        }
        return Instant.ofEpochSecond(Long.parseLong(String.valueOf(exp)));
    }

    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            String signingInput = parts[0] + "." + parts[1];
            String expectedSig = sign(signingInput);
            if (!expectedSig.equals(parts[2])) {
                return false;
            }
            return extractExpiration(token).isAfter(Instant.now());
        } catch (Exception ex) {
            return false;
        }
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }

    private Map<String, Object> parseClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token structure");
            }
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(decoded, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid token claims", ex);
        }
    }

    private byte[] toJsonBytes(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsBytes(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize claims", ex);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] signature = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(signature);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate JWT signature", ex);
        }
    }

    private String base64UrlEncode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
