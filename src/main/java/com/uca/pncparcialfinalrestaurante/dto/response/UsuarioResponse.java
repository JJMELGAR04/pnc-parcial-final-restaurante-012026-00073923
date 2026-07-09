package com.uca.pncparcialfinalrestaurante.dto.response;

import com.uca.pncparcialfinalrestaurante.domain.enums.RolNombre;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        RolNombre rol,
        Long sucursalId,
        String sucursalNombre,
        boolean activo
) {
}