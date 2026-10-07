package activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.productos.juegosdedardos.R;

import java.util.ArrayList;
import java.util.Collections;

public class ConfigurarNuevaPartidaActivity extends AppCompatActivity {

    private Spinner spnModoJuego;

    private TextView txtMaxRondas;
    private TextView btnRestarRonda;
    private TextView btnSumarRonda;

    private LinearLayout contenedorJugadores;

    private Button btnAnadirJugador;
    private Button btnCrearPartida;
    private Button btnCancelarPartida;
    private Button btnUltimaConfiguracion;
    private Button btnMostrarAjustesAvanzados;

    private LinearLayout contenedorAjustesAvanzados;
    private Switch switchCierreDoble;
    private Switch switchMantenerMultiplicador;
    private Switch switchOrdenAleatorio;
    private Spinner spnNumeroDardos;
    private TextView txtTituloNumeroDardos;

    // Modos de juego ------------------------------------------------------------

    private static final String MODO_301 = "301";
    private static final String MODO_501 = "501";
    private static final String MODO_CRICKET = "Cricket";
    private static final String MODO_CUT_THROAT = "Cut Throat Cricket";
    private static final String MODO_DOUBLE_DOWN = "Double Down";
    private static final String MODO_AROUND_CLOCK = "Around the Clock";
    private static final String MODO_SHANGHAI = "Shanghai";

    // Extras de Intent ----------------------------------------------------------

    private static final String EXTRA_MODO_JUEGO = "modoJuego";
    private static final String EXTRA_MAX_RONDAS = "maxRondas";
    private static final String EXTRA_NUMERO_JUGADORES = "numeroJugadores";

    private static final String EXTRA_JUGADOR = "jugador";
    private static final String EXTRA_COLOR_JUGADOR = "colorJugador";

    public static final String EXTRA_NUMERO_DARDOS = "numeroDardos";
    public static final String EXTRA_CIERRE_DOBLE = "cierreDoble";
    public static final String EXTRA_MANTENER_MULTIPLICADOR = "mantenerMultiplicador";
    public static final String EXTRA_ORDEN_ALEATORIO = "ordenAleatorio";

    // SharedPreferences: última configuración ----------------------------------

    private static final String SP_ULTIMA_PARTIDA = "configuracion_ultima_partida";

    // SharedPreferences: ajustes generales -------------------------------------

    private static final String SP_AJUSTES = "ajustes_partida";

    private static final String PREF_JUGADOR = "jugador_default";
    private static final String PREF_MODO = "modo_default";
    private static final String PREF_DARDOS = "dardos_default";
    private static final String PREF_CIERRE_DOBLE = "cierre_doble_default";
    private static final String PREF_MANTENER_MULTIPLICADOR = "mantener_multiplicador";
    private static final String PREF_ORDEN_ALEATORIO = "orden_aleatorio_default";

    // Configuración base --------------------------------------------------------

    private static final int RONDAS_ESTANDAR = 15;
    private static final int RONDAS_DOUBLE_DOWN = 9;
    private static final int RONDAS_AROUND_CLOCK = 21;
    private static final int RONDAS_SHANGHAI = 21;

    private int maxRondas = RONDAS_ESTANDAR;
    private boolean cargandoConfiguracion = false;

    private ArrayList<View> filasJugadores;
    private ArrayList<String> nombresJugadores;

    private SharedPreferences spUltimaPartida;
    private SharedPreferences.Editor editorUltimaPartida;
    private SharedPreferences spAjustes;

    private final int[] coloresJugadores = {
            R.color.jugador_rojo,
            R.color.jugador_verde,
            R.color.jugador_azul,
            R.color.jugador_amarillo,
            R.color.jugador_naranja,
            R.color.jugador_morado,
            R.color.jugador_cyan,
            R.color.jugador_blanco
    };

