package com.valledelsol.user.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationUserDTO(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password
) {
}
