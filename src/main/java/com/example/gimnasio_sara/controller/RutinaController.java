package com.example.gimnasio_sara.controller;

import com.example.gimnasio_sara.dto.*;
import com.example.gimnasio_sara.service.RutinaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/rutinas")
public class RutinaController {
    private final RutinaService service;
    public RutinaController(RutinaService service) { this.service = service; }
    @GetMapping public List<RutinaResponse> listar() { return service.listar(); }
    @GetMapping("/{id}") public RutinaResponse obtener(@PathVariable Long id) { return service.obtener(id); }
    @PostMapping public ResponseEntity<RutinaResponse> crear(@Valid @RequestBody RutinaRequest r) {
        RutinaResponse creado = service.crear(r);
        return ResponseEntity.created(URI.create("/api/rutinas/" + creado.id())).body(creado);
    }
    @PutMapping("/{id}") public RutinaResponse actualizar(@PathVariable Long id, @Valid @RequestBody RutinaRequest r) { return service.actualizar(id, r); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
    @PutMapping("/{id}/ejercicios/{ejercicioId}") public RutinaResponse asociar(@PathVariable Long id, @PathVariable Long ejercicioId) { return service.asociar(id, ejercicioId); }
    @DeleteMapping("/{id}/ejercicios/{ejercicioId}") public ResponseEntity<Void> desasociar(@PathVariable Long id, @PathVariable Long ejercicioId) { service.desasociar(id, ejercicioId); return ResponseEntity.noContent().build(); }
}
