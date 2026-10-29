package modelos;

/** Datos listos para representar una fila del historial. */
public final class PartidaResumenDTO {
    private final Partida partida;
    private final String ganador;
    public PartidaResumenDTO(Partida partida, String ganador) {
        this.partida = partida;
        this.ganador = ganador == null ? "Sin ganador" : ganador;
    }
    public Partida getPartida() { return partida; }
    public long getId() { return partida.getId(); }
    public String getGanador() { return ganador; }
}
