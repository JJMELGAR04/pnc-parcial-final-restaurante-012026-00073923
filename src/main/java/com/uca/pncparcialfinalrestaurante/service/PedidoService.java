package com.uca.pncparcialfinalrestaurante.service;

import com.uca.pncparcialfinalrestaurante.domain.ItemPedido;
import com.uca.pncparcialfinalrestaurante.domain.Mesa;
import com.uca.pncparcialfinalrestaurante.domain.Pedido;
import com.uca.pncparcialfinalrestaurante.domain.Producto;
import com.uca.pncparcialfinalrestaurante.domain.Usuario;
import com.uca.pncparcialfinalrestaurante.domain.enums.EstadoPedido;
import com.uca.pncparcialfinalrestaurante.dto.request.CambiarEstadoPedidoRequest;
import com.uca.pncparcialfinalrestaurante.dto.request.ItemPedidoRequest;
import com.uca.pncparcialfinalrestaurante.dto.request.PedidoRequest;
import com.uca.pncparcialfinalrestaurante.dto.response.ItemPedidoResponse;
import com.uca.pncparcialfinalrestaurante.dto.response.PedidoResponse;
import com.uca.pncparcialfinalrestaurante.exception.ForbiddenOperationException;
import com.uca.pncparcialfinalrestaurante.exception.ResourceNotFoundException;
import com.uca.pncparcialfinalrestaurante.repository.MesaRepository;
import com.uca.pncparcialfinalrestaurante.repository.PedidoRepository;
import com.uca.pncparcialfinalrestaurante.repository.ProductoRepository;
import com.uca.pncparcialfinalrestaurante.repository.UsuarioRepository;
import com.uca.pncparcialfinalrestaurante.security.CustomUserDetails;
import com.uca.pncparcialfinalrestaurante.security.SecurityUtils;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public PedidoResponse crear(PedidoRequest request) {
        CustomUserDetails current = SecurityUtils.getCurrentUser();

        // El cliente NUNCA se toma del body -- siempre del token autenticado.
        // (ver nota en PedidoRequest, Paso 4)
        Usuario cliente = usuarioRepository.findById(current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Mesa mesa = mesaRepository.findById(request.mesaId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada: " + request.mesaId()));

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .mesa(mesa)
                .sucursal(mesa.getSucursal())
                .estado(EstadoPedido.PENDIENTE)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedidoRequest itemReq : request.items()) {
            Producto producto = productoRepository.findById(itemReq.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + itemReq.productoId()));

            if (!producto.isDisponible()) {
                throw new ValidationException("El producto '" + producto.getNombre() + "' no esta disponible");
            }

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(itemReq.cantidad()));

            ItemPedido item = ItemPedido.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(itemReq.cantidad())
                    .precioUnitario(producto.getPrecio())
                    .subtotal(subtotal)
                    .build();

            pedido.getItems().add(item);
            total = total.add(subtotal);
        }

        pedido.setTotal(total);

        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        CustomUserDetails current = SecurityUtils.getCurrentUser();
        String rol = current.getUsuario().getRol().name();

        List<Pedido> pedidos = switch (rol) {
            case "ADMINISTRADOR" -> pedidoRepository.findAll();
            case "ENCARGADO_TURNO" -> pedidoRepository.findBySucursalId(current.getSucursalId());
            case "CLIENTE" -> pedidoRepository.findByClienteId(current.getId());
            default -> List.of();
        };

        return pedidos.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long id) {
        Pedido pedido = buscarConAccesoVerificado(id);
        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long id, CambiarEstadoPedidoRequest request) {
        CustomUserDetails current = SecurityUtils.getCurrentUser();
        Pedido pedido = buscarConAccesoVerificado(id);

        // Un CLIENTE solo puede pasar su propio pedido a CANCELADO, nada mas.
        // Confirmar/preparar/entregar es exclusivo de Encargado/Administrador.
        if (esCliente(current)) {
            if (request.nuevoEstado() != EstadoPedido.CANCELADO) {
                throw new ForbiddenOperationException("Un cliente solo puede cancelar su propio pedido");
            }
            if (!pedido.getCliente().getId().equals(current.getId())) {
                throw new ForbiddenOperationException("No puede cancelar un pedido que no es suyo");
            }
        }

        pedido.setEstado(request.nuevoEstado());
        return toResponse(pedidoRepository.save(pedido));
    }

    /**
     * Punto central de la Opcion B para pedidos:
     * - ADMINISTRADOR: acceso total, sin restriccion.
     * - ENCARGADO_TURNO: solo pedidos cuya sucursal coincide con la suya.
     *   Se usa findByIdAndSucursalId (filtrado en la query, ver Paso 3) para que
     *   un id de otra sucursal directamente no aparezca -- no se filtra en Java
     *   despues de traer el dato completo.
     * - CLIENTE: solo pedidos donde el es el cliente.
     */
    private Pedido buscarConAccesoVerificado(Long id) {
        CustomUserDetails current = SecurityUtils.getCurrentUser();
        String rol = current.getUsuario().getRol().name();

        return switch (rol) {
            case "ADMINISTRADOR" -> pedidoRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id));

            case "ENCARGADO_TURNO" -> pedidoRepository.findByIdAndSucursalId(id, current.getSucursalId())
                    .orElseThrow(() -> new ForbiddenOperationException(
                            "No tiene permisos para operar sobre pedidos de otra sucursal"));

            case "CLIENTE" -> {
                Pedido pedido = pedidoRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id));
                if (!pedido.getCliente().getId().equals(current.getId())) {
                    throw new ForbiddenOperationException("No puede acceder a un pedido que no es suyo");
                }
                yield pedido;
            }

            default -> throw new ForbiddenOperationException("Rol no autorizado");
        };
    }

    private boolean esCliente(CustomUserDetails user) {
        return user.getUsuario().getRol().name().equals("CLIENTE");
    }

    private PedidoResponse toResponse(Pedido p) {
        List<ItemPedidoResponse> items = p.getItems().stream()
                .map(i -> new ItemPedidoResponse(
                        i.getId(), i.getProducto().getId(), i.getProducto().getNombre(),
                        i.getCantidad(), i.getPrecioUnitario(), i.getSubtotal()
                ))
                .toList();

        return new PedidoResponse(
                p.getId(),
                p.getCliente().getId(), p.getCliente().getNombre(),
                p.getMesa().getId(), p.getMesa().getNumero(),
                p.getSucursal().getId(), p.getSucursal().getNombre(),
                p.getEstado(), p.getTotal(), items
        );
    }
}