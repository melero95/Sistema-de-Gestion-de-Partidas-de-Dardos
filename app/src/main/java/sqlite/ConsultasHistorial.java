
package sqlite;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelos.Jugador;
import modelos.Partida;
import modelos.PartidaResumenDTO;
import modelos.ResultadoPartida;

/**
 * Agrega las consultas de DAOs sin introducir SQL
 * en las Activities.
 */
public class ConsultasHistorial implements AutoCloseable {

    private final PartidaDao partidas;
    private final JugadorDao jugadores;
    private final ResultadoPartidaDao resultados;

    public ConsultasHistorial(Context context) {
        partidas = new PartidaDao(context);
        jugadores = new JugadorDao(context);
        resultados = new ResultadoPartidaDao(context);
    }

    /**
     * Consulta original, conservada por compatibilidad.
     */
    public List<PartidaResumenDTO> listar(
            String modo,
            boolean incluirOcultas) {

        return listar(modo, null, incluirOcultas);
    }

    /**
     * Devuelve partidas filtradas por modo y jugador.
     *
     * idJugador == null: todos los jugadores.
     *
     * Incluye jugadores inactivos, ya que las
     * participaciones históricas permanecen en SQLite.
     */
    public List<PartidaResumenDTO> listar(
            String modo,
            Long idJugador,
            boolean incluirOcultas) {

        List<PartidaResumenDTO> salida =
                new ArrayList<>();

        Set<Long> idsPartidasJugador = null;

        if (idJugador != null) {

            idsPartidasJugador = new HashSet<>();

            List<ResultadoPartida> participaciones =
                    resultados.listarPorJugador(idJugador);

            for (ResultadoPartida resultado :
                    participaciones) {

                idsPartidasJugador.add(
                        resultado.getIdPartida()
                );
            }
        }

        for (Partida p :
                partidas.listarHistorial(
                        modo,
                        incluirOcultas)) {

            if (idsPartidasJugador != null
                    && !idsPartidasJugador.contains(
                    p.getId())) {

                continue;
            }

            String ganador = "Sin ganador";

            if (p.getIdGanador() != null) {

                Jugador j = jugadores.obtenerPorId(
                        p.getIdGanador()
                );

                ganador = j == null
                        ? "Jugador no disponible"
                        : j.getNombre();
            }

            salida.add(
                    new PartidaResumenDTO(p, ganador)
            );
        }

        return salida;
    }

    /**
     * Devuelve todos los jugadores, incluidos inactivos.
     */
    public List<Jugador> listarJugadores() {
        return jugadores.listarTodos();
    }

    public Partida obtener(long id) {
        return partidas.obtenerPorId(id);
    }

    public boolean oculta(long id) {
        return partidas.estaOculta(id);
    }

    public boolean cambiarVisibilidad(
            long id,
            boolean oculta) {

        return partidas.cambiarOculta(id, oculta);
    }

    public int contarOcultas() {
        return partidas.contarOcultas();
    }

    public int restaurarTodas() {
        return partidas.restaurarTodas();
    }

    @Override
    public void close() {
        partidas.close();
        jugadores.close();
        resultados.close();
    }
}
