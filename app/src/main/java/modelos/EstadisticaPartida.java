package modelos;

/**
 * Estadísticas de UN participante en UNA partida finalizada.
 * Equivale a una fila de ESTADISTICAS_PARTIDA (relación 1:1 con RESULTADOS_PARTIDA).
 *
 * Las métricas comunes empiezan en cero. Las métricas específicas empiezan en null:
 * null indica "no aplicable", mientras que 0 indica "aplicable pero sin ocurrencias".
 *
 * Este modelo NO registra cada lanzamiento: la acumulación durante el juego
 * se realizará en un estado separado y se copiará aquí al finalizar.
 * No ejecuta SQL; el DAO convertirá Boolean a INTEGER (1/0) o NULL.
 */
public class EstadisticaPartida {

    // Asignado por SQLite al insertar: 0 indica todavía sin guardar.
    private long id;

    // Debe apuntar al ID de una fila ya insertada en RESULTADOS_PARTIDA.
    private long idResultado;

    // Estadísticas comunes (obligatorias; valor inicial 0).
    private int dardosLanzados;
    private int turnosJugados;
    private int simples;
    private int dobles;
    private int triples;
    private int fallos;

    // 301 / 501 (NULL cuando el modo no corresponde).
    private Integer busts;
    private Integer puntosValidos;
    private Integer mejorTurno;
    private Integer turnos180;
    private Integer dardosCierre;

    // Cricket / Cut Throat Cricket.
    private Integer marcasValidas;
    private Integer objetivosCerrados;
    private Integer puntosGenerados;
    private Integer puntosRecibidos;

    // Double Down.
    private Integer rondasPenalizadas;
    private Integer mejorRondaDd;
    private Integer puntosConseguidosDd;

    // Around the Clock.
    private Integer objetivosSuperados;
    private Integer aciertosObjetivo;
    private Boolean partidaCompletada;

    // Shanghai.
    private Integer puntosValidosShanghai;
    private Integer mejorRondaShanghai;
    private Integer rondasConPuntos;

    public EstadisticaPartida() {
        // Constructor vacío para lectura desde el DAO.
    }

