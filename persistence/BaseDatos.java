package com.citasmedicas.persistence;

import com.citasmedicas.model.*; // Importa todas las clases del modelo
import java.sql.*; // Importa las clases JDBC
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseDatos {

    private Connection connection; // Objeto de conexión a la base de datos
    private final String URL = "jdbc:sqlite:citas_medicas.db"; // Ruta al archivo de la base de datos SQLite

    // Generadores de IDs únicos para cada tipo de entidad
    private AtomicInteger pacienteIdCounter = new AtomicInteger(0);
    private AtomicInteger medicoIdCounter = new AtomicInteger(0);
    private AtomicInteger citaIdCounter = new AtomicInteger(0);
    private AtomicInteger historialIdCounter = new AtomicInteger(0);

    public BaseDatos() {
        conectar(); // Conectar al inicio
        inicializarEsquema(); // Crear tablas si no existen
        cargarContadoresId(); // Cargar los últimos IDs de la DB
    }

    // --- Métodos de Conexión ---
    public boolean conectar() {
        try {
            // Cargar el driver JDBC de SQLite (ya no es estrictamente necesario con JDBC 4.0+, pero es buena práctica)
            // Class.forName("org.sqlite.JDBC"); 
            
            connection = DriverManager.getConnection(URL);
            System.out.println("Conexión a SQLite establecida en: " + URL);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos SQLite: " + e.getMessage());
            connection = null; // Asegurarse de que la conexión es nula si falla
            return false;
        }
    }

    public void desconectar() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Desconexión de SQLite realizada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al desconectar de la base de datos SQLite: " + e.getMessage());
        }
    }

    // --- Métodos de Inicialización y Gestión de ID ---

    // Crea las tablas si no existen
    private void inicializarEsquema() {
        if (connection == null) {
            System.err.println("No hay conexión a la base de datos para inicializar el esquema.");
            return;
        }

        // Definición de tablas
        String createPacientesTable = "CREATE TABLE IF NOT EXISTS Pacientes (" +
                "id TEXT PRIMARY KEY," +
                "nombre TEXT NOT NULL," +
                "dni TEXT NOT NULL UNIQUE," +
                "edad INTEGER," +
                "antecedentes TEXT" +
                ");";

        String createMedicosTable = "CREATE TABLE IF NOT EXISTS Medicos (" +
                "id TEXT PRIMARY KEY," +
                "nombre TEXT NOT NULL," +
                "especialidad TEXT NOT NULL" +
                ");";

        String createCitasTable = "CREATE TABLE IF NOT EXISTS Citas (" +
                "id TEXT PRIMARY KEY," +
                "paciente_id TEXT NOT NULL," +
                "medico_id TEXT NOT NULL," +
                "fecha TEXT NOT NULL," + // Guardamos como TEXT en formato YYYY-MM-DD
                "hora TEXT NOT NULL," + // Guardamos como TEXT en formato HH:MM
                "motivo TEXT," +
                "estado TEXT NOT NULL," + // Programada, Realizada, Cancelada
                "FOREIGN KEY (paciente_id) REFERENCES Pacientes(id)," +
                "FOREIGN KEY (medico_id) REFERENCES Medicos(id)" +
                ");";

        String createHistorialEntradasTable = "CREATE TABLE IF NOT EXISTS HistorialEntradas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," + // ID autoincremental para las entradas del historial
                "paciente_id TEXT NOT NULL," +
                "fecha TEXT NOT NULL," +
                "descripcion TEXT," +
                "FOREIGN KEY (paciente_id) REFERENCES Pacientes(id)" +
                ");";

        // Tabla para almacenar los contadores de ID
        String createIdCountersTable = "CREATE TABLE IF NOT EXISTS IdCounters (" +
                "name TEXT PRIMARY KEY," +
                "value INTEGER NOT NULL" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPacientesTable);
            stmt.execute(createMedicosTable);
            stmt.execute(createCitasTable);
            stmt.execute(createHistorialEntradasTable);
            stmt.execute(createIdCountersTable);
            System.out.println("Esquema de la base de datos inicializado/verificado.");
        } catch (SQLException e) {
            System.err.println("Error al inicializar el esquema de la base de datos: " + e.getMessage());
        }
    }
    
    // Carga los contadores de ID al iniciar la aplicación
    private void cargarContadoresId() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, value FROM IdCounters")) {
            while (rs.next()) {
                String name = rs.getString("name");
                int value = rs.getInt("value");
                switch (name) {
                    case "paciente": pacienteIdCounter = new AtomicInteger(value); break;
                    case "medico": medicoIdCounter = new AtomicInteger(value); break;
                    case "cita": citaIdCounter = new AtomicInteger(value); break;
                    case "historial": historialIdCounter = new AtomicInteger(value); break;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar contadores de ID: " + e.getMessage());
        }
    }

    // Guarda los contadores de ID al cerrar la aplicación o cuando sea necesario
    public void guardarContadoresId() {
        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO IdCounters (name, value) VALUES (?, ?)")) {
            ps.setString(1, "paciente"); ps.setInt(2, pacienteIdCounter.get()); ps.addBatch();
            ps.setString(1, "medico"); ps.setInt(2, medicoIdCounter.get()); ps.addBatch();
            ps.setString(1, "cita"); ps.setInt(2, citaIdCounter.get()); ps.addBatch();
            ps.setString(1, "historial"); ps.setInt(2, historialIdCounter.get()); ps.addBatch();
            ps.executeBatch();
            System.out.println("Contadores de ID guardados.");
        } catch (SQLException e) {
            System.err.println("Error al guardar contadores de ID: " + e.getMessage());
        }
    }

    // Genera un nuevo ID para un tipo de entidad
    private String generarNuevoId(String prefix, AtomicInteger counter) {
        int newId = counter.incrementAndGet();
        return prefix + newId;
    }


    // --- Métodos CRUD genéricos (Ajustados para clases específicas) ---

    // Guardar/Actualizar Paciente
    public void guardarPaciente(Paciente paciente) {
        if (connection == null) { System.err.println("No hay conexión."); return; }
        
        // Si el paciente no tiene ID, es nuevo, generamos uno
        if (paciente.getId() == null || paciente.getId().isEmpty()) {
            paciente.setId(generarNuevoId("P", pacienteIdCounter));
        }

        String sql = "REPLACE INTO Pacientes (id, nombre, dni, edad, antecedentes) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, paciente.getId());
            pstmt.setString(2, paciente.getNombre());
            pstmt.setString(3, paciente.getDni());
            pstmt.setInt(4, paciente.getEdad());
            pstmt.setString(5, paciente.getHistorial().getAntecedentesMedicos()); // Guarda antecedentes
            pstmt.executeUpdate();
            System.out.println("Paciente guardado/actualizado: " + paciente.getNombre());
            
            // También guardamos/actualizamos sus entradas de historial si las tiene
            for (Historial.HistorialEntry entry : paciente.getHistorial().getEntradas()) {
                guardarHistorialEntry(paciente.getId(), entry);
            }

        } catch (SQLException e) {
            System.err.println("Error al guardar paciente: " + e.getMessage());
        }
    }

    // Obtener todos los pacientes
    public List<Paciente> obtenerTodosPacientes() {
        List<Paciente> pacientes = new ArrayList<>();
        if (connection == null) { System.err.println("No hay conexión."); return pacientes; }
        String sql = "SELECT id, nombre, dni, edad, antecedentes FROM Pacientes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                String nombre = rs.getString("nombre");
                String dni = rs.getString("dni");
                int edad = rs.getInt("edad");
                String antecedentes = rs.getString("antecedentes");

                Historial historial = new Historial(id); // Creamos un historial vacío
                historial.setAntecedentesMedicos(antecedentes);
                Paciente paciente = new Paciente(nombre, dni, edad, historial);
                paciente.setId(id);
                pacientes.add(paciente);
                
                // Cargamos las entradas del historial para este paciente
                cargarHistorialEntradas(paciente);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pacientes: " + e.getMessage());
        }
        return pacientes;
    }
    
    // Obtener un paciente por ID
    public Paciente obtenerPacientePorId(String id) {
        if (connection == null) { System.err.println("No hay conexión."); return null; }
        String sql = "SELECT id, nombre, dni, edad, antecedentes FROM Pacientes WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String nombre = rs.getString("nombre");
                String dni = rs.getString("dni");
                int edad = rs.getInt("edad");
                String antecedentes = rs.getString("antecedentes");
                
                Historial historial = new Historial(id);
                historial.setAntecedentesMedicos(antecedentes);
                Paciente paciente = new Paciente(nombre, dni, edad, historial);
                paciente.setId(id);
                cargarHistorialEntradas(paciente); // Cargar entradas específicas
                return paciente;
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener paciente por ID: " + e.getMessage());
        }
        return null;
    }


    // Guardar/Actualizar Medico
    public void guardarMedico(Medico medico) {
        if (connection == null) { System.err.println("No hay conexión."); return; }
        if (medico.getId() == null || medico.getId().isEmpty()) {
            medico.setId(generarNuevoId("M", medicoIdCounter));
        }
        String sql = "REPLACE INTO Medicos (id, nombre, especialidad) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, medico.getId());
            pstmt.setString(2, medico.getNombre());
            pstmt.setString(3, medico.getEspecialidad());
            pstmt.executeUpdate();
            System.out.println("Médico guardado/actualizado: " + medico.getNombre());
        } catch (SQLException e) {
            System.err.println("Error al guardar médico: " + e.getMessage());
        }
    }

    // Obtener todos los médicos
    public List<Medico> obtenerTodosMedicos() {
        List<Medico> medicos = new ArrayList<>();
        if (connection == null) { System.err.println("No hay conexión."); return medicos; }
        String sql = "SELECT id, nombre, especialidad FROM Medicos";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                String nombre = rs.getString("nombre");
                String especialidad = rs.getString("especialidad");
                Medico medico = new Medico(nombre, especialidad);
                medico.setId(id);
                medicos.add(medico);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener médicos: " + e.getMessage());
        }
        return medicos;
    }
    
    // Obtener un médico por ID
    public Medico obtenerMedicoPorId(String id) {
        if (connection == null) { System.err.println("No hay conexión."); return null; }
        String sql = "SELECT id, nombre, especialidad FROM Medicos WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String nombre = rs.getString("nombre");
                String especialidad = rs.getString("especialidad");
                Medico medico = new Medico(nombre, especialidad);
                medico.setId(id);
                return medico;
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener médico por ID: " + e.getMessage());
        }
        return null;
    }


    // Guardar/Actualizar Cita
    public void guardarCita(Cita cita) {
        if (connection == null) { System.err.println("No hay conexión."); return; }
        if (cita.getId() == null || cita.getId().isEmpty()) {
            cita.setId(generarNuevoId("C", citaIdCounter));
        }
        String sql = "REPLACE INTO Citas (id, paciente_id, medico_id, fecha, hora, motivo, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cita.getId());
            pstmt.setString(2, cita.getPaciente().getId());
            pstmt.setString(3, cita.getMedico().getId());
            pstmt.setString(4, cita.getFecha());
            pstmt.setString(5, cita.getHora());
            pstmt.setString(6, cita.getMotivo());
            pstmt.setString(7, cita.getEstado());
            pstmt.executeUpdate();
            System.out.println("Cita guardada/actualizada: " + cita.getId());
        } catch (SQLException e) {
            System.err.println("Error al guardar cita: " + e.getMessage());
        }
    }

    // Obtener todas las citas
    public List<Cita> obtenerTodasCitas() {
        List<Cita> citas = new ArrayList<>();
        if (connection == null) { System.err.println("No hay conexión."); return citas; }
        String sql = "SELECT id, paciente_id, medico_id, fecha, hora, motivo, estado FROM Citas";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                String pacienteId = rs.getString("paciente_id");
                String medicoId = rs.getString("medico_id");
                String fecha = rs.getString("fecha");
                String hora = rs.getString("hora");
                String motivo = rs.getString("motivo");
                String estado = rs.getString("estado");

                // Necesitamos cargar los objetos Paciente y Medico completos
                Paciente paciente = obtenerPacientePorId(pacienteId);
                Medico medico = obtenerMedicoPorId(medicoId);

                if (paciente != null && medico != null) {
                    Cita cita = new Cita(paciente, medico, fecha, hora, motivo);
                    cita.setId(id);
                    cita.setEstado(estado);
                    citas.add(cita);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener citas: " + e.getMessage());
        }
        return citas;
    }
    
    // Guardar entrada de historial (usada internamente por guardarPaciente)
    private void guardarHistorialEntry(String pacienteId, Historial.HistorialEntry entry) {
        if (connection == null) { System.err.println("No hay conexión."); return; }
        // Si la entrada no tiene un ID de DB (SQLite AUTOINCREMENT lo asigna), insertamos
        // Si ya tiene un ID, es una entrada existente y no la modificamos (Historial es append-only)
        if (entry.getId() == 0) { // Asumimos 0 si es nueva
            String sql = "INSERT INTO HistorialEntradas (paciente_id, fecha, descripcion) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, pacienteId);
                pstmt.setString(2, entry.getFecha());
                pstmt.setString(3, entry.getDescripcion());
                pstmt.executeUpdate();
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    entry.setId(generatedKeys.getInt(1)); // Asignar el ID generado por la DB a la entrada
                }
                System.out.println("Entrada de historial guardada para paciente " + pacienteId);
            } catch (SQLException e) {
                System.err.println("Error al guardar entrada de historial: " + e.getMessage());
            }
        }
    }
    
    // Cargar entradas de historial para un paciente
    private void cargarHistorialEntradas(Paciente paciente) {
        if (connection == null) { System.err.println("No hay conexión."); return; }
        String sql = "SELECT id, fecha, descripcion FROM HistorialEntradas WHERE paciente_id = ? ORDER BY fecha ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, paciente.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int entryId = rs.getInt("id");
                String fecha = rs.getString("fecha");
                String descripcion = rs.getString("descripcion");
                Historial.HistorialEntry entry = new Historial.HistorialEntry(fecha, descripcion);
                entry.setId(entryId); // Establecer el ID de la DB
                paciente.getHistorial().addEntradaFromDB(entry); // Método especial para añadir desde DB
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar entradas de historial para paciente " + paciente.getId() + ": " + e.getMessage());
        }
    }
    
    // Nuevo: Para el reporte de citas por día
     public List<Cita> obtenerCitasPorFecha(String fecha) {
         List<Cita> citas = new ArrayList<>();
         if (connection == null) {
             System.err.println("No hay conexión.");
             return citas;
         }
         String sql = "SELECT id, paciente_id, medico_id, fecha, hora, motivo, estado FROM Citas WHERE fecha = ?";
         try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
             pstmt.setString(1, fecha);
             ResultSet rs = pstmt.executeQuery();
             while (rs.next()) {
                 String id = rs.getString("id");
                 String pacienteId = rs.getString("paciente_id");
                 String medicoId = rs.getString("medico_id");
                 String hora = rs.getString("hora");
                 String motivo = rs.getString("motivo");
                 String estado = rs.getString("estado");

                 Paciente paciente = obtenerPacientePorId(pacienteId);
                 Medico medico = obtenerMedicoPorId(medicoId);

                 if (paciente != null && medico != null) {
                     Cita cita = new Cita(paciente, medico, fecha, hora, motivo);
                     cita.setId(id);
                     cita.setEstado(estado);
                     citas.add(cita);
                 }
             }
         } catch (SQLException e) {
             System.err.println("Error al obtener citas por fecha: " + e.getMessage());
         }
         return citas;
     }


    // --- Método main para pruebas iniciales de DB ---
    public static void main(String[] args) {
        BaseDatos db = new BaseDatos();
        db.conectar();
        // Aquí podrías agregar lógica para añadir datos de prueba y ver si se guardan
        // db.guardarPaciente(new Paciente("Test", "123", 30, new Historial("PTest")));
        db.desconectar();
    }
}