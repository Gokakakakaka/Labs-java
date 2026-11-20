package com.example.demo.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;
    private final TokenService tokens;

    public AuthController(AuthService auth, TokenService tokens) {
        this.auth = auth;
        this.tokens = tokens;
    }

    public record LoginRequest(@NotBlank @Email String email,
                               @NotBlank String password,
                               @Size(max = 255) String deviceId) {}
    public record RegisterRequest(@NotBlank @Size(max = 100) String name,
                                  @NotBlank @Email @Size(max = 190) String email,
                                  @NotBlank String password,
                                  @Size(max = 255) String deviceId) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}

    @PostMapping("/login")
    public TokenPair login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request.email(), request.password(), request.deviceId());
    }

    @PostMapping("/register")
    public ResponseEntity<TokenPair> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                auth.register(request.name(), request.email(), request.password(), request.deviceId()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPair> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResult result = tokens.refresh(request.refreshToken());
        if (result.replayDetected()) {
            throw AuthException.unauthorized("Обнаружено повторное использование refresh-токена; сессия отозвана");
        }
        return ResponseEntity.ok(result.tokens());
    }
}
