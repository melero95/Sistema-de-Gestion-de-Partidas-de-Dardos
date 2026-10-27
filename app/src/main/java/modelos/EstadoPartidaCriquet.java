package modelos;

import java.util.ArrayList;

public class EstadoPartidaCriquet {

    //Configuración ---------------------------------------------------------------

    private int maxRondas;

    //Estado general --------------------------------------------------------------

    private ArrayList<Integer> coloresJugadores;
    private ArrayList<Integer> puntuacionesJugadores;

    /*
     * Cada posición exterior representa un jugador.
     * Cada lista interior contiene las marcas de:
     * 15, 16, 17, 18, 19, 20 y diana.
     */
    private ArrayList<ArrayList<Integer>> marcasJugadores;

    //Estado del turno actual -----------------------------------------------------

    private int numeroDardo;
    private int multiplicadorSeleccionado;
    private ArrayList<String> tiradasTurno;

    //Pila completa utilizada por Deshacer ---------------------------------------

    private ArrayList<EstadoDeshacerCriquet> historialEstados;
    private ArrayList<EstadisticasJugadorEnCurso> estadisticasJugadores;

    //Constructor ----------------------------------------------------------------

    public EstadoPartidaCriquet() {

        coloresJugadores = new ArrayList<>();
        puntuacionesJugadores = new ArrayList<>();
        marcasJugadores = new ArrayList<>();

        tiradasTurno = new ArrayList<>();
        historialEstados = new ArrayList<>();
        estadisticasJugadores = new ArrayList<>();
    }

    //Getters y setters -----------------------------------------------------------

    public int getMaxRondas() {
        return maxRondas;
    }

    public void setMaxRondas(int maxRondas) {
        this.maxRondas = maxRondas;
    }

    public ArrayList<Integer> getColoresJugadores() {
        return coloresJugadores;
    }

    public void setColoresJugadores(
            ArrayList<Integer> coloresJugadores
    ) {
        this.coloresJugadores = coloresJugadores;
    }

    public ArrayList<Integer> getPuntuacionesJugadores() {
        return puntuacionesJugadores;
    }

    public void setPuntuacionesJugadores(
            ArrayList<Integer> puntuacionesJugadores
    ) {
        this.puntuacionesJugadores = puntuacionesJugadores;
    }

    public ArrayList<ArrayList<Integer>> getMarcasJugadores() {
        return marcasJugadores;
    }

    public void setMarcasJugadores(
            ArrayList<ArrayList<Integer>> marcasJugadores
    ) {
        this.marcasJugadores = marcasJugadores;
    }

    public int getNumeroDardo() {
        return numeroDardo;
    }

    public void setNumeroDardo(int numeroDardo) {
        this.numeroDardo = numeroDardo;
    }

    public int getMultiplicadorSeleccionado() {
        return multiplicadorSeleccionado;
    }

    public void setMultiplicadorSeleccionado(
            int multiplicadorSeleccionado
    ) {
        this.multiplicadorSeleccionado =
                multiplicadorSeleccionado;
    }

    public ArrayList<String> getTiradasTurno() {
        return tiradasTurno;
    }

    public void setTiradasTurno(
            ArrayList<String> tiradasTurno
    ) {
        this.tiradasTurno = tiradasTurno;
    }

    public ArrayList<EstadoDeshacerCriquet> getHistorialEstados() {
        return historialEstados;
    }

    public void setHistorialEstados(
            ArrayList<EstadoDeshacerCriquet> historialEstados
    ) {
        this.historialEstados = historialEstados;
    }

    public ArrayList<EstadisticasJugadorEnCurso> getEstadisticasJugadores() {
        return estadisticasJugadores;
    }

    public void setEstadisticasJugadores(ArrayList<EstadisticasJugadorEnCurso> valores) {
        this.estadisticasJugadores = valores == null
                ? new ArrayList<>() : EstadisticasJugadorEnCurso.copiarLista(valores);
    }

    //Instantánea utilizada por el botón Deshacer --------------------------------

    public static class EstadoDeshacerCriquet {
        private ArrayList<EstadisticasJugadorEnCurso> estadisticasJugadores = new ArrayList<>();

        private ArrayList<ArrayList<Integer>> marcasJugadores;
        private ArrayList<Integer> puntuacionesJugadores;

        private int jugadorActual;
        private int rondaActual;
        private int numeroDardo;

        private int multiplicadorSeleccionado;
        private ArrayList<String> tiradasTurno;

        public EstadoDeshacerCriquet() {

            marcasJugadores = new ArrayList<>();
            puntuacionesJugadores = new ArrayList<>();
            tiradasTurno = new ArrayList<>();
        }

        public ArrayList<ArrayList<Integer>> getMarcasJugadores() {
            return marcasJugadores;
        }

        public void setMarcasJugadores(
                ArrayList<ArrayList<Integer>> marcasJugadores
        ) {
            this.marcasJugadores = marcasJugadores;
        }

        public ArrayList<Integer> getPuntuacionesJugadores() {
            return puntuacionesJugadores;
        }

        public void setPuntuacionesJugadores(
                ArrayList<Integer> puntuacionesJugadores
        ) {
            this.puntuacionesJugadores =
                    puntuacionesJugadores;
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

        public int getNumeroDardo() {
            return numeroDardo;
        }

        public void setNumeroDardo(int numeroDardo) {
            this.numeroDardo = numeroDardo;
        }

        public int getMultiplicadorSeleccionado() {
            return multiplicadorSeleccionado;
        }

        public void setMultiplicadorSeleccionado(
                int multiplicadorSeleccionado
        ) {
            this.multiplicadorSeleccionado =
                    multiplicadorSeleccionado;
        }

        public ArrayList<String> getTiradasTurno() {
            return tiradasTurno;
        }

        public void setTiradasTurno(
                ArrayList<String> tiradasTurno
        ) {
            this.tiradasTurno = tiradasTurno;
        }

        public ArrayList<EstadisticasJugadorEnCurso> getEstadisticasJugadores() {
            return estadisticasJugadores;
        }

        public void setEstadisticasJugadores(ArrayList<EstadisticasJugadorEnCurso> valores) {
            this.estadisticasJugadores = valores == null
                    ? new ArrayList<>() : EstadisticasJugadorEnCurso.copiarLista(valores);
        }
    }
}
