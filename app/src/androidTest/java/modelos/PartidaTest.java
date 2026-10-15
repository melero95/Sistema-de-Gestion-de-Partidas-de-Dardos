package modelos;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/** Pruebas unitarias locales. Ejecutar con testDebugUnitTest, sin emulador. */
public class PartidaTest {

    @Test
    public void partidaNuevaTieneIdTemporalYUuid() {
        long antes = System.currentTimeMillis();
        Partida partida = new Partida();
        long despues = System.currentTimeMillis();

        assertEquals(0L, partida.getId());
        UUID.fromString(partida.getIdentificadorUnico());
        assertTrue(partida.getFecha() >= antes);
        assertTrue(partida.getFecha() <= despues);
    }

    @Test
    public void cadaPartidaTieneIdentificadorDistinto() {
        assertNotEquals(new Partida().getIdentificadorUnico(),
                new Partida().getIdentificadorUnico());
    }

    @Test
    public void almacenaTodosLosCamposDeUnaPartidaFinalizada() {
        Partida partida = new Partida("501", 15, 7, 3, 12L,
                "VICTORIA", "{\"version\":1}");
        partida.setId(9L);
        partida.setFecha(1792100000000L);

        assertEquals(9L, partida.getId());
        assertEquals("501", partida.getModo());
        assertEquals(15, partida.getRondasConfiguradas());
        assertEquals(7, partida.getRondasJugadas());
        assertEquals(3, partida.getNumeroJugadores());
        assertEquals(Long.valueOf(12), partida.getIdGanador());
        assertEquals("VICTORIA", partida.getMotivoFinalizacion());
        assertEquals("{\"version\":1}", partida.getConfiguracionJson());
        assertEquals(1792100000000L, partida.getFecha());
    }

    @Test
    public void permitePartidaSinGanadorYRecuperarUuid() {
        Partida partida = new Partida("Cricket", 0, 8, 2, null,
                "LIMITE_RONDAS", "{\"version\":1}");
        assertNull(partida.getIdGanador());
        partida.setIdentificadorUnico("uuid-persistido");
        assertEquals("uuid-persistido", partida.getIdentificadorUnico());
    }

    @Test
    public void rechazaNumeroDeJugadoresFueraDeRango() {
        Partida partida = new Partida();
        assertThrows(IllegalArgumentException.class, () -> partida.setNumeroJugadores(0));
        assertThrows(IllegalArgumentException.class, () -> partida.setNumeroJugadores(7));
        partida.setNumeroJugadores(1);
        partida.setNumeroJugadores(6);
    }

    @Test
    public void rechazaIdentificadoresYContadoresInvalidos() {
        Partida partida = new Partida();
        assertThrows(IllegalArgumentException.class, () -> partida.setId(-1));
        assertThrows(IllegalArgumentException.class, () -> partida.setFecha(0));
        assertThrows(IllegalArgumentException.class, () -> partida.setIdGanador(0L));
        assertThrows(IllegalArgumentException.class, () -> partida.setRondasConfiguradas(-1));
        assertThrows(IllegalArgumentException.class, () -> partida.setRondasJugadas(-1));
    }

    @Test
    public void rechazaTextosObligatoriosVacios() {
        Partida partida = new Partida();
        assertThrows(IllegalArgumentException.class, () -> partida.setIdentificadorUnico("  "));
        assertThrows(IllegalArgumentException.class, () -> partida.setModo(null));
        assertThrows(IllegalArgumentException.class, () -> partida.setMotivoFinalizacion("  "));
        assertThrows(IllegalArgumentException.class, () -> partida.setConfiguracionJson(""));
    }
}
