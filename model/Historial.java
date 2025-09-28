package com.citasmedicas.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Historial {
    private String pacienteId; // Nuevo: ID del paciente al que pertenece este historial
    private String antecedentesMedicos;
    private List<HistorialEntry> entradas;

    public Historial(String pacienteId) {
        this.pacienteId = pacienteId;
        this.antecedentesMedicos = "";
        this.entradas = new ArrayList<>();
    }
    
    // Nuevo: Setter para el ID del paciente
    public void setPacienteId(String pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getAntecedentesMedicos() {
        return antecedentesMedicos;
    }

    public void setAntecedentesMedicos(String antecedentesMedicos) {
        this.antecedentesMedicos = antecedentesMedicos;
    }

    public List<HistorialEntry> getEntradas() {
        return entradas;
    }
    
    // Método para añadir una nueva entrada
    public void addEntrada(String descripcion) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fechaActual = LocalDate.now().format(dtf);
        entradas.add(new HistorialEntry(fechaActual, descripcion));
    }

    // Nuevo: Método para añadir una entrada existente (cargada de la DB)
    public void addEntradaFromDB(HistorialEntry entry) {
        // Esto es importante para evitar duplicados si ya existe un entry.id en la lista
        // o para asignar el ID correcto si se cargó de la DB
        if (!entradas.contains(entry)) { // Asumiendo que HistorialEntry tiene equals/hashCode basado en ID
            entradas.add(entry);
        }
    }


    // Clase interna para representar una entrada del historial
    public static class HistorialEntry {
        private int id; // Nuevo: ID autoincremental de la base de datos
        private String fecha;
        private String descripcion;

        public HistorialEntry(String fecha, String descripcion) {
            this.fecha = fecha;
            this.descripcion = descripcion;
        }

        // Nuevo: Getters y Setters para el ID
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getFecha() {
            return fecha;
        }

        public String getDescripcion() {
            return descripcion;
        }
        
        @Override
        public String toString() {
            return fecha + ": " + descripcion;
        }

        // Opcional: Implementar equals y hashCode para manejar entradas por ID
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HistorialEntry that = (HistorialEntry) o;
            return id != 0 && id == that.id; // Compara por ID si existe
        }

        @Override
        public int hashCode() {
            return id != 0 ? id : super.hashCode(); // Usa ID si existe
        }
    }
}