    public EstadisticaPartida(long idResultado) {
        setIdResultado(idResultado);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("El ID de la estadística no puede ser negativo");
        }
        this.id = id;
    }

    public long getIdResultado() {
        return idResultado;
    }

    public void setIdResultado(long idResultado) {
        if (idResultado <= 0) {
            throw new IllegalArgumentException("El ID del resultado debe ser positivo");
        }
        this.idResultado = idResultado;
    }

    // Estadísticas comunes (obligatorias; valor inicial 0).

    /** Columna dardos_lanzados. */
    public int getDardosLanzados() {
        return dardosLanzados;
    }

    public void setDardosLanzados(int dardosLanzados) {
        validarNoNegativo("dardos_lanzados", dardosLanzados);
        this.dardosLanzados = dardosLanzados;
    }

    /** Columna turnos_jugados. */
    public int getTurnosJugados() {
        return turnosJugados;
    }

    public void setTurnosJugados(int turnosJugados) {
        validarNoNegativo("turnos_jugados", turnosJugados);
        this.turnosJugados = turnosJugados;
    }

    /** Columna simples. */
    public int getSimples() {
        return simples;
    }

    public void setSimples(int simples) {
        validarNoNegativo("simples", simples);
        this.simples = simples;
    }

    /** Columna dobles. */
    public int getDobles() {
        return dobles;
    }

    public void setDobles(int dobles) {
        validarNoNegativo("dobles", dobles);
        this.dobles = dobles;
    }

    /** Columna triples. */
    public int getTriples() {
        return triples;
    }

    public void setTriples(int triples) {
        validarNoNegativo("triples", triples);
        this.triples = triples;
    }

    /** Columna fallos. */
    public int getFallos() {
        return fallos;
    }

    public void setFallos(int fallos) {
        validarNoNegativo("fallos", fallos);
        this.fallos = fallos;
    }

    // 301 / 501 (NULL cuando el modo no corresponde).

    /** Columna busts. */
    public Integer getBusts() {
        return busts;
    }

    public void setBusts(Integer busts) {
        validarNoNegativo("busts", busts);
        this.busts = busts;
    }

    /** Columna puntos_validos. */
    public Integer getPuntosValidos() {
        return puntosValidos;
    }

    public void setPuntosValidos(Integer puntosValidos) {
        validarNoNegativo("puntos_validos", puntosValidos);
        this.puntosValidos = puntosValidos;
    }

    /** Columna mejor_turno. */
    public Integer getMejorTurno() {
        return mejorTurno;
    }

    public void setMejorTurno(Integer mejorTurno) {
        validarNoNegativo("mejor_turno", mejorTurno);
        this.mejorTurno = mejorTurno;
    }

    /** Columna turnos_180. */
    public Integer getTurnos180() {
        return turnos180;
    }

    public void setTurnos180(Integer turnos180) {
        validarNoNegativo("turnos_180", turnos180);
        this.turnos180 = turnos180;
    }

    /** Columna dardos_cierre. */
    public Integer getDardosCierre() {
        return dardosCierre;
    }

    public void setDardosCierre(Integer dardosCierre) {
        validarNoNegativo("dardos_cierre", dardosCierre);
        this.dardosCierre = dardosCierre;
    }

    // Cricket / Cut Throat Cricket.

    /** Columna marcas_validas. */
    public Integer getMarcasValidas() {
        return marcasValidas;
    }

    public void setMarcasValidas(Integer marcasValidas) {
        validarNoNegativo("marcas_validas", marcasValidas);
        this.marcasValidas = marcasValidas;
    }

    /** Columna objetivos_cerrados. */
    public Integer getObjetivosCerrados() {
        return objetivosCerrados;
    }

    public void setObjetivosCerrados(Integer objetivosCerrados) {
        validarNoNegativo("objetivos_cerrados", objetivosCerrados);
        this.objetivosCerrados = objetivosCerrados;
    }

    /** Columna puntos_generados. */
    public Integer getPuntosGenerados() {
        return puntosGenerados;
    }

    public void setPuntosGenerados(Integer puntosGenerados) {
        validarNoNegativo("puntos_generados", puntosGenerados);
        this.puntosGenerados = puntosGenerados;
    }

    /** Columna puntos_recibidos. */
    public Integer getPuntosRecibidos() {
        return puntosRecibidos;
    }

    public void setPuntosRecibidos(Integer puntosRecibidos) {
        validarNoNegativo("puntos_recibidos", puntosRecibidos);
        this.puntosRecibidos = puntosRecibidos;
    }

    // Double Down.

    /** Columna rondas_penalizadas. */
    public Integer getRondasPenalizadas() {
        return rondasPenalizadas;
    }

    public void setRondasPenalizadas(Integer rondasPenalizadas) {
        validarNoNegativo("rondas_penalizadas", rondasPenalizadas);
        this.rondasPenalizadas = rondasPenalizadas;
    }

    /** Columna mejor_ronda_dd. */
    public Integer getMejorRondaDd() {
        return mejorRondaDd;
    }

    public void setMejorRondaDd(Integer mejorRondaDd) {
        validarNoNegativo("mejor_ronda_dd", mejorRondaDd);
        this.mejorRondaDd = mejorRondaDd;
    }

    /** Columna puntos_conseguidos_dd. */
    public Integer getPuntosConseguidosDd() {
        return puntosConseguidosDd;
    }

    public void setPuntosConseguidosDd(Integer puntosConseguidosDd) {
        validarNoNegativo("puntos_conseguidos_dd", puntosConseguidosDd);
        this.puntosConseguidosDd = puntosConseguidosDd;
    }

    // Around the Clock.

    /** Columna objetivos_superados. */
    public Integer getObjetivosSuperados() {
        return objetivosSuperados;
    }

    public void setObjetivosSuperados(Integer objetivosSuperados) {
        validarNoNegativo("objetivos_superados", objetivosSuperados);
        this.objetivosSuperados = objetivosSuperados;
    }

    /** Columna aciertos_objetivo. */
    public Integer getAciertosObjetivo() {
        return aciertosObjetivo;
    }

    public void setAciertosObjetivo(Integer aciertosObjetivo) {
        validarNoNegativo("aciertos_objetivo", aciertosObjetivo);
        this.aciertosObjetivo = aciertosObjetivo;
    }

    /** partida_completada: Boolean permite distinguir true, false y null (no aplicable). */
    public Boolean getPartidaCompletada() {
        return partidaCompletada;
    }

    public void setPartidaCompletada(Boolean partidaCompletada) {
        this.partidaCompletada = partidaCompletada;
    }

    // Shanghai.

    /** Columna puntos_validos_shanghai. */
    public Integer getPuntosValidosShanghai() {
        return puntosValidosShanghai;
    }

    public void setPuntosValidosShanghai(Integer puntosValidosShanghai) {
        validarNoNegativo("puntos_validos_shanghai", puntosValidosShanghai);
        this.puntosValidosShanghai = puntosValidosShanghai;
    }

    /** Columna mejor_ronda_shanghai. */
    public Integer getMejorRondaShanghai() {
        return mejorRondaShanghai;
    }

    public void setMejorRondaShanghai(Integer mejorRondaShanghai) {
        validarNoNegativo("mejor_ronda_shanghai", mejorRondaShanghai);
        this.mejorRondaShanghai = mejorRondaShanghai;
    }

    /** Columna rondas_con_puntos. */
    public Integer getRondasConPuntos() {
        return rondasConPuntos;
    }

    public void setRondasConPuntos(Integer rondasConPuntos) {
        validarNoNegativo("rondas_con_puntos", rondasConPuntos);
        this.rondasConPuntos = rondasConPuntos;
    }

    private static void validarNoNegativo(String campo, Integer valor) {
        if (valor != null && valor < 0) {
            throw new IllegalArgumentException(campo + " no puede ser negativo");
        }
    }
}
