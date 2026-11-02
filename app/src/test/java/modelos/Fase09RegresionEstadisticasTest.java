package modelos;

import org.junit.Test;
import java.util.Arrays;
import java.util.ArrayList;
import static org.junit.Assert.*;

/** F09: regresión de contadores y snapshots de las siete modalidades. */
public class Fase09RegresionEstadisticasTest {
    @Test public void losSieteModosInicianConContadoresIndependientes() {
        String[] modos = {"301", "501", "Cricket", "Cut Throat Cricket", "Double Down", "Around the Clock", "Shanghai"};
        for (String modo : modos) {
            EstadisticasJugadorEnCurso a = new EstadisticasJugadorEnCurso(modo);
            EstadisticasJugadorEnCurso b = new EstadisticasJugadorEnCurso(modo);
            a.registrarDardo(1);
            assertEquals(modo, 1, a.getDardosLanzados());
            assertEquals(modo, 0, b.getDardosLanzados());
            assertEquals(modo, 1, a.convertirEnEstadisticaFinal(0).getDardosLanzados());
        }
    }
    @Test public void puntosBustNoBorraLanzamientos() {
        for (String modo : new String[]{"301", "501"}) {
            EstadisticasJugadorEnCurso e = new EstadisticasJugadorEnCurso(modo);
            e.registrarDardo(3); e.registrarDardo(0); e.registrarBust();
            e.registrarTurnoPuntos(0, false);
            assertEquals(2, e.getDardosLanzados());
            assertEquals(1, e.getBusts());
            assertEquals(0, e.getPuntosValidos());
        }
    }
    @Test public void cricketYCutThroatConservanMetricasSeparadas() {
        for (String modo : new String[]{"Cricket", "Cut Throat Cricket"}) {
            EstadisticasJugadorEnCurso e = new EstadisticasJugadorEnCurso(modo);
            e.registrarCricket(3, 1, 20, 10);
            assertEquals(Integer.valueOf(3), e.convertirEnEstadisticaFinal(0).getMarcasValidas());
            assertEquals(Integer.valueOf(20), e.convertirEnEstadisticaFinal(0).getPuntosGenerados());
            assertNull(e.convertirEnEstadisticaFinal(0).getBusts());
        }
    }
    @Test public void snapshotDeCuatroJugadoresNoSeModificaConNuevosDardos() {
        ArrayList<EstadisticasJugadorEnCurso> datos = new ArrayList<>();
        for (int i = 0; i < 4; i++) datos.add(new EstadisticasJugadorEnCurso("Cricket"));
        datos.get(3).registrarDardo(2);
        ArrayList<EstadisticasJugadorEnCurso> anterior = EstadisticasJugadorEnCurso.copiarLista(datos);
        datos.get(3).registrarDardo(3);
        assertEquals(1, anterior.get(3).getDardosLanzados());
        assertEquals(2, datos.get(3).getDardosLanzados());
        assertNotSame(datos.get(3), anterior.get(3));
        assertEquals(0, anterior.get(0).getDardosLanzados());
    }
    @Test public void estadisticasEspecificasRondasNoSeMezclan() {
        EstadisticasJugadorEnCurso dd = new EstadisticasJugadorEnCurso("Double Down");
        dd.registrarRondaDoubleDown(0, true);
        assertEquals(Integer.valueOf(1), dd.convertirEnEstadisticaFinal(0).getRondasPenalizadas());
        EstadisticasJugadorEnCurso clock = new EstadisticasJugadorEnCurso("Around the Clock");
        clock.registrarAciertoClock(3);
        assertEquals(Integer.valueOf(3), clock.convertirEnEstadisticaFinal(0).getObjetivosSuperados());
        EstadisticasJugadorEnCurso shanghai = new EstadisticasJugadorEnCurso("Shanghai");
        shanghai.registrarRondaShanghai(0); shanghai.registrarRondaShanghai(27);
        assertEquals(Integer.valueOf(1), shanghai.convertirEnEstadisticaFinal(0).getRondasConPuntos());
        assertNull(shanghai.convertirEnEstadisticaFinal(0).getRondasPenalizadas());
    }
}
