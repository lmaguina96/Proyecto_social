package com.citasmedicas.model;

// Importa aquí si tienes otras dependencias, ej. java.util.List, etc.

public class Paciente {
    private String id; // Nuevo: ID único para la base de datos
    private String nombre;
    private String dni;
    private int edad;
    private Historial historial;

    public Paciente(String nombre, String dni, int edad, Historial historial) {
        this.nombre = nombre;
        this.dni = dni;
        this.edad = edad;
        this.historial = historial;
        // El ID se asignará desde BaseDatos al guardar por primera vez
    }

    // Nuevo: Constructor para cargar desde la DB si se necesita crear un paciente sin historial inicial
    public Paciente(String nombre, String dni, int edad) {
        this.nombre = nombre;
        this.dni = dni;
        this.edad = edad;
        this.historial = new Historial(null); // Historial vacío, el ID del paciente se seteará más tarde
    }

    // Nuevo: Getters y Setters para el ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        if (this.historial != null) {
            this.historial.setPacienteId(id); // Asegura que el historial tenga el ID correcto del paciente
        }
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public Historial getHistorial() {
        return historial;
    }

    public void setHistorial(Historial historial) {
        this.historial = historial;
        if (this.id != null && this.historial != null) {
            this.historial.setPacienteId(this.id); // Asegura que el historial tenga el ID correcto del paciente
        }
    }
    
    @Override
    public String toString() {
        return "ID: " + id + ", Nombre: " + nombre + ", DNI: " + dni;
    }
}