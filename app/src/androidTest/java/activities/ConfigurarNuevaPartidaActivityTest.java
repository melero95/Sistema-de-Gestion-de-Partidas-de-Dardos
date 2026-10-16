package activities;

import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.productos.juegosdedardos.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;

import modelos.Jugador;
import sqlite.JugadorDao;

import static org.junit.Assert.*;

/** Pruebas no destructivas: no inserta, modifica ni elimina usuarios reales. */
@RunWith(AndroidJUnit4.class)
public class ConfigurarNuevaPartidaActivityTest {

    @Test
    public void muestraControlesNecesarios() {
        try (ActivityScenario<ConfigurarNuevaPartidaActivity> escenario =
                     ActivityScenario.launch(ConfigurarNuevaPartidaActivity.class)) {
            escenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.spnModoJuego));
                assertNotNull(activity.findViewById(R.id.btnGestionarJugadores));
                assertNotNull(activity.findViewById(R.id.btnAnadirJugador));
                assertNotNull(activity.findViewById(R.id.btnUltimaConfiguracion));
            });
        }
    }

    @Test
    public void mantieneSieteModos() {
        try (ActivityScenario<ConfigurarNuevaPartidaActivity> escenario =
                     ActivityScenario.launch(ConfigurarNuevaPartidaActivity.class)) {
            escenario.onActivity(activity -> {
                Spinner spinner = activity.findViewById(R.id.spnModoJuego);
                assertEquals(7, spinner.getCount());
            });
        }
    }

    @Test
    public void opcionesYJugadoresSeMuestranEnTarjetasSeparadas() {
        try (ActivityScenario<ConfigurarNuevaPartidaActivity> escenario =
                     ActivityScenario.launch(ConfigurarNuevaPartidaActivity.class)) {
            escenario.onActivity(activity -> {
                View tarjetaGeneral = activity.findViewById(R.id.cardConfiguracionPartida);
                View tarjetaJugadores = activity.findViewById(R.id.cardJugadores);
                View tarjetaOpciones = activity.findViewById(R.id.cardOpcionesAdicionales);
                assertNotNull(tarjetaGeneral);
                assertNotNull(tarjetaJugadores);
                assertNotNull(tarjetaOpciones);
                assertTrue(estaDentroDe(activity.findViewById(R.id.contenedorJugadores), tarjetaJugadores));
                assertTrue(estaDentroDe(activity.findViewById(R.id.btnGestionarJugadores), tarjetaJugadores));
                assertTrue(estaDentroDe(activity.findViewById(R.id.btnUltimaConfiguracion), tarjetaOpciones));
                assertTrue(estaDentroDe(activity.findViewById(R.id.btnMostrarAjustesAvanzados), tarjetaOpciones));
                assertTrue(estaDentroDe(activity.findViewById(R.id.spnModoJuego), tarjetaGeneral));
            });
        }
    }

    @Test
    public void selectorJugadorPermiteElegirOtroYSeVeConContraste() {
        try (ActivityScenario<ConfigurarNuevaPartidaActivity> escenario =
                     ActivityScenario.launch(ConfigurarNuevaPartidaActivity.class)) {
            escenario.onActivity(activity -> {
                LinearLayout filas = activity.findViewById(R.id.contenedorJugadores);
                if (filas.getChildCount() == 0) return; // No hay jugadores registrados.

                Spinner selector = filas.getChildAt(0).findViewById(R.id.spnJugador);
                assertTrue(selector.isEnabled());
                assertTrue(selector.isClickable());
                assertNotNull(selector.getBackground());
                TextView vistaSeleccionada = (TextView) selector.getAdapter().getView(
                        0, null, selector);
                assertEquals(Color.WHITE, vistaSeleccionada.getCurrentTextColor());
                assertNotNull(vistaSeleccionada.getCompoundDrawablesRelative()[2]);

                if (selector.getCount() >= 2) {
                    long primerId = ((Jugador) selector.getItemAtPosition(0)).getId();
                    selector.setSelection(1);
                    long segundoId = ((Jugador) selector.getSelectedItem()).getId();
                    assertNotEquals(primerId, segundoId);
                }
            });
        }
    }

    @Test
    public void filtraJugadoresOcupadosPorOtrosParticipantes() {
        Jugador ana = new Jugador(1L, "Ana", Color.RED, true);
        Jugador luis = new Jugador(2L, "Luis", Color.BLUE, true);
        Jugador eva = new Jugador(3L, "Eva", Color.GREEN, true);
        List<Jugador> activos = Arrays.asList(ana, luis, eva);

        List<Jugador> disponibles = ConfigurarNuevaPartidaActivity.filtrarDisponibles(
                activos, new HashSet<>(Collections.singletonList(2L)));

        assertEquals(2, disponibles.size());
        assertEquals(1L, disponibles.get(0).getId());
        assertEquals(3L, disponibles.get(1).getId());
        assertEquals(3, activos.size()); // No modifica la lista original.
    }

    @Test
    public void losSpinnersDeDosFilasNoOfrecenAlJugadorDelOtro() {
        try (ActivityScenario<ConfigurarNuevaPartidaActivity> escenario =
                     ActivityScenario.launch(ConfigurarNuevaPartidaActivity.class)) {
            escenario.onActivity(activity -> {
                try (JugadorDao dao = new JugadorDao(activity)) {
                    if (dao.listarActivos().size() < 2) return;
                }

                LinearLayout filas = activity.findViewById(R.id.contenedorJugadores);
                if (filas.getChildCount() == 1) {
                    activity.findViewById(R.id.btnAnadirJugador).performClick();
                }
                assertTrue(filas.getChildCount() >= 2);

                Spinner primero = filas.getChildAt(0).findViewById(R.id.spnJugador);
                Spinner segundo = filas.getChildAt(1).findViewById(R.id.spnJugador);
                long idPrimero = ((Jugador) primero.getSelectedItem()).getId();
                long idSegundo = ((Jugador) segundo.getSelectedItem()).getId();
                assertNotEquals(idPrimero, idSegundo);

                for (int i = 0; i < primero.getCount(); i++) {
                    assertNotEquals(idSegundo,
                            ((Jugador) primero.getItemAtPosition(i)).getId());
                }
                for (int i = 0; i < segundo.getCount(); i++) {
                    assertNotEquals(idPrimero,
                            ((Jugador) segundo.getItemAtPosition(i)).getId());
                }
            });
        }
    }

    private static boolean estaDentroDe(View vista, View contenedor) {
        ViewParent padre = vista.getParent();
        while (padre != null) {
            if (padre == contenedor) return true;
            padre = padre.getParent();
        }
        return false;
    }

    @Test
    public void cargaJugadoresActivosYBloqueaCreacionSiNoHay() {
        try (ActivityScenario<ConfigurarNuevaPartidaActivity> escenario =
                     ActivityScenario.launch(ConfigurarNuevaPartidaActivity.class)) {
            escenario.onActivity(activity -> {
                try (JugadorDao dao = new JugadorDao(activity)) {
                    List<Jugador> activos = dao.listarActivos();
                    LinearLayout filas = activity.findViewById(R.id.contenedorJugadores);
                    Button crear = activity.findViewById(R.id.btnCrearPartida);
                    if (activos.isEmpty()) {
                        assertEquals(0, filas.getChildCount());
                        assertFalse(crear.isEnabled());
                    } else {
                        assertTrue(filas.getChildCount() >= 1);
                        Spinner selector = filas.getChildAt(0).findViewById(R.id.spnJugador);
                        // La fila muestra su propio jugador y los que no están
                        // seleccionados en las demás filas.
                        assertEquals(activos.size() - filas.getChildCount() + 1,
                                selector.getCount());
                        assertTrue(crear.isEnabled());
                    }
                }
            });
        }
    }
}
