package com.example.gimnasio_sara.controller;

import com.example.gimnasio_sara.dto.*;
import com.example.gimnasio_sara.service.EjercicioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ejercicios")
public class EjercicioController {
    private final EjercicioService service;
    public EjercicioController(EjercicioService service) { this.service = service; }
    @GetMapping public List<EjercicioResponse> listar() { return service.listar(); }
    @GetMapping("/{id}") public EjercicioResponse obtener(@PathVariable Long id) { return service.obtener(id); }
    @PostMapping public ResponseEntity<EjercicioResponse> crear(@Valid @RequestBody EjercicioRequest r) {
        EjercicioResponse creado = service.crear(r);
        return ResponseEntity.created(URI.create("/api/ejercicios/" + creado.id())).body(creado);
    }
    @PutMapping("/{id}") public EjercicioResponse actualizar(@PathVariable Long id, @Valid @RequestBody EjercicioRequest r) { return service.actualizar(id, r); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
