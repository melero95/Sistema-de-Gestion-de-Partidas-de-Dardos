package modelos;

import java.util.ArrayList;

public class PartidaEnCurso {

    //Tipos de partida ------------------------------------------------------------

    public static final String TIPO_PUNTOS = "PUNTOS";
    public static final String TIPO_CRIQUET = "CRIQUET";
    public static final String TIPO_RONDAS = "RONDAS";

    //Versión del formato de guardado ---------------------------------------------

    public static final int VERSION_ACTUAL = 1;

    //Datos generales -------------------------------------------------------------

    private int versionGuardado;

    // Metadatos estables de persistencia (fase 06).
    private String identificadorUnico;
    private String configuracionJson;

    private String tipoPartida;
    private String modoJuego;

    private ArrayList<String> nombresJugadores;
    private ArrayList<Integer> coloresJugadores;

    private int numeroJugadores;
    private int jugadorActual;
    private int rondaActual;

    private long fechaGuardado;

    //Estados específicos ---------------------------------------------------------

    private EstadoPartidaPuntos estadoPuntos;
    private EstadoPartidaCriquet estadoCriquet;
    private EstadoPartidaRondas estadoRondas;

    //Constructor -----------------------------------------------------------------

    public PartidaEnCurso() {

        versionGuardado = VERSION_ACTUAL;

        nombresJugadores = new ArrayList<>();
        coloresJugadores = new ArrayList<>();
    }

    //Getters y setters -----------------------------------------------------------

    public String getIdentificadorUnico() { return identificadorUnico; }
    public void setIdentificadorUnico(String valor) { identificadorUnico = valor; }
    public String getConfiguracionJson() { return configuracionJson; }
    public void setConfiguracionJson(String valor) { configuracionJson = valor; }

    public int getVersionGuardado() {
        return versionGuardado;
    }

    public void setVersionGuardado(int versionGuardado) {
        this.versionGuardado = versionGuardado;
    }

    public String getTipoPartida() {
        return tipoPartida;
    }

    public void setTipoPartida(String tipoPartida) {
        this.tipoPartida = tipoPartida;
    }

    public String getModoJuego() {
        return modoJuego;
    }

    public void setModoJuego(String modoJuego) {
        this.modoJuego = modoJuego;
    }

    public ArrayList<String> getNombresJugadores() {
        return nombresJugadores;
    }

    public void setNombresJugadores(ArrayList<String> nombresJugadores) {
        this.nombresJugadores = nombresJugadores;
    }

    public ArrayList<Integer> getColoresJugadores() {
        return coloresJugadores;
    }

    public void setColoresJugadores(ArrayList<Integer> coloresJugadores) {
        this.coloresJugadores = coloresJugadores;
    }

    public int getNumeroJugadores() {
        return numeroJugadores;
    }

    public void setNumeroJugadores(int numeroJugadores) {
        this.numeroJugadores = numeroJugadores;
    }

    public int getJugadorActual() {
        return jugadorActual;
    }

    public void setJugadorActual(int jugadorActual) {
        this.jugadorActual = jugadorActual;
    }

    public int getRondaActual() {
        return rondaActual;
    }

    public void setRondaActual(int rondaActual) {
        this.rondaActual = rondaActual;
    }

    public long getFechaGuardado() {
        return fechaGuardado;
    }

    public void setFechaGuardado(long fechaGuardado) {
        this.fechaGuardado = fechaGuardado;
    }

    public EstadoPartidaPuntos getEstadoPuntos() {
        return estadoPuntos;
    }

    public void setEstadoPuntos(EstadoPartidaPuntos estadoPuntos) {
        this.estadoPuntos = estadoPuntos;
    }

    public EstadoPartidaCriquet getEstadoCriquet() {
        return estadoCriquet;
    }

    public void setEstadoCriquet(EstadoPartidaCriquet estadoCriquet) {
        this.estadoCriquet = estadoCriquet;
    }

    public EstadoPartidaRondas getEstadoRondas() {
        return estadoRondas;
    }

    public void setEstadoRondas(EstadoPartidaRondas estadoRondas) {
        this.estadoRondas = estadoRondas;
    }
}