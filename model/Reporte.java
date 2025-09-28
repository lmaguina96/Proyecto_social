package com.citasmedicas.model;

import com.citasmedicas.persistence.BaseDatos;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors; // Necesario para el reporte de Pacientes Atendidos

public class Reporte {
    private BaseDatos baseDatos;

    public Reporte(BaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }

    public String generarReportePacientesAtendidos() {
        // 1. Obtiene todas las citas marcadas como 'Realizada'
        List<Cita> citasRealizadas = baseDatos.obtenerTodasCitas().stream()
                .filter(c -> "Realizada".equals(c.getEstado()))
                .collect(Collectors.toList());

        // 2. Cuenta pacientes únicos
        long pacientesAtendidos = citasRealizadas.stream()
                .map(Cita::getPaciente)
                .distinct()
                .count();

        StringBuilder reporte = new StringBuilder("--- Reporte de Pacientes Atendidos ---\n");
        reporte.append("Total de citas realizadas: ").append(citasRealizadas.size()).append("\n");
        reporte.append("Total de pacientes únicos atendidos: ").append(pacientesAtendidos).append("\n");
        reporte.append("---------------------------------------------------\n");

        if (citasRealizadas.isEmpty()) {
            reporte.append("No hay citas marcadas como 'Realizada' todavía.\n");
        } else {
             reporte.append("Lista de pacientes con citas realizadas:\n");
             citasRealizadas.stream()
                     .map(Cita::getPaciente)
                     .distinct()
                     .forEach(p -> reporte.append(" - ").append(p.getNombre()).append(" (ID: ").append(p.getId()).append(")\n"));
        }
        return reporte.toString();
    }

    /**
     * Genera un reporte de TODAS las citas cuya fecha de programación es el día actual.
     * Incluye su estado actualizado (Programada, Realizada o Cancelada).
     */
    public String generarReporteCitasDelDia() {
        // Usamos el formato 'yyyy-MM-dd' para asegurar la coincidencia con la DB
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fechaActual = LocalDate.now().format(dtf);
        
        // Esta línea es la clave: obtenerCitasPorFecha(fechaActual) solo trae citas programadas para HOY
        List<Cita> citasHoy = baseDatos.obtenerCitasPorFecha(fechaActual); 
        
        StringBuilder reporte = new StringBuilder("--- Reporte de Citas Programadas para Hoy (" + fechaActual + ") ---\n");
        reporte.append("Total de citas programadas para hoy: ").append(citasHoy.size()).append("\n");
        reporte.append("---------------------------------------------------\n");

        if (citasHoy.isEmpty()) {
            reporte.append("No hay citas programadas para hoy.\n");
        } else {
            citasHoy.forEach(cita -> {
                String estadoDisplay = "Programada";
                if ("Realizada".equals(cita.getEstado())) {
                    estadoDisplay = "¡REALIZADA!"; // Mejora visual
                } else if ("Cancelada".equals(cita.getEstado())) {
                    estadoDisplay = "CANCELADA"; // Mejora visual
                }

                reporte.append("Cita ID: ").append(cita.getId()).append("\n");
                
                // Manejo de posibles nulos (aunque no debería pasar si la DB está bien)
                String nombrePaciente = cita.getPaciente() != null ? cita.getPaciente().getNombre() : "Paciente Desconocido";
                String nombreMedico = cita.getMedico() != null ? cita.getMedico().getNombre() : "Médico Desconocido";
                
                reporte.append("  Paciente: ").append(nombrePaciente).append("\n");
                reporte.append("  Médico: ").append(nombreMedico).append("\n");
                reporte.append("  Hora: ").append(cita.getHora()).append("\n");
                reporte.append("  Motivo: ").append(cita.getMotivo()).append("\n");
                reporte.append("  Estado: ").append(estadoDisplay).append("\n"); 
                reporte.append("---------------------------------------------------\n");
            });
        }
        return reporte.toString();
    }
}