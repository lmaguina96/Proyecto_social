package com.citasmedicas.model;

import com.citasmedicas.persistence.BaseDatos; // ¡Importante!
// Importa java.util.List y otros que puedas necesitar.
import java.util.List;

public class Administrador {
    
    // 1. Declarar la dependencia
    private BaseDatos baseDatos; 

    // 2. CONSTRUCTOR REQUERIDO: Acepta BaseDatos como argumento
    public Administrador(BaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }
    
    // Nota: La clase Administrador puede contener ahora la lógica de negocio 
    // para las acciones administrativas que necesiten la DB.

    public List<Cita> obtenerTodasLasCitas() {
        return baseDatos.obtenerTodasCitas();
    }
    
    // Ejemplo de método de administración usando la DB (opcional, ya lo hicimos en GUI)
    public boolean actualizarEstadoCita(String citaId, String nuevoEstado) {
        // En un sistema real, buscaríamos la cita por ID, la actualizaríamos
        // y la guardaríamos en la DB.
        
        // Aquí simulamos buscando todas y filtrando (menos eficiente, pero funcional)
        Cita cita = baseDatos.obtenerTodasCitas().stream()
                             .filter(c -> c.getId().equals(citaId))
                             .findFirst().orElse(null);

        if (cita != null) {
            cita.setEstado(nuevoEstado);
            baseDatos.guardarCita(cita);
            
            // Si la cita es 'Realizada', actualiza el historial
            if ("Realizada".equals(nuevoEstado)) {
                cita.getPaciente().getHistorial().addEntrada("Cita completada y marcada por Administrador.");
                baseDatos.guardarPaciente(cita.getPaciente()); 
            }
            return true;
        }
        return false;
    }
}