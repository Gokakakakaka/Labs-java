package com.example.demo.auth;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenService {
    private final JwtService jwt;
    private final UserSessionRepository sessions;
    private final JdbcTemplate jdbc;

    public TokenService(JwtService jwt, UserSessionRepository sessions, JdbcTemplate jdbc) {
        this.jwt = jwt;
        this.sessions = sessions;
        this.jdbc = jdbc;
    }

    @Transactional
    public TokenPair issue(String username, String deviceId) {
        TokenPair pair = createPair(username);
        saveSession(username, deviceId, pair);
        return pair;
    }

    @Transactional
    public RefreshResult refresh(String refreshToken) {
        Map<String, Object> claims = jwt.validateRefresh(refreshToken);
        String subject = (String) claims.get("sub");

        UserSession old = sessions.findByRefreshTokenForUpdate(refreshToken)
                .orElseThrow(() -> AuthException.unauthorized("Refresh-сессия не найдена"));

        Instant now = Instant.now();
        if (!old.username().equals(subject)
                || old.refreshTokenExpiry().getEpochSecond()
                    != ((Number) claims.get("exp")).longValue()) {
            throw AuthException.unauthorized("Refresh-токен не соответствует сохранённой сессии");
        }

        Boolean validUser = jdbc.queryForObject("""
                SELECT EXISTS(
                    SELECT 1 FROM users
                    WHERE email = ? AND enabled = TRUE AND account_non_locked = TRUE
                )
                """, Boolean.class, subject);
        if (!Boolean.TRUE.equals(validUser)) {
            throw AuthException.unauthorized("Пользователь не существует или заблокирован");
        }

        if (old.status() == SessionStatus.USED && old.refreshTokenExpiry().isAfter(now)) {
            sessions.updateStatus(old.id(), SessionStatus.REVOKED);
            return RefreshResult.replay();
        }
        if (old.status() != SessionStatus.ACTIVE || !old.refreshTokenExpiry().isAfter(now)) {
            throw AuthException.unauthorized("Refresh-сессия недействительна или отозвана");
        }

        sessions.updateStatus(old.id(), SessionStatus.USED);
        TokenPair next = createPair(subject);
        saveSession(subject, old.deviceId(), next);
        return RefreshResult.success(next);
    }

    private TokenPair createPair(String username) {
        JwtService.IssuedToken access = jwt.createAccessToken(username);
        JwtService.IssuedToken refresh = jwt.createRefreshToken(username);
        return new TokenPair(access.value(), refresh.value());
    }

    private void saveSession(String username, String deviceId, TokenPair pair) {
        Map<String, Object> accessClaims = jwt.validateAccess(pair.accessToken());
        Map<String, Object> refreshClaims = jwt.validateRefresh(pair.refreshToken());
        Instant accessExpiry = Instant.ofEpochSecond(((Number) accessClaims.get("exp")).longValue());
        Instant refreshExpiry = Instant.ofEpochSecond(((Number) refreshClaims.get("exp")).longValue());
        sessions.insert(new UserSession(UUID.randomUUID(), username, deviceId,
                pair.accessToken(), pair.refreshToken(), accessExpiry, refreshExpiry, SessionStatus.ACTIVE));
    }
}
