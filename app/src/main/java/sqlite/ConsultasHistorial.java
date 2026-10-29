package sqlite;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import modelos.Jugador;
import modelos.Partida;
import modelos.PartidaResumenDTO;

/** Agrega las consultas de DAOs sin introducir SQL en las Activities. */
public class ConsultasHistorial implements AutoCloseable {
    private final PartidaDao partidas;
    private final JugadorDao jugadores;
    public ConsultasHistorial(Context context) {
        partidas = new PartidaDao(context); jugadores = new JugadorDao(context);
    }
    public List<PartidaResumenDTO> listar(String modo, boolean incluirOcultas) {
        List<PartidaResumenDTO> salida = new ArrayList<>();
        for (Partida p : partidas.listarHistorial(modo, incluirOcultas)) {
            String ganador = "Sin ganador";
            if (p.getIdGanador() != null) {
                Jugador j = jugadores.obtenerPorId(p.getIdGanador());
                ganador = j == null ? "Jugador no disponible" : j.getNombre();
            }
            salida.add(new PartidaResumenDTO(p, ganador));
        }
        return salida;
    }
    public Partida obtener(long id) { return partidas.obtenerPorId(id); }
    public boolean oculta(long id) { return partidas.estaOculta(id); }
    public boolean cambiarVisibilidad(long id, boolean oculta) { return partidas.cambiarOculta(id, oculta); }
    public int contarOcultas() { return partidas.contarOcultas(); }
    public int restaurarTodas() { return partidas.restaurarTodas(); }
    @Override public void close() { partidas.close(); jugadores.close(); }
}
