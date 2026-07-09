package com.uca.pncparcialfinalrestaurante.dto.response;

import com.uca.pncparcialfinalrestaurante.domain.enums.EstadoPedido;

import java.math.BigDecimal;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long clienteId,
        String clienteNombre,
        Long mesaId,
        Integer mesaNumero,
        Long sucursalId,
        String sucursalNombre,
        EstadoPedido estado,
        BigDecimal total,
        List<ItemPedidoResponse> items
) {
}