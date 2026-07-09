package com.uca.pncparcialfinalrestaurante.controller;

import com.uca.pncparcialfinalrestaurante.dto.request.CambiarEstadoMesaRequest;
import com.uca.pncparcialfinalrestaurante.dto.request.MesaRequest;
import com.uca.pncparcialfinalrestaurante.dto.response.MesaResponse;
import com.uca.pncparcialfinalrestaurante.service.MesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ENCARGADO_TURNO')")
public class MesaController {

    private final MesaService mesaService;

    @GetMapping
    public ResponseEntity<List<MesaResponse>> listar() {
        return ResponseEntity.ok(mesaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MesaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(mesaService.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MesaResponse> crear(@Valid @RequestBody MesaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mesaService.crear(request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<MesaResponse> cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoMesaRequest request) {
        return ResponseEntity.ok(mesaService.cambiarEstado(id, request));
    }
}