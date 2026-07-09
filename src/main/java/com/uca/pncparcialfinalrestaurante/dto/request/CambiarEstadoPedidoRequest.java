package com.uca.pncparcialfinalrestaurante.dto.request;

import com.uca.pncparcialfinalrestaurante.domain.enums.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoPedidoRequest(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoPedido nuevoEstado
) {
}