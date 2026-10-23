package com.example.gimnasio_sara.dto;

import com.example.gimnasio_sara.entity.Nivel;
import jakarta.validation.constraints.*;
import java.util.Set;

public record RutinaRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 1000) String descripcion,
        @NotNull Nivel nivel,
        @NotNull @Min(1) @Max(600) Integer duracionMinutos,
        @NotNull @Size(max = 100) Set<@NotNull @Positive Long> ejercicioIds) {}
