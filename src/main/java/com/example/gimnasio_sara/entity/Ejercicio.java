package com.example.gimnasio_sara.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ejercicios")
public class Ejercicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(nullable = false, length = 1000) private String descripcion;
    @Column(nullable = false, length = 80) private String grupoMuscular;
    @Column(nullable = false, length = 100) private String equipo;
    @ManyToMany(mappedBy = "ejercicios")
    private Set<Rutina> rutinas = new HashSet<>();

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String value) { nombre = value; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String value) { descripcion = value; }
    public String getGrupoMuscular() { return grupoMuscular; }
    public void setGrupoMuscular(String value) { grupoMuscular = value; }
    public String getEquipo() { return equipo; }
    public void setEquipo(String value) { equipo = value; }
    public Set<Rutina> getRutinas() { return rutinas; }
}
