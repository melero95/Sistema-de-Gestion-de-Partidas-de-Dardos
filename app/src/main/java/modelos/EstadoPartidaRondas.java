package modelos;

import java.util.ArrayList;

public class EstadoPartidaRondas {

    //Configuración ---------------------------------------------------------------

    private int ultimoNumeroAroundClock;
    private int maxRondas;
    private int numeroDardosTurno;
    private boolean ordenAleatorio;

    //Estado general --------------------------------------------------------------

    private ArrayList<Integer> coloresRecursosJugadores;
    private ArrayList<Integer> coloresJugadores;
    private ArrayList<Integer> puntuacionesJugadores;
    private ArrayList<Integer> objetivosJugadores;
    private ArrayList<Boolean> jugadoresFinalizados;
    private ArrayList<Integer> ordenFinalizacion;

    //Estado del turno actual -----------------------------------------------------

    private int dardoActual;
    private int puntosValidosTurno;
    private boolean haAcertadoObjetivoTurno;

    private ArrayList<String> textosDardos;

    //Pila completa utilizada por Deshacer ---------------------------------------

    private ArrayList<EstadoDeshacerRondas> historialEstados;
    private ArrayList<EstadisticasJugadorEnCurso> estadisticasJugadores;

    //Constructor ----------------------------------------------------------------

    public EstadoPartidaRondas() {

        coloresRecursosJugadores = new ArrayList<>();
        coloresJugadores = new ArrayList<>();
        puntuacionesJugadores = new ArrayList<>();
        objetivosJugadores = new ArrayList<>();
        jugadoresFinalizados = new ArrayList<>();
        ordenFinalizacion = new ArrayList<>();

        textosDardos = new ArrayList<>();
        historialEstados = new ArrayList<>();
        estadisticasJugadores = new ArrayList<>();
    }

    //Getters y setters -----------------------------------------------------------

    public int getUltimoNumeroAroundClock() {
        return ultimoNumeroAroundClock;
    }

    public void setUltimoNumeroAroundClock(
            int ultimoNumeroAroundClock
    ) {
        this.ultimoNumeroAroundClock =
                ultimoNumeroAroundClock;
    }

    public int getMaxRondas() {
        return maxRondas;
    }

    public void setMaxRondas(int maxRondas) {
        this.maxRondas = maxRondas;
    }

    public int getNumeroDardosTurno() {
        return numeroDardosTurno;
    }

    public void setNumeroDardosTurno(int numeroDardosTurno) {
        this.numeroDardosTurno = numeroDardosTurno;
    }

    public boolean isOrdenAleatorio() {
        return ordenAleatorio;
    }

    public void setOrdenAleatorio(boolean ordenAleatorio) {
        this.ordenAleatorio = ordenAleatorio;
    }


    public ArrayList<Integer> getColoresRecursosJugadores() {
        return coloresRecursosJugadores;
    }

    public void setColoresRecursosJugadores(
            ArrayList<Integer> coloresRecursosJugadores
    ) {
        this.coloresRecursosJugadores =
                coloresRecursosJugadores;
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
        this.puntuacionesJugadores =
                puntuacionesJugadores;
    }


    public ArrayList<Integer> getObjetivosJugadores() {
        return objetivosJugadores;
    }

    public void setObjetivosJugadores(
            ArrayList<Integer> objetivosJugadores
    ) {
        this.objetivosJugadores = objetivosJugadores;
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

    public int getPuntosValidosTurno() {
        return puntosValidosTurno;
    }

    public void setPuntosValidosTurno(
            int puntosValidosTurno
    ) {
        this.puntosValidosTurno =
                puntosValidosTurno;
    }

    public boolean isHaAcertadoObjetivoTurno() {
        return haAcertadoObjetivoTurno;
    }

    public void setHaAcertadoObjetivoTurno(
            boolean haAcertadoObjetivoTurno
    ) {
        this.haAcertadoObjetivoTurno =
                haAcertadoObjetivoTurno;
    }

    public ArrayList<String> getTextosDardos() {
        return textosDardos;
    }

    public void setTextosDardos(
            ArrayList<String> textosDardos
    ) {
        this.textosDardos = textosDardos;
    }

    public ArrayList<EstadoDeshacerRondas> getHistorialEstados() {
        return historialEstados;
    }

    public void setHistorialEstados(
            ArrayList<EstadoDeshacerRondas> historialEstados
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

    //Instantánea utilizada por Deshacer -----------------------------------------

    public static class EstadoDeshacerRondas {
        private ArrayList<EstadisticasJugadorEnCurso> estadisticasJugadores = new ArrayList<>();

        private ArrayList<Integer> puntuacionesJugadores;
        private ArrayList<Integer> objetivosJugadores;
        private ArrayList<Boolean> jugadoresFinalizados;
        private ArrayList<Integer> ordenFinalizacion;

        private int jugadorActual;
        private int rondaActual;
        private int dardoActual;

        private int puntosValidosTurno;
        private boolean haAcertadoObjetivoTurno;

        private ArrayList<String> textosDardos;

        public EstadoDeshacerRondas() {

            puntuacionesJugadores = new ArrayList<>();
            objetivosJugadores = new ArrayList<>();
            jugadoresFinalizados = new ArrayList<>();
            ordenFinalizacion = new ArrayList<>();
            textosDardos = new ArrayList<>();
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


        public ArrayList<Integer> getObjetivosJugadores() {
            return objetivosJugadores;
        }

        public void setObjetivosJugadores(
                ArrayList<Integer> objetivosJugadores
        ) {
            this.objetivosJugadores = objetivosJugadores;
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

        public int getPuntosValidosTurno() {
            return puntosValidosTurno;
        }

        public void setPuntosValidosTurno(
                int puntosValidosTurno
        ) {
            this.puntosValidosTurno =
                    puntosValidosTurno;
        }

        public boolean isHaAcertadoObjetivoTurno() {
            return haAcertadoObjetivoTurno;
        }

        public void setHaAcertadoObjetivoTurno(
                boolean haAcertadoObjetivoTurno
        ) {
            this.haAcertadoObjetivoTurno =
                    haAcertadoObjetivoTurno;
        }

        public ArrayList<String> getTextosDardos() {
            return textosDardos;
        }

        public void setTextosDardos(
                ArrayList<String> textosDardos
        ) {
            this.textosDardos = textosDardos;
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
