package com.uca.pncparcialfinalrestaurante.dto.response;

public record SucursalResponse(
        Long id,
        String nombre,
        String direccion,
        String telefono,
        boolean activa
) {
}