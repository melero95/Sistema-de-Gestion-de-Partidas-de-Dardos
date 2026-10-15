package modelos;

import java.util.UUID;

/**
 * Datos generales de una partida FINALIZADA, para la tabla PARTIDAS.
 * No representa el estado de una partida en curso (PartidaEnCurso).
 * Los participantes y sus puntuaciones se guardarán en RESULTADOS_PARTIDA.
 */
public class Partida {

    // El ID lo asigna SQLite. Cero significa que aún no está insertada.
    private long id;

    // Se genera una sola vez y deberá conservarse durante los reintentos de guardado.
    private String identificadorUnico;

    // Fecha de finalización en milisegundos Unix (System.currentTimeMillis()).
    private long fecha;
    private String modo;
    private int rondasConfiguradas;
    private int rondasJugadas;
    private int numeroJugadores;

    // Puede ser null si el modo o la regla de finalización no declara ganador.
    private Long idGanador;
    private String motivoFinalizacion;

    // Configuración completa de la partida, en formato JSON versionado.
    private String configuracionJson;

    public Partida() {
        identificadorUnico = UUID.randomUUID().toString();
        fecha = System.currentTimeMillis();
    }

    public Partida(String modo, int rondasConfiguradas, int rondasJugadas,
                   int numeroJugadores, Long idGanador,
                   String motivoFinalizacion, String configuracionJson) {
        this();
        setModo(modo);
        setRondasConfiguradas(rondasConfiguradas);
        setRondasJugadas(rondasJugadas);
        setNumeroJugadores(numeroJugadores);
        setIdGanador(idGanador);
        setMotivoFinalizacion(motivoFinalizacion);
        setConfiguracionJson(configuracionJson);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("El ID de partida no puede ser negativo");
        }
        this.id = id;
    }

    public String getIdentificadorUnico() {
        return identificadorUnico;
    }

    /** Solo cambiar al reconstruir una partida leída de SQLite. */
    public void setIdentificadorUnico(String identificadorUnico) {
        this.identificadorUnico = textoObligatorio(identificadorUnico, "identificador único");
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        if (fecha <= 0) {
            throw new IllegalArgumentException("La fecha debe ser positiva");
        }
        this.fecha = fecha;
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = textoObligatorio(modo, "modo de juego");
    }

    public int getRondasConfiguradas() {
        return rondasConfiguradas;
    }

    public void setRondasConfiguradas(int rondasConfiguradas) {
        if (rondasConfiguradas < 0) {
            throw new IllegalArgumentException("Las rondas configuradas no pueden ser negativas");
        }
        this.rondasConfiguradas = rondasConfiguradas;
    }

    public int getRondasJugadas() {
        return rondasJugadas;
    }

    public void setRondasJugadas(int rondasJugadas) {
        if (rondasJugadas < 0) {
            throw new IllegalArgumentException("Las rondas jugadas no pueden ser negativas");
        }
        this.rondasJugadas = rondasJugadas;
    }

    public int getNumeroJugadores() {
        return numeroJugadores;
    }

    public void setNumeroJugadores(int numeroJugadores) {
        if (numeroJugadores < 1 || numeroJugadores > 6) {
            throw new IllegalArgumentException("La partida debe tener entre 1 y 6 jugadores");
        }
        this.numeroJugadores = numeroJugadores;
    }

    public Long getIdGanador() {
        return idGanador;
    }

    public void setIdGanador(Long idGanador) {
        if (idGanador != null && idGanador <= 0) {
            throw new IllegalArgumentException("El ID del ganador debe ser positivo");
        }
        this.idGanador = idGanador;
    }

    public String getMotivoFinalizacion() {
        return motivoFinalizacion;
    }

    public void setMotivoFinalizacion(String motivoFinalizacion) {
        this.motivoFinalizacion = textoObligatorio(motivoFinalizacion, "motivo de finalización");
    }

    public String getConfiguracionJson() {
        return configuracionJson;
    }

    public void setConfiguracionJson(String configuracionJson) {
        // La estructura exacta del JSON se definirá en la fase 04.
        this.configuracionJson = textoObligatorio(configuracionJson, "configuración JSON");
    }

    private static String textoObligatorio(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Falta el campo obligatorio: " + campo);
        }
        return valor.trim();
    }
}