    private final String[] nombresColores = {
            "Rojo",
            "Verde",
            "Azul",
            "Amarillo",
            "Naranja",
            "Morado",
            "Cyan",
            "Blanco"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.blue));

        setContentView(R.layout.activity_nueva_partida);

        inicializarComponentes();
        inicializarSharedPreferences();
        inicializarListas();

        cargarSpinnerModoJuego();
        configurarCambioModoJuego();
        configurarBotonesRondas();
        configurarBotonesPrincipales();
        configurarAjustesAvanzados();

        cargarValoresPredeterminadosDesdeAjustes();

        agregarFilaJugador();
        aplicarJugadorPredeterminadoPrimeraFila();

        aplicarConfiguracionSegunModo(obtenerModoJuegoSeleccionado());
    }

    // Inicialización ------------------------------------------------------------

    private void inicializarComponentes() {
        spnModoJuego = findViewById(R.id.spnModoJuego);

        txtMaxRondas = findViewById(R.id.txtMaxRondas);
        btnRestarRonda = findViewById(R.id.btnRestarRonda);
        btnSumarRonda = findViewById(R.id.btnSumarRonda);

        contenedorJugadores = findViewById(R.id.contenedorJugadores);

        btnAnadirJugador = findViewById(R.id.btnAnadirJugador);
        btnCrearPartida = findViewById(R.id.btnCrearPartida);
        btnCancelarPartida = findViewById(R.id.btnCancelarPartida);
        btnUltimaConfiguracion = findViewById(R.id.btnUltimaConfiguracion);
        btnMostrarAjustesAvanzados = findViewById(R.id.btnMostrarAjustesAvanzados);

        contenedorAjustesAvanzados = findViewById(R.id.contenedorAjustesAvanzados);
        switchCierreDoble = findViewById(R.id.switchCierreDoble);
        switchMantenerMultiplicador = findViewById(R.id.switchMantenerMultiplicador);
        switchOrdenAleatorio = findViewById(R.id.switchOrdenAleatorio);
        spnNumeroDardos = findViewById(R.id.spnNumeroDardos);
        txtTituloNumeroDardos = findViewById(R.id.txtTituloNumeroDardos);
    }

    private void inicializarSharedPreferences() {
        spUltimaPartida = getSharedPreferences(SP_ULTIMA_PARTIDA, MODE_PRIVATE);
        editorUltimaPartida = spUltimaPartida.edit();

        spAjustes = getSharedPreferences(SP_AJUSTES, MODE_PRIVATE);
    }

    private void inicializarListas() {
        filasJugadores = new ArrayList<>();

        // Temporal. Más adelante estos nombres se cargarán desde SQLite.
        nombresJugadores = new ArrayList<>();
        nombresJugadores.add("Jugador");
        nombresJugadores.add("Luis");
        nombresJugadores.add("Invitado");
    }

    private void cargarSpinnerModoJuego() {
        ArrayList<String> modosJuego = new ArrayList<>();

        modosJuego.add(MODO_301);
        modosJuego.add(MODO_501);
        modosJuego.add(MODO_CRICKET);
        modosJuego.add(MODO_CUT_THROAT);
        modosJuego.add(MODO_DOUBLE_DOWN);
        modosJuego.add(MODO_AROUND_CLOCK);
        modosJuego.add(MODO_SHANGHAI);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                modosJuego
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnModoJuego.setAdapter(adapter);
    }

    // Listeners -----------------------------------------------------------------

    private void configurarCambioModoJuego() {
        spnModoJuego.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!cargandoConfiguracion) {
                    aplicarConfiguracionSegunModo(obtenerModoJuegoSeleccionado());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No es necesario hacer nada.
            }
        });
    }

    private void configurarBotonesRondas() {
        actualizarTextoRondas();

        btnSumarRonda.setOnClickListener(v -> {
            if (!rondasBloqueadas()) {
                maxRondas++;
                actualizarTextoRondas();
            }
        });

        btnRestarRonda.setOnClickListener(v -> {
            if (!rondasBloqueadas() && maxRondas > 1) {
                maxRondas--;
                actualizarTextoRondas();
            }
        });
    }

    private void configurarBotonesPrincipales() {
        btnAnadirJugador.setOnClickListener(v -> agregarFilaJugador());

        btnCancelarPartida.setOnClickListener(v -> finish());

        btnCrearPartida.setOnClickListener(v -> {
            if (!validarConfiguracionPartida()) {
                return;
            }

            if (maxRondas >= 20 && rondasEditablesParaModo(obtenerModoJuegoSeleccionado())) {
                mostrarDialogoMuchasRondas();
            } else {
                crearPartida();
            }
        });

        btnUltimaConfiguracion.setOnClickListener(v -> cargarUltimaConfiguracion());
    }

    private void configurarAjustesAvanzados() {
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new Integer[]{1, 2, 3, 4}
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnNumeroDardos.setAdapter(adapter);

        btnMostrarAjustesAvanzados.setOnClickListener(v -> {
            boolean mostrar = contenedorAjustesAvanzados.getVisibility() != View.VISIBLE;

            contenedorAjustesAvanzados.setVisibility(mostrar ? View.VISIBLE : View.GONE);
            btnMostrarAjustesAvanzados.setText(mostrar ? "Menos ajustes ▲" : "Más ajustes ▼");
        });
    }

    // AjustesActivity -----------------------------------------------------------

    private void cargarValoresPredeterminadosDesdeAjustes() {
        cargandoConfiguracion = true;

        String modoPredeterminado = spAjustes.getString(PREF_MODO, MODO_501);
        seleccionarModoJuegoEnSpinner(modoPredeterminado);

        int dardos = Math.max(1, Math.min(4, spAjustes.getInt(PREF_DARDOS, 3)));
        spnNumeroDardos.setSelection(dardos - 1);

        switchCierreDoble.setChecked(spAjustes.getBoolean(PREF_CIERRE_DOBLE, false));
        switchMantenerMultiplicador.setChecked(
                spAjustes.getBoolean(PREF_MANTENER_MULTIPLICADOR, false));
        switchOrdenAleatorio.setChecked(spAjustes.getBoolean(PREF_ORDEN_ALEATORIO, false));

        cargandoConfiguracion = false;
    }

    private void aplicarJugadorPredeterminadoPrimeraFila() {
        if (filasJugadores.isEmpty()) {
            return;
        }

        String jugadorPredeterminado = spAjustes.getString(PREF_JUGADOR, "Jugador");

        View primeraFila = filasJugadores.get(0);
        Spinner spnJugador = primeraFila.findViewById(R.id.spnJugador);

        seleccionarJugadorEnSpinner(spnJugador, jugadorPredeterminado);
    }

    // Reglas según modo de juego ------------------------------------------------

    private void aplicarConfiguracionSegunModo(String modoJuego) {
        boolean esPuntos = MODO_301.equals(modoJuego) || MODO_501.equals(modoJuego);

        // Cierre con doble solo disponible en 301 y 501.
        switchCierreDoble.setVisibility(esPuntos ? View.VISIBLE : View.GONE);

        // Mantener multiplicador disponible en Puntos y Cricket.
        boolean admiteMultiplicadorPersistente = esPuntos || esModoCricket(modoJuego);
        switchMantenerMultiplicador.setVisibility(
                admiteMultiplicadorPersistente ? View.VISIBLE : View.GONE
        );

        // Estos ajustes están disponibles en todos los modos.
        txtTituloNumeroDardos.setVisibility(View.VISIBLE);
        spnNumeroDardos.setVisibility(View.VISIBLE);
        switchOrdenAleatorio.setVisibility(View.VISIBLE);

        // Configuración de rondas según el modo.
        if (MODO_DOUBLE_DOWN.equals(modoJuego)) {
            bloquearRondas(RONDAS_DOUBLE_DOWN);

        } else if (MODO_AROUND_CLOCK.equals(modoJuego)) {
            bloquearRondas(RONDAS_AROUND_CLOCK);

        } else if (MODO_SHANGHAI.equals(modoJuego)) {
            bloquearRondas(RONDAS_SHANGHAI);

        } else if (esPuntos) {
            maxRondas = RONDAS_ESTANDAR;
            actualizarTextoRondas();
            desbloquearRondas();

        } else {
            // Cricket y Cut Throat mantienen las rondas editables.
            desbloquearRondas();
        }

        // Cricket necesita al menos dos jugadores.
        if (esModoCricket(modoJuego)) {
            asegurarMinimoDosJugadores();
            intentarEvitarJugadorRepetidoEnCricket();
        }

        actualizarVisibilidadBotonesEliminar();
    }

    private boolean esModoCricket(String modoJuego) {
        return MODO_CRICKET.equals(modoJuego) || MODO_CUT_THROAT.equals(modoJuego);
    }

    private boolean rondasEditablesParaModo(String modoJuego) {
        return !MODO_DOUBLE_DOWN.equals(modoJuego)
                && !MODO_AROUND_CLOCK.equals(modoJuego)
                && !MODO_SHANGHAI.equals(modoJuego);
    }

    private boolean rondasBloqueadas() {
        return !btnSumarRonda.isEnabled() || !btnRestarRonda.isEnabled();
    }

    private void bloquearRondas(int rondasFijas) {
        maxRondas = rondasFijas;
        actualizarTextoRondas();

        btnRestarRonda.setEnabled(false);
        btnSumarRonda.setEnabled(false);

        btnRestarRonda.setAlpha(0.35f);
        btnSumarRonda.setAlpha(0.35f);
        txtMaxRondas.setAlpha(0.75f);
    }

    private void desbloquearRondas() {
        btnRestarRonda.setEnabled(true);
        btnSumarRonda.setEnabled(true);

        btnRestarRonda.setAlpha(1f);
        btnSumarRonda.setAlpha(1f);
        txtMaxRondas.setAlpha(1f);
    }

    private void actualizarTextoRondas() {
        txtMaxRondas.setText(String.valueOf(maxRondas));
    }

    private void asegurarMinimoDosJugadores() {
        while (filasJugadores.size() < 2) {
            agregarFilaJugador();
        }
    }

    private void intentarEvitarJugadorRepetidoEnCricket() {
        if (filasJugadores.size() < 2 || nombresJugadores.size() < 2) {
            return;
        }

        Spinner spnJugador1 = filasJugadores.get(0).findViewById(R.id.spnJugador);
        Spinner spnJugador2 = filasJugadores.get(1).findViewById(R.id.spnJugador);

        String jugador1 = spnJugador1.getSelectedItem().toString();
        String jugador2 = spnJugador2.getSelectedItem().toString();

        if (!jugador1.equals(jugador2)) {
            return;
        }

        for (int i = 0; i < spnJugador2.getCount(); i++) {
            String candidato = spnJugador2.getItemAtPosition(i).toString();

            if (!candidato.equals(jugador1)) {
                spnJugador2.setSelection(i);
                return;
            }
        }
    }

    // Jugadores -----------------------------------------------------------------

    private void agregarFilaJugador() {
        LayoutInflater inflater = LayoutInflater.from(this);

        View fila = inflater.inflate(
                R.layout.item_jugador_partida,
                contenedorJugadores,
                false
        );

        ImageButton btnColorJugador = fila.findViewById(R.id.btnColorJugador);
        TextView txtNombreJugador = fila.findViewById(R.id.txtNombreJugador);
        Spinner spnJugador = fila.findViewById(R.id.spnJugador);
        TextView btnEliminarJugador = fila.findViewById(R.id.btnEliminarJugador);

        configurarColorInicialJugador(btnColorJugador, txtNombreJugador);
        configurarSpinnerJugador(spnJugador);
        configurarSelectorColor(btnColorJugador, txtNombreJugador);
        configurarBotonEliminarJugador(fila, btnEliminarJugador);

        filasJugadores.add(fila);
        contenedorJugadores.addView(fila);

        actualizarTextoJugadores();
        actualizarVisibilidadBotonesEliminar();
    }

    private void configurarColorInicialJugador(ImageButton btnColorJugador, TextView txtNombreJugador) {
        int posicionColor = filasJugadores.size() % coloresJugadores.length;
        int colorRecurso = coloresJugadores[posicionColor];
        int colorReal = ContextCompat.getColor(this, colorRecurso);

        btnColorJugador.setImageTintList(ColorStateList.valueOf(colorReal));
        txtNombreJugador.setTextColor(colorReal);
        btnColorJugador.setTag(colorRecurso);
    }

    private void configurarSpinnerJugador(Spinner spnJugador) {
        ArrayAdapter<String> adapterJugadores = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nombresJugadores
        );

        adapterJugadores.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnJugador.setAdapter(adapterJugadores);
    }

    private void configurarSelectorColor(ImageButton btnColorJugador, TextView txtNombreJugador) {
        btnColorJugador.setOnClickListener(v -> new MaterialAlertDialogBuilder(this, R.style.TemaDialogoDardos)
                .setTitle("Selecciona un color")
                .setItems(nombresColores, (dialog, which) -> {
                    int colorRecurso = coloresJugadores[which];
                    int colorSeleccionado = ContextCompat.getColor(this, colorRecurso);

                    btnColorJugador.setImageTintList(ColorStateList.valueOf(colorSeleccionado));
                    txtNombreJugador.setTextColor(colorSeleccionado);
                    btnColorJugador.setTag(colorRecurso);
                })
                .show());
    }

    private void configurarBotonEliminarJugador(View fila, TextView btnEliminarJugador) {
        btnEliminarJugador.setOnClickListener(v -> {
            if (filasJugadores.size() <= 1) {
                Toast.makeText(this, "Debe haber al menos un jugador", Toast.LENGTH_SHORT).show();
                return;
            }

            if (esModoCricket(obtenerModoJuegoSeleccionado()) && filasJugadores.size() <= 2) {
                Toast.makeText(this, "Cricket necesita al menos dos jugadores", Toast.LENGTH_SHORT).show();
                return;
            }

            contenedorJugadores.removeView(fila);
            filasJugadores.remove(fila);

            actualizarTextoJugadores();
            actualizarVisibilidadBotonesEliminar();
        });
    }

    private void actualizarTextoJugadores() {
        for (int i = 0; i < filasJugadores.size(); i++) {
            View fila = filasJugadores.get(i);
            TextView txtNombreJugador = fila.findViewById(R.id.txtNombreJugador);
            txtNombreJugador.setText("Jugador " + (i + 1) + ":");
        }
    }

    private void actualizarVisibilidadBotonesEliminar() {
        boolean modoCricket = esModoCricket(obtenerModoJuegoSeleccionado());

        for (View fila : filasJugadores) {
            TextView btnEliminarJugador = fila.findViewById(R.id.btnEliminarJugador);

            if (filasJugadores.size() <= 1) {
                btnEliminarJugador.setVisibility(View.GONE);
            } else if (modoCricket && filasJugadores.size() <= 2) {
                btnEliminarJugador.setVisibility(View.GONE);
            } else {
                btnEliminarJugador.setVisibility(View.VISIBLE);
            }
        }
    }

    private ArrayList<String> obtenerNombresJugadoresSeleccionados() {
        ArrayList<String> nombresSeleccionados = new ArrayList<>();

        for (View fila : filasJugadores) {
            Spinner spnJugador = fila.findViewById(R.id.spnJugador);
            nombresSeleccionados.add(spnJugador.getSelectedItem().toString());
        }

        return nombresSeleccionados;
    }

    private ArrayList<Integer> obtenerColoresJugadoresSeleccionados() {
        ArrayList<Integer> coloresSeleccionados = new ArrayList<>();

        for (View fila : filasJugadores) {
            ImageButton btnColorJugador = fila.findViewById(R.id.btnColorJugador);
            coloresSeleccionados.add((int) btnColorJugador.getTag());
        }

        return coloresSeleccionados;
    }

    // Última configuración ------------------------------------------------------

    private void guardarConfiguracionUltimaPartida() {
        String modoJuego = obtenerModoJuegoSeleccionado();
        ArrayList<String> nombresSeleccionados = obtenerNombresJugadoresSeleccionados();
        ArrayList<Integer> coloresSeleccionados = obtenerColoresJugadoresSeleccionados();

        editorUltimaPartida.putString(EXTRA_MODO_JUEGO, modoJuego);
        editorUltimaPartida.putInt(EXTRA_MAX_RONDAS, maxRondas);
        editorUltimaPartida.putInt(EXTRA_NUMERO_JUGADORES, filasJugadores.size());
        editorUltimaPartida.putInt(EXTRA_NUMERO_DARDOS, obtenerNumeroDardosSeleccionado());
        editorUltimaPartida.putBoolean(EXTRA_CIERRE_DOBLE, switchCierreDoble.isChecked());
        editorUltimaPartida.putBoolean(
                EXTRA_MANTENER_MULTIPLICADOR,
                switchMantenerMultiplicador.isChecked()
        );
        editorUltimaPartida.putBoolean(EXTRA_ORDEN_ALEATORIO, switchOrdenAleatorio.isChecked());

        for (int i = 0; i < nombresSeleccionados.size(); i++) {
            editorUltimaPartida.putString(EXTRA_JUGADOR + (i + 1), nombresSeleccionados.get(i));
            editorUltimaPartida.putInt(EXTRA_COLOR_JUGADOR + (i + 1), coloresSeleccionados.get(i));
        }

        editorUltimaPartida.apply();
    }

    private void cargarUltimaConfiguracion() {
        if (!spUltimaPartida.contains(EXTRA_MODO_JUEGO)) {
            Toast.makeText(this, "No hay una configuración anterior guardada", Toast.LENGTH_SHORT).show();
            return;
        }

        cargandoConfiguracion = true;

        String modoJuegoGuardado = spUltimaPartida.getString(EXTRA_MODO_JUEGO, MODO_501);
        int rondasGuardadas = spUltimaPartida.getInt(EXTRA_MAX_RONDAS, RONDAS_ESTANDAR);
        int numeroJugadoresGuardado = spUltimaPartida.getInt(EXTRA_NUMERO_JUGADORES, 1);

        int dardosGuardados = Math.max(1, Math.min(4,
                spUltimaPartida.getInt(EXTRA_NUMERO_DARDOS, 3)));

        spnNumeroDardos.setSelection(dardosGuardados - 1);
        switchCierreDoble.setChecked(spUltimaPartida.getBoolean(EXTRA_CIERRE_DOBLE, false));
        switchMantenerMultiplicador.setChecked(
                spUltimaPartida.getBoolean(
                        EXTRA_MANTENER_MULTIPLICADOR,
                        spAjustes.getBoolean(PREF_MANTENER_MULTIPLICADOR, false)
                )
        );
        switchOrdenAleatorio.setChecked(spUltimaPartida.getBoolean(EXTRA_ORDEN_ALEATORIO, false));

        seleccionarModoJuegoEnSpinner(modoJuegoGuardado);

        contenedorJugadores.removeAllViews();
        filasJugadores.clear();

        for (int i = 1; i <= numeroJugadoresGuardado; i++) {
            agregarFilaJugador();

            View fila = filasJugadores.get(i - 1);
            Spinner spnJugador = fila.findViewById(R.id.spnJugador);
            ImageButton btnColorJugador = fila.findViewById(R.id.btnColorJugador);
            TextView txtNombreJugador = fila.findViewById(R.id.txtNombreJugador);

            String nombreJugador = spUltimaPartida.getString(EXTRA_JUGADOR + i, "Jugador");
            int colorJugador = spUltimaPartida.getInt(EXTRA_COLOR_JUGADOR + i, coloresJugadores[0]);

            seleccionarJugadorEnSpinner(spnJugador, nombreJugador);
            aplicarColorAFila(btnColorJugador, txtNombreJugador, colorJugador);
        }

        maxRondas = rondasGuardadas;
        actualizarTextoRondas();

        cargandoConfiguracion = false;

        aplicarConfiguracionSegunModo(modoJuegoGuardado);

        Toast.makeText(this, "Última configuración cargada", Toast.LENGTH_SHORT).show();
    }

    private void seleccionarModoJuegoEnSpinner(String modoJuego) {
        for (int i = 0; i < spnModoJuego.getCount(); i++) {
            if (spnModoJuego.getItemAtPosition(i).toString().equals(modoJuego)) {
                spnModoJuego.setSelection(i);
                return;
            }
        }
    }

    private void seleccionarJugadorEnSpinner(Spinner spinner, String nombreJugador) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(nombreJugador)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void aplicarColorAFila(
            ImageButton btnColorJugador,
            TextView txtNombreJugador,
            int colorRecurso
    ) {
        int colorReal = ContextCompat.getColor(this, colorRecurso);

        btnColorJugador.setImageTintList(ColorStateList.valueOf(colorReal));
        txtNombreJugador.setTextColor(colorReal);
        btnColorJugador.setTag(colorRecurso);
    }

    // Validación ----------------------------------------------------------------

    private boolean validarConfiguracionPartida() {
        if (filasJugadores.isEmpty()) {
            Toast.makeText(this, "Debes seleccionar al menos un jugador", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (esModoCricket(obtenerModoJuegoSeleccionado())) {
            if (filasJugadores.size() < 2) {
                Toast.makeText(this, "Cricket necesita al menos dos jugadores", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (hayJugadoresRepetidos()) {
                Toast.makeText(this, "En Cricket no puedes repetir el mismo jugador", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private boolean hayJugadoresRepetidos() {
        ArrayList<String> nombresSeleccionados = obtenerNombresJugadoresSeleccionados();

        for (int i = 0; i < nombresSeleccionados.size(); i++) {
            for (int j = i + 1; j < nombresSeleccionados.size(); j++) {
                if (nombresSeleccionados.get(i).equals(nombresSeleccionados.get(j))) {
                    return true;
                }
            }
        }

        return false;
    }

    private void mostrarDialogoMuchasRondas() {
        new MaterialAlertDialogBuilder(this, R.style.TemaDialogoDardos)
                .setTitle("Muchas rondas")
                .setMessage(
                        "Has seleccionado " + maxRondas +
                                " rondas. La partida puede durar bastante tiempo.\n\n" +
                                "¿Deseas continuar?"
                )
                .setPositiveButton("Continuar", (dialog, which) -> crearPartida())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Crear partida -------------------------------------------------------------

    private void crearPartida() {
        String modoJuego = obtenerModoJuegoSeleccionado();

        guardarConfiguracionUltimaPartida();

        Intent intent = crearIntentSegunModo(modoJuego);

        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Modo de juego no disponible todavía", Toast.LENGTH_SHORT).show();
        }
    }

    private Intent crearIntentSegunModo(String modoJuego) {
        Intent intent = null;

        switch (modoJuego) {

            case MODO_301:
            case MODO_501:
                intent = new Intent(this, PartidaPuntosActivity.class);
                break;

            case MODO_CRICKET:
            case MODO_CUT_THROAT:
                intent = new Intent(this, PartidaCriquetActivity.class);
                break;

            case MODO_DOUBLE_DOWN:
            case MODO_AROUND_CLOCK:
            case MODO_SHANGHAI:
                intent = new Intent(this, PartidaRondasActivity.class);
                break;
        }

        if (intent != null) {
            cargarExtrasEnIntent(intent, modoJuego);
        }

        return intent;
    }

    private void cargarExtrasEnIntent(Intent intent, String modoJuego) {
        ArrayList<String> nombresSeleccionados = obtenerNombresJugadoresSeleccionados();
        ArrayList<Integer> coloresSeleccionados = obtenerColoresJugadoresSeleccionados();

        if (switchOrdenAleatorio.isChecked()) {
            ArrayList<Integer> orden = new ArrayList<>();

            for (int i = 0; i < nombresSeleccionados.size(); i++) {
                orden.add(i);
            }

            Collections.shuffle(orden);

            ArrayList<String> nombresMezclados = new ArrayList<>();
            ArrayList<Integer> coloresMezclados = new ArrayList<>();

            for (Integer indice : orden) {
                nombresMezclados.add(nombresSeleccionados.get(indice));
                coloresMezclados.add(coloresSeleccionados.get(indice));
            }

            nombresSeleccionados = nombresMezclados;
            coloresSeleccionados = coloresMezclados;
        }

        intent.putExtra(EXTRA_MODO_JUEGO, modoJuego);
        intent.putExtra(EXTRA_MAX_RONDAS, maxRondas);
        intent.putExtra(EXTRA_NUMERO_JUGADORES, nombresSeleccionados.size());

        intent.putExtra(EXTRA_NUMERO_DARDOS, obtenerNumeroDardosSeleccionado());
        intent.putExtra(EXTRA_CIERRE_DOBLE, switchCierreDoble.isChecked());
        intent.putExtra(
                EXTRA_MANTENER_MULTIPLICADOR,
                switchMantenerMultiplicador.isChecked()
        );
        intent.putExtra(EXTRA_ORDEN_ALEATORIO, switchOrdenAleatorio.isChecked());

        for (int i = 0; i < nombresSeleccionados.size(); i++) {
            intent.putExtra(EXTRA_JUGADOR + (i + 1), nombresSeleccionados.get(i));
            intent.putExtra(EXTRA_COLOR_JUGADOR + (i + 1), coloresSeleccionados.get(i));
        }

        intent.putStringArrayListExtra("nombresJugadores", nombresSeleccionados);
        intent.putIntegerArrayListExtra("colorJugador", coloresSeleccionados);

        // Lista usada actualmente por PartidaPuntosActivity.
        intent.putStringArrayListExtra(
                PartidaPuntosActivity.EXTRA_NOMBRES_JUGADORES,
                nombresSeleccionados
        );
    }

    // Utilidades ----------------------------------------------------------------

    private String obtenerModoJuegoSeleccionado() {
        return spnModoJuego.getSelectedItem().toString();
    }

    private int obtenerNumeroDardosSeleccionado() {
        Object seleccionado = spnNumeroDardos.getSelectedItem();

        if (seleccionado instanceof Integer) {
            return (Integer) seleccionado;
        }

        return 3;
    }
}