package com.citasmedicas.gui;

import com.citasmedicas.model.*;
import com.citasmedicas.persistence.BaseDatos;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InterfazGrafica extends JFrame {

    // ====================================================================
    // 1. DECLARACIÓN DE DEPENDENCIAS Y COMPONENTES SWING
    // ====================================================================

    // Dependencias
    private BaseDatos baseDatos;
    private Reporte reporte;
    private Administrador administrador;

    // Componentes principales
    private JTabbedPane tabbedPane;

    // Componentes de Registro Paciente
    private JTextField txtNombrePaciente, txtDniPaciente, txtEdadPaciente;
    private JTextArea txtAntecedentesPaciente;
    private JButton btnRegistrarPaciente;
    
    // Componentes de Programación Citas
    private JComboBox<Paciente> cbPacientesCitas; 
    private JComboBox<Medico> cbMedicosCitas;     
    private JTextField txtFechaCita, txtHoraCita, txtMotivoCita;
    private JButton btnProgramarCita;

    // Componentes de Consulta Historial
    private JComboBox<Paciente> cbPacientesHistorial;
    private JTextArea txtHistorialPaciente;
    private JButton btnConsultarHistorial;
    private JScrollPane scrollHistorial; // Para hacer scroll en el historial

    // Componentes de Administrar Citas
    private JTable tblCitas;
    private JButton btnMarcarRealizada;
    private JButton btnCancelarCita;

    // Componentes de Reportes
    private JTextArea txtReportes;
    private JButton btnReportePacientes;
    private JButton btnReporteCitasDia;
    private JScrollPane scrollReportes; // Para hacer scroll en los reportes


    // ====================================================================
    // 2. CONSTRUCTOR (Orden de inicialización corregido)
    // ====================================================================

    public InterfazGrafica() {
        setTitle("Sistema de Gestión de Citas Médicas");
        setSize(800, 600);
        setLocationRelativeTo(null); 

        // 2A. Inicialización de DB y Lógica (Modelo)
        baseDatos = new BaseDatos(); // Esto conecta y crea las tablas SQLite
        reporte = new Reporte(baseDatos); // Pasa la DB al objeto Reporte
        administrador = new Administrador(baseDatos); // Pasa la DB al Administrador
        
        // 2B. INICIALIZACIÓN DE COMPONENTES SWING (CORRECCIÓN del NullPointerException)
        // Esto debe ir ANTES de cualquier llamada a 'actualizarComboBoxes()'
        inicializarComponentes(); 

        // 2C. Cargar Datos Iniciales (Ahora los ComboBoxes existen)
        actualizarComboBoxes(); 
        actualizarTablaCitas(); 

        // 2D. Configurar Listeners (Lógica de botones)
        configurarListeners(); 
        
        // 2E. Configuración final de la ventana
        add(tabbedPane); 
        
        // 2F. GESTIÓN DEL CIERRE DE LA APLICACIÓN
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(InterfazGrafica.this,
                        "¿Está seguro de que desea cerrar la aplicación y guardar los datos?", "Cerrar Aplicación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    baseDatos.guardarContadoresId(); // Guarda el último ID usado
                    baseDatos.desconectar(); // Cierra la conexión SQLite
                    System.exit(0); // Cierra la aplicación
                }
            }
        });
    }

    // ====================================================================
    // 3. INICIALIZACIÓN DE COMPONENTES DE LA GUI
    // ====================================================================
    private void inicializarComponentes() {
        tabbedPane = new JTabbedPane();

        // 3A. Pestaña de Registro de Paciente
        JPanel panelRegistroPaciente = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNombrePaciente = new JTextField(20);
        txtDniPaciente = new JTextField(20);
        txtEdadPaciente = new JTextField(20);
        txtAntecedentesPaciente = new JTextArea(5, 20);
        txtAntecedentesPaciente.setLineWrap(true);
        btnRegistrarPaciente = new JButton("Registrar Paciente");

        gbc.gridx = 0; gbc.gridy = 0; panelRegistroPaciente.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panelRegistroPaciente.add(txtNombrePaciente, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panelRegistroPaciente.add(new JLabel("DNI:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panelRegistroPaciente.add(txtDniPaciente, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panelRegistroPaciente.add(new JLabel("Edad:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panelRegistroPaciente.add(txtEdadPaciente, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panelRegistroPaciente.add(new JLabel("Antecedentes:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridheight = 2; panelRegistroPaciente.add(new JScrollPane(txtAntecedentesPaciente), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridheight = 1; panelRegistroPaciente.add(btnRegistrarPaciente, gbc);
        tabbedPane.addTab("1. Registrar Paciente", panelRegistroPaciente);

        // 3B. Pestaña de Programación de Citas
        JPanel panelProgramacionCitas = new JPanel(new GridBagLayout());
        cbPacientesCitas = new JComboBox<>();
        cbMedicosCitas = new JComboBox<>();
        txtFechaCita = new JTextField("", 20);
        txtHoraCita = new JTextField("", 20);
        txtMotivoCita = new JTextField(20);
        btnProgramarCita = new JButton("Programar Cita");
        
        gbc.gridx = 0; gbc.gridy = 0; panelProgramacionCitas.add(new JLabel("Paciente:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panelProgramacionCitas.add(cbPacientesCitas, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panelProgramacionCitas.add(new JLabel("Médico:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panelProgramacionCitas.add(cbMedicosCitas, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panelProgramacionCitas.add(new JLabel("Fecha (DD-MM-AAAA):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panelProgramacionCitas.add(txtFechaCita, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panelProgramacionCitas.add(new JLabel("Hora (HH:MM):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panelProgramacionCitas.add(txtHoraCita, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panelProgramacionCitas.add(new JLabel("Motivo:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; panelProgramacionCitas.add(txtMotivoCita, gbc);
        gbc.gridx = 1; gbc.gridy = 5; panelProgramacionCitas.add(btnProgramarCita, gbc);
        tabbedPane.addTab("2. Programar Cita", panelProgramacionCitas);

        // 3C. Pestaña de Consulta de Historial
        JPanel panelConsultaHistorial = new JPanel(new BorderLayout(10, 10));
        JPanel panelControlHistorial = new JPanel(new FlowLayout());
        
        cbPacientesHistorial = new JComboBox<>();
        btnConsultarHistorial = new JButton("Consultar Historial");
        txtHistorialPaciente = new JTextArea(25, 60);
        txtHistorialPaciente.setEditable(false);
        scrollHistorial = new JScrollPane(txtHistorialPaciente);
        
        panelControlHistorial.add(new JLabel("Seleccionar Paciente:"));
        panelControlHistorial.add(cbPacientesHistorial);
        panelControlHistorial.add(btnConsultarHistorial);
        
        panelConsultaHistorial.add(panelControlHistorial, BorderLayout.NORTH);
        panelConsultaHistorial.add(scrollHistorial, BorderLayout.CENTER);
        tabbedPane.addTab("3. Consultar Historial", panelConsultaHistorial);

        // 3D. Pestaña de Administrar Citas
        JPanel panelAdminCitas = new JPanel(new BorderLayout());
        JPanel panelBotonesAdmin = new JPanel(new FlowLayout());
        
        String[] columnNames = {"ID Cita", "Paciente", "Médico", "Fecha", "Hora", "Motivo", "Estado"};
        tblCitas = new JTable(new DefaultTableModel(columnNames, 0));
        JScrollPane scrollCitas = new JScrollPane(tblCitas);
        
        btnMarcarRealizada = new JButton("Marcar como Realizada");
        btnCancelarCita = new JButton("Cancelar Cita");
        
        panelBotonesAdmin.add(btnMarcarRealizada);
        panelBotonesAdmin.add(btnCancelarCita);
        
        panelAdminCitas.add(scrollCitas, BorderLayout.CENTER);
        panelAdminCitas.add(panelBotonesAdmin, BorderLayout.SOUTH);
        tabbedPane.addTab("4. Administrar Citas", panelAdminCitas);

        // 3E. Pestaña de Reportes
        JPanel panelReportes = new JPanel(new BorderLayout(10, 10));
        JPanel panelBotonesReportes = new JPanel(new FlowLayout());
        
        txtReportes = new JTextArea(25, 60);
        txtReportes.setEditable(false);
        scrollReportes = new JScrollPane(txtReportes);
        
        btnReportePacientes = new JButton("Reporte Pacientes Atendidos");
        btnReporteCitasDia = new JButton("Reporte Citas del Día");
        
        panelBotonesReportes.add(btnReportePacientes);
        panelBotonesReportes.add(btnReporteCitasDia);
        
        panelReportes.add(panelBotonesReportes, BorderLayout.NORTH);
        panelReportes.add(scrollReportes, BorderLayout.CENTER);
        tabbedPane.addTab("5. Reportes", panelReportes);
    }
    
    // ====================================================================
    // 4. MÉTODOS DE ACTUALIZACIÓN DE DATOS (Carga desde SQLite)
    // ====================================================================

    private void actualizarComboBoxes() {
        // --- PACIENTES ---
        cbPacientesHistorial.removeAllItems();
        cbPacientesCitas.removeAllItems();
        List<Paciente> pacientes = baseDatos.obtenerTodosPacientes(); // Carga de la DB
        for (Paciente p : pacientes) {
            cbPacientesHistorial.addItem(p);
            cbPacientesCitas.addItem(p);
        }

        // --- MÉDICOS ---
        cbMedicosCitas.removeAllItems();
        List<Medico> medicos = baseDatos.obtenerTodosMedicos(); // Carga de la DB
        
        // Si no hay médicos, añade algunos de ejemplo y los guarda en la DB
        if (medicos.isEmpty()) { 
            Medico m1 = new Medico("Dr. Juan Pérez", "General");
            Medico m2 = new Medico("Dra. Ana García", "Pediatría");
            baseDatos.guardarMedico(m1);
            baseDatos.guardarMedico(m2);
            medicos = baseDatos.obtenerTodosMedicos(); // Vuelve a cargar para tener IDs
        }

        for (Medico m : medicos) {
            cbMedicosCitas.addItem(m);
        }
    }
    
    private void actualizarTablaCitas() {
        DefaultTableModel model = (DefaultTableModel) tblCitas.getModel();
        model.setRowCount(0); // Limpiar filas existentes

        List<Cita> todasCitas = baseDatos.obtenerTodasCitas(); // Carga de la DB
        for (Cita cita : todasCitas) {
            // Asegúrate de que los IDs del paciente y médico existen para evitar NullPointer
            String nombrePaciente = cita.getPaciente() != null ? cita.getPaciente().getNombre() : "N/D";
            String nombreMedico = cita.getMedico() != null ? cita.getMedico().getNombre() : "N/D";

            model.addRow(new Object[]{
                cita.getId(),
                nombrePaciente,
                nombreMedico,
                cita.getFecha(),
                cita.getHora(),
                cita.getMotivo(),
                cita.getEstado()
            });
        }
    }


    // ====================================================================
    // 5. LISTENERS (Manejo de la lógica de persistencia en SQLite)
    // ====================================================================

    private void configurarListeners() {
        
        // --- Registrar Paciente ---
        btnRegistrarPaciente.addActionListener(e -> {
            String nombre = txtNombrePaciente.getText();
            String dni = txtDniPaciente.getText();
            String edadStr = txtEdadPaciente.getText();
            String antecedentes = txtAntecedentesPaciente.getText();

            if (nombre.isEmpty() || dni.isEmpty() || edadStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int edad = Integer.parseInt(edadStr);
                Historial historial = new Historial(null); 
                historial.setAntecedentesMedicos(antecedentes);
                Paciente nuevoPaciente = new Paciente(nombre, dni, edad, historial);

                baseDatos.guardarPaciente(nuevoPaciente); // Guarda en SQLite

                JOptionPane.showMessageDialog(this, "Paciente registrado con éxito. ID: " + nuevoPaciente.getId());
                txtNombrePaciente.setText("");
                txtDniPaciente.setText("");
                txtEdadPaciente.setText("");
                txtAntecedentesPaciente.setText("");
                actualizarComboBoxes(); // Refresca las listas
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Edad debe ser un número válido.", "Error de formato", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Programar Cita ---
        btnProgramarCita.addActionListener(e -> {
            Paciente pacienteSeleccionado = (Paciente) cbPacientesCitas.getSelectedItem();
            Medico medicoSeleccionado = (Medico) cbMedicosCitas.getSelectedItem();
            String fecha = txtFechaCita.getText(); 
            String hora = txtHoraCita.getText();   
            String motivo = txtMotivoCita.getText();

            if (pacienteSeleccionado == null || medicoSeleccionado == null || fecha.isEmpty() || hora.isEmpty() || motivo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione paciente y médico, e ingrese fecha, hora y motivo.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Cita nuevaCita = new Cita(pacienteSeleccionado, medicoSeleccionado, fecha, hora, motivo);
            baseDatos.guardarCita(nuevaCita); // Guarda en SQLite

            JOptionPane.showMessageDialog(this, "Cita programada con éxito. ID: " + nuevaCita.getId());
            txtFechaCita.setText("");
            txtHoraCita.setText("");
            txtMotivoCita.setText("");
            actualizarTablaCitas(); // Refresca la tabla de administración
        });

        // --- Consultar Historial ---
        btnConsultarHistorial.addActionListener(e -> {
            Paciente pacienteSeleccionado = (Paciente) cbPacientesHistorial.getSelectedItem();
            if (pacienteSeleccionado == null) {
                txtHistorialPaciente.setText("Seleccione un paciente.");
                return;
            }

            Historial historial = pacienteSeleccionado.getHistorial();
            StringBuilder sb = new StringBuilder();
            sb.append("Historial de: ").append(pacienteSeleccionado.getNombre()).append("\n");
            sb.append("DNI: ").append(pacienteSeleccionado.getDni()).append("\n");
            sb.append("Antecedentes Médicos: ").append(historial.getAntecedentesMedicos()).append("\n");
            sb.append("--- Entradas ---\n");
            if (historial.getEntradas().isEmpty()) {
                sb.append("No hay entradas en el historial.\n");
            } else {
                for (Historial.HistorialEntry entry : historial.getEntradas()) {
                    sb.append(entry.getFecha()).append(": ").append(entry.getDescripcion()).append("\n");
                }
            }
            txtHistorialPaciente.setText(sb.toString());
        });

        // --- Marcar Realizada ---
        btnMarcarRealizada.addActionListener(e -> {
            int selectedRow = tblCitas.getSelectedRow();
            if (selectedRow != -1) {
                String citaId = (String) tblCitas.getValueAt(selectedRow, 0);
                
                // Buscar la cita completa y el paciente asociado
                Cita cita = baseDatos.obtenerTodasCitas().stream()
                                     .filter(c -> c.getId().equals(citaId))
                                     .findFirst().orElse(null);
                
                if (cita != null && !cita.getEstado().equals("Realizada")) {
                    cita.setEstado("Realizada");
                    baseDatos.guardarCita(cita); // Actualizar estado en la DB
                    
                    // Añadir entrada al historial del paciente
                    cita.getPaciente().getHistorial().addEntrada("Cita realizada con " + cita.getMedico().getNombre() + " por " + cita.getMotivo());
                    baseDatos.guardarPaciente(cita.getPaciente()); // Guardar paciente para actualizar el historial
                    
                    JOptionPane.showMessageDialog(this, "Cita " + citaId + " marcada como Realizada.");
                    actualizarTablaCitas(); // Refrescar tabla
                } else if (cita != null && cita.getEstado().equals("Realizada")) {
                     JOptionPane.showMessageDialog(this, "Esta cita ya está marcada como Realizada.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una cita para marcar como Realizada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Cancelar Cita ---
        btnCancelarCita.addActionListener(e -> {
            int selectedRow = tblCitas.getSelectedRow();
            if (selectedRow != -1) {
                String citaId = (String) tblCitas.getValueAt(selectedRow, 0);
                
                Cita cita = baseDatos.obtenerTodasCitas().stream()
                                     .filter(c -> c.getId().equals(citaId))
                                     .findFirst().orElse(null);

                if (cita != null && !cita.getEstado().equals("Cancelada")) {
                    cita.setEstado("Cancelada");
                    baseDatos.guardarCita(cita); // Actualizar estado en la DB
                    JOptionPane.showMessageDialog(this, "Cita " + citaId + " Cancelada.");
                    actualizarTablaCitas(); // Refrescar tabla
                } else if (cita != null && cita.getEstado().equals("Cancelada")) {
                    JOptionPane.showMessageDialog(this, "Esta cita ya está Cancelada.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una cita para Cancelar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // --- Reporte Pacientes Atendidos ---
        btnReportePacientes.addActionListener(e -> {
            txtReportes.setText(reporte.generarReportePacientesAtendidos());
        });

        // --- Reporte Citas del Día ---
        btnReporteCitasDia.addActionListener(e -> {
            txtReportes.setText(reporte.generarReporteCitasDelDia());
        });
    }
    
    // ====================================================================
    // 6. MÉTODO MAIN
    // ====================================================================
    public static void main(String[] args) {
        // Asegura que la GUI se ejecute en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            new InterfazGrafica().setVisible(true);
        });
    }
}