package com.uca.pncparcialfinalrestaurante.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MesaRequest(

        @NotNull(message = "El numero de mesa es obligatorio")
        @Min(value = 1, message = "El numero de mesa debe ser mayor a 0")
        Integer numero,

        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad debe ser mayor a 0")
        Integer capacidad,

        @NotNull(message = "La sucursal es obligatoria")
        Long sucursalId
) {
}