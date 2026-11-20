package com.example.demo.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    private static final String ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final ObjectMapper mapper;
    private final byte[] secret;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public JwtService(
            ObjectMapper mapper,
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds:900}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds:2592000}") long refreshTtlSeconds) {
        this.mapper = mapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) {
            throw new IllegalArgumentException("security.jwt.secret must contain at least 32 bytes");
        }
        if (accessTtlSeconds <= 0 || refreshTtlSeconds <= 0) {
            throw new IllegalArgumentException("JWT lifetimes must be positive");
        }
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public IssuedToken createAccessToken(String username) {
        return create(username, "access", accessTtlSeconds);
    }

    public IssuedToken createRefreshToken(String username) {
        return create(username, "refresh", refreshTtlSeconds);
    }

    private IssuedToken create(String username, String type, long ttlSeconds) {
        long now = Instant.now().getEpochSecond();
        long expiry = now + ttlSeconds;
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", username);
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("type", type);
        claims.put("iat", now);
        claims.put("exp", expiry);

        try {
            String h = ENCODER.encodeToString(mapper.writeValueAsBytes(header));
            String p = ENCODER.encodeToString(mapper.writeValueAsBytes(claims));
            String signingInput = h + "." + p;
            String signature = ENCODER.encodeToString(sign(signingInput));
            return new IssuedToken(signingInput + "." + signature, Instant.ofEpochSecond(expiry));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create JWT", e);
        }
    }

    public Map<String, Object> validateAccess(String token) {
        return validate(token, "access");
    }

    public Map<String, Object> validateRefresh(String token) {
        return validate(token, "refresh");
    }

    private Map<String, Object> validate(String token, String expectedType) {
        try {
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("Token is empty");
            }
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) {
                throw new IllegalArgumentException("Malformed JWT");
            }
            Map<String, Object> header = mapper.readValue(DECODER.decode(parts[0]),
                    new TypeReference<>() {});
            if (!"HS256".equals(header.get("alg")) || !"JWT".equals(header.get("typ"))) {
                throw new IllegalArgumentException("Unsupported JWT header");
            }
            byte[] expected = sign(parts[0] + "." + parts[1]);
            byte[] actual = DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expected, actual)) {
                throw new IllegalArgumentException("Invalid JWT signature");
            }
            Map<String, Object> claims = mapper.readValue(DECODER.decode(parts[1]),
                    new TypeReference<>() {});
            String subject = stringClaim(claims, "sub");
            String jti = stringClaim(claims, "jti");
            String type = stringClaim(claims, "type");
            long expiry = numberClaim(claims, "exp");
            numberClaim(claims, "iat");
            if (subject.isBlank() || jti.isBlank() || !expectedType.equals(type)) {
                throw new IllegalArgumentException("Invalid JWT claims or token type");
            }
            if (expiry <= Instant.now().getEpochSecond()) {
                throw new IllegalArgumentException("JWT expired");
            }
            return claims;
        } catch (Exception e) {
            throw AuthException.unauthorized("Недействительный или просроченный " + expectedType + "-токен");
        }
    }

    private String stringClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        if (!(value instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException("Missing claim: " + key);
        }
        return s;
    }

    private long numberClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        if (!(value instanceof Number n)) {
            throw new IllegalArgumentException("Missing numeric claim: " + key);
        }
        return n.longValue();
    }

    private byte[] sign(String value) throws Exception {
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(secret, ALGORITHM));
        return mac.doFinal(value.getBytes(StandardCharsets.US_ASCII));
    }

    public record IssuedToken(String value, Instant expiry) {}
}
