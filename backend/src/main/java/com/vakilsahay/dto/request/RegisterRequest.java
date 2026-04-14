package com.vakilsahay.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ─── RegisterRequest ────────────────────────────────────────────────────────
public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 2, max = 100) String fullName,
        @NotBlank @Size(min = 8, max = 100) String password
) {}