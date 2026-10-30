package modelos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Datos agregados de lectura para la pantalla Estadísticas. */
public final class EstadisticasResumenDTO {
    public static final class Modalidad {
        public final String modo;
        public final long partidas;
        public Modalidad(String modo, long partidas) {
            this.modo = modo;
            this.partidas = partidas;
        }
    }
    public final long totalPartidas;
    public final long totalJugadores;
    public final String modalidadMasJugada;
    public final List<Modalidad> partidasPorModo;

    public EstadisticasResumenDTO(long totalPartidas, long totalJugadores,
                                  String modalidadMasJugada, List<Modalidad> partidasPorModo) {
        this.totalPartidas = totalPartidas;
        this.totalJugadores = totalJugadores;
        this.modalidadMasJugada = modalidadMasJugada;
        this.partidasPorModo = Collections.unmodifiableList(new ArrayList<>(partidasPorModo));
    }
}
