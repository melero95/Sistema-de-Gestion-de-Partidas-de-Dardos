package modelos;

/** Marca histórica de un jugador en un ranking determinado. */
public final class RecordEstadisticoDTO {
    public final long idJugador;
    public final String nombre;
    public final int color;
    public final String modo;
    public final double valor;
    public final long partidas;

    public RecordEstadisticoDTO(long idJugador, String nombre, int color,
                               String modo, double valor, long partidas) {
        this.idJugador = idJugador;
        this.nombre = nombre;
        this.color = color;
        this.modo = modo;
        this.valor = valor;
        this.partidas = partidas;
    }
}
