package com.example.gimnasio_sara.entity;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "rutinas")
public class Rutina {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(nullable = false, length = 1000) private String descripcion;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Nivel nivel;
    @Column(nullable = false) private int duracionMinutos;
    @ManyToMany
    @JoinTable(name = "rutina_ejercicio", joinColumns = @JoinColumn(name = "rutina_id"),
            inverseJoinColumns = @JoinColumn(name = "ejercicio_id"))
    private Set<Ejercicio> ejercicios = new LinkedHashSet<>();

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String value) { nombre = value; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String value) { descripcion = value; }
    public Nivel getNivel() { return nivel; }
    public void setNivel(Nivel value) { nivel = value; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(int value) { duracionMinutos = value; }
    public Set<Ejercicio> getEjercicios() { return ejercicios; }
}
