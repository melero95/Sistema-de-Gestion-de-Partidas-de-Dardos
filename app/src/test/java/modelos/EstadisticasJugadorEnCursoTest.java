package modelos;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/** Pruebas JVM: no requieren emulador ni manipulan SQLite o SharedPreferences. */
public class EstadisticasJugadorEnCursoTest {

    @Test
    public void iniciaContadoresEnCeroYNoPersistidos() {
        EstadisticasJugadorEnCurso actual = new EstadisticasJugadorEnCurso("301");
        assertEquals(0, actual.getDardosLanzados());
        assertEquals(0, actual.getBusts());
        assertEquals(0, actual.getTurnosJugados());
        assertNull(actual.convertirEnEstadisticaFinal(5).getMarcasValidas());
        assertEquals(Integer.valueOf(0), actual.convertirEnEstadisticaFinal(5).getBusts());
    }

    @Test
    public void cuentaDardosYTurnosSinLanzamientos() {
        EstadisticasJugadorEnCurso actual = new EstadisticasJugadorEnCurso("501");
        actual.registrarDardo(1);
        actual.registrarDardo(2);
        actual.registrarDardo(3);
        actual.registrarDardo(0);
        actual.registrarTurno();
        actual.registrarTurno(); // turno pasado, sin dardos
        assertEquals(4, actual.getDardosLanzados());
        assertEquals(2, actual.getTurnosJugados());
        assertEquals(1, actual.getSimples());
        assertEquals(1, actual.getDobles());
        assertEquals(1, actual.getTriples());
        assertEquals(1, actual.getFallos());
    }

    @Test
    public void bustMantieneDardosPeroNoSumaPuntosAnulados() {
        EstadisticasJugadorEnCurso actual = new EstadisticasJugadorEnCurso("301");
        actual.registrarDardo(3);
        actual.registrarDardo(2);
        actual.registrarBust();
        actual.registrarTurnoPuntos(0, false);
        assertEquals(2, actual.getDardosLanzados());
        assertEquals(1, actual.getBusts());
        assertEquals(0, actual.getPuntosValidos());
    }

    @Test
    public void puntuacionMejorTurno180YCierre() {
        EstadisticasJugadorEnCurso actual = new EstadisticasJugadorEnCurso("501");
        actual.registrarTurnoPuntos(60, false);
        actual.registrarTurnoPuntos(180, true);
        actual.registrarTurnoPuntos(21, false);
        actual.registrarCierre(2);
        EstadisticaPartida finalizada = actual.convertirEnEstadisticaFinal(2);
        assertEquals(Integer.valueOf(261), finalizada.getPuntosValidos());
        assertEquals(Integer.valueOf(180), finalizada.getMejorTurno());
        assertEquals(Integer.valueOf(1), finalizada.getTurnos180());
        assertEquals(Integer.valueOf(2), finalizada.getDardosCierre());
        assertNull(finalizada.getRondasPenalizadas());
    }

    @Test
    public void cricketSoloExportaSusPropiasMetricas() {
        EstadisticasJugadorEnCurso actual = new EstadisticasJugadorEnCurso("Cut Throat Cricket");
        actual.registrarCricket(3, 1, 0, 40);
        actual.registrarCricket(2, 1, 20, 0);
        EstadisticaPartida finalizada = actual.convertirEnEstadisticaFinal(1);
        assertEquals(Integer.valueOf(5), finalizada.getMarcasValidas());
        assertEquals(Integer.valueOf(2), finalizada.getObjetivosCerrados());
        assertEquals(Integer.valueOf(20), finalizada.getPuntosGenerados());
        assertEquals(Integer.valueOf(40), finalizada.getPuntosRecibidos());
        assertNull(finalizada.getBusts());
    }

