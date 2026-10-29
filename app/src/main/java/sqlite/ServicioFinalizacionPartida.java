package sqlite;

import android.content.Context;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import modelos.ConfiguracionPartida;
import modelos.EstadisticaPartida;
import modelos.Partida;
import modelos.ResultadoPartida;
import preferencias.GestorPartidaEnCurso;
import modelos.PartidaEnCurso;
import utilidades.DatosFinalizacionPartida;

/** Traduce la fotografía final a modelos de persistencia sin SQL en Activities. */
public final class ServicioFinalizacionPartida {
    private ServicioFinalizacionPartida() { }

    public static long guardar(Context contexto, Intent resultado,
                               int rondasJugadas, String motivo,
                               ArrayList<Integer> posiciones,
                               ArrayList<Integer> puntuaciones,
                               ArrayList<Integer> indicesOriginales) {
        DatosFinalizacionPartida datos = DatosFinalizacionPartida.desdeIntent(resultado);
        if (datos == null) throw new IllegalArgumentException("No se recibieron estadísticas finales");
        ConfiguracionPartida config = datos.validarConfiguracion();
        int n = config.getParticipantes().size();
        if (posiciones == null || puntuaciones == null || indicesOriginales == null
                || n != posiciones.size() || n != puntuaciones.size()
                || n != indicesOriginales.size() || n != datos.estadisticas.size()) {
            throw new IllegalArgumentException("Clasificación incompleta");
        }
        boolean[] vistos = new boolean[n];
        List<ResultadoPartida> resultados = new ArrayList<>();
        List<EstadisticaPartida> estadisticas = new ArrayList<>();
        Long ganador = null;
        for (int i = 0; i < n; i++) {
            int indice = indicesOriginales.get(i);
            if (indice < 0 || indice >= n || vistos[indice])
                throw new IllegalArgumentException("Índice de jugador repetido o incorrecto");
            vistos[indice] = true;
            ConfiguracionPartida.Participante jugador = config.getParticipantes().get(indice);
            int posicion = posiciones.get(i);
            if (posicion < 1 || posicion > n)
                throw new IllegalArgumentException("Posición de jugador inválida");
            if (posicion == 1 && ganador == null) ganador = jugador.getIdJugador();
            resultados.add(new ResultadoPartida(0, jugador.getIdJugador(), posicion,
                    puntuaciones.get(i), jugador.getOrdenTurno()));
            estadisticas.add(datos.estadisticas.get(indice).convertirEnEstadisticaFinal(0));
        }
        Partida partida = new Partida(config.getModo(), config.getRondas(), rondasJugadas,
                n, ganador, motivo, datos.configuracionJson);
        partida.setIdentificadorUnico(datos.identificadorUnico);
        long id;
        try (RepositorioPartidas repositorio = new RepositorioPartidas(contexto)) {
            id = repositorio.guardarPartidaCompleta(partida, resultados, estadisticas);
        }
        // No borrar una partida diferente (p.ej. una revancha ya iniciada).
        PartidaEnCurso enCurso = GestorPartidaEnCurso.cargarPartida(contexto);
        if (enCurso != null && datos.identificadorUnico.equals(enCurso.getIdentificadorUnico())) {
            GestorPartidaEnCurso.eliminarPartida(contexto);
        }
        return id;
    }
}
