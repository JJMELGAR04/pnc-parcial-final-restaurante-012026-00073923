package com.uca.pncparcialfinalrestaurante.service;

import com.uca.pncparcialfinalrestaurante.domain.Mesa;
import com.uca.pncparcialfinalrestaurante.domain.Sucursal;
import com.uca.pncparcialfinalrestaurante.domain.enums.EstadoMesa;
import com.uca.pncparcialfinalrestaurante.dto.request.CambiarEstadoMesaRequest;
import com.uca.pncparcialfinalrestaurante.dto.request.MesaRequest;
import com.uca.pncparcialfinalrestaurante.dto.response.MesaResponse;
import com.uca.pncparcialfinalrestaurante.exception.ForbiddenOperationException;
import com.uca.pncparcialfinalrestaurante.exception.ResourceNotFoundException;
import com.uca.pncparcialfinalrestaurante.repository.MesaRepository;
import com.uca.pncparcialfinalrestaurante.repository.SucursalRepository;
import com.uca.pncparcialfinalrestaurante.security.CustomUserDetails;
import com.uca.pncparcialfinalrestaurante.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MesaService {

    private final MesaRepository mesaRepository;
    private final SucursalRepository sucursalRepository;

    @Transactional(readOnly = true)
    public List<MesaResponse> listar() {
        CustomUserDetails current = SecurityUtils.getCurrentUser();

        // Regla de negocio (autorizacion por atributo): un ADMINISTRADOR ve todas
        // las mesas; un ENCARGADO_TURNO solo ve las de su propia sucursal.
        // Esto NO se resuelve con el rol solo -- se necesita comparar el dato real.
        List<Mesa> mesas = esAdministrador(current)
                ? mesaRepository.findAll()
                : mesaRepository.findBySucursalId(current.getSucursalId());

        return mesas.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MesaResponse obtener(Long id) {
        Mesa mesa = buscarOLanzar(id);
        verificarAccesoASucursal(mesa.getSucursal().getId());
        return toResponse(mesa);
    }

    @Transactional
    public MesaResponse crear(MesaRequest request) {
        // Solo Administrador llega aqui (restringido en el controller),
        // por lo que no hace falta validar sucursal contra el usuario actual.
        Sucursal sucursal = sucursalRepository.findById(request.sucursalId())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada: " + request.sucursalId()));

        Mesa mesa = Mesa.builder()
                .numero(request.numero())
                .capacidad(request.capacidad())
                .estado(EstadoMesa.LIBRE)
                .sucursal(sucursal)
                .build();

        return toResponse(mesaRepository.save(mesa));
    }

    @Transactional
    public MesaResponse cambiarEstado(Long id, CambiarEstadoMesaRequest request) {
        Mesa mesa = buscarOLanzar(id);

        // Aqui esta el nucleo de la Opcion B: un Encargado de turno puede cambiar
        // el estado de una mesa SOLO si pertenece a su propia sucursal.
        // El rol ya se valido en el controller (@PreAuthorize hasAnyRole), pero el
        // rol por si solo no alcanza -- falta esta comparacion de atributo.
        verificarAccesoASucursal(mesa.getSucursal().getId());

        mesa.setEstado(request.nuevoEstado());
        return toResponse(mesaRepository.save(mesa));
    }

    /**
     * Lanza ForbiddenOperationException si el usuario actual es ENCARGADO_TURNO
     * y la sucursal del recurso no coincide con la suya.
     * ADMINISTRADOR siempre pasa, sin importar la sucursal.
     */
    private void verificarAccesoASucursal(Long sucursalDelRecursoId) {
        CustomUserDetails current = SecurityUtils.getCurrentUser();

        if (esAdministrador(current)) {
            return;
        }

        Long sucursalDelUsuario = current.getSucursalId();

        if (sucursalDelUsuario == null || !sucursalDelUsuario.equals(sucursalDelRecursoId)) {
            throw new ForbiddenOperationException(
                    "No tiene permisos para operar sobre recursos de otra sucursal"
            );
        }
    }

    private boolean esAdministrador(CustomUserDetails user) {
        return user.getUsuario().getRol().name().equals("ADMINISTRADOR");
    }

    private Mesa buscarOLanzar(Long id) {
        return mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada: " + id));
    }

    private MesaResponse toResponse(Mesa m) {
        return new MesaResponse(
                m.getId(), m.getNumero(), m.getCapacidad(), m.getEstado(),
                m.getSucursal().getId(), m.getSucursal().getNombre()
        );
    }
}