package modelos;

import java.util.ArrayList;
import java.util.List;

/**
 * Contadores de UN jugador durante UNA partida todavía no persistida en SQLite.
 *
 * No depende de Android ni accede a la base de datos. Los motores de juego
 * deciden cuándo una tirada es válida y cuándo debe registrarse cada evento.
 *
 * IMPORTANTE PARA DESHACER: usar copiar() o copiarLista(), nunca guardar la
 * referencia mutable a este objeto en una instantánea del estado anterior.
 */
public final class EstadisticasJugadorEnCurso {

    private String modo;

    // Comunes a todos los modos.
    private int dardosLanzados;
    private int turnosJugados;
    private int simples;
    private int dobles;
    private int triples;
    private int fallos;

    // 301 / 501.
    private int busts;
    private int puntosValidos;
    private int mejorTurno;
    private int turnos180;
    private int dardosCierre;

    // Cricket / Cut Throat Cricket.
    private int marcasValidas;
    private int objetivosCerrados;
    private int puntosGenerados;
    private int puntosRecibidos;

    // Double Down.
    private int rondasPenalizadas;
    private int mejorRondaDd;
    private int puntosConseguidosDd;

    // Around the Clock.
    private int objetivosSuperados;
    private int aciertosObjetivo;
    private boolean partidaCompletada;

    // Shanghai.
    private int puntosValidosShanghai;
    private int mejorRondaShanghai;
    private int rondasConPuntos;

    /** Constructor para deserialización con Gson en una fase posterior. */
    private EstadisticasJugadorEnCurso() {
    }

    public EstadisticasJugadorEnCurso(String modo) {
        validarModo(modo);
        this.modo = modo;
    }

    /** Crea una COPIA independiente: todos los campos actuales son primitivos o String. */
    public EstadisticasJugadorEnCurso copiar() {
        EstadisticasJugadorEnCurso copia = new EstadisticasJugadorEnCurso(modo);
        copia.dardosLanzados = dardosLanzados;
        copia.turnosJugados = turnosJugados;
        copia.simples = simples;
        copia.dobles = dobles;
        copia.triples = triples;
        copia.fallos = fallos;
        copia.busts = busts;
        copia.puntosValidos = puntosValidos;
        copia.mejorTurno = mejorTurno;
        copia.turnos180 = turnos180;
        copia.dardosCierre = dardosCierre;
        copia.marcasValidas = marcasValidas;
        copia.objetivosCerrados = objetivosCerrados;
        copia.puntosGenerados = puntosGenerados;
        copia.puntosRecibidos = puntosRecibidos;
        copia.rondasPenalizadas = rondasPenalizadas;
        copia.mejorRondaDd = mejorRondaDd;
        copia.puntosConseguidosDd = puntosConseguidosDd;
        copia.objetivosSuperados = objetivosSuperados;
        copia.aciertosObjetivo = aciertosObjetivo;
        copia.partidaCompletada = partidaCompletada;
        copia.puntosValidosShanghai = puntosValidosShanghai;
        copia.mejorRondaShanghai = mejorRondaShanghai;
        copia.rondasConPuntos = rondasConPuntos;
        return copia;
    }

    /** Copia también los ELEMENTOS de la lista (no solamente la propia lista). */
    public static ArrayList<EstadisticasJugadorEnCurso> copiarLista(
            List<EstadisticasJugadorEnCurso> originales) {
        if (originales == null) {
            throw new IllegalArgumentException("La lista de estadísticas es obligatoria");
        }
        ArrayList<EstadisticasJugadorEnCurso> copias = new ArrayList<>();
        for (EstadisticasJugadorEnCurso original : originales) {
            if (original == null) {
                throw new IllegalArgumentException("No puede haber estadísticas nulas");
            }
            copias.add(original.copiar());
        }
        return copias;
    }

    /**
     * Registrar un dardo REALMENTE lanzado. Clasificación elegida por el motor:
     * 0 = sin acierto válido para la modalidad, 1 = simple, 2 = doble, 3 = triple.
     * Una tirada que cause bust sigue contabilizando el dardo físico.
     */
    public void registrarDardo(int tipo) {
        if (tipo < 0 || tipo > 3) {
            throw new IllegalArgumentException("Tipo de dardo válido: 0, 1, 2 o 3");
        }
        dardosLanzados = sumar(dardosLanzados, 1);
        switch (tipo) {
            case 0: fallos = sumar(fallos, 1); break;
            case 1: simples = sumar(simples, 1); break;
            case 2: dobles = sumar(dobles, 1); break;
            case 3: triples = sumar(triples, 1); break;
            default: throw new AssertionError("Tipo no contemplado");
        }
    }

    /** Se registra una sola vez al concluir cada turno, incluso si no se lanzó. */
    public void registrarTurno() {
        turnosJugados = sumar(turnosJugados, 1);
    }

