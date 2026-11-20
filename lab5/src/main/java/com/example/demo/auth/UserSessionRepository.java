package com.example.demo.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserSessionRepository {
    private final JdbcTemplate jdbc;

    public UserSessionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<UserSession> mapper = (rs, rowNum) -> new UserSession(
            UUID.fromString(rs.getString("id")),
            rs.getString("username"),
            rs.getString("device_id"),
            rs.getString("access_token"),
            rs.getString("refresh_token"),
            rs.getTimestamp("access_token_expiry").toInstant(),
            rs.getTimestamp("refresh_token_expiry").toInstant(),
            SessionStatus.valueOf(rs.getString("status"))
    );

    public Optional<UserSession> findByRefreshTokenForUpdate(String refreshToken) {
        return jdbc.query("SELECT * FROM user_sessions WHERE refresh_token = ? FOR UPDATE",
                mapper, refreshToken).stream().findFirst();
    }

    public void insert(UserSession session) {
        jdbc.update("""
                INSERT INTO user_sessions
                (id, username, device_id, access_token, refresh_token,
                 access_token_expiry, refresh_token_expiry, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                session.id().toString(), session.username(), session.deviceId(),
                session.accessToken(), session.refreshToken(),
                Timestamp.from(session.accessTokenExpiry()),
                Timestamp.from(session.refreshTokenExpiry()), session.status().name());
    }

    public int updateStatus(UUID id, SessionStatus status) {
        return jdbc.update("UPDATE user_sessions SET status = ? WHERE id = ?",
                status.name(), id.toString());
    }
}
