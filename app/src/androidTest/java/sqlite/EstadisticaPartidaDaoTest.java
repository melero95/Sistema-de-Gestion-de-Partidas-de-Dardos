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

import modelos.EstadisticaPartida;
import modelos.Jugador;
import modelos.Partida;
import modelos.ResultadoPartida;

@RunWith(AndroidJUnit4.class)
public class EstadisticaPartidaDaoTest {

    private static final String BD = "pruebas_estadistica_dao.db";
    private Context contexto;
    private BaseDatosHelper helper;
    private EstadisticaPartidaDao dao;
    private ResultadoPartidaDao resultados;
    private long partida;
    private long ana;
    private long luis;
    private long primerResultado;
    private long segundoResultado;

    @Before
    public void preparar() {
        contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();
        contexto.deleteDatabase(BD);
        helper = new BaseDatosHelper(contexto, BD);
        dao = new EstadisticaPartidaDao(helper);
        resultados = new ResultadoPartidaDao(helper);
        JugadorDao jugadores = new JugadorDao(helper);
        ana = jugadores.insertar(new Jugador("Ana", 0xFF112233));
        luis = jugadores.insertar(new Jugador("Luis", 0xFF445566));
        partida = new PartidaDao(helper).insertar(new Partida(
                "Cricket", 15, 9, 2, ana, "VICTORIA", "{\"version\":1}"));
        // Luis comenzó primero, pero Ana ganó.
        segundoResultado = resultados.insertar(new ResultadoPartida(partida, luis, 2, 12, 1));
        primerResultado = resultados.insertar(new ResultadoPartida(partida, ana, 1, 25, 2));
    }

    @After
    public void limpiar() {
        if (helper != null) helper.close();
        if (contexto != null) contexto.deleteDatabase(BD);
    }

    private EstadisticaPartida nueva(long idResultado) {
        EstadisticaPartida e = new EstadisticaPartida(idResultado);
        e.setDardosLanzados(27);
        e.setTurnosJugados(9);
        e.setSimples(10);
        e.setDobles(4);
        e.setTriples(2);
        e.setFallos(11);
        return e;
    }

    @Test
    public void insertarYRecuperarContadoresComunes() {
        EstadisticaPartida e = nueva(primerResultado);
        long id = dao.insertar(e);
        assertTrue(id > 0);
        assertEquals(id, e.getId());
        EstadisticaPartida leida = dao.obtenerPorId(id);
        assertEquals(primerResultado, leida.getIdResultado());
        assertEquals(27, leida.getDardosLanzados());
        assertEquals(9, leida.getTurnosJugados());
        assertEquals(10, leida.getSimples());
        assertEquals(4, leida.getDobles());
        assertEquals(2, leida.getTriples());
        assertEquals(11, leida.getFallos());
    }

    @Test
    public void nullYceroNoSonLoMismo() {
        EstadisticaPartida e = nueva(primerResultado);
        e.setBusts(null);
        e.setMarcasValidas(0);
        dao.insertar(e);
        EstadisticaPartida leida = dao.obtenerPorResultado(primerResultado);
        assertNull(leida.getBusts());
        assertEquals(Integer.valueOf(0), leida.getMarcasValidas());
        assertNull(leida.getPartidaCompletada());
    }

    @Test
    public void modoPuntosPreservaCampos() {
        EstadisticaPartida e = nueva(primerResultado);
        e.setBusts(2);
        e.setPuntosValidos(180);
        e.setMejorTurno(90);
        e.setTurnos180(0);
        e.setDardosCierre(2);
        dao.insertar(e);
        EstadisticaPartida leida = dao.obtenerPorResultado(primerResultado);
        assertEquals(Integer.valueOf(2), leida.getBusts());
        assertEquals(Integer.valueOf(180), leida.getPuntosValidos());
        assertEquals(Integer.valueOf(90), leida.getMejorTurno());
        assertEquals(Integer.valueOf(0), leida.getTurnos180());
        assertEquals(Integer.valueOf(2), leida.getDardosCierre());
    }

