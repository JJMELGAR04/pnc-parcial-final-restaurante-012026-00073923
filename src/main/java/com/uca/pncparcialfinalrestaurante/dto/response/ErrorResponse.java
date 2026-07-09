package com.uca.pncparcialfinalrestaurante.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        List<String> detalles
) {
    public static ErrorResponse of(int status, String error, String mensaje) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje, null);
    }

    public static ErrorResponse of(int status, String error, String mensaje, List<String> detalles) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje, detalles);
    }
}