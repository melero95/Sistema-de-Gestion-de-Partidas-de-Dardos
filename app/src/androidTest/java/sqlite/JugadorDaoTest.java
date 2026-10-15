package sqlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

/** Pruebas instrumentales con una BD independiente de la aplicación. */
@RunWith(AndroidJUnit4.class)
public class JugadorDaoTest {

    private static final String NOMBRE_BD_PRUEBAS = "gestor_dardos_test_jugador_dao.db";

    private Context contexto;
    private JugadorDao dao;

    @Before
    public void preparar() {
        contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();
        contexto.deleteDatabase(NOMBRE_BD_PRUEBAS);
        BaseDatosHelper helper = new BaseDatosHelper(contexto, NOMBRE_BD_PRUEBAS);
        dao = new JugadorDao(helper);
    }

    @After
    public void limpiar() {
        if (dao != null) {
            dao.close();
        }
        if (contexto != null) {
            contexto.deleteDatabase(NOMBRE_BD_PRUEBAS);
        }
    }

    @Test
    public void insertarAsignaIdYPermiteRecuperarDatos() {
        Jugador jugador = new Jugador("  álvaro   pérez  ", 0xFF2B41C5);

        long id = dao.insertar(jugador);

        assertTrue(id > 0L);
        assertEquals(id, jugador.getId());
        Jugador recuperado = dao.obtenerPorId(id);
        assertNotNull(recuperado);
        assertEquals("ÁLVARO PÉREZ", recuperado.getNombre());
        assertEquals(0xFF2B41C5, recuperado.getColor());
        assertTrue(recuperado.isActivo());
    }

    @Test
    public void noPermiteNombresDuplicadosIgnorandoMayusculas() {
        dao.insertar(new Jugador("Ana", 0xFF112233));
        Jugador repetido = new Jugador("  aNa  ", 0xFF445566);

        try {
            dao.insertar(repetido);
            fail("SQLite debería rechazar un nombre duplicado");
        } catch (SQLiteConstraintException esperada) {
            assertEquals(0L, repetido.getId());
            assertEquals(1, dao.listarTodos().size());
        }
    }

    @Test
    public void listarTodosOrdenaPorNombreEIncluyeInactivos() {
        dao.insertar(new Jugador("Zoe", 0xFF000001));
        dao.insertar(new Jugador("Carlos", 0xFF000002));
        Jugador ana = new Jugador("Ana", 0xFF000003);
        ana.setActivo(false);
        dao.insertar(ana);

        List<Jugador> todos = dao.listarTodos();
        assertEquals(3, todos.size());
        assertEquals("ANA", todos.get(0).getNombre());
        assertFalse(todos.get(0).isActivo());
        assertEquals("CARLOS", todos.get(1).getNombre());
        assertEquals("ZOE", todos.get(2).getNombre());
    }

    @Test
    public void listarActivosOmiteInactivosPeroLosConservaEnBaseDatos() {
        Jugador ana = new Jugador("Ana", 0xFF123456);
        dao.insertar(ana);
        Jugador luis = new Jugador("Luis", 0xFF654321);
        dao.insertar(luis);

        assertTrue(dao.cambiarActivo(ana.getId(), false));

        assertEquals(1, dao.listarActivos().size());
        assertEquals("LUIS", dao.listarActivos().get(0).getNombre());
        assertEquals(2, dao.listarTodos().size());
        assertNotNull(dao.obtenerPorId(ana.getId()));
    }

    @Test
    public void actualizarModificaNombreColorYEstadoSinCambiarId() {
        Jugador jugador = new Jugador("Ana", 0xFF123456);
        long id = dao.insertar(jugador);
        jugador.setNombre("  María  López ");
        jugador.setColor(0xFF334455);
        jugador.setActivo(false);

        assertTrue(dao.actualizar(jugador));

        Jugador guardado = dao.obtenerPorId(id);
        assertNotNull(guardado);
        assertEquals(id, guardado.getId());
        assertEquals("MARÍA LÓPEZ", guardado.getNombre());
        assertEquals(0xFF334455, guardado.getColor());
        assertFalse(guardado.isActivo());
    }

    @Test
    public void actualizarNoPermiteRenombrarAlNombreDeOtroJugador() {
        dao.insertar(new Jugador("Ana", 0xFF010101));
        Jugador luis = new Jugador("Luis", 0xFF020202);
        dao.insertar(luis);
        luis.setNombre("ana");

        try {
            dao.actualizar(luis);
            fail("La unicidad de nombres debería impedir la edición");
        } catch (SQLiteConstraintException esperada) {
            assertEquals("LUIS", dao.obtenerPorId(luis.getId()).getNombre());
        }
    }

    @Test
    public void cambiarActivoPermiteReactivarConservandoIdentidad() {
        Jugador jugador = new Jugador("Ana", 0xFF123456);
        long id = dao.insertar(jugador);

        assertTrue(dao.cambiarActivo(id, false));
        assertFalse(dao.obtenerPorId(id).isActivo());
        assertTrue(dao.cambiarActivo(id, true));
        assertTrue(dao.obtenerPorId(id).isActivo());
        assertEquals(id, dao.obtenerPorId(id).getId());
    }

    @Test
    public void buscarPorNombreNormalizaEspaciosYMayusculas() {
        Jugador jugador = new Jugador("Ana María", 0xFF556677);
        long id = dao.insertar(jugador);

        assertEquals(id, dao.obtenerPorNombre("  ana   maría  ").getId());
        assertNull(dao.obtenerPorNombre("Otro"));
    }

    @Test
    public void operacionesSobreIdInexistenteNoInventanRegistros() {
        assertNull(dao.obtenerPorId(50L));
        assertFalse(dao.cambiarActivo(50L, false));
        assertFalse(dao.actualizar(new Jugador(50L, "Ana", 0xFF112233, true)));
        assertTrue(dao.listarTodos().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void insertarNoAceptaJugadorConIdExistente() {
        dao.insertar(new Jugador(25L, "Ana", 0xFF112233, true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void obtenerPorIdNoAceptaCero() {
        dao.obtenerPorId(0L);
    }
}
