package com.example.gimnasio_sara.service;

import com.example.gimnasio_sara.dto.*;
import com.example.gimnasio_sara.entity.*;
import com.example.gimnasio_sara.exception.RecursoNoEncontradoException;
import com.example.gimnasio_sara.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class RutinaService {
    private final RutinaRepository rutinas;
    private final EjercicioRepository ejercicios;
    public RutinaService(RutinaRepository rutinas, EjercicioRepository ejercicios) { this.rutinas = rutinas; this.ejercicios = ejercicios; }
    private Rutina buscar(Long id) { return rutinas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Rutina " + id + " no encontrada")); }
    private Ejercicio buscarEjercicio(Long id) { return ejercicios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Ejercicio " + id + " no encontrado")); }
    public List<RutinaResponse> listar() { return rutinas.findAll(Sort.by("id")).stream().map(RutinaResponse::from).toList(); }
    public RutinaResponse obtener(Long id) { return RutinaResponse.from(buscar(id)); }
    @Transactional
    public RutinaResponse crear(RutinaRequest r) { return guardar(new Rutina(), r); }
    @Transactional
    public RutinaResponse actualizar(Long id, RutinaRequest r) { return guardar(buscar(id), r); }
    private RutinaResponse guardar(Rutina rutina, RutinaRequest r) {
        Set<Ejercicio> seleccionados = new LinkedHashSet<>(ejercicios.findAllById(r.ejercicioIds()));
        if (seleccionados.size() != r.ejercicioIds().size()) {
            throw new RecursoNoEncontradoException("Uno o más ejercicios seleccionados no existen");
        }
        rutina.setNombre(r.nombre().trim()); rutina.setDescripcion(r.descripcion().trim());
        rutina.setNivel(r.nivel()); rutina.setDuracionMinutos(r.duracionMinutos());
        rutina.getEjercicios().clear(); rutina.getEjercicios().addAll(seleccionados);
        return RutinaResponse.from(rutinas.save(rutina));
    }
    @Transactional
    public RutinaResponse asociar(Long rutinaId, Long ejercicioId) {
        Rutina rutina = buscar(rutinaId); rutina.getEjercicios().add(buscarEjercicio(ejercicioId));
        return RutinaResponse.from(rutina);
    }
    @Transactional
    public void desasociar(Long rutinaId, Long ejercicioId) {
        Rutina rutina = buscar(rutinaId); Ejercicio ejercicio = buscarEjercicio(ejercicioId);
        rutina.getEjercicios().remove(ejercicio);
    }
    @Transactional
    public void eliminar(Long id) { rutinas.delete(buscar(id)); }
}
