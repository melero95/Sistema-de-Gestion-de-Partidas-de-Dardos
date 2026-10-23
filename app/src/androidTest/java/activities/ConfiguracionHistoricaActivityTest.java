package activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.productos.juegosdedardos.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import modelos.ConfiguracionPartida;
import modelos.Jugador;
import sqlite.JugadorDao;
import utilidades.SerializadorConfiguracionPartida;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/** No inserta ni modifica jugadores. Las pruebas requieren usuarios activos de prueba. */
@RunWith(AndroidJUnit4.class)
public class ConfiguracionHistoricaActivityTest {

    @Test
    public void precargaModoReglasParticipantesYOrdenSinCambiarUltimaConfiguracion() {
        Context contexto = ApplicationProvider.getApplicationContext();
        List<Jugador> activos = obtenerActivos(contexto);
        assumeTrue("Se necesitan dos jugadores activos de prueba", activos.size() >= 2);

        // Intencionadamente se invierte el orden alfabético ofrecido por SQLite.
        Jugador primero = activos.get(1);
        Jugador segundo = activos.get(0);
        ArrayList<ConfiguracionPartida.Participante> participantes = new ArrayList<>();
        participantes.add(new ConfiguracionPartida.Participante(
                primero.getId(), primero.getNombre(),
                ContextCompat.getColor(contexto, R.color.jugador_rojo), 1));
        participantes.add(new ConfiguracionPartida.Participante(
                segundo.getId(), segundo.getNombre(),
                ContextCompat.getColor(contexto, R.color.jugador_azul), 2));

        ConfiguracionPartida original = new ConfiguracionPartida(
                "501", 18, 4, true, true, true, participantes);
        String json = SerializadorConfiguracionPartida.aJson(original);

        SharedPreferences preferencias = contexto.getSharedPreferences(
                "configuracion_ultima_partida", Context.MODE_PRIVATE);
        String modoAntes = preferencias.getString("modoJuego", null);
        int rondasAntes = preferencias.getInt("maxRondas", -1);

        Intent intent = new Intent(contexto, ConfigurarNuevaPartidaActivity.class);
        intent.putExtra(ConfigurarNuevaPartidaActivity.EXTRA_CONFIGURACION_JSON, json);
        try (ActivityScenario<ConfigurarNuevaPartidaActivity> escenario =
                     ActivityScenario.launch(intent)) {
            escenario.onActivity(activity -> {
                Spinner modo = activity.findViewById(R.id.spnModoJuego);
                assertEquals("501", modo.getSelectedItem().toString());
                TextView rondas = activity.findViewById(R.id.txtMaxRondas);
                assertEquals("18", rondas.getText().toString());
                Spinner dardos = activity.findViewById(R.id.spnNumeroDardos);
                assertEquals(4, ((Integer) dardos.getSelectedItem()).intValue());
                assertTrue(((Switch) activity.findViewById(R.id.switchCierreDoble)).isChecked());
                assertTrue(((Switch) activity.findViewById(R.id.switchMantenerMultiplicador)).isChecked());
                assertTrue(((Switch) activity.findViewById(R.id.switchOrdenAleatorio)).isChecked());
                LinearLayout filas = activity.findViewById(R.id.contenedorJugadores);
                assertEquals(2, filas.getChildCount());
                Spinner selector1 = filas.getChildAt(0).findViewById(R.id.spnJugador);
                Spinner selector2 = filas.getChildAt(1).findViewById(R.id.spnJugador);
                assertEquals(primero.getId(), ((Jugador) selector1.getSelectedItem()).getId());
                assertEquals(segundo.getId(), ((Jugador) selector2.getSelectedItem()).getId());
            });
        }
        assertEquals(modoAntes, preferencias.getString("modoJuego", null));
        assertEquals(rondasAntes, preferencias.getInt("maxRondas", -1));
    }

    @Test
    public void usuarioHistoricoNoRegistradoNuncaSeSeleccionaPorNombre() {
        Context contexto = ApplicationProvider.getApplicationContext();
        List<Jugador> activos = obtenerActivos(contexto);
        assumeTrue("Se necesita un jugador activo de prueba", !activos.isEmpty());
        Jugador primero = activos.get(0);

        ConfiguracionPartida config = new ConfiguracionPartida("301", 10, 3,
                false, false, false, java.util.Arrays.asList(
                new ConfiguracionPartida.Participante(primero.getId(), primero.getNombre(),
                        primero.getColor(), 1),
                new ConfiguracionPartida.Participante(Long.MAX_VALUE, "JUGADOR INACTIVO",
                        primero.getColor(), 2)));
        Intent intent = new Intent(contexto, ConfigurarNuevaPartidaActivity.class);
        intent.putExtra(ConfigurarNuevaPartidaActivity.EXTRA_CONFIGURACION_JSON,
                SerializadorConfiguracionPartida.aJson(config));
        try (ActivityScenario<ConfigurarNuevaPartidaActivity> escenario =
                     ActivityScenario.launch(intent)) {
            escenario.onActivity(activity -> {
                LinearLayout filas = activity.findViewById(R.id.contenedorJugadores);
                for (int i = 0; i < filas.getChildCount(); i++) {
                    Spinner s = filas.getChildAt(i).findViewById(R.id.spnJugador);
                    assertNotEquals(Long.MAX_VALUE, ((Jugador) s.getSelectedItem()).getId());
                }
            });
        }
    }

    private static List<Jugador> obtenerActivos(Context contexto) {
        try (JugadorDao dao = new JugadorDao(contexto)) {
            return dao.listarActivos();
        }
    }
}
