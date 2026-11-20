package com.example.demo.auth;

import com.example.demo.security.CustomUserDetails;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    public AuthService(AuthenticationManager authenticationManager, TokenService tokenService,
                       JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    public TokenPair login(String email, String password, String deviceId) {
        require(email, "email");
        require(password, "password");
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email.trim().toLowerCase(Locale.ROOT), password));
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
            return tokenService.issue(user.getUsername(), deviceId);
        } catch (AuthenticationException ex) {
            throw AuthException.unauthorized("Неверный email или пароль");
        }
    }

    @Transactional
    public TokenPair register(String name, String email, String password, String deviceId) {
        require(name, "name");
        require(email, "email");
        require(password, "password");
        if (name.length() > 100 || email.length() > 190 ||
                !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") ||
                password.length() < 8 || password.length() > 72 ||
                !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") ||
                !password.matches(".*[0-9].*") || !password.matches(".*[^A-Za-z0-9].*")) {
            throw AuthException.badRequest("Некорректные данные регистрации");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        try {
            jdbc.update("""
                    INSERT INTO users
                    (name, email, password_hash, role, enabled, account_non_locked)
                    VALUES (?, ?, ?, 'USER', TRUE, TRUE)
                    """, name.trim(), normalizedEmail, encoder.encode(password));
        } catch (DuplicateKeyException ex) {
            throw AuthException.conflict("Учётная запись с таким email уже существует");
        }
        return tokenService.issue(normalizedEmail, deviceId);
    }

    private void require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw AuthException.badRequest("Поле " + field + " обязательно");
        }
    }
}
