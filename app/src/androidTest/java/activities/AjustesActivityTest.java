package activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.productos.juegosdedardos.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import modelos.Jugador;
import sqlite.JugadorDao;

import static org.junit.Assert.*;

/**
 * Pruebas instrumentales de integracion de Ajustes con SQLite.
 * No modifica la tabla JUGADORES. Si cambia SharedPreferences durante un
 * test, restituye todas sus claves y valores originales al terminar.
 */
@RunWith(AndroidJUnit4.class)
public class AjustesActivityTest {

    private Context contexto() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    private List<Jugador> activos() {
        try (JugadorDao dao = new JugadorDao(contexto())) {
            return dao.listarActivos();
        }
    }

    @Test
    public void spinnerMuestraSoloJugadoresActivos() {
        List<Jugador> registrados = activos();
        try (ActivityScenario<AjustesActivity> escenario =
                     ActivityScenario.launch(AjustesActivity.class)) {
            escenario.onActivity(activity -> {
                Spinner spinner = activity.findViewById(R.id.spnJugadorPredeterminado);
                View aviso = activity.findViewById(R.id.txtAvisoJugadoresAjustes);
                assertEquals(registrados.size(), spinner.getCount());
                assertEquals(!registrados.isEmpty(), spinner.isEnabled());
                assertEquals(registrados.isEmpty() ? View.VISIBLE : View.GONE,
                        aviso.getVisibility());
                for (int i = 0; i < spinner.getCount(); i++) {
                    Jugador actual = (Jugador) spinner.getItemAtPosition(i);
                    assertEquals(registrados.get(i).getId(), actual.getId());
                    assertTrue(actual.isActivo());
                }
            });
        }
    }

    @Test
    public void seleccionaPorIdAunqueNombreGuardadoSeaAntiguo() {
        List<Jugador> registrados = activos();
        if (registrados.isEmpty()) return;
        long esperado = registrados.get(registrados.size() - 1).getId();
        PreferenciasTemporales temporal = new PreferenciasTemporales(contexto());
        try {
            temporal.prefs.edit()
                    .putLong(AjustesActivity.PREF_JUGADOR_ID, esperado)
                    .putString(AjustesActivity.PREF_JUGADOR, "NOMBRE ANTIGUO")
                    .commit();
            try (ActivityScenario<AjustesActivity> escenario =
                         ActivityScenario.launch(AjustesActivity.class)) {
                escenario.onActivity(activity -> {
                    Spinner spinner = activity.findViewById(R.id.spnJugadorPredeterminado);
                    assertEquals(esperado, ((Jugador) spinner.getSelectedItem()).getId());
                });
            }
        } finally {
            temporal.restaurar();
        }
    }

    @Test
    public void idInexistenteEligePrimerActivoEnLugarDelNombreAntiguo() {
        List<Jugador> registrados = activos();
        if (registrados.isEmpty()) return;
        PreferenciasTemporales temporal = new PreferenciasTemporales(contexto());
        try {
            temporal.prefs.edit()
                    .putLong(AjustesActivity.PREF_JUGADOR_ID, Long.MAX_VALUE)
                    .putString(AjustesActivity.PREF_JUGADOR,
                            registrados.get(registrados.size() - 1).getNombre())
                    .commit();
            try (ActivityScenario<AjustesActivity> escenario =
                         ActivityScenario.launch(AjustesActivity.class)) {
                escenario.onActivity(activity -> {
                    Spinner spinner = activity.findViewById(R.id.spnJugadorPredeterminado);
                    assertEquals(registrados.get(0).getId(),
                            ((Jugador) spinner.getSelectedItem()).getId());
                });
            }
        } finally {
            temporal.restaurar();
        }
    }

    @Test
    public void migraPreferenciaAnteriorQueSoloGuardabaNombre() {
        List<Jugador> registrados = activos();
        if (registrados.isEmpty()) return;
        Jugador esperado = registrados.get(registrados.size() - 1);
        PreferenciasTemporales temporal = new PreferenciasTemporales(contexto());
        try {
            temporal.prefs.edit()
                    .remove(AjustesActivity.PREF_JUGADOR_ID)
                    .putString(AjustesActivity.PREF_JUGADOR,
                            "  " + esperado.getNombre().toLowerCase(java.util.Locale.ROOT) + "  ")
                    .commit();
            try (ActivityScenario<AjustesActivity> escenario =
                         ActivityScenario.launch(AjustesActivity.class)) {
                escenario.onActivity(activity -> {
                    Spinner spinner = activity.findViewById(R.id.spnJugadorPredeterminado);
                    assertEquals(esperado.getId(),
                            ((Jugador) spinner.getSelectedItem()).getId());
                });
            }
        } finally {
            temporal.restaurar();
        }
    }