    /** En 301/501, el bust NO descuenta dardos lanzados. */
    public void registrarBust() {
        exigirPuntos();
        busts = sumar(busts, 1);
    }

    /**
     * Registrar los puntos NETOS válidos de un turno de Puntos una sola vez
     * cuando termina. Si hay bust, el motor debe pasar 0, aunque haya
     * lanzamientos previos en ese turno. Se señala expresamente un 180 válido.
     */
    public void registrarTurnoPuntos(int puntosNetos, boolean esTurno180) {
        exigirPuntos();
        noNegativo(puntosNetos, "puntosNetos");
        if (esTurno180 && puntosNetos != 180) {
            throw new IllegalArgumentException("Un turno 180 debe sumar exactamente 180 puntos válidos");
        }
        puntosValidos = sumar(puntosValidos, puntosNetos);
        mejorTurno = Math.max(mejorTurno, puntosNetos);
        if (esTurno180) turnos180 = sumar(turnos180, 1);
    }

    /** Número de dardos del turno de cierre según el motor de Puntos. */
    public void registrarCierre(int dardosEnTurnoCierre) {
        exigirPuntos();
        if (dardosEnTurnoCierre < 1) {
            throw new IllegalArgumentException("El cierre requiere al menos un dardo");
        }
        dardosCierre = dardosEnTurnoCierre;
    }

    /**
     * Actualizaciones de Cricket (marcas efectivas, cierres y puntos).
     * Los puntos recibidos pueden registrarse en otro jugador distinto al que lanza.
     */
    public void registrarCricket(int nuevasMarcas, int nuevosCierres,
                                 int nuevosPuntosGenerados, int nuevosPuntosRecibidos) {
        exigirCricket();
        noNegativo(nuevasMarcas, "nuevasMarcas");
        noNegativo(nuevosCierres, "nuevosCierres");
        noNegativo(nuevosPuntosGenerados, "nuevosPuntosGenerados");
        noNegativo(nuevosPuntosRecibidos, "nuevosPuntosRecibidos");
        marcasValidas = sumar(marcasValidas, nuevasMarcas);
        objetivosCerrados = sumar(objetivosCerrados, nuevosCierres);
        puntosGenerados = sumar(puntosGenerados, nuevosPuntosGenerados);
        puntosRecibidos = sumar(puntosRecibidos, nuevosPuntosRecibidos);
    }

    /** Registrar cada ronda completada de Double Down. */
    public void registrarRondaDoubleDown(int puntosObtenidos, boolean penalizada) {
        exigirModo(ConfiguracionPartida.MODO_DOUBLE_DOWN);
        noNegativo(puntosObtenidos, "puntosObtenidos");
        if (penalizada && puntosObtenidos != 0) {
            throw new IllegalArgumentException("Una ronda penalizada no obtiene puntos válidos");
        }
        puntosConseguidosDd = sumar(puntosConseguidosDd, puntosObtenidos);
        mejorRondaDd = Math.max(mejorRondaDd, puntosObtenidos);
        if (penalizada) rondasPenalizadas = sumar(rondasPenalizadas, 1);
    }

    /** Un solo acierto puede superar varios objetivos (doble o triple). */
    public void registrarAciertoClock(int objetivosAvanzados) {
        exigirModo(ConfiguracionPartida.MODO_AROUND_CLOCK);
        if (objetivosAvanzados < 1) {
            throw new IllegalArgumentException("Un acierto debe avanzar al menos un objetivo");
        }
        objetivosSuperados = sumar(objetivosSuperados, objetivosAvanzados);
        aciertosObjetivo = sumar(aciertosObjetivo, 1);
    }

    public void marcarClockCompletado() {
        exigirModo(ConfiguracionPartida.MODO_AROUND_CLOCK);
        partidaCompletada = true;
    }

    /** Registrar cada ronda completada de Shanghai (incluyendo rondas a cero). */
    public void registrarRondaShanghai(int puntosRonda) {
        exigirModo(ConfiguracionPartida.MODO_SHANGHAI);
        noNegativo(puntosRonda, "puntosRonda");
        puntosValidosShanghai = sumar(puntosValidosShanghai, puntosRonda);
        mejorRondaShanghai = Math.max(mejorRondaShanghai, puntosRonda);
        if (puntosRonda > 0) rondasConPuntos = sumar(rondasConPuntos, 1);
    }

