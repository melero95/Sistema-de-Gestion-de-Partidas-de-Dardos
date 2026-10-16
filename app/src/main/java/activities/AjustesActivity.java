package activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.productos.juegosdedardos.R;

import java.util.ArrayList;
import java.util.List;

import modelos.Jugador;
import sqlite.JugadorDao;

/** Preferencias de nuevas partidas. Jugadores obtenidos mediante DAO SQLite. */
public class AjustesActivity extends AppCompatActivity {

    public static final String SP_AJUSTES = "ajustes_partida";

    // La clave antigua se conserva para compatibilidad y migracion por nombre.
    public static final String PREF_JUGADOR = "jugador_default";
    public static final String PREF_JUGADOR_ID = "jugador_default_id";

    public static final String PREF_MODO = "modo_default";
    public static final String PREF_DARDOS = "dardos_default";
    public static final String PREF_MANTENER_MULTIPLICADOR = "mantener_multiplicador";
    public static final String PREF_CIERRE_DOBLE = "cierre_doble_default";
    public static final String PREF_ORDEN_ALEATORIO = "orden_aleatorio_default";
    public static final String PREF_CONFIRMAR_SALIDA = "confirmar_salida";
    public static final String PREF_VIBRACION = "vibracion";

    private static final String[] MODOS = {
            "301", "501", "Cricket", "Cut Throat Cricket", "Double Down",
            "Around the Clock", "Shanghai"
    };
    private static final Integer[] DARDOS = {1, 2, 3, 4};

    private Spinner spnJugadorPredeterminado;
    private Spinner spnModoPredeterminado;
    private Spinner spnDardosPredeterminados;
    private TextView txtAvisoJugadoresAjustes;
    private CheckBox chkMantenerMultiplicador;
    private CheckBox chkCierreDoble;
    private CheckBox chkOrdenAleatorio;
    private CheckBox chkConfirmarSalida;
    private CheckBox chkVibracion;

