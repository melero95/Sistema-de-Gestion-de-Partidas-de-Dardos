package modelos;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/** La fase 05 conserva contadores también dentro de las instantáneas de Deshacer. */
public class EstadosEstadisticasGuardadasTest {
    private static final Gson gson = new Gson();

    private static ArrayList<EstadisticasJugadorEnCurso> dos(String modo) {
        return new ArrayList<>(Arrays.asList(
                new EstadisticasJugadorEnCurso(modo),
                new EstadisticasJugadorEnCurso(modo)));
    }

    @Test public void puntosGuardaCopiaIndependiente() {
        EstadoPartidaPuntos estado = new EstadoPartidaPuntos();
        ArrayList<EstadisticasJugadorEnCurso> datos = dos("301");
        datos.get(0).registrarDardo(2);
        estado.setEstadisticasJugadores(datos);
        datos.get(0).registrarDardo(0);
        assertEquals(1, estado.getEstadisticasJugadores().get(0).getDardosLanzados());
    }

    @Test public void cricketRestauraEstadisticasEnJson() {
        EstadoPartidaCriquet estado = new EstadoPartidaCriquet();
        ArrayList<EstadisticasJugadorEnCurso> datos = dos("Cricket");
        datos.get(0).registrarCricket(2, 0, 0, 0);
        estado.setEstadisticasJugadores(datos);
        EstadoPartidaCriquet.EstadoDeshacerCriquet paso = new EstadoPartidaCriquet.EstadoDeshacerCriquet();
        paso.setEstadisticasJugadores(datos);
        estado.setHistorialEstados(new ArrayList<>(Arrays.asList(paso)));
        EstadoPartidaCriquet restaurado = gson.fromJson(gson.toJson(estado), EstadoPartidaCriquet.class);
        assertEquals(Integer.valueOf(2), restaurado.getEstadisticasJugadores().get(0)
                .convertirEnEstadisticaFinal(1).getMarcasValidas());
        assertEquals(2, restaurado.getHistorialEstados().get(0).getEstadisticasJugadores().size());
    }

    @Test public void rondasConservaSnapshotPorParticipante() {
        EstadoPartidaRondas estado = new EstadoPartidaRondas();
        ArrayList<EstadisticasJugadorEnCurso> datos = dos("Shanghai");
        datos.get(1).registrarRondaShanghai(45);
        EstadoPartidaRondas.EstadoDeshacerRondas anterior = new EstadoPartidaRondas.EstadoDeshacerRondas();
        anterior.setEstadisticasJugadores(datos);
        estado.setHistorialEstados(new ArrayList<>(Arrays.asList(anterior)));
        datos.get(1).registrarRondaShanghai(15);
        EstadoPartidaRondas restaurado = gson.fromJson(gson.toJson(estado), EstadoPartidaRondas.class);
        assertEquals(Integer.valueOf(45), restaurado.getHistorialEstados().get(0)
                .getEstadisticasJugadores().get(1)
                .convertirEnEstadisticaFinal(1).getPuntosValidosShanghai());
    }

    @Test public void copiarListaNoComparteElementos() {
        ArrayList<EstadisticasJugadorEnCurso> datos = dos("501");
        ArrayList<EstadisticasJugadorEnCurso> copia = EstadisticasJugadorEnCurso.copiarLista(datos);
        datos.get(1).registrarDardo(3);
        assertEquals(0, copia.get(1).getDardosLanzados());
        assertNotSame(datos.get(1), copia.get(1));
    }
}
