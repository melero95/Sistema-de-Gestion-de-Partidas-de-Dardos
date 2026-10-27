package modelos;

import java.util.ArrayList;

public class EstadoPartidaPuntos {

    //Configuración ---------------------------------------------------------------

    private int puntuacionInicial;
    private int maxRondas;
    private int numeroDardosTurno = 3;
    private boolean cierreDoble;
    private boolean ordenAleatorio;

    //Estado general --------------------------------------------------------------

    private ArrayList<Integer> puntuacionesJugadores;
    private ArrayList<Integer> coloresJugadores;
    private ArrayList<Boolean> jugadoresFinalizados;
    private ArrayList<Integer> ordenFinalizacion;

    //Estado del turno actual -----------------------------------------------------

    private int dardoActual;
    private int puntosTurnoActual;

    private ArrayList<Integer> puntosDardos;
    private ArrayList<String> textosDardos;

    //Historial mostrado y pila de deshacer --------------------------------------

    private ArrayList<RegistroTiradaGuardada> historialTiradas;
    private ArrayList<EstadoDeshacerGuardado> historialEstados;
    private ArrayList<EstadisticasJugadorEnCurso> estadisticasJugadores;

    //Constructor ----------------------------------------------------------------

    public EstadoPartidaPuntos() {

        puntuacionesJugadores = new ArrayList<>();
        coloresJugadores = new ArrayList<>();
        jugadoresFinalizados = new ArrayList<>();
        ordenFinalizacion = new ArrayList<>();

        puntosDardos = new ArrayList<>();
        textosDardos = new ArrayList<>();

        historialTiradas = new ArrayList<>();
        historialEstados = new ArrayList<>();
        estadisticasJugadores = new ArrayList<>();
    }

    //Getters y setters -----------------------------------------------------------

    public int getPuntuacionInicial() {
        return puntuacionInicial;
    }

    public void setPuntuacionInicial(int puntuacionInicial) {
        this.puntuacionInicial = puntuacionInicial;
    }

    public int getMaxRondas() {
        return maxRondas;
    }

    public void setMaxRondas(int maxRondas) {
        this.maxRondas = maxRondas;
    }

    public int getNumeroDardosTurno() { return numeroDardosTurno; }
    public void setNumeroDardosTurno(int numeroDardosTurno) { this.numeroDardosTurno = numeroDardosTurno; }
    public boolean isCierreDoble() { return cierreDoble; }
    public void setCierreDoble(boolean cierreDoble) { this.cierreDoble = cierreDoble; }
    public boolean isOrdenAleatorio() { return ordenAleatorio; }
    public void setOrdenAleatorio(boolean ordenAleatorio) { this.ordenAleatorio = ordenAleatorio; }

    public ArrayList<Integer> getPuntuacionesJugadores() {
        return puntuacionesJugadores;
    }

    public void setPuntuacionesJugadores(
            ArrayList<Integer> puntuacionesJugadores
    ) {
        this.puntuacionesJugadores = puntuacionesJugadores;
    }

    public ArrayList<Integer> getColoresJugadores() {
        return coloresJugadores;
    }

    public void setColoresJugadores(
            ArrayList<Integer> coloresJugadores
    ) {
        this.coloresJugadores = coloresJugadores;
    }

    public ArrayList<Boolean> getJugadoresFinalizados() {
        return jugadoresFinalizados;
    }

    public void setJugadoresFinalizados(
            ArrayList<Boolean> jugadoresFinalizados
    ) {
        this.jugadoresFinalizados = jugadoresFinalizados;
    }

    public ArrayList<Integer> getOrdenFinalizacion() {
        return ordenFinalizacion;
    }

    public void setOrdenFinalizacion(
            ArrayList<Integer> ordenFinalizacion
    ) {
        this.ordenFinalizacion = ordenFinalizacion;
    }

    public int getDardoActual() {
        return dardoActual;
    }

    public void setDardoActual(int dardoActual) {
        this.dardoActual = dardoActual;
    }

    public int getPuntosTurnoActual() {
        return puntosTurnoActual;
    }

    public void setPuntosTurnoActual(int puntosTurnoActual) {
        this.puntosTurnoActual = puntosTurnoActual;
    }

    public ArrayList<Integer> getPuntosDardos() {
        return puntosDardos;
    }

    public void setPuntosDardos(
            ArrayList<Integer> puntosDardos
    ) {
        this.puntosDardos = puntosDardos;
    }

    public ArrayList<String> getTextosDardos() {
        return textosDardos;
    }

    public void setTextosDardos(
            ArrayList<String> textosDardos
    ) {
        this.textosDardos = textosDardos;
    }

    public ArrayList<RegistroTiradaGuardada> getHistorialTiradas() {
        return historialTiradas;
    }

    public void setHistorialTiradas(
            ArrayList<RegistroTiradaGuardada> historialTiradas
    ) {
        this.historialTiradas = historialTiradas;
    }

    public ArrayList<EstadoDeshacerGuardado> getHistorialEstados() {
        return historialEstados;
    }

    public void setHistorialEstados(
            ArrayList<EstadoDeshacerGuardado> historialEstados
    ) {
        this.historialEstados = historialEstados;
    }

    public ArrayList<EstadisticasJugadorEnCurso> getEstadisticasJugadores() {
        return estadisticasJugadores;
    }

    public void setEstadisticasJugadores(ArrayList<EstadisticasJugadorEnCurso> datos) {
        estadisticasJugadores = datos == null
                ? new ArrayList<>() : EstadisticasJugadorEnCurso.copiarLista(datos);
    }