    @Test
    public void doubleDownCalculaMejorRondaYPenalizaciones() {
        EstadisticasJugadorEnCurso actual = new EstadisticasJugadorEnCurso("Double Down");
        actual.registrarRondaDoubleDown(30, false);
        actual.registrarRondaDoubleDown(0, true);
        actual.registrarRondaDoubleDown(10, false);
        EstadisticaPartida finalizada = actual.convertirEnEstadisticaFinal(1);
        assertEquals(Integer.valueOf(1), finalizada.getRondasPenalizadas());
        assertEquals(Integer.valueOf(30), finalizada.getMejorRondaDd());
        assertEquals(Integer.valueOf(40), finalizada.getPuntosConseguidosDd());
        assertNull(finalizada.getRondasConPuntos());
    }

    @Test
    public void clockCuentaAciertoDobleComoDosObjetivos() {
        EstadisticasJugadorEnCurso actual = new EstadisticasJugadorEnCurso("Around the Clock");
        actual.registrarDardo(2);
        actual.registrarAciertoClock(2);
        actual.marcarClockCompletado();
        EstadisticaPartida finalizada = actual.convertirEnEstadisticaFinal(1);
        assertEquals(Integer.valueOf(2), finalizada.getObjetivosSuperados());
        assertEquals(Integer.valueOf(1), finalizada.getAciertosObjetivo());
        assertEquals(Boolean.TRUE, finalizada.getPartidaCompletada());
        assertNull(finalizada.getPuntosValidosShanghai());
    }

    @Test
    public void shanghaiCuentaRondasConYSinPuntos() {
        EstadisticasJugadorEnCurso actual = new EstadisticasJugadorEnCurso("Shanghai");
        actual.registrarRondaShanghai(0);
        actual.registrarRondaShanghai(18);
        actual.registrarRondaShanghai(12);
        EstadisticaPartida finalizada = actual.convertirEnEstadisticaFinal(1);
        assertEquals(Integer.valueOf(30), finalizada.getPuntosValidosShanghai());
        assertEquals(Integer.valueOf(18), finalizada.getMejorRondaShanghai());
        assertEquals(Integer.valueOf(2), finalizada.getRondasConPuntos());
        assertNull(finalizada.getPartidaCompletada());
    }

    @Test
    public void snapshotYCopiaDeListaSonIndependientes() {
        EstadisticasJugadorEnCurso original = new EstadisticasJugadorEnCurso("301");
        original.registrarDardo(1);
        EstadisticasJugadorEnCurso copia = original.copiar();
        ArrayList<EstadisticasJugadorEnCurso> lista =
                EstadisticasJugadorEnCurso.copiarLista(Arrays.asList(original));
        original.registrarDardo(2);
        assertEquals(1, copia.getDardosLanzados());
        assertEquals(1, lista.get(0).getDardosLanzados());
        assertNotSame(original, lista.get(0));
        assertEquals(2, original.getDardosLanzados());
    }

    @Test
    public void impideTipoModoYValoresIncorrectos() {
        assertLanza(IllegalArgumentException.class,
                () -> new EstadisticasJugadorEnCurso("Modalidad inventada"));
        EstadisticasJugadorEnCurso puntos = new EstadisticasJugadorEnCurso("301");
        assertLanza(IllegalArgumentException.class, () -> puntos.registrarDardo(4));
        assertLanza(IllegalArgumentException.class, () -> puntos.registrarTurnoPuntos(-1, false));
        assertLanza(IllegalArgumentException.class, () -> puntos.registrarTurnoPuntos(60, true));
        assertLanza(IllegalStateException.class, () -> puntos.registrarRondaShanghai(30));
        assertLanza(IllegalArgumentException.class, () -> puntos.convertirEnEstadisticaFinal(0));
    }

    private static void assertLanza(Class<? extends Throwable> esperado, Runnable accion) {
        try {
            accion.run();
            fail("Se esperaba una excepción " + esperado.getSimpleName());
        } catch (Throwable ex) {
            if (ex instanceof AssertionError) throw (AssertionError) ex;
            assertTrue("Excepción inesperada: " + ex, esperado.isInstance(ex));
        }
    }
}
