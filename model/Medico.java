package com.citasmedicas.model;

public class Medico {
    private String id; // Nuevo: ID único para la base de datos
    private String nombre;
    private String especialidad;

    public Medico(String nombre, String especialidad) {
        this.nombre = nombre;
        this.especialidad = especialidad;
        // El ID se asignará desde BaseDatos al guardar por primera vez
    }

    // Nuevo: Getters y Setters para el ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }
    
    @Override
    public String toString() {
        return "ID: " + id + ", Nombre: " + nombre + ", Especialidad: " + especialidad;
    }
}