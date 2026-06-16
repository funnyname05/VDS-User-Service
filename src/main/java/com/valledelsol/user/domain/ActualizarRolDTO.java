package com.valledelsol.user.domain;

import jakarta.validation.constraints.NotNull;

public record ActualizarRolDTO(
        @NotNull
        Role nuevoRol
) {
}
