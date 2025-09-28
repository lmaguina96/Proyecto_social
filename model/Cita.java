package com.citasmedicas.model;

public class Cita {
    private String id; // Nuevo: ID único para la base de datos
    private Paciente paciente;
    private Medico medico;
    private String fecha;
    private String hora;
    private String motivo;
    private String estado; // Programada, Realizada, Cancelada

    public Cita(Paciente paciente, Medico medico, String fecha, String hora, String motivo) {
        this.paciente = paciente;
        this.medico = medico;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = "Programada"; // Estado inicial
        // El ID se asignará desde BaseDatos al guardar por primera vez
    }

    // Nuevo: Getters y Setters para el ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    @Override
    public String toString() {
        return "ID Cita: " + id + ", Paciente: " + paciente.getNombre() + ", Médico: " + medico.getNombre() + ", Fecha: " + fecha + ", Hora: " + hora + ", Estado: " + estado;
    }
}