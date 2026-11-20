package com.example.demo.auth;

public record RefreshResult(TokenPair tokens, boolean replayDetected) {
    public static RefreshResult success(TokenPair tokens) {
        return new RefreshResult(tokens, false);
    }

    public static RefreshResult replay() {
        return new RefreshResult(null, true);
    }
}
