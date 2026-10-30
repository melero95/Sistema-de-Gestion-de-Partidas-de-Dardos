package modelos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Resumen histórico de un jugador, independientemente de si está activo. */
public final class EstadisticasJugadorDTO {
    public static final class UltimaPartida {
        public final long idPartida;
        public final long fecha;
        public final String modo;
        public final int posicion;
        public final boolean victoria;
        public UltimaPartida(long idPartida, long fecha, String modo, int posicion, boolean victoria) {
            this.idPartida = idPartida;
            this.fecha = fecha;
            this.modo = modo;
            this.posicion = posicion;
            this.victoria = victoria;
        }
    }
    public final long idJugador;
    public final String nombre;
    public final int color;
    public final boolean activo;
    public final long partidas;
    public final long victorias;
    public final long segundos;
    public final long terceros;
    public final double porcentajeVictorias;
    public final List<EstadisticasResumenDTO.Modalidad> partidasPorModo;
    public final List<UltimaPartida> ultimasPartidas;

    public EstadisticasJugadorDTO(long idJugador, String nombre, int color, boolean activo,
                                 long partidas, long victorias, long segundos, long terceros,
                                 List<EstadisticasResumenDTO.Modalidad> partidasPorModo,
                                 List<UltimaPartida> ultimasPartidas) {
        this.idJugador = idJugador;
        this.nombre = nombre;
        this.color = color;
        this.activo = activo;
        this.partidas = partidas;
        this.victorias = victorias;
        this.segundos = segundos;
        this.terceros = terceros;
        this.porcentajeVictorias = partidas == 0 ? 0d : 100d * victorias / partidas;
        this.partidasPorModo = Collections.unmodifiableList(new ArrayList<>(partidasPorModo));
        this.ultimasPartidas = Collections.unmodifiableList(new ArrayList<>(ultimasPartidas));
    }
}
