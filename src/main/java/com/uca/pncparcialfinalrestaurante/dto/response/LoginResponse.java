package com.uca.pncparcialfinalrestaurante.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInMs
) {
    public static LoginResponse of(String accessToken, String refreshToken, long expiresInMs) {
        return new LoginResponse(accessToken, refreshToken, "Bearer", expiresInMs);
    }
}