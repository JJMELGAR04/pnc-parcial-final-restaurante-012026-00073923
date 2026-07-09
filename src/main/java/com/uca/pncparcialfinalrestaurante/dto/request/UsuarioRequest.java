package com.uca.pncparcialfinalrestaurante.dto.request;

import com.uca.pncparcialfinalrestaurante.domain.enums.RolNombre;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, message = "La contrasena debe tener minimo 8 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        RolNombre rol,

        /**
         * Obligatorio solo si rol = ENCARGADO_TURNO.
         * Se valida en el service, no aqui, porque es una regla que depende de otro campo
         * (bean validation por si solo no valida bien reglas cruzadas entre campos sin
         * anotaciones custom adicionales).
         */
        Long sucursalId
) {
}