    //Registro completo de una tirada --------------------------------------------

    public static class RegistroTiradaGuardada {

        private int indiceJugador;
        private String nombreJugador;
        private int ronda;

        private ArrayList<Integer> dardos;
        private ArrayList<String> textosDardos;

        private int puntosTotales;
        private int puntuacionAntes;
        private int puntuacionDespues;

        private boolean turnoPasado;

        public RegistroTiradaGuardada() {
            dardos = new ArrayList<>();
            textosDardos = new ArrayList<>();
        }

        public int getIndiceJugador() {
            return indiceJugador;
        }

        public void setIndiceJugador(int indiceJugador) {
            this.indiceJugador = indiceJugador;
        }

        public String getNombreJugador() {
            return nombreJugador;
        }

        public void setNombreJugador(String nombreJugador) {
            this.nombreJugador = nombreJugador;
        }

        public int getRonda() {
            return ronda;
        }

        public void setRonda(int ronda) {
            this.ronda = ronda;
        }

        public ArrayList<Integer> getDardos() {
            return dardos;
        }

        public void setDardos(ArrayList<Integer> dardos) {
            this.dardos = dardos;
        }

        public ArrayList<String> getTextosDardos() {
            return textosDardos;
        }

        public void setTextosDardos(
                ArrayList<String> textosDardos
        ) {
            this.textosDardos = textosDardos;
        }

        public int getPuntosTotales() {
            return puntosTotales;
        }

        public void setPuntosTotales(int puntosTotales) {
            this.puntosTotales = puntosTotales;
        }

        public int getPuntuacionAntes() {
            return puntuacionAntes;
        }

        public void setPuntuacionAntes(int puntuacionAntes) {
            this.puntuacionAntes = puntuacionAntes;
        }

        public int getPuntuacionDespues() {
            return puntuacionDespues;
        }

        public void setPuntuacionDespues(int puntuacionDespues) {
            this.puntuacionDespues = puntuacionDespues;
        }

        public boolean isTurnoPasado() {
            return turnoPasado;
        }

        public void setTurnoPasado(boolean turnoPasado) {
            this.turnoPasado = turnoPasado;
        }
    }

    //Instantánea usada por el botón deshacer ------------------------------------

    public static class EstadoDeshacerGuardado {

        private ArrayList<Integer> puntuacionesJugadores;
        private ArrayList<Boolean> jugadoresFinalizados;
        private ArrayList<Integer> ordenFinalizacion;

        private int jugadorActual;
        private int rondaActual;
        private int dardoActual;
        private int puntosTurnoActual;

        private ArrayList<Integer> puntosDardos;
        private ArrayList<String> textosDardos;

        private int tamanoHistorialTiradas;
        private ArrayList<EstadisticasJugadorEnCurso> estadisticasJugadores;

        public EstadoDeshacerGuardado() {

            puntuacionesJugadores = new ArrayList<>();
            jugadoresFinalizados = new ArrayList<>();
            ordenFinalizacion = new ArrayList<>();

            puntosDardos = new ArrayList<>();
            textosDardos = new ArrayList<>();
            estadisticasJugadores = new ArrayList<>();
        }

        public ArrayList<Integer> getPuntuacionesJugadores() {
            return puntuacionesJugadores;
        }

        public void setPuntuacionesJugadores(
                ArrayList<Integer> puntuacionesJugadores
        ) {
            this.puntuacionesJugadores = puntuacionesJugadores;
        }

        public ArrayList<Boolean> getJugadoresFinalizados() {
            return jugadoresFinalizados;
        }

        public void setJugadoresFinalizados(
                ArrayList<Boolean> jugadoresFinalizados
        ) {
            this.jugadoresFinalizados = jugadoresFinalizados;
        }

        public ArrayList<Integer> getOrdenFinalizacion() {
            return ordenFinalizacion;
        }

        public void setOrdenFinalizacion(
                ArrayList<Integer> ordenFinalizacion
        ) {
            this.ordenFinalizacion = ordenFinalizacion;
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

        public int getDardoActual() {
            return dardoActual;
        }

        public void setDardoActual(int dardoActual) {
            this.dardoActual = dardoActual;
        }

        public int getPuntosTurnoActual() {
            return puntosTurnoActual;
        }

        public void setPuntosTurnoActual(int puntosTurnoActual) {
            this.puntosTurnoActual = puntosTurnoActual;
        }

        public ArrayList<Integer> getPuntosDardos() {
            return puntosDardos;
        }

        public void setPuntosDardos(
                ArrayList<Integer> puntosDardos
        ) {
            this.puntosDardos = puntosDardos;
        }

        public ArrayList<String> getTextosDardos() {
            return textosDardos;
        }

        public void setTextosDardos(
                ArrayList<String> textosDardos
        ) {
            this.textosDardos = textosDardos;
        }

        public int getTamanoHistorialTiradas() {
            return tamanoHistorialTiradas;
        }

        public void setTamanoHistorialTiradas(
                int tamanoHistorialTiradas
        ) {
            this.tamanoHistorialTiradas =
                    tamanoHistorialTiradas;
        }

        public ArrayList<EstadisticasJugadorEnCurso> getEstadisticasJugadores() {
            return estadisticasJugadores;
        }

        public void setEstadisticasJugadores(ArrayList<EstadisticasJugadorEnCurso> datos) {
            estadisticasJugadores = datos == null
                    ? new ArrayList<>() : EstadisticasJugadorEnCurso.copiarLista(datos);
        }
    }
}
