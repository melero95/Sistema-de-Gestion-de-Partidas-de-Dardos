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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.productos.juegosdedardos.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelos.Jugador;
import modelos.ConfiguracionPartida;
import sqlite.JugadorDao;
import utilidades.SerializadorConfiguracionPartida;

/**
 * Configura una nueva partida usando los jugadores ACTIVOS registrados en SQLite.
 * Conserva las extras existentes de nombres y colores (R.color) para no romper
 * los tres motores de partida; adicionalmente transmite los ID persistentes.
 */
public class ConfigurarNuevaPartidaActivity extends AppCompatActivity {

    private Spinner spnModoJuego;
    private TextView txtMaxRondas;
    private TextView btnRestarRonda;
    private TextView btnSumarRonda;
    private TextView txtAvisoSinJugadores;
    private LinearLayout contenedorJugadores;

    private Button btnAnadirJugador;
    private Button btnGestionarJugadores;
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

    private static final String MODO_301 = "301";
    private static final String MODO_501 = "501";
    private static final String MODO_CRICKET = "Cricket";
    private static final String MODO_CUT_THROAT = "Cut Throat Cricket";
    private static final String MODO_DOUBLE_DOWN = "Double Down";
    private static final String MODO_AROUND_CLOCK = "Around the Clock";
    private static final String MODO_SHANGHAI = "Shanghai";

    private static final String EXTRA_MODO_JUEGO = "modoJuego";
    private static final String EXTRA_MAX_RONDAS = "maxRondas";
    private static final String EXTRA_NUMERO_JUGADORES = "numeroJugadores";
    private static final String EXTRA_JUGADOR = "jugador";
    private static final String EXTRA_COLOR_JUGADOR = "colorJugador";

    // Nuevos identificadores para las fases 04-06.
    public static final String EXTRA_ID_JUGADOR = "idJugador";
    public static final String EXTRA_IDS_JUGADORES = "idsJugadores";

    public static final String EXTRA_NUMERO_DARDOS = "numeroDardos";
    public static final String EXTRA_CIERRE_DOBLE = "cierreDoble";
    public static final String EXTRA_MANTENER_MULTIPLICADOR = "mantenerMultiplicador";
    public static final String EXTRA_ORDEN_ALEATORIO = "ordenAleatorio";
    // Permite trasladar la fotografía histórica, sin sobreescribir SharedPreferences.
    public static final String EXTRA_CONFIGURACION_JSON = "configuracionJson";

    private static final String SP_ULTIMA_PARTIDA = "configuracion_ultima_partida";
    private static final String SP_AJUSTES = "ajustes_partida";
    private static final String PREF_JUGADOR = AjustesActivity.PREF_JUGADOR;
    private static final String PREF_JUGADOR_ID = AjustesActivity.PREF_JUGADOR_ID;
    private static final String PREF_MODO = "modo_default";
    private static final String PREF_DARDOS = "dardos_default";
    private static final String PREF_CIERRE_DOBLE = "cierre_doble_default";
    private static final String PREF_MANTENER_MULTIPLICADOR = "mantener_multiplicador";
    private static final String PREF_ORDEN_ALEATORIO = "orden_aleatorio_default";

    private static final int RONDAS_ESTANDAR = 15;
    private static final int RONDAS_DOUBLE_DOWN = 9;
    private static final int RONDAS_AROUND_CLOCK = 21;
    private static final int RONDAS_SHANGHAI = 21;
    private static final int MAX_JUGADORES = 6;

    private int maxRondas = RONDAS_ESTANDAR;
    private boolean cargandoConfiguracion = false;
    private boolean pantallaInicializada = false;
    private boolean actualizandoSelectores = false;
    // El orden histórico ya está resuelto, aunque la partida usara sorteo.
    private boolean conservarOrdenHistorico = false;
    private String ultimoModoAplicado;

    private final ArrayList<View> filasJugadores = new ArrayList<>();
    private final ArrayList<Jugador> jugadoresActivos = new ArrayList<>();

    private JugadorDao jugadorDao;
    private SharedPreferences spUltimaPartida;
    private SharedPreferences spAjustes;

    // Los motores de juego siguen recibiendo IDs R.color, no enteros ARGB.
    private final int[] coloresJugadores = {
            R.color.jugador_rojo, R.color.jugador_verde,
            R.color.jugador_azul, R.color.jugador_amarillo,
            R.color.jugador_naranja, R.color.jugador_morado,
            R.color.jugador_cyan, R.color.jugador_blanco
    };

