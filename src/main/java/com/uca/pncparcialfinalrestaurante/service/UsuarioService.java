package com.uca.pncparcialfinalrestaurante.service;

import com.uca.pncparcialfinalrestaurante.domain.Sucursal;
import com.uca.pncparcialfinalrestaurante.domain.Usuario;
import com.uca.pncparcialfinalrestaurante.domain.enums.RolNombre;
import com.uca.pncparcialfinalrestaurante.dto.request.UsuarioRequest;
import com.uca.pncparcialfinalrestaurante.dto.response.UsuarioResponse;
import com.uca.pncparcialfinalrestaurante.exception.ResourceNotFoundException;
import com.uca.pncparcialfinalrestaurante.repository.SucursalRepository;
import com.uca.pncparcialfinalrestaurante.repository.UsuarioRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return toResponse(buscarOLanzar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ValidationException("Ya existe un usuario con ese email");
        }

        Sucursal sucursal = resolverSucursal(request);

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .rol(request.rol())
                .sucursal(sucursal)
                .activo(true)
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscarOLanzar(id);
        Sucursal sucursal = resolverSucursal(request);

        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setRol(request.rol());
        usuario.setSucursal(sucursal);

        // El password solo se actualiza si viene uno nuevo no vacio;
        // no queremos forzar a reenviar el password actual en cada edicion.
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = buscarOLanzar(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }


    private Sucursal resolverSucursal(UsuarioRequest request) {
        if (request.rol() == RolNombre.ENCARGADO_TURNO) {
            if (request.sucursalId() == null) {
                throw new ValidationException("Un Encargado de turno debe tener una sucursal asignada");
            }
            return sucursalRepository.findById(request.sucursalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada: " + request.sucursalId()));
        }
        return null;
    }

    private Usuario buscarOLanzar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getNombre(), u.getEmail(), u.getRol(),
                u.getSucursal() != null ? u.getSucursal().getId() : null,
                u.getSucursal() != null ? u.getSucursal().getNombre() : null,
                u.isActivo()
        );
    }
}