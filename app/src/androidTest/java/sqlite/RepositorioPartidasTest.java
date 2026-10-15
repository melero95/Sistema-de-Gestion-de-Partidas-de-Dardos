package sqlite;

import static org.junit.Assert.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import modelos.EstadisticaPartida;
import modelos.Jugador;
import modelos.Partida;
import modelos.ResultadoPartida;

@RunWith(AndroidJUnit4.class)
public class RepositorioPartidasTest {

    private static final String BD = "pruebas_repositorio_partidas.db";
    private Context contexto;
    private BaseDatosHelper helper;
    private RepositorioPartidas repositorio;
    private long ana;
    private long luis;

    @Before
    public void preparar() {
        contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();
        contexto.deleteDatabase(BD);
        helper = new BaseDatosHelper(contexto, BD);
        repositorio = new RepositorioPartidas(helper);
        JugadorDao jugadores = new JugadorDao(helper);
        ana = jugadores.insertar(new Jugador("Ana", 0xFF334455));
        luis = jugadores.insertar(new Jugador("Luis", 0xFF667788));
    }

    @After
    public void limpiar() {
        if (helper != null) helper.close();
        if (contexto != null) contexto.deleteDatabase(BD);
    }

    private Partida nuevaPartida() {
        return new Partida("501", 15, 8, 2, ana,
                "VICTORIA", "{\"version\":1,\"dardos\":3}");
    }

    // Construcción válida de participantes nuevos: constructor vacío + setters.
    private List<ResultadoPartida> resultadosNuevos() {
        ResultadoPartida a = new ResultadoPartida();
        a.setIdJugador(ana);
        a.setPosicion(1);
        a.setPuntuacionFinal(0);
        a.setOrdenTurno(2);
        ResultadoPartida b = new ResultadoPartida();
        b.setIdJugador(luis);
        b.setPosicion(2);
        b.setPuntuacionFinal(82);
        b.setOrdenTurno(1);
        return new ArrayList<>(Arrays.asList(a, b));
    }

    private List<EstadisticaPartida> estadisticasNuevas() {
        EstadisticaPartida a = new EstadisticaPartida();
        a.setDardosLanzados(21);
        a.setBusts(2);
        a.setPuntosValidos(501);
        EstadisticaPartida b = new EstadisticaPartida();
        b.setDardosLanzados(24);
        b.setBusts(1);
        b.setPuntosValidos(419);
        return new ArrayList<>(Arrays.asList(a, b));
    }

