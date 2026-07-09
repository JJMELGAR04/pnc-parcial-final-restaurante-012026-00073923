package com.uca.pncparcialfinalrestaurante.dto.request;

import com.uca.pncparcialfinalrestaurante.domain.enums.EstadoMesa;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoMesaRequest(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoMesa nuevoEstado
) {
}