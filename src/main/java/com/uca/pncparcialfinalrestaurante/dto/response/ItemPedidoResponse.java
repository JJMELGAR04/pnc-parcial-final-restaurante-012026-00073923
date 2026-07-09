package com.uca.pncparcialfinalrestaurante.dto.response;

import java.math.BigDecimal;

public record ItemPedidoResponse(
        Long id,
        Long productoId,
        String productoNombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}