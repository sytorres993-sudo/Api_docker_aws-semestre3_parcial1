package com.example.gimnasio_sara.service;

import com.example.gimnasio_sara.dto.*;
import com.example.gimnasio_sara.entity.*;
import com.example.gimnasio_sara.exception.RecursoNoEncontradoException;
import com.example.gimnasio_sara.repository.EjercicioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class EjercicioService {
    private final EjercicioRepository repository;
    public EjercicioService(EjercicioRepository repository) { this.repository = repository; }
    public List<EjercicioResponse> listar() { return repository.findAll(Sort.by("id")).stream().map(EjercicioResponse::from).toList(); }
    public EjercicioResponse obtener(Long id) { return EjercicioResponse.from(buscar(id)); }
    private Ejercicio buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Ejercicio " + id + " no encontrado"));
    }
    @Transactional
    public EjercicioResponse crear(EjercicioRequest request) { return guardar(new Ejercicio(), request); }
    @Transactional
    public EjercicioResponse actualizar(Long id, EjercicioRequest request) { return guardar(buscar(id), request); }
    private EjercicioResponse guardar(Ejercicio e, EjercicioRequest r) {
        e.setNombre(r.nombre().trim()); e.setDescripcion(r.descripcion().trim());
        e.setGrupoMuscular(r.grupoMuscular().trim()); e.setEquipo(r.equipo().trim());
        return EjercicioResponse.from(repository.save(e));
    }
    @Transactional
    public void eliminar(Long id) {
        Ejercicio e = buscar(id);
        for (Rutina rutina : new HashSet<>(e.getRutinas())) { rutina.getEjercicios().remove(e); }
        e.getRutinas().clear();
        repository.delete(e);
    }
}