    /**
     * Transformación para la fase 06: las métricas que NO pertenecen al modo
     * se dejan como null en EstadisticaPartida. NO inserta en SQLite.
     */
    public EstadisticaPartida convertirEnEstadisticaFinal(long idResultado) {
        validarModo(modo);
        EstadisticaPartida finalizada = new EstadisticaPartida(idResultado);
        finalizada.setDardosLanzados(dardosLanzados);
        finalizada.setTurnosJugados(turnosJugados);
        finalizada.setSimples(simples);
        finalizada.setDobles(dobles);
        finalizada.setTriples(triples);
        finalizada.setFallos(fallos);

        switch (modo) {
            case ConfiguracionPartida.MODO_301:
            case ConfiguracionPartida.MODO_501:
                finalizada.setBusts(busts);
                finalizada.setPuntosValidos(puntosValidos);
                finalizada.setMejorTurno(mejorTurno);
                finalizada.setTurnos180(turnos180);
                finalizada.setDardosCierre(dardosCierre);
                break;
            case ConfiguracionPartida.MODO_CRICKET:
            case ConfiguracionPartida.MODO_CUT_THROAT:
                finalizada.setMarcasValidas(marcasValidas);
                finalizada.setObjetivosCerrados(objetivosCerrados);
                finalizada.setPuntosGenerados(puntosGenerados);
                finalizada.setPuntosRecibidos(puntosRecibidos);
                break;
            case ConfiguracionPartida.MODO_DOUBLE_DOWN:
                finalizada.setRondasPenalizadas(rondasPenalizadas);
                finalizada.setMejorRondaDd(mejorRondaDd);
                finalizada.setPuntosConseguidosDd(puntosConseguidosDd);
                break;
            case ConfiguracionPartida.MODO_AROUND_CLOCK:
                finalizada.setObjetivosSuperados(objetivosSuperados);
                finalizada.setAciertosObjetivo(aciertosObjetivo);
                finalizada.setPartidaCompletada(partidaCompletada);
                break;
            case ConfiguracionPartida.MODO_SHANGHAI:
                finalizada.setPuntosValidosShanghai(puntosValidosShanghai);
                finalizada.setMejorRondaShanghai(mejorRondaShanghai);
                finalizada.setRondasConPuntos(rondasConPuntos);
                break;
            default: throw new IllegalStateException("Modo no admitido");
        }
        return finalizada;
    }

    public String getModo() { return modo; }
    public int getDardosLanzados() { return dardosLanzados; }
    public int getTurnosJugados() { return turnosJugados; }
    public int getSimples() { return simples; }
    public int getDobles() { return dobles; }
    public int getTriples() { return triples; }
    public int getFallos() { return fallos; }
    public int getBusts() { return busts; }
    public int getPuntosValidos() { return puntosValidos; }
    public int getMejorTurno() { return mejorTurno; }
    public int getTurnos180() { return turnos180; }
    public int getDardosCierre() { return dardosCierre; }
    public int getMarcasValidas() { return marcasValidas; }
    public int getObjetivosCerrados() { return objetivosCerrados; }
    public int getPuntosGenerados() { return puntosGenerados; }
    public int getPuntosRecibidos() { return puntosRecibidos; }
    public int getRondasPenalizadas() { return rondasPenalizadas; }
    public int getMejorRondaDd() { return mejorRondaDd; }
    public int getPuntosConseguidosDd() { return puntosConseguidosDd; }
    public int getObjetivosSuperados() { return objetivosSuperados; }
    public int getAciertosObjetivo() { return aciertosObjetivo; }
    public boolean isPartidaCompletada() { return partidaCompletada; }
    public int getPuntosValidosShanghai() { return puntosValidosShanghai; }
    public int getMejorRondaShanghai() { return mejorRondaShanghai; }
    public int getRondasConPuntos() { return rondasConPuntos; }

    private void exigirPuntos() {
        if (!ConfiguracionPartida.MODO_301.equals(modo)
                && !ConfiguracionPartida.MODO_501.equals(modo)) {
            throw new IllegalStateException("La operación exige 301 o 501");
        }
    }

    private void exigirCricket() {
        if (!ConfiguracionPartida.MODO_CRICKET.equals(modo)
                && !ConfiguracionPartida.MODO_CUT_THROAT.equals(modo)) {
            throw new IllegalStateException("La operación exige Cricket o Cut Throat Cricket");
        }
    }

    private void exigirModo(String esperado) {
        if (!esperado.equals(modo)) {
            throw new IllegalStateException("Operación no válida para el modo " + modo);
        }
    }

    private static void validarModo(String modo) {
        if (!ConfiguracionPartida.MODO_301.equals(modo)
                && !ConfiguracionPartida.MODO_501.equals(modo)
                && !ConfiguracionPartida.MODO_CRICKET.equals(modo)
                && !ConfiguracionPartida.MODO_CUT_THROAT.equals(modo)
                && !ConfiguracionPartida.MODO_DOUBLE_DOWN.equals(modo)
                && !ConfiguracionPartida.MODO_AROUND_CLOCK.equals(modo)
                && !ConfiguracionPartida.MODO_SHANGHAI.equals(modo)) {
            throw new IllegalArgumentException("Modo de juego no reconocido: " + modo);
        }
    }

    private static void noNegativo(int valor, String nombre) {
        if (valor < 0) throw new IllegalArgumentException(nombre + " no puede ser negativo");
    }

    private static int sumar(int actual, int incremento) {
        return Math.addExact(actual, incremento);
    }
}
