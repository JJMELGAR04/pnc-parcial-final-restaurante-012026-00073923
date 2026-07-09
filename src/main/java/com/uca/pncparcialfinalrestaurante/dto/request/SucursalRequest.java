package com.uca.pncparcialfinalrestaurante.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SucursalRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(max = 200)
        String direccion,

        @Size(max = 20)
        String telefono
) {
}