    @Test
    public void guardaIdYNombreDeJugadorSeleccionado() {
        List<Jugador> registrados = activos();
        if (registrados.isEmpty()) return;
        Jugador esperado = registrados.get(registrados.size() - 1);
        PreferenciasTemporales temporal = new PreferenciasTemporales(contexto());
        try {
            try (ActivityScenario<AjustesActivity> escenario =
                         ActivityScenario.launch(AjustesActivity.class)) {
                escenario.onActivity(activity -> {
                    Spinner spinner = activity.findViewById(R.id.spnJugadorPredeterminado);
                    spinner.setSelection(registrados.size() - 1);
                    activity.findViewById(R.id.btnGuardarAjustes).performClick();
                });
            }
            assertEquals(esperado.getId(), temporal.prefs.getLong(
                    AjustesActivity.PREF_JUGADOR_ID, -1L));
            assertEquals(esperado.getNombre(), temporal.prefs.getString(
                    AjustesActivity.PREF_JUGADOR, ""));
        } finally {
            temporal.restaurar();
        }
    }

    @Test
    public void nuevaPartidaUsaIdPredeterminadoAunqueCambieElNombre() {
        List<Jugador> registrados = activos();
        if (registrados.isEmpty()) return;
        long esperado = registrados.get(registrados.size() - 1).getId();
        PreferenciasTemporales temporal = new PreferenciasTemporales(contexto());
        try {
            temporal.prefs.edit()
                    .putLong(AjustesActivity.PREF_JUGADOR_ID, esperado)
                    .putString(AjustesActivity.PREF_JUGADOR, "NOMBRE YA CAMBIADO")
                    .commit();
            try (ActivityScenario<ConfigurarNuevaPartidaActivity> escenario =
                         ActivityScenario.launch(ConfigurarNuevaPartidaActivity.class)) {
                escenario.onActivity(activity -> {
                    LinearLayout filas = activity.findViewById(R.id.contenedorJugadores);
                    assertTrue(filas.getChildCount() >= 1);
                    Spinner spinner = filas.getChildAt(0).findViewById(R.id.spnJugador);
                    assertEquals(esperado, ((Jugador) spinner.getSelectedItem()).getId());
                });
            }
        } finally {
            temporal.restaurar();
        }
    }

    @Test
    public void noReasignaIdInactivoAOtroJugadorConElMismoNombre() {
        List<Jugador> registros = Arrays.asList(
                new Jugador(2L, "ANA", 0, true),
                new Jugador(3L, "LUIS", 0, true));
        assertEquals(0, AjustesActivity.posicionJugadorPreferido(
                registros, 99L, "LUIS"));
        assertEquals(1, AjustesActivity.posicionJugadorPreferido(
                registros, 3L, "ANA"));
        assertEquals(1, AjustesActivity.posicionJugadorPreferido(
                registros, 0L, "LUIS"));
        assertEquals(-1, AjustesActivity.posicionJugadorPreferido(
                new ArrayList<>(), 3L, "LUIS"));
    }

    @Test
    public void incluyeShanghaiEnModosPredeterminados() {
        try (ActivityScenario<AjustesActivity> escenario =
                     ActivityScenario.launch(AjustesActivity.class)) {
            escenario.onActivity(activity -> {
                Spinner spinner = activity.findViewById(R.id.spnModoPredeterminado);
                assertEquals(7, spinner.getCount());
                boolean tieneShanghai = false;
                for (int i = 0; i < spinner.getCount(); i++) {
                    if ("Shanghai".equals(spinner.getItemAtPosition(i).toString())) {
                        tieneShanghai = true;
                    }
                }
                assertTrue(tieneShanghai);
            });
        }
    }

    private static class PreferenciasTemporales {
        final SharedPreferences prefs;
        private final Map<String, ?> original;

        PreferenciasTemporales(Context context) {
            prefs = context.getSharedPreferences(AjustesActivity.SP_AJUSTES, Context.MODE_PRIVATE);
            original = new HashMap<>(prefs.getAll());
        }

        @SuppressWarnings("unchecked")
        void restaurar() {
            SharedPreferences.Editor editor = prefs.edit().clear();
            for (Map.Entry<String, ?> entrada : original.entrySet()) {
                String clave = entrada.getKey();
                Object valor = entrada.getValue();
                if (valor instanceof String) editor.putString(clave, (String) valor);
                else if (valor instanceof Boolean) editor.putBoolean(clave, (Boolean) valor);
                else if (valor instanceof Integer) editor.putInt(clave, (Integer) valor);
                else if (valor instanceof Long) editor.putLong(clave, (Long) valor);
                else if (valor instanceof Float) editor.putFloat(clave, (Float) valor);
                else if (valor instanceof Set) {
                    editor.putStringSet(clave, new HashSet<>((Set<String>) valor));
                }
            }
            editor.commit();
        }
    }
}
