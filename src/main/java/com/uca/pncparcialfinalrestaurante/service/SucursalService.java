package com.uca.pncparcialfinalrestaurante.service;

import com.uca.pncparcialfinalrestaurante.domain.Sucursal;
import com.uca.pncparcialfinalrestaurante.dto.request.SucursalRequest;
import com.uca.pncparcialfinalrestaurante.dto.response.SucursalResponse;
import com.uca.pncparcialfinalrestaurante.exception.ResourceNotFoundException;
import com.uca.pncparcialfinalrestaurante.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SucursalService {

    private final SucursalRepository sucursalRepository;

    @Transactional(readOnly = true)
    public List<SucursalResponse> listar() {
        return sucursalRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SucursalResponse obtener(Long id) {
        return toResponse(buscarOLanzar(id));
    }

    @Transactional
    public SucursalResponse crear(SucursalRequest request) {
        Sucursal sucursal = Sucursal.builder()
                .nombre(request.nombre())
                .direccion(request.direccion())
                .telefono(request.telefono())
                .activa(true)
                .build();
        return toResponse(sucursalRepository.save(sucursal));
    }

    @Transactional
    public SucursalResponse actualizar(Long id, SucursalRequest request) {
        Sucursal sucursal = buscarOLanzar(id);
        sucursal.setNombre(request.nombre());
        sucursal.setDireccion(request.direccion());
        sucursal.setTelefono(request.telefono());
        return toResponse(sucursalRepository.save(sucursal));
    }

    @Transactional
    public void desactivar(Long id) {
        Sucursal sucursal = buscarOLanzar(id);
        sucursal.setActiva(false);
        sucursalRepository.save(sucursal);
    }

    private Sucursal buscarOLanzar(Long id) {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada: " + id));
    }

    private SucursalResponse toResponse(Sucursal s) {
        return new SucursalResponse(s.getId(), s.getNombre(), s.getDireccion(), s.getTelefono(), s.isActiva());
    }
}