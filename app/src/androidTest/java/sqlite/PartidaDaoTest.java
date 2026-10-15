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

@RunWith(AndroidJUnit4.class)
public class PartidaDaoTest {

    private static final String BD = "pruebas_partida_dao.db";
    private Context contexto;
    private BaseDatosHelper helper;
    private PartidaDao dao;
    private JugadorDao jugadores;

    @Before
    public void preparar() {
        contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();
        contexto.deleteDatabase(BD);
        helper = new BaseDatosHelper(contexto, BD);
        dao = new PartidaDao(helper);
        jugadores = new JugadorDao(helper);
    }

    @After
    public void limpiar() {
        if (helper != null) helper.close();
        if (contexto != null) contexto.deleteDatabase(BD);
    }

    private Partida nueva(String modo, long fecha, Long idGanador) {
        Partida partida = new Partida(modo, 15, 3, 2, idGanador,
                "VICTORIA", "{\"version\":1,\"nombre\":\"Ñ\"}");
        partida.setFecha(fecha);
        return partida;
    }

    @Test
    public void insertarAsignaIdYConservaMetadatos() {
        long ganador = jugadores.insertar(new Jugador("Ana", 0xFF000001));
        Partida partida = nueva("501", 123456789L, ganador);
        long id = dao.insertar(partida);
        assertTrue(id > 0);
        assertEquals(id, partida.getId());
        Partida leida = dao.obtenerPorId(id);
        assertNotNull(leida);
        assertEquals(partida.getIdentificadorUnico(), leida.getIdentificadorUnico());
        assertEquals("501", leida.getModo());
        assertEquals(123456789L, leida.getFecha());
        assertEquals(15, leida.getRondasConfiguradas());
        assertEquals(3, leida.getRondasJugadas());
        assertEquals(2, leida.getNumeroJugadores());
        assertEquals(Long.valueOf(ganador), leida.getIdGanador());
        assertEquals(partida.getConfiguracionJson(), leida.getConfiguracionJson());
    }

    @Test
    public void ganadorPuedeSerNull() {
        Partida p = nueva("Shanghai", 2000L, null);
        assertNull(dao.obtenerPorId(dao.insertar(p)).getIdGanador());
    }

    @Test
    public void ordenRecienteFechaYId() {
        Partida antigua = nueva("301", 1000L, null);
        Partida mismo1 = nueva("501", 3000L, null);
        Partida mismo2 = nueva("Cricket", 3000L, null);
        dao.insertar(antigua);
        dao.insertar(mismo1);
        dao.insertar(mismo2);
        List<Partida> lista = dao.listarRecientes();
        assertEquals(3, lista.size());
        assertEquals(mismo2.getId(), lista.get(0).getId());
        assertEquals(mismo1.getId(), lista.get(1).getId());
        assertEquals(antigua.getId(), lista.get(2).getId());
    }

    @Test
    public void listarPorModoFiltra() {
        dao.insertar(nueva("301", 1000L, null));
        dao.insertar(nueva("501", 2000L, null));
        dao.insertar(nueva("301", 3000L, null));
        assertEquals(2, dao.listarPorModo("301").size());
        assertEquals(0, dao.listarPorModo("Cricket").size());
    }

    @Test
    public void localizarPorUuidYPorIdInexistentes() {
        Partida p = nueva("301", 1000L, null);
        dao.insertar(p);
        assertEquals(p.getId(), dao.obtenerPorIdentificadorUnico(p.getIdentificadorUnico()).getId());
        assertNull(dao.obtenerPorId(987654L));
        assertNull(dao.obtenerPorIdentificadorUnico("no-existe"));
    }

    @Test
    public void uuidRepetidoSeRechaza() {
        Partida a = nueva("301", 1000L, null);
        dao.insertar(a);
        Partida b = nueva("301", 1001L, null);
        b.setIdentificadorUnico(a.getIdentificadorUnico());
        try {
            dao.insertar(b);
            fail("Debería rechazarse el identificador duplicado");
        } catch (SQLiteConstraintException esperado) {
            assertEquals(0L, b.getId());
            assertEquals(1, dao.listarRecientes().size());
        }
    }

    @Test
    public void ganadorInexistenteViolaClaveForanea() {
        Partida p = nueva("501", 1000L, 999999L);
        try {
            dao.insertar(p);
            fail("FK id_ganador debe existir");
        } catch (SQLiteConstraintException esperado) {
            assertTrue(dao.listarRecientes().isEmpty());
        }
    }

    @Test
    public void noReinsertarObjetoConIdAsignado() {
        Partida p = nueva("501", 1000L, null);
        dao.insertar(p);
        try {
            dao.insertar(p);
            fail("No se debe reinsertar el mismo objeto");
        } catch (IllegalArgumentException esperado) {
            assertEquals(1, dao.listarRecientes().size());
        }
    }
}
