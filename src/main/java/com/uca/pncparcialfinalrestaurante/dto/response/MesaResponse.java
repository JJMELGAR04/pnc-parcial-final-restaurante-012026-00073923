package com.uca.pncparcialfinalrestaurante.dto.response;

import com.uca.pncparcialfinalrestaurante.domain.enums.EstadoMesa;

public record MesaResponse(
        Long id,
        Integer numero,
        Integer capacidad,
        EstadoMesa estado,
        Long sucursalId,
        String sucursalNombre
) {
}