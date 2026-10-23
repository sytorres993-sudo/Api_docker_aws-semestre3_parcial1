package com.example.gimnasio_sara.dto;

import com.example.gimnasio_sara.entity.*;
import java.util.Comparator;
import java.util.List;

public record RutinaResponse(Long id, String nombre, String descripcion, Nivel nivel,
                             int duracionMinutos, List<EjercicioDetalle> ejercicios) {
    public record EjercicioDetalle(Long id, String nombre, String descripcion, String grupoMuscular, String equipo) {}
    public static RutinaResponse from(Rutina r) {
        return new RutinaResponse(r.getId(), r.getNombre(), r.getDescripcion(), r.getNivel(), r.getDuracionMinutos(),
                r.getEjercicios().stream().sorted(Comparator.comparing(Ejercicio::getId))
                    .map(e -> new EjercicioDetalle(e.getId(), e.getNombre(), e.getDescripcion(), e.getGrupoMuscular(), e.getEquipo())).toList());
    }
}
