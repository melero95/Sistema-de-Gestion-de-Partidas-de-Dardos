package utilidades;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import modelos.ConfiguracionPartida;

import static org.junit.Assert.*;

/** Tests JVM (testDebugUnitTest): no requieren emulador ni SQLite. */
public class SerializadorConfiguracionPartidaTest {

    @Test
    public void conservaTodosLosDatosYElOrdenReal() {
        ConfiguracionPartida original = new ConfiguracionPartida(
                "501", 15, 4, true, true, true, Arrays.asList(
                        new ConfiguracionPartida.Participante(15L, "LUIS", 0xFFFF0000, 1),
                        new ConfiguracionPartida.Participante(9L, "ANA", 0xFF0000FF, 2)));
        String json = SerializadorConfiguracionPartida.aJson(original);
        ConfiguracionPartida recuperada = SerializadorConfiguracionPartida.desdeJson(json);

        assertTrue(json.contains("\"version\":1"));
        assertEquals("501", recuperada.getModo());
        assertEquals(15, recuperada.getRondas());
        assertEquals(4, recuperada.getDardosPorTurno());
        assertTrue(recuperada.isCierreDoble());
        assertTrue(recuperada.isMantenerMultiplicador());
        assertTrue(recuperada.isOrdenAleatorio());
        assertEquals(2, recuperada.getParticipantes().size());
        assertEquals(15L, recuperada.getParticipantes().get(0).getIdJugador());
        assertEquals(9L, recuperada.getParticipantes().get(1).getIdJugador());
        assertEquals("ANA", recuperada.getParticipantes().get(1).getNombre());
        assertEquals(0xFF0000FF, recuperada.getParticipantes().get(1).getColorArgb());
        assertEquals(2, recuperada.getParticipantes().get(1).getOrdenTurno());
    }

    @Test
    public void admiteSieteModalidades() {
        String[] modos = {"301", "501", "Cricket", "Cut Throat Cricket",
                "Double Down", "Around the Clock", "Shanghai"};
        for (String modo : modos) {
            int rondas = "Double Down".equals(modo) ? 9
                    : ("Around the Clock".equals(modo) || "Shanghai".equals(modo)) ? 21 : 15;
            ConfiguracionPartida p = new ConfiguracionPartida(modo, rondas, 3,
                    false, false, false, Arrays.asList(
                    new ConfiguracionPartida.Participante(1L, "A", 0xFF000000, 1),
                    new ConfiguracionPartida.Participante(2L, "B", 0xFFFFFFFF, 2)));
            assertEquals(modo, SerializadorConfiguracionPartida
                    .desdeJson(SerializadorConfiguracionPartida.aJson(p)).getModo());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rechazaVersionFutura() {
        String valido = SerializadorConfiguracionPartida.aJson(ejemplo());
        SerializadorConfiguracionPartida.desdeJson(
                valido.replace("\"version\":1", "\"version\":2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rechazaParticipantesRepetidos() {
        new ConfiguracionPartida("301", 15, 3, false, false, false, Arrays.asList(
                new ConfiguracionPartida.Participante(1L, "ANA", 1, 1),
                new ConfiguracionPartida.Participante(1L, "LUIS", 2, 2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rechazaCampoObligatorioAusente() {
        String json = SerializadorConfiguracionPartida.aJson(ejemplo());
        SerializadorConfiguracionPartida.desdeJson(
                json.replace("\"dardosPorTurno\":3,", ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rechazaFormatoInvalido() {
        SerializadorConfiguracionPartida.desdeJson("{json incorrecto}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rechazaUnJugadorEnCricket() {
        new ConfiguracionPartida("Cricket", 15, 3, false, false, false,
                Collections.singletonList(
                        new ConfiguracionPartida.Participante(1L, "ANA", 1, 1)));
    }

    private static ConfiguracionPartida ejemplo() {
        return new ConfiguracionPartida("301", 15, 3, false, false, false,
                Collections.singletonList(
                        new ConfiguracionPartida.Participante(1L, "ANA", 0xFFFF0000, 1)));
    }
}
