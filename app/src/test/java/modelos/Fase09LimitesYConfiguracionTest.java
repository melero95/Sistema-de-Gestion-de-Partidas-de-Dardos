package modelos;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import utilidades.SerializadorConfiguracionPartida;
import static org.junit.Assert.*;

/** F09: límites en el modelo, independientemente de la interfaz. */
public class Fase09LimitesYConfiguracionTest {
    private static final String[] GENERALES = {"301", "501", "Double Down", "Around the Clock", "Shanghai"};
    private static final String[] CRIQUET = {"Cricket", "Cut Throat Cricket"};
    private static List<ConfiguracionPartida.Participante> jugadores(int numero) {
        List<ConfiguracionPartida.Participante> lista = new ArrayList<>();
        for (int i = 1; i <= numero; i++)
            lista.add(new ConfiguracionPartida.Participante(i, "JUGADOR " + i, 0xFF112233 + i, i));
        return lista;
    }
    private static ConfiguracionPartida crear(String modo, int numero) {
        int rondas = "Double Down".equals(modo) ? 9 :
                ("Around the Clock".equals(modo) || "Shanghai".equals(modo) ? 21 : 15);
        return new ConfiguracionPartida(modo, rondas, 3, false, true, false, jugadores(numero));
    }
    private static void rechaza(String modo, int numero) {
        try { crear(modo, numero); fail(modo + " aceptó " + numero + " participantes"); }
        catch (IllegalArgumentException esperado) { /* validación correcta */ }
    }
    @Test public void cincoModalidadesAceptanUnoYSeis() {
        for (String modo : GENERALES) {
            assertEquals(1, crear(modo, 1).getParticipantes().size());
            assertEquals(6, crear(modo, 6).getParticipantes().size());
        }
    }
    @Test public void cincoModalidadesRechazanCeroYSiete() {
        for (String modo : GENERALES) { rechaza(modo, 0); rechaza(modo, 7); }
    }
    @Test public void criquetYCutThroatAdmitenDosTresYCuatro() {
        for (String modo : CRIQUET) for (int n = 2; n <= 4; n++)
            assertEquals(n, crear(modo, n).getParticipantes().size());
    }
    @Test public void criquetYCutThroatRechazanUnoCincoYSeis() {
        for (String modo : CRIQUET) for (int n : new int[]{1, 5, 6}) rechaza(modo, n);
    }
    @Test public void serializacionConservaCuatroJugadoresYOrden() {
        for (String modo : CRIQUET) {
            ConfiguracionPartida recuperada = SerializadorConfiguracionPartida.desdeJson(
                    SerializadorConfiguracionPartida.aJson(crear(modo, 4)));
            assertEquals(4, recuperada.getParticipantes().size());
            for (int i = 0; i < 4; i++) {
                assertEquals(i+1, recuperada.getParticipantes().get(i).getOrdenTurno());
                assertEquals(i+1, recuperada.getParticipantes().get(i).getIdJugador());
            }
        }
    }
    @Test public void jsonHistoricoConCincoJugadoresCriquetEsRechazado() {
        String json = SerializadorConfiguracionPartida.aJson(crear("501", 5));
        try {
            SerializadorConfiguracionPartida.desdeJson(json.replace("\"modo\":\"501\"", "\"modo\":\"Cricket\""));
            fail("La configuración histórica debe cumplir el nuevo máximo");
        } catch (IllegalArgumentException esperado) { }
    }
    @Test public void noPermiteIdDuplicadoNiOrdenNoCorrelativo() {
        List<ConfiguracionPartida.Participante> repetidos = jugadores(2);
        repetidos.set(1, new ConfiguracionPartida.Participante(1, "OTRO", 0, 2));
        try { new ConfiguracionPartida("301", 15, 3, false, false, false, repetidos); fail(); }
        catch (IllegalArgumentException esperado) { }
        List<ConfiguracionPartida.Participante> desorden = jugadores(2);
        desorden.set(1, new ConfiguracionPartida.Participante(2, "OTRO", 0, 3));
        try { new ConfiguracionPartida("301", 15, 3, false, false, false, desorden); fail(); }
        catch (IllegalArgumentException esperado) { }
    }
    @Test public void reglasDeRondasYNumeroDardosSeConservan() {
        try { new ConfiguracionPartida("Double Down", 10, 3, false, false, false, jugadores(2)); fail(); }
        catch (IllegalArgumentException esperado) { }
        try { new ConfiguracionPartida("Shanghai", 21, 5, false, false, false, jugadores(2)); fail(); }
        catch (IllegalArgumentException esperado) { }
    }
}