    @Test
    public void modoCricketPreservaCampos() {
        EstadisticaPartida e = nueva(primerResultado);
        e.setMarcasValidas(18);
        e.setObjetivosCerrados(6);
        e.setPuntosGenerados(85);
        e.setPuntosRecibidos(12);
        dao.insertar(e);
        EstadisticaPartida leida = dao.obtenerPorResultado(primerResultado);
        assertEquals(Integer.valueOf(18), leida.getMarcasValidas());
        assertEquals(Integer.valueOf(6), leida.getObjetivosCerrados());
        assertEquals(Integer.valueOf(85), leida.getPuntosGenerados());
        assertEquals(Integer.valueOf(12), leida.getPuntosRecibidos());
    }

    @Test
    public void modoDoubleDownPreservaCampos() {
        EstadisticaPartida e = nueva(primerResultado);
        e.setRondasPenalizadas(2);
        e.setMejorRondaDd(150);
        e.setPuntosConseguidosDd(300);
        dao.insertar(e);
        EstadisticaPartida leida = dao.obtenerPorResultado(primerResultado);
        assertEquals(Integer.valueOf(2), leida.getRondasPenalizadas());
        assertEquals(Integer.valueOf(150), leida.getMejorRondaDd());
        assertEquals(Integer.valueOf(300), leida.getPuntosConseguidosDd());
    }

    @Test
    public void modoAroundTheClockPreservaCamposYBooleanFalse() {
        EstadisticaPartida e = nueva(primerResultado);
        e.setObjetivosSuperados(15);
        e.setAciertosObjetivo(10);
        e.setPartidaCompletada(false);
        dao.insertar(e);
        EstadisticaPartida leida = dao.obtenerPorResultado(primerResultado);
        assertEquals(Integer.valueOf(15), leida.getObjetivosSuperados());
        assertEquals(Integer.valueOf(10), leida.getAciertosObjetivo());
        assertEquals(Boolean.FALSE, leida.getPartidaCompletada());
    }

    @Test
    public void modoAroundTheClockPermiteBooleanTrue() {
        EstadisticaPartida e = nueva(primerResultado);
        e.setPartidaCompletada(true);
        dao.insertar(e);
        assertEquals(Boolean.TRUE, dao.obtenerPorResultado(primerResultado).getPartidaCompletada());
    }

    @Test
    public void modoShanghaiPreservaCampos() {
        EstadisticaPartida e = nueva(primerResultado);
        e.setPuntosValidosShanghai(75);
        e.setMejorRondaShanghai(45);
        e.setRondasConPuntos(3);
        dao.insertar(e);
        EstadisticaPartida leida = dao.obtenerPorResultado(primerResultado);
        assertEquals(Integer.valueOf(75), leida.getPuntosValidosShanghai());
        assertEquals(Integer.valueOf(45), leida.getMejorRondaShanghai());
        assertEquals(Integer.valueOf(3), leida.getRondasConPuntos());
    }

    @Test
    public void listarPorPartidaSigueClasificacion() {
        dao.insertar(nueva(segundoResultado));
        dao.insertar(nueva(primerResultado));
        List<EstadisticaPartida> lista = dao.listarPorPartida(partida);
        assertEquals(2, lista.size());
        assertEquals(primerResultado, lista.get(0).getIdResultado());
        assertEquals(segundoResultado, lista.get(1).getIdResultado());
    }

    @Test
    public void estadisticaDuplicadaParaUnResultadoSeRechaza() {
        dao.insertar(nueva(primerResultado));
        try {
            dao.insertar(nueva(primerResultado));
            fail("Solo puede existir una estadística por resultado");
        } catch (SQLiteConstraintException esperado) {
            assertEquals(1, dao.listarPorPartida(partida).size());
        }
    }

    @Test
    public void resultadoInexistenteViolaClaveForanea() {
        try {
            dao.insertar(nueva(987654L));
            fail("El resultado debe existir");
        } catch (SQLiteConstraintException esperado) {
            assertTrue(dao.listarPorPartida(partida).isEmpty());
        }
    }

    @Test
    public void consultaInexistenteEsNull() {
        assertNull(dao.obtenerPorId(99999L));
        assertNull(dao.obtenerPorResultado(99999L));
        assertTrue(dao.listarPorPartida(99999L).isEmpty());
    }
}