    private long contar(String tabla) {
        try (Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + tabla, null)) {
            assertTrue(c.moveToFirst());
            return c.getLong(0);
        }
    }

    @Test
    public void guardaTresTablasJuntasConVinculosCorrectos() {
        Partida p = nuevaPartida();
        List<ResultadoPartida> r = resultadosNuevos();
        List<EstadisticaPartida> e = estadisticasNuevas();
        long id = repositorio.guardarPartidaCompleta(p, r, e);
        assertTrue(id > 0L);
        assertEquals(id, p.getId());
        assertEquals(1L, contar(BaseDatosHelper.TABLA_PARTIDAS));
        assertEquals(2L, contar(BaseDatosHelper.TABLA_RESULTADOS));
        assertEquals(2L, contar(BaseDatosHelper.TABLA_ESTADISTICAS));
        assertEquals(id, r.get(0).getIdPartida());
        assertTrue(r.get(0).getId() > 0L);
        assertEquals(r.get(0).getId(), e.get(0).getIdResultado());
        assertTrue(e.get(0).getId() > 0L);
        assertEquals(Integer.valueOf(501),
                new EstadisticaPartidaDao(helper).obtenerPorResultado(r.get(0).getId()).getPuntosValidos());
    }

    @Test
    public void reintentoMismoUuidNoDuplicaRegistros() {
        Partida primera = nuevaPartida();
        long id = repositorio.guardarPartidaCompleta(
                primera, resultadosNuevos(), estadisticasNuevas());
        // El reintento normal reconstruye objetos nuevos, pero conserva UUID.
        Partida reintento = nuevaPartida();
        reintento.setIdentificadorUnico(primera.getIdentificadorUnico());
        long idRepetido = repositorio.guardarPartidaCompleta(
                reintento, resultadosNuevos(), estadisticasNuevas());
        assertEquals(id, idRepetido);
        assertEquals(1L, contar(BaseDatosHelper.TABLA_PARTIDAS));
        assertEquals(2L, contar(BaseDatosHelper.TABLA_RESULTADOS));
        assertEquals(2L, contar(BaseDatosHelper.TABLA_ESTADISTICAS));
    }

    @Test
    public void reintentoConLosMismosObjetosNoDuplica() {
        Partida p = nuevaPartida();
        List<ResultadoPartida> resultados = resultadosNuevos();
        List<EstadisticaPartida> estadisticas = estadisticasNuevas();
        long id = repositorio.guardarPartidaCompleta(p, resultados, estadisticas);
        assertEquals(id, repositorio.guardarPartidaCompleta(p, resultados, estadisticas));
        assertEquals(1L, contar(BaseDatosHelper.TABLA_PARTIDAS));
        assertEquals(2L, contar(BaseDatosHelper.TABLA_RESULTADOS));
        assertEquals(2L, contar(BaseDatosHelper.TABLA_ESTADISTICAS));
    }

    @Test
    public void errorClaveForaneaHaceRollbackDeTodoYPermiteReintentar() {
        Partida partida = nuevaPartida();
        List<ResultadoPartida> resultados = resultadosNuevos();
        List<EstadisticaPartida> estadisticas = estadisticasNuevas();
        resultados.get(1).setIdJugador(999999L);
        try {
            repositorio.guardarPartidaCompleta(partida, resultados, estadisticas);
            fail("El segundo resultado debe fallar por FK inexistente");
        } catch (SQLiteConstraintException esperado) {
            assertEquals(0L, partida.getId());
            assertEquals(0L, resultados.get(0).getId());
            assertEquals(0L, resultados.get(0).getIdPartida());
            assertEquals(0L, estadisticas.get(0).getId());
            assertEquals(0L, estadisticas.get(0).getIdResultado());
            assertEquals(0L, contar(BaseDatosHelper.TABLA_PARTIDAS));
            assertEquals(0L, contar(BaseDatosHelper.TABLA_RESULTADOS));
            assertEquals(0L, contar(BaseDatosHelper.TABLA_ESTADISTICAS));
        }
        resultados.get(1).setIdJugador(luis);
        assertTrue(repositorio.guardarPartidaCompleta(partida, resultados, estadisticas) > 0L);
        assertEquals(2L, contar(BaseDatosHelper.TABLA_ESTADISTICAS));
    }

    @Test
    public void rechazaNumeroDeParticipantesIncorrecto() {
        Partida p = nuevaPartida();
        try {
            repositorio.guardarPartidaCompleta(p,
                    resultadosNuevos().subList(0, 1), estadisticasNuevas().subList(0, 1));
            fail("El número de participantes debe coincidir");
        } catch (IllegalArgumentException esperado) {
            assertEquals(0L, contar(BaseDatosHelper.TABLA_PARTIDAS));
        }
    }

    @Test
    public void rechazaJugadorDuplicadoSinIniciarGuardado() {
        List<ResultadoPartida> resultados = resultadosNuevos();
        resultados.get(1).setIdJugador(ana);
        try {
            repositorio.guardarPartidaCompleta(
                    nuevaPartida(), resultados, estadisticasNuevas());
            fail("No debe admitirse un mismo jugador dos veces");
        } catch (IllegalArgumentException esperado) {
            assertEquals(0L, contar(BaseDatosHelper.TABLA_PARTIDAS));
        }
    }

    @Test
    public void rechazaOrdenDuplicadoAntesDeEscribir() {
        List<ResultadoPartida> resultados = resultadosNuevos();
        resultados.get(1).setOrdenTurno(2);
        try {
            repositorio.guardarPartidaCompleta(
                    nuevaPartida(), resultados, estadisticasNuevas());
            fail("Cada turno debe ser único");
        } catch (IllegalArgumentException esperado) {
            assertEquals(0L, contar(BaseDatosHelper.TABLA_PARTIDAS));
        }
    }

    @Test
    public void rechazaGanadorNoParticipante() {
        Partida p = nuevaPartida();
        p.setIdGanador(999999L);
        try {
            repositorio.guardarPartidaCompleta(p,
                    resultadosNuevos(), estadisticasNuevas());
            fail("El ganador debe estar en la lista de participantes");
        } catch (IllegalArgumentException esperado) {
            assertEquals(0L, contar(BaseDatosHelper.TABLA_PARTIDAS));
        }
    }

    @Test
    public void rechazaNumeroDistintoDeEstadisticas() {
        try {
            repositorio.guardarPartidaCompleta(nuevaPartida(),
                    resultadosNuevos(), estadisticasNuevas().subList(0, 1));
            fail("Cada resultado debe tener estadística");
        } catch (IllegalArgumentException esperado) {
            assertEquals(0L, contar(BaseDatosHelper.TABLA_PARTIDAS));
        }
    }
}
