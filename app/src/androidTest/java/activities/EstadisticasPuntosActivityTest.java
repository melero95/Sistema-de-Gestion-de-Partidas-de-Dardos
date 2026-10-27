package activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.productos.juegosdedardos.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import modelos.EstadisticasJugadorEnCurso;
import modelos.PartidaEnCurso;
import preferencias.GestorPartidaEnCurso;

import static org.junit.Assert.*;

/**
 * Pruebas de la fase 05 sobre el motor real 301/501.
 * Guardan y restauran los SharedPreferences usados para continuar partidas,
 * evitando dejar el estado de prueba como partida pendiente del usuario.
 */
@RunWith(AndroidJUnit4.class)
public class EstadisticasPuntosActivityTest {

    private static final String PREFS = "preferencias_partida_en_curso";
    private static final String CLAVE = "partida_guardada";
    private static final String PREF_CONFIG = "configuracion_dardos_partida";
    private static final String CLAVE_MULTIPLICADOR = "mantener_multiplicador_puntos";

    private static Intent nuevaPartida(Context context) {
        Intent intent = new Intent(context, PartidaPuntosActivity.class);
        intent.putExtra("modoJuego", "301");
        intent.putExtra("maxRondas", 15);
        intent.putStringArrayListExtra("nombresJugadores",
                new ArrayList<>(Arrays.asList("ANA", "LUIS")));
        intent.putExtra("colorJugador1", R.color.jugador_rojo);
        intent.putExtra("colorJugador2", R.color.jugador_azul);
        intent.putExtra(ConfigurarNuevaPartidaActivity.EXTRA_NUMERO_DARDOS, 3);
        return intent;
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<EstadisticasJugadorEnCurso> estadisticas(PartidaPuntosActivity activity) {
        try {
            Field campo = PartidaPuntosActivity.class.getDeclaredField("estadisticasJugadores");
            campo.setAccessible(true);
            return (ArrayList<EstadisticasJugadorEnCurso>) campo.get(activity);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("No se pudo leer el estado de estadísticas", ex);
        }
    }

    private static void invocar(PartidaPuntosActivity activity, String nombre,
                                Class<?>[] tipos, Object... argumentos) {
        try {
            Method metodo = PartidaPuntosActivity.class.getDeclaredMethod(nombre, tipos);
            metodo.setAccessible(true);
            metodo.invoke(activity, argumentos);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Falló " + nombre, ex);
        }
    }

    private static void lanzarSimple20(PartidaPuntosActivity activity) {
        invocar(activity, "registrarResultadoDardo",
                new Class<?>[]{int.class, String.class}, 20, "20");
    }

    @Test
    public void registraDardoYDeshacerRecuperaTodosLosContadores() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences config = context.getSharedPreferences(PREF_CONFIG, Context.MODE_PRIVATE);
        boolean habiaPartida = prefs.contains(CLAVE);
        String partidaOriginal = prefs.getString(CLAVE, null);
        boolean habiaMultiplicador = config.contains(CLAVE_MULTIPLICADOR);
        boolean multiplicadorOriginal = config.getBoolean(CLAVE_MULTIPLICADOR, false);

        try {
            try (ActivityScenario<PartidaPuntosActivity> escenario =
                         ActivityScenario.launch(nuevaPartida(context))) {
                escenario.onActivity(activity -> {
                    assertEquals(2, estadisticas(activity).size());
                    lanzarSimple20(activity);
                    EstadisticasJugadorEnCurso ana = estadisticas(activity).get(0);
                    assertEquals(1, ana.getDardosLanzados());
                    assertEquals(1, ana.getSimples());
                    assertEquals(0, ana.getPuntosValidos()); // Aún no terminó el turno.

                    PartidaEnCurso guardada = GestorPartidaEnCurso.cargarPartida(activity);
                    assertNotNull(guardada);
                    assertEquals(1, guardada.getEstadoPuntos()
                            .getEstadisticasJugadores().get(0).getDardosLanzados());

                    invocar(activity, "deshacerUltimaTirada", new Class<?>[0]);
                    ana = estadisticas(activity).get(0);
                    assertEquals(0, ana.getDardosLanzados());
                    assertEquals(0, ana.getSimples());
                    assertEquals(0, ana.getTurnosJugados());
                    assertEquals(0, ana.getPuntosValidos());
                });
            }
        } finally {
            restaurarPreferencias(prefs, habiaPartida, partidaOriginal,
                    config, habiaMultiplicador, multiplicadorOriginal);
        }
    }

    @Test
    public void continuarYDeshacerMantienenInstantaneaIndependiente() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences config = context.getSharedPreferences(PREF_CONFIG, Context.MODE_PRIVATE);
        boolean habiaPartida = prefs.contains(CLAVE);
        String partidaOriginal = prefs.getString(CLAVE, null);
        boolean habiaMultiplicador = config.contains(CLAVE_MULTIPLICADOR);
        boolean multiplicadorOriginal = config.getBoolean(CLAVE_MULTIPLICADOR, false);
        try {
            try (ActivityScenario<PartidaPuntosActivity> escenario =
                         ActivityScenario.launch(nuevaPartida(context))) {
                escenario.onActivity(EstadisticasPuntosActivityTest::lanzarSimple20);
            }
            Intent reanudar = new Intent(context, PartidaPuntosActivity.class);
            reanudar.putExtra(PartidaPuntosActivity.EXTRA_REANUDAR_PARTIDA, true);
            try (ActivityScenario<PartidaPuntosActivity> escenario =
                         ActivityScenario.launch(reanudar)) {
                escenario.onActivity(activity -> {
                    EstadisticasJugadorEnCurso ana = estadisticas(activity).get(0);
                    assertEquals(1, ana.getDardosLanzados());
                    assertEquals(1, ana.getSimples());
                    assertEquals(0, estadisticas(activity).get(1).getDardosLanzados());
                    invocar(activity, "deshacerUltimaTirada", new Class<?>[0]);
                    assertEquals(0, estadisticas(activity).get(0).getDardosLanzados());
                    assertEquals(0, estadisticas(activity).get(0).getSimples());
                });
            }
        } finally {
            restaurarPreferencias(prefs, habiaPartida, partidaOriginal,
                    config, habiaMultiplicador, multiplicadorOriginal);
        }
    }

    private static void restaurarPreferencias(
            SharedPreferences prefs, boolean habiaPartida, String partidaOriginal,
            SharedPreferences config, boolean habiaMultiplicador, boolean multiplicadorOriginal) {
        SharedPreferences.Editor editor = prefs.edit();
        if (habiaPartida) editor.putString(CLAVE, partidaOriginal);
        else editor.remove(CLAVE);
        assertTrue(editor.commit());
        SharedPreferences.Editor ajustes = config.edit();
        if (habiaMultiplicador) ajustes.putBoolean(CLAVE_MULTIPLICADOR, multiplicadorOriginal);
        else ajustes.remove(CLAVE_MULTIPLICADOR);
        assertTrue(ajustes.commit());
    }
}
