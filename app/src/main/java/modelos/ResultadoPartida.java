package modelos;

/**
 * Resultado individual de un jugador en una partida FINALIZADA.
 *
 * Se corresponde con una fila de la tabla RESULTADOS_PARTIDA.
 * La posición refleja la clasificación (admite empates); el orden de turno
 * refleja el orden real de participación, aunque fuese aleatorio.
 *
 * Los índices de jugadores que circulan por los Intent actuales comienzan
 * en 0. Al crear este modelo, convertirlos a posiciones 1..6.
 */
public class ResultadoPartida {

    // Lo asigna SQLite al insertar. 0 significa que todavía no se ha guardado.
    private long id;

    // Claves foráneas: deben pertenecer a una partida y un jugador existentes.
    private long idPartida;
    private long idJugador;

    // Clasificación y orden real de juego: valores 1..6.
    private int posicion;
    private Integer puntuacionFinal;
    private int ordenTurno;

    public ResultadoPartida() {
        // Necesario para reconstruir los registros desde el DAO.
    }

    public ResultadoPartida(long idPartida, long idJugador, int posicion,
                            Integer puntuacionFinal, int ordenTurno) {
        setIdPartida(idPartida);
        setIdJugador(idJugador);
        setPosicion(posicion);
        setPuntuacionFinal(puntuacionFinal);
        setOrdenTurno(ordenTurno);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("El ID del resultado no puede ser negativo");
        }
        this.id = id;
    }

    public long getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(long idPartida) {
        if (idPartida < 0) {
            throw new IllegalArgumentException("El ID de la partida no puede ser negativo");
        }
        this.idPartida = idPartida;
    }

    public long getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(long idJugador) {
        if (idJugador <= 0) {
            throw new IllegalArgumentException("El ID del jugador debe ser positivo");
        }
        this.idJugador = idJugador;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        if (posicion < 1 || posicion > 6) {
            throw new IllegalArgumentException("La posición debe estar entre 1 y 6");
        }
        this.posicion = posicion;
    }

    /** Puede ser null cuando un modo no expresa un marcador final convencional. */
    public Integer getPuntuacionFinal() {
        return puntuacionFinal;
    }

    public void setPuntuacionFinal(Integer puntuacionFinal) {
        this.puntuacionFinal = puntuacionFinal;
    }

    public int getOrdenTurno() {
        return ordenTurno;
    }

    public void setOrdenTurno(int ordenTurno) {
        if (ordenTurno < 1 || ordenTurno > 6) {
            throw new IllegalArgumentException("El orden de turno debe estar entre 1 y 6");
        }
        this.ordenTurno = ordenTurno;
    }
}