    private final String[] nombresColores = {
            "Rojo", "Verde", "Azul", "Amarillo", "Naranja", "Morado", "Cyan", "Blanco"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.blue));
        setContentView(R.layout.activity_nueva_partida);

        jugadorDao = new JugadorDao(this);
        inicializarComponentes();
        spUltimaPartida = getSharedPreferences(SP_ULTIMA_PARTIDA, MODE_PRIVATE);
        spAjustes = getSharedPreferences(SP_AJUSTES, MODE_PRIVATE);

        cargarJugadoresActivos();
        cargarSpinnerModoJuego();
        configurarCambioModoJuego();
        configurarBotonesRondas();
        configurarBotonesPrincipales();
        configurarAjustesAvanzados();
        cargarValoresPredeterminadosDesdeAjustes();

        if (!jugadoresActivos.isEmpty()) {
            agregarFilaJugador();
            aplicarJugadorPredeterminadoPrimeraFila();
        }
        aplicarConfiguracionSegunModo(obtenerModoJuegoSeleccionado());
        actualizarDisponibilidadJugadores();
        if (getIntent() != null && getIntent().hasExtra(EXTRA_CONFIGURACION_JSON)) {
            cargarConfiguracionHistorica(getIntent().getStringExtra(EXTRA_CONFIGURACION_JSON));
        }
        pantallaInicializada = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Permite registrar, editar o desactivar desde JugadoresActivity y volver.
        if (pantallaInicializada) {
            refrescarJugadoresActivosConservandoSeleccion();
        }
    }

    @Override
    protected void onDestroy() {
        if (jugadorDao != null) {
            jugadorDao.close();
        }
        super.onDestroy();
    }

    private void inicializarComponentes() {
        spnModoJuego = findViewById(R.id.spnModoJuego);
        txtMaxRondas = findViewById(R.id.txtMaxRondas);
        btnRestarRonda = findViewById(R.id.btnRestarRonda);
        btnSumarRonda = findViewById(R.id.btnSumarRonda);
        contenedorJugadores = findViewById(R.id.contenedorJugadores);
        txtAvisoSinJugadores = findViewById(R.id.txtAvisoSinJugadores);
        btnAnadirJugador = findViewById(R.id.btnAnadirJugador);
        btnGestionarJugadores = findViewById(R.id.btnGestionarJugadores);
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

    private void cargarJugadoresActivos() {
        List<Jugador> desdeBd = jugadorDao.listarActivos();
        jugadoresActivos.clear();
        jugadoresActivos.addAll(desdeBd);
    }

    private void actualizarDisponibilidadJugadores() {
        boolean hayActivos = !jugadoresActivos.isEmpty();
        txtAvisoSinJugadores.setVisibility(hayActivos ? View.GONE : View.VISIBLE);
        btnCrearPartida.setEnabled(hayActivos);
        btnAnadirJugador.setEnabled(hayActivos && filasJugadores.size() < limiteJugadores());
        btnAnadirJugador.setAlpha(btnAnadirJugador.isEnabled() ? 1f : 0.55f);
    }

    private int limiteJugadores() {
        return Math.min(MAX_JUGADORES, jugadoresActivos.size());
    }

    private void refrescarJugadoresActivosConservandoSeleccion() {
        ArrayList<Long> idsPrevios = obtenerIdsJugadoresSeleccionados();
        ArrayList<Integer> coloresPrevios = obtenerColoresJugadoresSeleccionados();
        cargarJugadoresActivos();

        if (jugadoresActivos.isEmpty()) {
            filasJugadores.clear();
            contenedorJugadores.removeAllViews();
            actualizarDisponibilidadJugadores();
            return;
        }

        while (filasJugadores.size() > limiteJugadores()) {
            quitarUltimaFila();
        }
        if (filasJugadores.isEmpty()) {
            agregarFilaJugador();
            aplicarJugadorPredeterminadoPrimeraFila();
        } else {
            Set<Long> idsOcupados = new HashSet<>();
            for (int i = 0; i < filasJugadores.size(); i++) {
                View fila = filasJugadores.get(i);
                Spinner spinner = fila.findViewById(R.id.spnJugador);
                ImageButton botonColor = fila.findViewById(R.id.btnColorJugador);
                TextView etiqueta = fila.findViewById(R.id.txtNombreJugador);
                long idAnterior = i < idsPrevios.size() ? idsPrevios.get(i) : 0L;
                int posicion = buscarPosicionPorId(idAnterior);
                if (posicion < 0 || idsOcupados.contains(idAnterior)) {
                    posicion = primeraPosicionDisponible(idsOcupados);
                }
                prepararSpinner(spinner, botonColor, etiqueta, posicion);
                Jugador seleccionado = obtenerJugadorSeleccionado(spinner);
                if (seleccionado != null) {
                    idsOcupados.add(seleccionado.getId());
                }
                if (seleccionado != null && seleccionado.getId() == idAnterior
                        && i < coloresPrevios.size()) {
                    aplicarColorAFila(botonColor, etiqueta, coloresPrevios.get(i));
                }
            }
        }

        if (esModoCricket(obtenerModoJuegoSeleccionado())) {
            asegurarMinimoDosJugadores();
        }
        actualizarTextoJugadores();
        actualizarVisibilidadBotonesEliminar();
        actualizarOpcionesSpinners();
        actualizarDisponibilidadJugadores();
    }

    private void cargarSpinnerModoJuego() {
        ArrayList<String> modos = new ArrayList<>();
        Collections.addAll(modos, MODO_301, MODO_501, MODO_CRICKET,
                MODO_CUT_THROAT, MODO_DOUBLE_DOWN, MODO_AROUND_CLOCK, MODO_SHANGHAI);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, modos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnModoJuego.setAdapter(adapter);
    }

    private void configurarCambioModoJuego() {
        spnModoJuego.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String modo = obtenerModoJuegoSeleccionado();
                if (!cargandoConfiguracion && !modo.equals(ultimoModoAplicado)) {
                    conservarOrdenHistorico = false;
                    aplicarConfiguracionSegunModo(modo);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
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
        btnAnadirJugador.setOnClickListener(v -> {
            if (filasJugadores.size() >= limiteJugadores()) {
                Toast.makeText(this, "No hay más jugadores activos disponibles", Toast.LENGTH_SHORT).show();
                return;
            }
            agregarFilaJugador();
            // Abrir inmediatamente el selector de la nueva fila.
            // El usuario puede elegir cualquier jugador activo disponible.
            if (!filasJugadores.isEmpty()) {
                View nuevaFila = filasJugadores.get(filasJugadores.size() - 1);
                Spinner selector = nuevaFila.findViewById(R.id.spnJugador);
                selector.post(selector::performClick);
            }
        });
        btnGestionarJugadores.setOnClickListener(v ->
                startActivity(new Intent(this, JugadoresActivity.class)));
        btnCancelarPartida.setOnClickListener(v -> finish());
        btnCrearPartida.setOnClickListener(v -> {
            if (!validarConfiguracionPartida()) return;
            if (maxRondas >= 20 && rondasEditablesParaModo(obtenerModoJuegoSeleccionado())) {
                mostrarDialogoMuchasRondas();
            } else {
                crearPartida();
            }
        });
        btnUltimaConfiguracion.setOnClickListener(v -> cargarUltimaConfiguracion());
    }

    private void configurarAjustesAvanzados() {
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new Integer[]{1, 2, 3, 4});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnNumeroDardos.setAdapter(adapter);
        switchOrdenAleatorio.setOnCheckedChangeListener((buttonView, checked) -> {
            if (!cargandoConfiguracion) conservarOrdenHistorico = false;
        });
        btnMostrarAjustesAvanzados.setOnClickListener(v -> {
            boolean mostrar = contenedorAjustesAvanzados.getVisibility() != View.VISIBLE;
            contenedorAjustesAvanzados.setVisibility(mostrar ? View.VISIBLE : View.GONE);
            btnMostrarAjustesAvanzados.setText(mostrar ? "Menos ajustes ▲" : "Más ajustes ▼");
        });
    }

    private void cargarValoresPredeterminadosDesdeAjustes() {
        cargandoConfiguracion = true;
        seleccionarModoJuegoEnSpinner(spAjustes.getString(PREF_MODO, MODO_501));
        int dardos = Math.max(1, Math.min(4, spAjustes.getInt(PREF_DARDOS, 3)));
        spnNumeroDardos.setSelection(dardos - 1);
        switchCierreDoble.setChecked(spAjustes.getBoolean(PREF_CIERRE_DOBLE, false));
        switchMantenerMultiplicador.setChecked(spAjustes.getBoolean(PREF_MANTENER_MULTIPLICADOR, false));
        switchOrdenAleatorio.setChecked(spAjustes.getBoolean(PREF_ORDEN_ALEATORIO, false));
        cargandoConfiguracion = false;
    }

    private void aplicarJugadorPredeterminadoPrimeraFila() {
        if (filasJugadores.isEmpty() || jugadoresActivos.isEmpty()) return;

        Spinner spinner = filasJugadores.get(0).findViewById(R.id.spnJugador);
        long idPredeterminado = spAjustes.getLong(PREF_JUGADOR_ID, 0L);
        boolean seleccionado = false;

        if (idPredeterminado > 0L) {
            // Si el ID fue desactivado, se mantiene el primer jugador activo.
            // No se reutiliza el nombre antiguo, que podria pertenecer a otro.
            seleccionado = seleccionarJugadorPorId(spinner, idPredeterminado);
        } else {
            // Compatibilidad con preferencias guardadas antes de esta fase.
            String nombreAntiguo = spAjustes.getString(PREF_JUGADOR, "");
            seleccionado = seleccionarJugadorPorNombre(spinner, nombreAntiguo);
        }

        if (!seleccionado) spinner.setSelection(0);
        Jugador elegido = obtenerJugadorSeleccionado(spinner);
        if (elegido != null) {
            View fila = filasJugadores.get(0);
            aplicarColorAFila(fila.findViewById(R.id.btnColorJugador),
                    fila.findViewById(R.id.txtNombreJugador),
                    recursoColorParaArgb(elegido.getColor()));
            spinner.setTag(elegido.getId());
        }
        actualizarOpcionesSpinners();
    }

    private void aplicarConfiguracionSegunModo(String modo) {
        ultimoModoAplicado = modo;
        boolean esPuntos = MODO_301.equals(modo) || MODO_501.equals(modo);
        switchCierreDoble.setVisibility(esPuntos ? View.VISIBLE : View.GONE);
        switchMantenerMultiplicador.setVisibility(
                esPuntos || esModoCricket(modo) ? View.VISIBLE : View.GONE);
        txtTituloNumeroDardos.setVisibility(View.VISIBLE);
        spnNumeroDardos.setVisibility(View.VISIBLE);
        switchOrdenAleatorio.setVisibility(View.VISIBLE);

        if (MODO_DOUBLE_DOWN.equals(modo)) bloquearRondas(RONDAS_DOUBLE_DOWN);
        else if (MODO_AROUND_CLOCK.equals(modo)) bloquearRondas(RONDAS_AROUND_CLOCK);
        else if (MODO_SHANGHAI.equals(modo)) bloquearRondas(RONDAS_SHANGHAI);
        else if (esPuntos) {
            maxRondas = RONDAS_ESTANDAR;
            actualizarTextoRondas();
            desbloquearRondas();
        } else desbloquearRondas();

        if (esModoCricket(modo)) asegurarMinimoDosJugadores();
        actualizarVisibilidadBotonesEliminar();
    }

    private boolean esModoCricket(String modo) {
        return MODO_CRICKET.equals(modo) || MODO_CUT_THROAT.equals(modo);
    }

    private boolean rondasEditablesParaModo(String modo) {
        return !MODO_DOUBLE_DOWN.equals(modo) && !MODO_AROUND_CLOCK.equals(modo)
                && !MODO_SHANGHAI.equals(modo);
    }

    private boolean rondasBloqueadas() {
        return !btnSumarRonda.isEnabled() || !btnRestarRonda.isEnabled();
    }

    private void bloquearRondas(int rondas) {
        maxRondas = rondas;
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
        while (filasJugadores.size() < 2 && filasJugadores.size() < limiteJugadores()) {
            agregarFilaJugador();
        }
    }

    private void agregarFilaJugador() {
        if (filasJugadores.size() >= limiteJugadores()) return;
        View fila = LayoutInflater.from(this)
                .inflate(R.layout.item_jugador_partida, contenedorJugadores, false);
        ImageButton botonColor = fila.findViewById(R.id.btnColorJugador);
        TextView etiqueta = fila.findViewById(R.id.txtNombreJugador);
        Spinner spinner = fila.findViewById(R.id.spnJugador);
        TextView btnEliminar = fila.findViewById(R.id.btnEliminarJugador);

        Set<Long> yaSeleccionados = new HashSet<>(obtenerIdsJugadoresSeleccionados());
        prepararSpinner(spinner, botonColor, etiqueta, primeraPosicionDisponible(yaSeleccionados));
        botonColor.setOnClickListener(v -> new MaterialAlertDialogBuilder(this,
                R.style.TemaDialogoDardos)
                .setTitle("Selecciona un color")
                .setItems(nombresColores, (dialog, which) ->
                        aplicarColorAFila(botonColor, etiqueta, coloresJugadores[which]))
                .show());
        btnEliminar.setOnClickListener(v -> {
            int minimo = esModoCricket(obtenerModoJuegoSeleccionado()) ? 2 : 1;
            if (filasJugadores.size() <= minimo) {
                Toast.makeText(this, "Este modo necesita al menos " + minimo
                        + " jugador(es)", Toast.LENGTH_SHORT).show();
                return;
            }
            contenedorJugadores.removeView(fila);
            filasJugadores.remove(fila);
            actualizarOpcionesSpinners();
            actualizarTextoJugadores();
            actualizarVisibilidadBotonesEliminar();
            actualizarDisponibilidadJugadores();
        });
        filasJugadores.add(fila);
        contenedorJugadores.addView(fila);
        actualizarOpcionesSpinners();
        actualizarTextoJugadores();
        actualizarVisibilidadBotonesEliminar();
        actualizarDisponibilidadJugadores();
    }

    /**
     * Inicializa el selector con todos los jugadores activos. Una vez añadida
     * la fila al contenedor, actualizarOpcionesSpinners() restringe cada lista
     * al jugador propio y a los que no están ocupados por otros participantes.
     */
    private void prepararSpinner(Spinner spinner, ImageButton botonColor,
                                 TextView etiqueta, int posicionSeleccion) {
        spinner.setOnItemSelectedListener(null);
        ArrayAdapter<Jugador> adaptador = crearAdaptadorJugadores(jugadoresActivos);
        spinner.setAdapter(adaptador);
        if (!jugadoresActivos.isEmpty()) {
            int posicion = Math.max(0, Math.min(posicionSeleccion,
                    jugadoresActivos.size() - 1));
            spinner.setSelection(posicion, false);
            Jugador elegido = jugadoresActivos.get(posicion);
            aplicarColorAFila(botonColor, etiqueta,
                    recursoColorParaArgb(elegido.getColor()));
            spinner.setTag(elegido.getId());
        }
        configurarListenerSpinner(spinner, botonColor, etiqueta);
    }

    private ArrayAdapter<Jugador> crearAdaptadorJugadores(List<Jugador> opciones) {
        ArrayAdapter<Jugador> adaptador = new ArrayAdapter<>(this,
                R.layout.item_spinner_jugador, new ArrayList<>(opciones));
        adaptador.setDropDownViewResource(R.layout.item_spinner_jugador_desplegable);
        return adaptador;
    }

    /**
     * En cada fila aparecen solo el jugador seleccionado en esa misma fila
     * y los jugadores activos que no estén asignados a ninguna otra fila.
     * Se utilizan IDs, nunca nombres, para identificar duplicados.
     */
    private void actualizarOpcionesSpinners() {
        if (actualizandoSelectores || filasJugadores.isEmpty()) return;
        actualizandoSelectores = true;
        try {
            ArrayList<Long> ids = obtenerIdsJugadoresSeleccionados();
            for (int i = 0; i < filasJugadores.size(); i++) {
                View fila = filasJugadores.get(i);
                Spinner spinner = fila.findViewById(R.id.spnJugador);
                ImageButton botonColor = fila.findViewById(R.id.btnColorJugador);
                TextView etiqueta = fila.findViewById(R.id.txtNombreJugador);
                long idActual = ids.get(i);
                Set<Long> ocupadosPorOtros = new HashSet<>(ids);
                ocupadosPorOtros.remove(idActual);
                List<Jugador> opciones = filtrarDisponibles(
                        jugadoresActivos, ocupadosPorOtros);

                spinner.setOnItemSelectedListener(null);
                spinner.setAdapter(crearAdaptadorJugadores(opciones));
                int posicion = buscarPosicionEnSpinner(spinner, idActual);
                if (posicion >= 0) {
                    spinner.setSelection(posicion, false);
                }
                configurarListenerSpinner(spinner, botonColor, etiqueta);
            }
        } finally {
            actualizandoSelectores = false;
        }
    }

    // Separada de la UI para poder comprobar el filtrado mediante tests.
    static List<Jugador> filtrarDisponibles(
            List<Jugador> jugadores, Set<Long> ocupadosPorOtros) {
        List<Jugador> disponibles = new ArrayList<>();
        for (Jugador jugador : jugadores) {
            if (!ocupadosPorOtros.contains(jugador.getId())) {
                disponibles.add(jugador);
            }
        }
        return disponibles;
    }

    private void configurarListenerSpinner(Spinner spinner, ImageButton botonColor,
                                            TextView etiqueta) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (actualizandoSelectores || cargandoConfiguracion) return;
                Jugador elegido = (Jugador) parent.getItemAtPosition(position);
                if (elegido == null) return;
                Object anterior = spinner.getTag();
                if (anterior instanceof Long && ((Long) anterior) == elegido.getId()) return;
                conservarOrdenHistorico = false;

                // El color de la partida solo se reinicia si cambia el jugador.
                aplicarColorAFila(botonColor, etiqueta,
                        recursoColorParaArgb(elegido.getColor()));
                spinner.setTag(elegido.getId());
                actualizarOpcionesSpinners();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private int buscarPosicionEnSpinner(Spinner spinner, long idJugador) {
        for (int i = 0; i < spinner.getCount(); i++) {
            Jugador jugador = (Jugador) spinner.getItemAtPosition(i);
            if (jugador.getId() == idJugador) return i;
        }
        return -1;
    }

    private int recursoColorParaArgb(int colorArgb) {
        for (int recurso : coloresJugadores) {
            if (ContextCompat.getColor(this, recurso) == colorArgb) return recurso;
        }
        return R.color.jugador_blanco;
    }

    private void aplicarColorAFila(ImageButton botonColor, TextView etiqueta, int colorRecurso) {
        if (!esRecursoColorPermitido(colorRecurso)) colorRecurso = R.color.jugador_blanco;
        int colorArgb = ContextCompat.getColor(this, colorRecurso);
        botonColor.setImageTintList(ColorStateList.valueOf(colorArgb));
        etiqueta.setTextColor(colorArgb);
        botonColor.setTag(colorRecurso);
    }

    private boolean esRecursoColorPermitido(int recurso) {
        for (int candidato : coloresJugadores) if (recurso == candidato) return true;
        return false;
    }

    private Jugador obtenerJugadorSeleccionado(Spinner spinner) {
        Object valor = spinner.getSelectedItem();
        return valor instanceof Jugador ? (Jugador) valor : null;
    }

    private int buscarPosicionPorId(long idJugador) {
        for (int i = 0; i < jugadoresActivos.size(); i++) {
            if (jugadoresActivos.get(i).getId() == idJugador) return i;
        }
        return -1;
    }

    private int primeraPosicionDisponible(Set<Long> ocupados) {
        for (int i = 0; i < jugadoresActivos.size(); i++) {
            if (!ocupados.contains(jugadoresActivos.get(i).getId())) return i;
        }
        return 0;
    }

    private boolean seleccionarJugadorPorId(Spinner spinner, long id) {
        int posicion = buscarPosicionEnSpinner(spinner, id);
        if (posicion < 0) return false;
        spinner.setSelection(posicion);
        return true;
    }

    private boolean seleccionarJugadorPorNombre(Spinner spinner, String nombre) {
        if (nombre == null) return false;
        for (int i = 0; i < spinner.getCount(); i++) {
            Jugador candidato = (Jugador) spinner.getItemAtPosition(i);
            if (candidato.getNombre().equalsIgnoreCase(nombre.trim())) {
                spinner.setSelection(i);
                return true;
            }
        }
        return false;
    }

    private void quitarUltimaFila() {
        if (filasJugadores.isEmpty()) return;
        View fila = filasJugadores.remove(filasJugadores.size() - 1);
        contenedorJugadores.removeView(fila);
    }

    private void actualizarTextoJugadores() {
        for (int i = 0; i < filasJugadores.size(); i++) {
            TextView etiqueta = filasJugadores.get(i).findViewById(R.id.txtNombreJugador);
            etiqueta.setText("Jugador " + (i + 1) + ":");
        }
    }

    private void actualizarVisibilidadBotonesEliminar() {
        int minimo = esModoCricket(obtenerModoJuegoSeleccionado()) ? 2 : 1;
        for (View fila : filasJugadores) {
            TextView eliminar = fila.findViewById(R.id.btnEliminarJugador);
            eliminar.setVisibility(filasJugadores.size() <= minimo ? View.GONE : View.VISIBLE);
        }
    }

    private ArrayList<Long> obtenerIdsJugadoresSeleccionados() {
        ArrayList<Long> ids = new ArrayList<>();
        for (View fila : filasJugadores) {
            Jugador jugador = obtenerJugadorSeleccionado(fila.findViewById(R.id.spnJugador));
            ids.add(jugador == null ? 0L : jugador.getId());
        }
        return ids;
    }

    private ArrayList<String> obtenerNombresJugadoresSeleccionados() {
        ArrayList<String> nombres = new ArrayList<>();
        for (View fila : filasJugadores) {
            Jugador jugador = obtenerJugadorSeleccionado(fila.findViewById(R.id.spnJugador));
            nombres.add(jugador == null ? "" : jugador.getNombre());
        }
        return nombres;
    }

    private ArrayList<Integer> obtenerColoresJugadoresSeleccionados() {
        ArrayList<Integer> colores = new ArrayList<>();
        for (View fila : filasJugadores) {
            ImageButton boton = fila.findViewById(R.id.btnColorJugador);
            Object tag = boton.getTag();
            colores.add(tag instanceof Integer ? (Integer) tag : R.color.jugador_blanco);
        }
        return colores;
    }

    private void guardarConfiguracionUltimaPartida() {
        SharedPreferences.Editor editor = spUltimaPartida.edit();
        ArrayList<Long> ids = obtenerIdsJugadoresSeleccionados();
        ArrayList<String> nombres = obtenerNombresJugadoresSeleccionados();
        ArrayList<Integer> colores = obtenerColoresJugadoresSeleccionados();
        editor.putString(EXTRA_MODO_JUEGO, obtenerModoJuegoSeleccionado());
        editor.putInt(EXTRA_MAX_RONDAS, maxRondas);
        editor.putInt(EXTRA_NUMERO_JUGADORES, filasJugadores.size());
        editor.putInt(EXTRA_NUMERO_DARDOS, obtenerNumeroDardosSeleccionado());
        editor.putBoolean(EXTRA_CIERRE_DOBLE, switchCierreDoble.isChecked());
        editor.putBoolean(EXTRA_MANTENER_MULTIPLICADOR, switchMantenerMultiplicador.isChecked());
        editor.putBoolean(EXTRA_ORDEN_ALEATORIO, switchOrdenAleatorio.isChecked());
        for (int i = 0; i < nombres.size(); i++) {
            editor.putLong(EXTRA_ID_JUGADOR + (i + 1), ids.get(i));
            editor.putString(EXTRA_JUGADOR + (i + 1), nombres.get(i));
            editor.putInt(EXTRA_COLOR_JUGADOR + (i + 1), colores.get(i));
        }
        editor.apply();
    }

    private void cargarUltimaConfiguracion() {
        if (!spUltimaPartida.contains(EXTRA_MODO_JUEGO)) {
            Toast.makeText(this, "No hay una configuración anterior guardada", Toast.LENGTH_SHORT).show();
            return;
        }
        if (jugadoresActivos.isEmpty()) {
            Toast.makeText(this, "Registra primero un jugador activo", Toast.LENGTH_SHORT).show();
            return;
        }
        cargandoConfiguracion = true;
        conservarOrdenHistorico = false;
        String modo = spUltimaPartida.getString(EXTRA_MODO_JUEGO, MODO_501);
        int rondas = spUltimaPartida.getInt(EXTRA_MAX_RONDAS, RONDAS_ESTANDAR);
        int numero = Math.min(limiteJugadores(), Math.max(1,
                spUltimaPartida.getInt(EXTRA_NUMERO_JUGADORES, 1)));
        int dardos = Math.max(1, Math.min(4,
                spUltimaPartida.getInt(EXTRA_NUMERO_DARDOS, 3)));
        spnNumeroDardos.setSelection(dardos - 1);
        switchCierreDoble.setChecked(spUltimaPartida.getBoolean(EXTRA_CIERRE_DOBLE, false));
        switchMantenerMultiplicador.setChecked(spUltimaPartida.getBoolean(
                EXTRA_MANTENER_MULTIPLICADOR,
                spAjustes.getBoolean(PREF_MANTENER_MULTIPLICADOR, false)));
        switchOrdenAleatorio.setChecked(spUltimaPartida.getBoolean(EXTRA_ORDEN_ALEATORIO, false));
        seleccionarModoJuegoEnSpinner(modo);

        filasJugadores.clear();
        contenedorJugadores.removeAllViews();
        boolean sustituciones = false;
        Set<Long> ocupados = new HashSet<>();
        for (int i = 1; i <= numero; i++) {
            agregarFilaJugador();
            View fila = filasJugadores.get(i - 1);
            Spinner spinner = fila.findViewById(R.id.spnJugador);
            long idGuardado = spUltimaPartida.getLong(EXTRA_ID_JUGADOR + i, 0L);
            String nombreGuardado = spUltimaPartida.getString(EXTRA_JUGADOR + i, "");
            int posicion = buscarPosicionPorId(idGuardado);
            if (posicion < 0 && idGuardado == 0L) {
                for (int j = 0; j < jugadoresActivos.size(); j++) {
                    if (jugadoresActivos.get(j).getNombre().equalsIgnoreCase(nombreGuardado.trim())) {
                        posicion = j;
                        break;
                    }
                }
            }
            if (posicion < 0 || ocupados.contains(jugadoresActivos.get(posicion).getId())) {
                sustituciones = true;
                posicion = primeraPosicionDisponible(ocupados);
            }
            long idDeseado = jugadoresActivos.get(posicion).getId();
            if (!seleccionarJugadorPorId(spinner, idDeseado)) {
                sustituciones = true;
            }
            Jugador elegido = obtenerJugadorSeleccionado(spinner);
            if (elegido == null) continue;
            spinner.setTag(elegido.getId());
            ocupados.add(elegido.getId());
            int color = spUltimaPartida.getInt(EXTRA_COLOR_JUGADOR + i,
                    recursoColorParaArgb(elegido.getColor()));
            aplicarColorAFila(fila.findViewById(R.id.btnColorJugador),
                    fila.findViewById(R.id.txtNombreJugador), color);
        }

        cargandoConfiguracion = false;
        actualizarOpcionesSpinners();
        aplicarConfiguracionSegunModo(modo);
        if (rondasEditablesParaModo(modo)) {
            maxRondas = Math.max(1, rondas);
            actualizarTextoRondas();
        }
        actualizarDisponibilidadJugadores();
        if (sustituciones || numero != spUltimaPartida.getInt(EXTRA_NUMERO_JUGADORES, 1)) {
            Toast.makeText(this, "Algunos jugadores ya no están disponibles. Revisa la selección.",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Última configuración cargada", Toast.LENGTH_SHORT).show();
        }
    }

    private void seleccionarModoJuegoEnSpinner(String modo) {
        for (int i = 0; i < spnModoJuego.getCount(); i++) {
            if (spnModoJuego.getItemAtPosition(i).toString().equals(modo)) {
                spnModoJuego.setSelection(i);
                return;
            }
        }
    }

    private boolean validarConfiguracionPartida() {
        if (filasJugadores.isEmpty()) {
            Toast.makeText(this, "Debes registrar al menos un jugador activo", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (filasJugadores.size() > MAX_JUGADORES) {
            Toast.makeText(this, "Máximo 6 jugadores por partida", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (esModoCricket(obtenerModoJuegoSeleccionado()) && filasJugadores.size() < 2) {
            Toast.makeText(this, "Cricket necesita al menos dos jugadores activos",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        Set<Long> ids = new HashSet<>();
        for (Long id : obtenerIdsJugadoresSeleccionados()) {
            if (id == 0L || !ids.add(id)) {
                Toast.makeText(this, "Selecciona jugadores distintos y activos",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void mostrarDialogoMuchasRondas() {
        new MaterialAlertDialogBuilder(this, R.style.TemaDialogoDardos)
                .setTitle("Muchas rondas")
                .setMessage("Has seleccionado " + maxRondas
                        + " rondas. La partida puede durar bastante tiempo.\n\n¿Deseas continuar?")
                .setPositiveButton("Continuar", (dialog, which) -> crearPartida())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void crearPartida() {
        String modo = obtenerModoJuegoSeleccionado();
        Intent intent;
        try {
            intent = crearIntentSegunModo(modo);
        } catch (IllegalArgumentException excepcion) {
            Toast.makeText(this, "No se puede crear la configuración: "
                    + excepcion.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        if (intent == null) {
            Toast.makeText(this, "Modo de juego no disponible todavía", Toast.LENGTH_SHORT).show();
            return;
        }
        guardarConfiguracionUltimaPartida();
        startActivity(intent);
    }

    private Intent crearIntentSegunModo(String modo) {
        Intent intent;
        switch (modo) {
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
            default:
                return null;
        }
        cargarExtrasEnIntent(intent, modo);
        return intent;
    }

    private void cargarExtrasEnIntent(Intent intent, String modo) {
        ArrayList<Long> ids = obtenerIdsJugadoresSeleccionados();
        ArrayList<String> nombres = obtenerNombresJugadoresSeleccionados();
        ArrayList<Integer> colores = obtenerColoresJugadoresSeleccionados();

        if (switchOrdenAleatorio.isChecked() && !conservarOrdenHistorico) {
            ArrayList<Integer> orden = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) orden.add(i);
            Collections.shuffle(orden);
            ArrayList<Long> idsMezclados = new ArrayList<>();
            ArrayList<String> nombresMezclados = new ArrayList<>();
            ArrayList<Integer> coloresMezclados = new ArrayList<>();
            for (Integer indice : orden) {
                idsMezclados.add(ids.get(indice));
                nombresMezclados.add(nombres.get(indice));
                coloresMezclados.add(colores.get(indice));
            }
            ids = idsMezclados;
            nombres = nombresMezclados;
            colores = coloresMezclados;
        }

        intent.putExtra(EXTRA_MODO_JUEGO, modo);
        intent.putExtra(EXTRA_MAX_RONDAS, maxRondas);
        intent.putExtra(EXTRA_NUMERO_JUGADORES, nombres.size());
        intent.putExtra(EXTRA_NUMERO_DARDOS, obtenerNumeroDardosSeleccionado());
        intent.putExtra(EXTRA_CIERRE_DOBLE, switchCierreDoble.isChecked());
        intent.putExtra(EXTRA_MANTENER_MULTIPLICADOR, switchMantenerMultiplicador.isChecked());
        intent.putExtra(EXTRA_ORDEN_ALEATORIO, switchOrdenAleatorio.isChecked());

        long[] idsArray = new long[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            idsArray[i] = ids.get(i);
            intent.putExtra(EXTRA_ID_JUGADOR + (i + 1), ids.get(i));
            intent.putExtra(EXTRA_JUGADOR + (i + 1), nombres.get(i));
            intent.putExtra(EXTRA_COLOR_JUGADOR + (i + 1), colores.get(i));
        }
        intent.putExtra(EXTRA_IDS_JUGADORES, idsArray);
        // Se serializa DESPUÉS del posible sorteo: ordenTurno es el orden real.
        ArrayList<ConfiguracionPartida.Participante> participantes = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            participantes.add(new ConfiguracionPartida.Participante(
                    ids.get(i), nombres.get(i),
                    ContextCompat.getColor(this, colores.get(i)), i + 1));
        }
        ConfiguracionPartida configuracion = new ConfiguracionPartida(
                modo, maxRondas, obtenerNumeroDardosSeleccionado(),
                switchCierreDoble.isChecked(), switchMantenerMultiplicador.isChecked(),
                switchOrdenAleatorio.isChecked(), participantes);
        intent.putExtra(EXTRA_CONFIGURACION_JSON,
                SerializadorConfiguracionPartida.aJson(configuracion));
        intent.putStringArrayListExtra("nombresJugadores", nombres);
        intent.putIntegerArrayListExtra("colorJugador", colores);
        intent.putStringArrayListExtra(
                PartidaPuntosActivity.EXTRA_NOMBRES_JUGADORES, nombres);
    }

    /**
     * Precarga una fotografía guardada en PARTIDAS.configuracion_json.
     * No modifica preferencias, ni reactiva jugadores antiguos, ni accede
     * directamente a SQLite; las personas elegibles proceden del JugadorDao.
     *
     * Los jugadores inactivos o inexistentes necesitan una revisión manual
     * antes de pulsar Crear partida. Nunca se les vuelve a asignar por nombre.
     */
    private void cargarConfiguracionHistorica(String json) {
        final ConfiguracionPartida configuracion;
        try {
            configuracion = SerializadorConfiguracionPartida.desdeJson(json);
        } catch (IllegalArgumentException ex) {
            Toast.makeText(this, "Configuración histórica incompatible o dañada",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (jugadoresActivos.isEmpty()) {
            Toast.makeText(this, "No hay jugadores activos para recuperar esta partida",
                    Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<String> jugadoresNoDisponibles = new ArrayList<>();
        boolean colorNoDisponible = false;
        cargandoConfiguracion = true;
        try {
            seleccionarModoJuegoEnSpinner(configuracion.getModo());
            spnNumeroDardos.setSelection(configuracion.getDardosPorTurno() - 1);
            switchCierreDoble.setChecked(configuracion.isCierreDoble());
            switchMantenerMultiplicador.setChecked(
                    configuracion.isMantenerMultiplicador());
            switchOrdenAleatorio.setChecked(configuracion.isOrdenAleatorio());

            filasJugadores.clear();
            contenedorJugadores.removeAllViews();
            Set<Long> ocupados = new HashSet<>();
            List<ConfiguracionPartida.Participante> originales =
                    configuracion.getParticipantes();
            int cantidadVisible = Math.min(originales.size(), limiteJugadores());

            for (int i = 0; i < cantidadVisible; i++) {
                ConfiguracionPartida.Participante original = originales.get(i);
                int posicion = buscarPosicionPorId(original.getIdJugador());
                if (posicion < 0
                        || ocupados.contains(original.getIdJugador())) {
                    jugadoresNoDisponibles.add(original.getNombre());
                    posicion = primeraPosicionDisponible(ocupados);
                }
                agregarFilaJugador();
                View fila = filasJugadores.get(i);
                Spinner spinner = fila.findViewById(R.id.spnJugador);
                ImageButton botonColor = fila.findViewById(R.id.btnColorJugador);
                TextView etiqueta = fila.findViewById(R.id.txtNombreJugador);
                prepararSpinner(spinner, botonColor, etiqueta, posicion);
                Jugador elegido = obtenerJugadorSeleccionado(spinner);
                if (elegido != null) {
                    ocupados.add(elegido.getId());
                }
                int recursoColor = recursoColorParaArgb(original.getColorArgb());
                if (ContextCompat.getColor(this, recursoColor) != original.getColorArgb()) {
                    colorNoDisponible = true;
                }
                aplicarColorAFila(botonColor, etiqueta, recursoColor);
            }
            // Si había más participantes que jugadores activos, se reducen
            // las filas y se advierte expresamente de los que faltan.
            for (int i = cantidadVisible; i < originales.size(); i++) {
                jugadoresNoDisponibles.add(originales.get(i).getNombre());
            }

            aplicarConfiguracionSegunModo(configuracion.getModo());
            if (rondasEditablesParaModo(configuracion.getModo())) {
                maxRondas = configuracion.getRondas();
                actualizarTextoRondas();
            }
            // Aunque ordenAleatorio sea true, el historial tiene un orden
            // real que ya fue sorteado y no debemos mezclar de nuevo.
            conservarOrdenHistorico = true;
        } finally {
            cargandoConfiguracion = false;
            actualizarOpcionesSpinners();
            actualizarTextoJugadores();
            actualizarVisibilidadBotonesEliminar();
            actualizarDisponibilidadJugadores();
        }

        if (!jugadoresNoDisponibles.isEmpty() || colorNoDisponible) {
            String mensaje = "La configuración se ha recuperado parcialmente.";
            if (!jugadoresNoDisponibles.isEmpty()) {
                mensaje += "\n\nEstos jugadores no están activos o ya no existen: "
                        + android.text.TextUtils.join(", ", jugadoresNoDisponibles)
                        + ". Se han elegido otros jugadores activos cuando ha sido posible.";
            }
            if (colorNoDisponible) {
                mensaje += "\n\nAlgún color histórico no está en la paleta y se ha "
                        + "sustituido por blanco.";
            }
            mensaje += "\n\nRevisa los participantes antes de crear una nueva partida.";
            new MaterialAlertDialogBuilder(this, R.style.TemaDialogoDardos)
                    .setTitle("Revisar configuración histórica")
                    .setMessage(mensaje)
                    .setPositiveButton("Entendido", null)
                    .show();
        } else {
            Toast.makeText(this, "Configuración histórica recuperada",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String obtenerModoJuegoSeleccionado() {
        Object seleccionado = spnModoJuego.getSelectedItem();
        return seleccionado == null ? MODO_501 : seleccionado.toString();
    }

    private int obtenerNumeroDardosSeleccionado() {
        Object seleccionado = spnNumeroDardos.getSelectedItem();
        return seleccionado instanceof Integer ? (Integer) seleccionado : 3;
    }
}
