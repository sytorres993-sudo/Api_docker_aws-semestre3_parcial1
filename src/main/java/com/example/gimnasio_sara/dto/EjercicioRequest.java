package com.example.gimnasio_sara.dto;

import jakarta.validation.constraints.*;

public record EjercicioRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 1000) String descripcion,
        @NotBlank @Size(max = 80) String grupoMuscular,
        @NotBlank @Size(max = 100) String equipo) {}