    private SharedPreferences preferencias;
    private JugadorDao jugadorDao;
    private final List<Jugador> jugadoresActivos = new ArrayList<>();
    private boolean primerResumeCompletado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.blue));
        setContentView(R.layout.activity_ajustes);

        preferencias = getSharedPreferences(SP_AJUSTES, MODE_PRIVATE);
        jugadorDao = new JugadorDao(this);
        inicializarVistas();
        prepararSpinners();
        cargarPreferencias();
        configurarBotones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!primerResumeCompletado) {
            primerResumeCompletado = true;
            return;
        }
        // Si se modifican los jugadores en otra pantalla, refrescar sin
        // perder los ajustes que aun no se han guardado.
        Jugador anterior = jugadorSeleccionado();
        long idAnterior = anterior == null ? 0L : anterior.getId();
        actualizarListaJugadores();
        if (idAnterior > 0L) {
            seleccionarPorId(idAnterior);
        } else {
            seleccionarJugadorPreferidoGuardado();
        }
    }

    @Override
    protected void onDestroy() {
        if (jugadorDao != null) jugadorDao.close();
        super.onDestroy();
    }

    private void inicializarVistas() {
        spnJugadorPredeterminado = findViewById(R.id.spnJugadorPredeterminado);
        spnModoPredeterminado = findViewById(R.id.spnModoPredeterminado);
        spnDardosPredeterminados = findViewById(R.id.spnDardosPredeterminados);
        txtAvisoJugadoresAjustes = findViewById(R.id.txtAvisoJugadoresAjustes);
        chkMantenerMultiplicador = findViewById(R.id.chkMantenerMultiplicador);
        chkCierreDoble = findViewById(R.id.chkCierreDoblePredeterminado);
        chkOrdenAleatorio = findViewById(R.id.chkOrdenAleatorioPredeterminado);
        chkConfirmarSalida = findViewById(R.id.chkConfirmarSalida);
        chkVibracion = findViewById(R.id.chkVibracion);
    }

    private void prepararSpinners() {
        configurarSpinner(spnModoPredeterminado, MODOS);
        configurarSpinner(spnDardosPredeterminados, DARDOS);
    }

    private <T> void configurarSpinner(Spinner spinner, T[] valores) {
        ArrayAdapter<T> adaptador = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, valores);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adaptador);
    }

    private void actualizarListaJugadores() {
        jugadoresActivos.clear();
        jugadoresActivos.addAll(jugadorDao.listarActivos());
        ArrayAdapter<Jugador> adaptador = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, jugadoresActivos);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnJugadorPredeterminado.setAdapter(adaptador);
        boolean hayJugadores = !jugadoresActivos.isEmpty();
        spnJugadorPredeterminado.setEnabled(hayJugadores);
        txtAvisoJugadoresAjustes.setVisibility(hayJugadores ? View.GONE : View.VISIBLE);
    }

    private void cargarPreferencias() {
        actualizarListaJugadores();
        seleccionarJugadorPreferidoGuardado();
        seleccionarValor(spnModoPredeterminado,
                preferencias.getString(PREF_MODO, "501"));
        int dardos = Math.max(1, Math.min(4, preferencias.getInt(PREF_DARDOS, 3)));
        spnDardosPredeterminados.setSelection(dardos - 1);
        chkMantenerMultiplicador.setChecked(
                preferencias.getBoolean(PREF_MANTENER_MULTIPLICADOR, false));
        chkCierreDoble.setChecked(preferencias.getBoolean(PREF_CIERRE_DOBLE, false));
        chkOrdenAleatorio.setChecked(preferencias.getBoolean(PREF_ORDEN_ALEATORIO, false));
        chkConfirmarSalida.setChecked(preferencias.getBoolean(PREF_CONFIRMAR_SALIDA, true));
        chkVibracion.setChecked(preferencias.getBoolean(PREF_VIBRACION, true));
    }

    private void seleccionarJugadorPreferidoGuardado() {
        long idGuardado = preferencias.getLong(PREF_JUGADOR_ID, 0L);
        String nombreAntiguo = preferencias.getString(PREF_JUGADOR, "");
        int posicion = posicionJugadorPreferido(jugadoresActivos, idGuardado, nombreAntiguo);
        if (posicion >= 0) spnJugadorPredeterminado.setSelection(posicion);
    }

    /**
     * Con ID guardado este es la unica identidad valida. Si ya no esta activo,
     * se elige el primer activo en vez de reasignar un nombre antiguo.
     * Solo se busca por nombre para migrar las preferencias previas sin ID.
     */
    static int posicionJugadorPreferido(List<Jugador> activos, long id, String nombreAntiguo) {
        if (activos == null || activos.isEmpty()) return -1;
        if (id > 0L) {
            for (int i = 0; i < activos.size(); i++) {
                if (activos.get(i).getId() == id) return i;
            }
            return 0;
        }
        if (nombreAntiguo != null) {
            for (int i = 0; i < activos.size(); i++) {
                if (activos.get(i).getNombre().equalsIgnoreCase(nombreAntiguo.trim())) return i;
            }
        }
        return 0;
    }

    private boolean seleccionarPorId(long id) {
        for (int i = 0; i < jugadoresActivos.size(); i++) {
            if (jugadoresActivos.get(i).getId() == id) {
                spnJugadorPredeterminado.setSelection(i);
                return true;
            }
        }
        return false;
    }

    private Jugador jugadorSeleccionado() {
        Object valor = spnJugadorPredeterminado.getSelectedItem();
        return valor instanceof Jugador ? (Jugador) valor : null;
    }

    private void configurarBotones() {
        Button btnGuardar = findViewById(R.id.btnGuardarAjustes);
        Button btnRestablecer = findViewById(R.id.btnRestablecerAjustes);
        Button btnVolver = findViewById(R.id.btnVolverAjustes);
        btnGuardar.setOnClickListener(v -> {
            guardarPreferencias();
            Toast.makeText(this, "Ajustes guardados", Toast.LENGTH_SHORT).show();
            finish();
        });
        btnRestablecer.setOnClickListener(v -> restablecerPreferencias());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void guardarPreferencias() {
        SharedPreferences.Editor editor = preferencias.edit();
        Jugador seleccionado = jugadorSeleccionado();
        if (seleccionado == null) {
            // Permitir guardar el resto de opciones aunque no haya activos.
            editor.remove(PREF_JUGADOR_ID).remove(PREF_JUGADOR);
        } else {
            editor.putLong(PREF_JUGADOR_ID, seleccionado.getId());
            editor.putString(PREF_JUGADOR, seleccionado.getNombre());
        }
        editor.putString(PREF_MODO, valorSeleccionado(spnModoPredeterminado));
        editor.putInt(PREF_DARDOS, (Integer) spnDardosPredeterminados.getSelectedItem());
        editor.putBoolean(PREF_MANTENER_MULTIPLICADOR, chkMantenerMultiplicador.isChecked());
        editor.putBoolean(PREF_CIERRE_DOBLE, chkCierreDoble.isChecked());
        editor.putBoolean(PREF_ORDEN_ALEATORIO, chkOrdenAleatorio.isChecked());
        editor.putBoolean(PREF_CONFIRMAR_SALIDA, chkConfirmarSalida.isChecked());
        editor.putBoolean(PREF_VIBRACION, chkVibracion.isChecked());
        editor.apply();
    }

    private void restablecerPreferencias() {
        preferencias.edit().clear().apply();
        cargarPreferencias();
        Toast.makeText(this, "Ajustes restablecidos", Toast.LENGTH_SHORT).show();
    }

    private String valorSeleccionado(Spinner spinner) {
        return String.valueOf(spinner.getSelectedItem());
    }

    private void seleccionarValor(Spinner spinner, String valor) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (String.valueOf(spinner.getItemAtPosition(i)).equals(valor)) {
                spinner.setSelection(i);
                return;
            }
        }
    }
}
