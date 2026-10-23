package com.example.gimnasio_sara.dto;

import com.example.gimnasio_sara.entity.Ejercicio;
import java.util.List;

public record EjercicioResponse(Long id, String nombre, String descripcion, String grupoMuscular,
                                String equipo, List<Long> rutinaIds) {
    public static EjercicioResponse from(Ejercicio e) {
        return new EjercicioResponse(e.getId(), e.getNombre(), e.getDescripcion(), e.getGrupoMuscular(),
                e.getEquipo(), e.getRutinas().stream().map(r -> r.getId()).sorted().toList());
    }
}
