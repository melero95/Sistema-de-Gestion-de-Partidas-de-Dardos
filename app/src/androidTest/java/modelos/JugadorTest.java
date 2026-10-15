package modelos;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JugadorTest {

    @Test
    public void jugadorNuevoNormalizaNombreYEstaActivo() {
        int colorArgb = 0xFF2B41C5;
        Jugador jugador = new Jugador("  álvaro    pérez  ", colorArgb);

        assertEquals(0L, jugador.getId());
        assertEquals("ÁLVARO PÉREZ", jugador.getNombre());
        assertEquals(colorArgb, jugador.getColor());
        assertTrue(jugador.isActivo());
    }

    @Test
    public void jugadorExistentePuedeEstarInactivo() {
        Jugador jugador = new Jugador(12L, "Ana", 0xFF445566, false);

        assertEquals(12L, jugador.getId());
        assertEquals("ANA", jugador.toString());
        assertFalse(jugador.isActivo());
    }

    @Test(expected = IllegalArgumentException.class)
    public void noAdmiteNombreEnBlanco() {
        new Jugador("   ", 0xFF000000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noAdmiteIdNegativo() {
        new Jugador(-1L, "Ana", 0xFF000000, true);
    }
}
