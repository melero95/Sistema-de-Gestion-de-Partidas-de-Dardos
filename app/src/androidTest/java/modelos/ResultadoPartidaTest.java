package modelos;

import org.junit.Test;

import static org.junit.Assert.*;

/** Pruebas unitarias Java; no necesitan dispositivo Android. */
public class ResultadoPartidaTest {

    @Test
    public void resultadoNuevoTieneIdSinGuardar() {
        ResultadoPartida resultado = new ResultadoPartida();
        assertEquals(0L, resultado.getId());
    }

    @Test
    public void guardaLosCamposDeUnParticipante() {
        ResultadoPartida resultado = new ResultadoPartida(9L, 14L, 2, 120, 1);
        resultado.setId(23L);

        assertEquals(23L, resultado.getId());
        assertEquals(9L, resultado.getIdPartida());
        assertEquals(14L, resultado.getIdJugador());
        assertEquals(2, resultado.getPosicion());
        assertEquals(Integer.valueOf(120), resultado.getPuntuacionFinal());
        assertEquals(1, resultado.getOrdenTurno());
    }

    @Test
    public void permitePuntuacionFinalNula() {
        ResultadoPartida resultado = new ResultadoPartida(3L, 7L, 1, null, 1);
        assertNull(resultado.getPuntuacionFinal());
        resultado.setPuntuacionFinal(0);
        assertEquals(Integer.valueOf(0), resultado.getPuntuacionFinal());
        resultado.setPuntuacionFinal(null);
        assertNull(resultado.getPuntuacionFinal());
    }

    @Test
    public void distingueClasificacionDeOrdenDeTurno() {
        ResultadoPartida resultado = new ResultadoPartida(3L, 7L, 1, 45, 4);
        assertEquals(1, resultado.getPosicion());
        assertEquals(4, resultado.getOrdenTurno());
    }

    @Test
    public void permitePrimeraYUltimaPosicion() {
        ResultadoPartida resultado = new ResultadoPartida(3L, 7L, 1, 0, 6);
        resultado.setPosicion(6);
        resultado.setOrdenTurno(1);
        assertEquals(6, resultado.getPosicion());
        assertEquals(1, resultado.getOrdenTurno());
    }

    @Test
    public void rechazaIdentificadoresIncorrectos() {
        ResultadoPartida resultado = new ResultadoPartida();
        assertThrows(IllegalArgumentException.class, () -> resultado.setId(-1));
        assertThrows(IllegalArgumentException.class, () -> resultado.setIdPartida(0));
        assertThrows(IllegalArgumentException.class, () -> resultado.setIdPartida(-5));
        assertThrows(IllegalArgumentException.class, () -> resultado.setIdJugador(0));
    }

    @Test
    public void rechazaPosicionesYTurnosFueraDeRango() {
        ResultadoPartida resultado = new ResultadoPartida();
        assertThrows(IllegalArgumentException.class, () -> resultado.setPosicion(0));
        assertThrows(IllegalArgumentException.class, () -> resultado.setPosicion(7));
        assertThrows(IllegalArgumentException.class, () -> resultado.setOrdenTurno(0));
        assertThrows(IllegalArgumentException.class, () -> resultado.setOrdenTurno(7));
    }
}
