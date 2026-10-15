package sqlite;

import static org.junit.Assert.*;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import modelos.Jugador;
import modelos.Partida;
import modelos.ResultadoPartida;

@RunWith(AndroidJUnit4.class)
public class ResultadoPartidaDaoTest {

    private static final String BD = "pruebas_resultado_dao.db";
    private Context contexto;
    private BaseDatosHelper helper;
    private ResultadoPartidaDao dao;
    private long partida;
    private long ana;
    private long luis;

    @Before
    public void preparar() {
        contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();
        contexto.deleteDatabase(BD);
        helper = new BaseDatosHelper(contexto, BD);
        dao = new ResultadoPartidaDao(helper);
        JugadorDao jugadores = new JugadorDao(helper);
        ana = jugadores.insertar(new Jugador("Ana", 0xFF000001));
        luis = jugadores.insertar(new Jugador("Luis", 0xFF000002));
        partida = new PartidaDao(helper).insertar(new Partida(
                "301", 15, 8, 2, ana, "VICTORIA", "{\"version\":1}"));
    }

    @After
    public void limpiar() {
        if (helper != null) helper.close();
        if (contexto != null) contexto.deleteDatabase(BD);
    }

    @Test
    public void insertarYRecuperarConPuntuacion() {
        ResultadoPartida r = new ResultadoPartida(partida, ana, 1, 0, 2);
        long id = dao.insertar(r);
        assertTrue(id > 0);
        assertEquals(id, r.getId());
        ResultadoPartida leido = dao.obtenerPorId(id);
        assertNotNull(leido);
        assertEquals(partida, leido.getIdPartida());
        assertEquals(ana, leido.getIdJugador());
        assertEquals(1, leido.getPosicion());
        assertEquals(Integer.valueOf(0), leido.getPuntuacionFinal());
        assertEquals(2, leido.getOrdenTurno());
    }

    @Test
    public void puntuacionNullSeConserva() {
        long id = dao.insertar(new ResultadoPartida(partida, ana, 1, null, 1));
        assertNull(dao.obtenerPorId(id).getPuntuacionFinal());
    }

    @Test
    public void ordenarPorClasificacionNoPorTurno() {
        dao.insertar(new ResultadoPartida(partida, ana, 2, 20, 1));
        dao.insertar(new ResultadoPartida(partida, luis, 1, 0, 2));
        List<ResultadoPartida> r = dao.listarPorPartida(partida);
        assertEquals(2, r.size());
        assertEquals(luis, r.get(0).getIdJugador());
        assertEquals(ana, r.get(1).getIdJugador());
    }

    @Test
    public void permiteEmpatesEnPosicion() {
        dao.insertar(new ResultadoPartida(partida, ana, 1, 20, 1));
        dao.insertar(new ResultadoPartida(partida, luis, 1, 20, 2));
        assertEquals(2, dao.listarPorPartida(partida).size());
    }

    @Test
    public void consultaPorPartidaYJugador() {
        ResultadoPartida r = new ResultadoPartida(partida, ana, 1, 0, 1);
        dao.insertar(r);
        assertEquals(r.getId(), dao.obtenerPorPartidaYJugador(partida, ana).getId());
        assertNull(dao.obtenerPorPartidaYJugador(partida, luis));
        assertEquals(1, dao.listarPorJugador(ana).size());
        assertTrue(dao.listarPorJugador(luis).isEmpty());
    }

    @Test
    public void rechazaJugadorDuplicadoEnPartida() {
        dao.insertar(new ResultadoPartida(partida, ana, 1, 0, 1));
        try {
            dao.insertar(new ResultadoPartida(partida, ana, 2, 20, 2));
            fail("Un jugador no puede tener dos resultados en la misma partida");
        } catch (SQLiteConstraintException esperado) {
            assertEquals(1, dao.listarPorPartida(partida).size());
        }
    }

    @Test
    public void rechazaOrdenTurnoDuplicado() {
        dao.insertar(new ResultadoPartida(partida, ana, 1, 0, 1));
        try {
            dao.insertar(new ResultadoPartida(partida, luis, 2, 20, 1));
            fail("El orden de turno debe ser único");
        } catch (SQLiteConstraintException esperado) {
            assertEquals(1, dao.listarPorPartida(partida).size());
        }
    }

    @Test
    public void rechazaClavesForaneasInexistentes() {
        try {
            dao.insertar(new ResultadoPartida(99999L, ana, 1, 0, 1));
            fail("La partida debe existir");
        } catch (SQLiteConstraintException esperado) {
            // OK
        }
        try {
            dao.insertar(new ResultadoPartida(partida, 99999L, 1, 0, 1));
            fail("El jugador debe existir");
        } catch (SQLiteConstraintException esperado) {
            assertTrue(dao.listarPorPartida(partida).isEmpty());
        }
    }

    @Test
    public void resultadoSinInsertarNoAdmiteIdReutilizado() {
        ResultadoPartida r = new ResultadoPartida(partida, ana, 1, 0, 1);
        dao.insertar(r);
        try {
            dao.insertar(r);
            fail("No se debe insertar un resultado ya guardado");
        } catch (IllegalArgumentException esperado) {
            assertEquals(1, dao.listarPorPartida(partida).size());
        }
    }
}
