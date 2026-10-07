package activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.productos.juegosdedardos.R;

import modelos.EstadoPartidaCriquet;
import modelos.PartidaEnCurso;
import preferencias.GestorPartidaEnCurso;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Partida de Criquet y Cut Throat Criquet.
 *
 * Características:
 * - De 2 a 6 jugadores.
 * - Objetivos siempre situados en el centro del marcador.
 * - Con dos jugadores se muestra uno a cada lado.
 * - Cambio automático después del tercer dardo.
 * - El botón SIGUIENTE TURNO permite terminar el turno antes.
 * - X1, X2 y X3 muestran visualmente cuál está seleccionado.
 * - Finalización inmediata cuando un jugador cierra todos los objetivos
 *   y cumple la condición de puntuación de su modo.
 */
public class PartidaCriquetActivity extends AppCompatActivity {

    // Extras recibidos ---------------------------------------------------------

    public static final String EXTRA_MODO_JUEGO = "modoJuego";
    public static final String EXTRA_MAX_RONDAS = "maxRondas";
    public static final String EXTRA_NUMERO_JUGADORES_CONFIG = "numeroJugadores";
    public static final String EXTRA_JUGADOR = "jugador";
    public static final String EXTRA_NOMBRES_JUGADORES = "nombresJugadores";
    public static final String EXTRA_COLOR_JUGADOR = "colorJugador";
    public static final String EXTRA_REANUDAR_PARTIDA = "reanudarPartida";

    /*
     * Datos específicos de Cricket. ResultadoActivity puede ignorarlos
     * actualmente y recuperarlos en el futuro para una pantalla de detalles.
     */
    public static final String EXTRA_MARCAS_CRIQUET =
            "resultado_marcas_criquet";

    public static final String EXTRA_OBJETIVOS_CRIQUET =
            "resultado_objetivos_criquet";

    // Constantes ---------------------------------------------------------------

    private static final int MIN_JUGADORES = 2;
    private static final int MAX_JUGADORES = 6;

    private static final int CAPACIDAD_MAX_DARDOS = 4;
    private static final String PREF_CONFIG_PARTIDA = "configuracion_dardos_partida";
    private static final String CLAVE_DARDOS_CRIQUET = "numero_dardos_criquet";
    private static final String CLAVE_ORDEN_ALEATORIO_CRIQUET =
            "orden_aleatorio_criquet";
    private static final String CLAVE_MANTENER_MULTIPLICADOR_CRIQUET =
            "mantener_multiplicador_criquet";
    private static final int MARCAS_PARA_CERRAR = 3;

    /*
     * Índices:
     * 0 = 15
     * 1 = 16
     * 2 = 17
     * 3 = 18
     * 4 = 19
     * 5 = 20
     * 6 = Diana
     */
    private static final int[] OBJETIVOS = {
            15, 16, 17, 18, 19, 20, 25
    };

    private static final String[] ETIQUETAS_OBJETIVOS = {
            "15", "16", "17", "18", "19", "20", "D"
    };

    private enum ModoCriquet {
        CRIQUET,
        CUT_THROAT
    }

    // Datos de la partida ------------------------------------------------------

    private ModoCriquet modoCriquet;

    private String modoJuego;
    private int maxRondas;
    private int numeroDardosTurno = 3;
    private boolean ordenAleatorio;
    private boolean mantenerMultiplicador;

    private String[] nombresJugadores;
    private int[] coloresJugadores;

    private int[][] marcasJugadores;
    private int[] puntuacionesJugadores;

    private int jugadorActual;
    private int rondaActual;
    private int numeroDardo;

    private int multiplicadorSeleccionado;

    private boolean partidaFinalizada;
    private boolean partidaCargadaCorrectamente;

    private final String[] tiradasTurno =
            new String[CAPACIDAD_MAX_DARDOS];

    /*
     * Se guarda el estado anterior a cada dardo y a cada cambio manual.
     * Si el tercer dardo provoca un cambio automático, o si se pulsa
     * SIGUIENTE TURNO, deshacer devuelve correctamente al estado anterior.
     */
    private final Deque<EstadoPartida> historialEstados =
            new ArrayDeque<>();

    // Vistas generales ---------------------------------------------------------

    private TextView txtModoJuego;
    private TextView txtJugadorActual;

    private TextView txtRondaActual;
    private TextView txtMaxRondas;
    private TextView txtNumeroDardo;

    private TextView txtTirada1;
    private TextView txtTirada2;
    private TextView txtTirada3;
    private TextView txtTirada4;

    private ImageView imgDardo1;
    private ImageView imgDardo2;
    private ImageView imgDardo3;
    private ImageView imgDardo4;

    // Marcador dinámico --------------------------------------------------------

    private LinearLayout contenedorJugadoresIzquierda;
    private LinearLayout contenedorJugadoresDerecha;

    private TextView[] txtNombresJugadores;
    private TextView[] txtPuntosJugadores;
    private TextView[][] txtMarcasJugadores;

    // Botonera -----------------------------------------------------------------

    private Button btnFuera;

    private Button btnX1;
    private Button btnX2;
    private Button btnX3;

    private Button btnNumero15;
    private Button btnNumero16;
    private Button btnNumero17;
    private Button btnNumero18;
    private Button btnNumero19;
    private Button btnNumero20;
    private Button btnBull;

    private Button btnDeshacerTirada;
    private Button btnVerMarcador;
    private Button btnSiguienteTurno;
    private Button btnSalirPartida;

    private final List<Button> botonesPuntuacion =
            new ArrayList<>();

    private final List<Button> botonesMultiplicadores =
            new ArrayList<>();

    private final ColorStateList[] tintesOriginalesMultiplicadores =
            new ColorStateList[3];

    private final ColorStateList[] coloresTextoOriginalesMultiplicadores =
            new ColorStateList[3];

    // Ciclo de vida ------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(getColor(R.color.blue));
        getWindow().setNavigationBarColor(getColor(R.color.blue));

        setContentView(R.layout.activity_partida_criquet);

        inicializarVistas();
        configurarListeners();
        configurarBotonAtras();

        boolean reanudarPartida =
                getIntent().getBooleanExtra(
                        EXTRA_REANUDAR_PARTIDA,
                        false
                );

        if (reanudarPartida) {

            partidaCargadaCorrectamente =
                    cargarPartidaGuardada();

            if (!partidaCargadaCorrectamente) {

                Toast.makeText(
                        this,
                        "No se ha podido recuperar la partida guardada",
                        Toast.LENGTH_LONG
                ).show();

                volverMainActivity();
                return;
            }

        } else {

            recibirDatosPartida();
            inicializarPartida();

            partidaCargadaCorrectamente = true;
        }

        crearMarcadorJugadores();
        actualizarInterfazCompleta();
        guardarPartidaEnCurso();
    }

    // Inicialización de vistas -------------------------------------------------

    private void inicializarVistas() {

        txtModoJuego = findViewById(R.id.txtModoJuego);
        txtJugadorActual = findViewById(R.id.txtJugadorActual);

        txtRondaActual = findViewById(R.id.txtRondaActual);
        txtMaxRondas = findViewById(R.id.txtMaxRondas);
        txtNumeroDardo = findViewById(R.id.txtNumeroDardo);

        txtTirada1 = findViewById(R.id.txtTirada1);
        txtTirada2 = findViewById(R.id.txtTirada2);
        txtTirada3 = findViewById(R.id.txtTirada3);
        txtTirada4 = findViewById(R.id.txtTirada4);

        imgDardo1 = findViewById(R.id.imgDardo1);
        imgDardo2 = findViewById(R.id.imgDardo2);
        imgDardo3 = findViewById(R.id.imgDardo3);
        imgDardo4 = findViewById(R.id.imgDardo4);

        contenedorJugadoresIzquierda =
                findViewById(R.id.contenedorJugadoresIzquierda);

        contenedorJugadoresDerecha =
                findViewById(R.id.contenedorJugadoresDerecha);

        btnFuera = findViewById(R.id.btnFuera);

        btnX1 = findViewById(R.id.btnX1);
        btnX2 = findViewById(R.id.btnX2);
        btnX3 = findViewById(R.id.btnX3);

        btnNumero15 = findViewById(R.id.btnNumero15);
        btnNumero16 = findViewById(R.id.btnNumero16);
        btnNumero17 = findViewById(R.id.btnNumero17);
        btnNumero18 = findViewById(R.id.btnNumero18);
        btnNumero19 = findViewById(R.id.btnNumero19);
        btnNumero20 = findViewById(R.id.btnNumero20);
        btnBull = findViewById(R.id.btnBull);

        btnDeshacerTirada =
                findViewById(R.id.btnDeshacerTirada);

        btnVerMarcador =
                findViewById(R.id.btnVerMarcador);

        btnSiguienteTurno =
                findViewById(R.id.btnSiguienteTurno);

        btnSalirPartida =
                findViewById(R.id.btnSalirPartida);

        botonesPuntuacion.clear();
        botonesPuntuacion.add(btnNumero15);
        botonesPuntuacion.add(btnNumero16);
        botonesPuntuacion.add(btnNumero17);
        botonesPuntuacion.add(btnNumero18);
        botonesPuntuacion.add(btnNumero19);
        botonesPuntuacion.add(btnNumero20);
        botonesPuntuacion.add(btnBull);

        botonesMultiplicadores.clear();
        botonesMultiplicadores.add(btnX1);
        botonesMultiplicadores.add(btnX2);
        botonesMultiplicadores.add(btnX3);

        for (int i = 0; i < botonesMultiplicadores.size(); i++) {

            Button boton = botonesMultiplicadores.get(i);

            tintesOriginalesMultiplicadores[i] =
                    boton.getBackgroundTintList();

            coloresTextoOriginalesMultiplicadores[i] =
                    boton.getTextColors();
        }
    }

    // Recepción de datos -------------------------------------------------------

    private void recibirDatosPartida() {

        Intent intent = getIntent();

        modoJuego =
                intent.getStringExtra(EXTRA_MODO_JUEGO);

        maxRondas =
                intent.getIntExtra(EXTRA_MAX_RONDAS, 15);

        numeroDardosTurno =
                Math.max(
                        1,
                        Math.min(
                                CAPACIDAD_MAX_DARDOS,
                                intent.getIntExtra(
                                        ConfigurarNuevaPartidaActivity.EXTRA_NUMERO_DARDOS,
                                        3
                                )
                        )
                );

        ordenAleatorio =
                intent.getBooleanExtra(
                        ConfigurarNuevaPartidaActivity.EXTRA_ORDEN_ALEATORIO,
                        false
                );

        mantenerMultiplicador =
                intent.getBooleanExtra(
                        ConfigurarNuevaPartidaActivity.EXTRA_MANTENER_MULTIPLICADOR,
                        false
                );

        if (modoJuego == null || modoJuego.trim().isEmpty()) {
            modoJuego = "Cricket";
        }

        modoCriquet =
                interpretarModoJuego(modoJuego);

        recibirNombresJugadores(intent);
        recibirColoresJugadores(intent);

        normalizarNombresJugadores();
    }

    private void recibirNombresJugadores(Intent intent) {

        /*
         * ConfigurarNuevaPartidaActivity envía los nombres
         * mediante putStringArrayListExtra().
         */
        ArrayList<String> nombresRecibidos =
                intent.getStringArrayListExtra(
                        EXTRA_NOMBRES_JUGADORES
                );

        if (nombresRecibidos != null
                && !nombresRecibidos.isEmpty()) {

            nombresJugadores =
                    nombresRecibidos.toArray(
                            new String[0]
                    );

        } else {

            /*
             * Respaldo usando los extras individuales:
             * jugador1, jugador2, jugador3...
             */
            int numeroJugadores =
                    intent.getIntExtra(
                            EXTRA_NUMERO_JUGADORES_CONFIG,
                            MIN_JUGADORES
                    );

            numeroJugadores =
                    Math.max(
                            MIN_JUGADORES,
                            Math.min(
                                    numeroJugadores,
                                    MAX_JUGADORES
                            )
                    );

            nombresJugadores =
                    new String[numeroJugadores];

            for (int i = 0;
                 i < numeroJugadores;
                 i++) {

                String nombre =
                        intent.getStringExtra(
                                EXTRA_JUGADOR + (i + 1)
                        );

                if (nombre == null
                        || nombre.trim().isEmpty()) {

                    nombre = "Jugador " + (i + 1);
                }

                nombresJugadores[i] = nombre;
            }
        }

        if (nombresJugadores.length < MIN_JUGADORES) {

            String primerNombre =
                    nombresJugadores.length > 0
                            ? nombresJugadores[0]
                            : "Jugador 1";

            nombresJugadores =
                    new String[]{
                            primerNombre,
                            "Jugador 2"
                    };
        }

        if (nombresJugadores.length > MAX_JUGADORES) {

            nombresJugadores =
                    Arrays.copyOf(
                            nombresJugadores,
                            MAX_JUGADORES
                    );
        }
    }

    private void recibirColoresJugadores(Intent intent) {

        int[] coloresPorDefecto = {
                R.color.jugador_rojo,
                R.color.jugador_verde,
                R.color.jugador_azul,
                R.color.jugador_amarillo,
                R.color.jugador_naranja,
                R.color.jugador_morado
        };

        ArrayList<Integer> coloresRecibidos =
                intent.getIntegerArrayListExtra(
                        EXTRA_COLOR_JUGADOR
                );

        coloresJugadores =
                new int[nombresJugadores.length];

        for (int i = 0;
             i < nombresJugadores.length;
             i++) {

            int colorPorDefecto =
                    ContextCompat.getColor(
                            this,
                            coloresPorDefecto[
                                    i % coloresPorDefecto.length
                                    ]
                    );

            Integer colorRecibido = null;

            /*
             * Primera opción: lista enviada por
             * ConfigurarNuevaPartidaActivity o ResultadoActivity.
             */
            if (coloresRecibidos != null
                    && i < coloresRecibidos.size()) {

                colorRecibido =
                        coloresRecibidos.get(i);
            }

            /*
             * Segunda opción: extras individuales
             * colorJugador1, colorJugador2, etc.
             */
            String claveColorIndividual =
                    EXTRA_COLOR_JUGADOR + (i + 1);

            if (colorRecibido == null
                    && intent.hasExtra(claveColorIndividual)) {

                colorRecibido =
                        intent.getIntExtra(
                                claveColorIndividual,
                                coloresPorDefecto[
                                        i % coloresPorDefecto.length
                                        ]
                        );
            }

            if (colorRecibido == null) {

                coloresJugadores[i] =
                        colorPorDefecto;

            } else {

                coloresJugadores[i] =
                        resolverColorRecibido(
                                colorRecibido,
                                colorPorDefecto
                        );
            }
        }
    }

    private int resolverColorRecibido(
            int valorRecibido,
            int colorPorDefecto) {

        try {

            String tipoRecurso =
                    getResources()
                            .getResourceTypeName(valorRecibido);

            if ("color".equals(tipoRecurso)) {

                return ContextCompat.getColor(
                        this,
                        valorRecibido
                );
            }

        } catch (Resources.NotFoundException ignored) {

            /*
             * No es un ID de recurso.
             * Se considera un color ARGB directo.
             */
        }

        if (valorRecibido == 0) {
            return colorPorDefecto;
        }

        return valorRecibido;
    }

    private ModoCriquet interpretarModoJuego(String modoRecibido) {

        String modoNormalizado = modoRecibido
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace("_", " ")
                .replace("-", " ");

        if (modoNormalizado.contains("CUT")) {
            return ModoCriquet.CUT_THROAT;
        }

        return ModoCriquet.CRIQUET;
    }

    private void normalizarNombresJugadores() {

        for (int i = 0; i < nombresJugadores.length; i++) {

            if (nombresJugadores[i] == null
                    || nombresJugadores[i].trim().isEmpty()) {

                nombresJugadores[i] = "Jugador " + (i + 1);

            } else {

                nombresJugadores[i] =
                        nombresJugadores[i].trim();
            }
        }
    }

    // Inicialización de partida ------------------------------------------------

    private void inicializarPartida() {

        marcasJugadores =
                new int[nombresJugadores.length][OBJETIVOS.length];

        puntuacionesJugadores =
                new int[nombresJugadores.length];

        jugadorActual = 0;
        rondaActual = 1;
        numeroDardo = 1;

        multiplicadorSeleccionado = 1;
        partidaFinalizada = false;

        Arrays.fill(tiradasTurno, "");
        historialEstados.clear();

        txtNombresJugadores =
                new TextView[nombresJugadores.length];

        txtPuntosJugadores =
                new TextView[nombresJugadores.length];

        txtMarcasJugadores =
                new TextView[
                        nombresJugadores.length
                        ][OBJETIVOS.length];
    }


    //Guardado persistente --------------------------------------------------------

    private void guardarPartidaEnCurso() {

        if (!partidaCargadaCorrectamente
                || partidaFinalizada
                || nombresJugadores == null
                || nombresJugadores.length == 0) {

            return;
        }

        PartidaEnCurso partida =
                new PartidaEnCurso();

        partida.setTipoPartida(
                PartidaEnCurso.TIPO_CRIQUET
        );

        partida.setModoJuego(modoJuego);

        partida.setNombresJugadores(
                new ArrayList<>(
                        Arrays.asList(nombresJugadores)
                )
        );

        partida.setNumeroJugadores(
                nombresJugadores.length
        );

        partida.setJugadorActual(
                jugadorActual
        );

        partida.setRondaActual(
                rondaActual
        );

        ArrayList<Integer> colores =
                convertirArrayIntALista(
                        coloresJugadores
                );

        partida.setColoresJugadores(
                new ArrayList<>(colores)
        );

        EstadoPartidaCriquet estado =
                new EstadoPartidaCriquet();

        estado.setMaxRondas(maxRondas);

        estado.setColoresJugadores(
                colores
        );

        estado.setPuntuacionesJugadores(
                convertirArrayIntALista(
                        puntuacionesJugadores
                )
        );

        estado.setMarcasJugadores(
                convertirMatrizALista(
                        marcasJugadores
                )
        );

        estado.setNumeroDardo(
                numeroDardo
        );

        estado.setMultiplicadorSeleccionado(
                multiplicadorSeleccionado
        );

        estado.setTiradasTurno(
                new ArrayList<>(
                        Arrays.asList(tiradasTurno)
                )
        );

        estado.setHistorialEstados(
                construirHistorialGuardado()
        );

        partida.setEstadoCriquet(estado);

        getSharedPreferences(
                PREF_CONFIG_PARTIDA,
                MODE_PRIVATE
        ).edit()
                .putInt(
                        CLAVE_DARDOS_CRIQUET,
                        numeroDardosTurno
                )
                .putBoolean(
                        CLAVE_ORDEN_ALEATORIO_CRIQUET,
                        ordenAleatorio
                )
                .putBoolean(
                        CLAVE_MANTENER_MULTIPLICADOR_CRIQUET,
                        mantenerMultiplicador
                )
                .apply();

        GestorPartidaEnCurso.guardarPartida(
                this,
                partida
        );
    }

    //Carga persistente -----------------------------------------------------------

    private boolean cargarPartidaGuardada() {

        PartidaEnCurso partida =
                GestorPartidaEnCurso.cargarPartida(this);

        if (partida == null
                || !PartidaEnCurso.TIPO_CRIQUET.equals(
                partida.getTipoPartida()
        )
                || partida.getEstadoCriquet() == null
                || partida.getNombresJugadores() == null
                || partida.getNombresJugadores().size()
                < MIN_JUGADORES) {

            return false;
        }

        EstadoPartidaCriquet estado =
                partida.getEstadoCriquet();

        modoJuego = partida.getModoJuego();
        modoCriquet = interpretarModoJuego(modoJuego);

        int numeroJugadores =
                Math.min(
                        partida.getNombresJugadores().size(),
                        MAX_JUGADORES
                );

        nombresJugadores =
                partida.getNombresJugadores()
                        .subList(
                                0,
                                numeroJugadores
                        )
                        .toArray(new String[0]);

        normalizarNombresJugadores();

        maxRondas =
                estado.getMaxRondas() > 0
                        ? estado.getMaxRondas()
                        : 15;

        jugadorActual =
                Math.max(
                        0,
                        Math.min(
                                partida.getJugadorActual(),
                                numeroJugadores - 1
                        )
                );

        rondaActual =
                Math.max(
                        1,
                        partida.getRondaActual()
                );

        numeroDardosTurno =
                getSharedPreferences(
                        PREF_CONFIG_PARTIDA,
                        MODE_PRIVATE
                ).getInt(
                        CLAVE_DARDOS_CRIQUET,
                        3
                );

        numeroDardosTurno =
                Math.max(
                        1,
                        Math.min(
                                CAPACIDAD_MAX_DARDOS,
                                numeroDardosTurno
                        )
                );

        ordenAleatorio =
                getSharedPreferences(
                        PREF_CONFIG_PARTIDA,
                        MODE_PRIVATE
                ).getBoolean(
                        CLAVE_ORDEN_ALEATORIO_CRIQUET,
                        false
                );

        mantenerMultiplicador =
                getSharedPreferences(
                        PREF_CONFIG_PARTIDA,
                        MODE_PRIVATE
                ).getBoolean(
                        CLAVE_MANTENER_MULTIPLICADOR_CRIQUET,
                        false
                );

        numeroDardo =
                Math.max(
                        1,
                        Math.min(
                                estado.getNumeroDardo(),
                                numeroDardosTurno
                        )
                );

        multiplicadorSeleccionado =
                Math.max(
                        1,
                        Math.min(
                                estado.getMultiplicadorSeleccionado(),
                                3
                        )
                );

        ArrayList<Integer> coloresGuardados =
                estado.getColoresJugadores();

        if (coloresGuardados == null
                || coloresGuardados.isEmpty()) {

            coloresGuardados =
                    partida.getColoresJugadores();
        }

        coloresJugadores =
                convertirListaAArrayInt(
                        coloresGuardados,
                        numeroJugadores,
                        Color.WHITE
                );

        puntuacionesJugadores =
                convertirListaAArrayInt(
                        estado.getPuntuacionesJugadores(),
                        numeroJugadores,
                        0
                );

        marcasJugadores =
                convertirListaAMatriz(
                        estado.getMarcasJugadores(),
                        numeroJugadores,
                        OBJETIVOS.length
                );

        Arrays.fill(tiradasTurno, "");

        if (estado.getTiradasTurno() != null) {

            int limite =
                    Math.min(
                            tiradasTurno.length,
                            estado.getTiradasTurno().size()
                    );

            for (int i = 0; i < limite; i++) {

                String valor =
                        estado.getTiradasTurno().get(i);

                tiradasTurno[i] =
                        valor == null ? "" : valor;
            }
        }

        historialEstados.clear();
        restaurarHistorialEstados(
                estado.getHistorialEstados()
        );

        txtNombresJugadores =
                new TextView[numeroJugadores];

        txtPuntosJugadores =
                new TextView[numeroJugadores];

        txtMarcasJugadores =
                new TextView[
                        numeroJugadores
                        ][OBJETIVOS.length];

        partidaFinalizada = false;

        return true;
    }

    //Conversión del historial ----------------------------------------------------

    private ArrayList<EstadoPartidaCriquet.EstadoDeshacerCriquet>
    construirHistorialGuardado() {

        ArrayList<EstadoPartidaCriquet.EstadoDeshacerCriquet> resultado =
                new ArrayList<>();

        for (EstadoPartida estado :
                historialEstados) {

            EstadoPartidaCriquet.EstadoDeshacerCriquet guardado =
                    new EstadoPartidaCriquet.EstadoDeshacerCriquet();

            guardado.setMarcasJugadores(
                    convertirMatrizALista(
                            estado.marcasJugadores
                    )
            );

            guardado.setPuntuacionesJugadores(
                    convertirArrayIntALista(
                            estado.puntuacionesJugadores
                    )
            );

            guardado.setJugadorActual(
                    estado.jugadorActual
            );

            guardado.setRondaActual(
                    estado.rondaActual
            );

            guardado.setNumeroDardo(
                    estado.numeroDardo
            );

            guardado.setMultiplicadorSeleccionado(
                    estado.multiplicadorSeleccionado
            );

            guardado.setTiradasTurno(
                    new ArrayList<>(
                            Arrays.asList(
                                    estado.tiradasTurno
                            )
                    )
            );

            resultado.add(guardado);
        }

        return resultado;
    }

    private void restaurarHistorialEstados(
            ArrayList<EstadoPartidaCriquet.EstadoDeshacerCriquet> estados
    ) {

        if (estados == null) {
            return;
        }

        for (EstadoPartidaCriquet.EstadoDeshacerCriquet guardado :
                estados) {

            if (guardado == null) {
                continue;
            }

            historialEstados.addLast(
                    new EstadoPartida(
                            convertirListaAMatriz(
                                    guardado.getMarcasJugadores(),
                                    nombresJugadores.length,
                                    OBJETIVOS.length
                            ),
                            convertirListaAArrayInt(
                                    guardado.getPuntuacionesJugadores(),
                                    nombresJugadores.length,
                                    0
                            ),
                            guardado.getJugadorActual(),
                            guardado.getRondaActual(),
                            guardado.getNumeroDardo(),
                            guardado.getMultiplicadorSeleccionado(),
                            convertirListaAArrayString(
                                    guardado.getTiradasTurno(),
                                    CAPACIDAD_MAX_DARDOS
                            )
                    )
            );
        }
    }

    //Métodos auxiliares de conversión -------------------------------------------

    private ArrayList<Integer> convertirArrayIntALista(
            int[] valores
    ) {

        ArrayList<Integer> resultado =
                new ArrayList<>();

        if (valores != null) {

            for (int valor : valores) {
                resultado.add(valor);
            }
        }

        return resultado;
    }

    private ArrayList<ArrayList<Integer>> convertirMatrizALista(
            int[][] matriz
    ) {

        ArrayList<ArrayList<Integer>> resultado =
                new ArrayList<>();

        if (matriz == null) {
            return resultado;
        }

        for (int[] fila : matriz) {

            resultado.add(
                    convertirArrayIntALista(fila)
            );
        }

        return resultado;
    }

    private int[] convertirListaAArrayInt(
            ArrayList<Integer> valores,
            int tamano,
            int valorPorDefecto
    ) {

        int[] resultado =
                new int[tamano];

        Arrays.fill(
                resultado,
                valorPorDefecto
        );

        if (valores == null) {
            return resultado;
        }

        int limite =
                Math.min(
                        valores.size(),
                        tamano
                );

        for (int i = 0; i < limite; i++) {

            Integer valor =
                    valores.get(i);

            if (valor != null) {
                resultado[i] = valor;
            }
        }

        return resultado;
    }

    private int[][] convertirListaAMatriz(
            ArrayList<ArrayList<Integer>> valores,
            int filas,
            int columnas
    ) {

        int[][] resultado =
                new int[filas][columnas];

        if (valores == null) {
            return resultado;
        }

        int limiteFilas =
                Math.min(
                        valores.size(),
                        filas
                );

        for (int fila = 0;
             fila < limiteFilas;
             fila++) {

            ArrayList<Integer> valoresFila =
                    valores.get(fila);

            if (valoresFila == null) {
                continue;
            }

            int limiteColumnas =
                    Math.min(
                            valoresFila.size(),
                            columnas
                    );

            for (int columna = 0;
                 columna < limiteColumnas;
                 columna++) {

                Integer valor =
                        valoresFila.get(columna);

                if (valor != null) {

                    resultado[fila][columna] =
                            Math.max(
                                    0,
                                    Math.min(
                                            valor,
                                            MARCAS_PARA_CERRAR
                                    )
                            );
                }
            }
        }

        return resultado;
    }

    private String[] convertirListaAArrayString(
            ArrayList<String> valores,
            int tamano
    ) {

        String[] resultado =
                new String[tamano];

        Arrays.fill(resultado, "");

        if (valores == null) {
            return resultado;
        }

        int limite =
                Math.min(
                        valores.size(),
                        tamano
                );

        for (int i = 0; i < limite; i++) {

            String valor =
                    valores.get(i);

            resultado[i] =
                    valor == null ? "" : valor;
        }

        return resultado;
    }

    // Creación del marcador ----------------------------------------------------

    private void crearMarcadorJugadores() {

        contenedorJugadoresIzquierda.removeAllViews();
        contenedorJugadoresDerecha.removeAllViews();

        /*
         * Distribución:
         * 2 jugadores -> 1 izquierda y 1 derecha.
         * 3 jugadores -> 2 izquierda y 1 derecha.
         * 4 jugadores -> 2 y 2.
         * 5 jugadores -> 3 y 2.
         * 6 jugadores -> 3 y 3.
         */
        int jugadoresIzquierda =
                (nombresJugadores.length + 1) / 2;

        for (int jugador = 0;
             jugador < nombresJugadores.length;
             jugador++) {

            LinearLayout columna =
                    crearColumnaJugador(jugador);

            if (jugador < jugadoresIzquierda) {

                contenedorJugadoresIzquierda.addView(columna);

            } else {

                contenedorJugadoresDerecha.addView(columna);
            }
        }
    }

    private LinearLayout crearColumnaJugador(int indiceJugador) {

        LinearLayout columna = new LinearLayout(this);

        columna.setOrientation(LinearLayout.VERTICAL);

        columna.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f
                )
        );

        TextView txtNombre = new TextView(this);

        txtNombre.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        convertirDp(22)
                )
        );

        txtNombre.setGravity(Gravity.CENTER);
        txtNombre.setIncludeFontPadding(false);
        txtNombre.setMaxLines(1);
        txtNombre.setTextSize(
                nombresJugadores.length >= 5 ? 8 : 10
        );

        txtNombre.setTypeface(
                txtNombre.getTypeface(),
                android.graphics.Typeface.BOLD
        );

        columna.addView(txtNombre);
        txtNombresJugadores[indiceJugador] = txtNombre;

        /*
         * El XML muestra de arriba abajo:
         * 20, 19, 18, 17, 16, 15 y Diana.
         */
        int[] ordenVisual = {5, 4, 3, 2, 1, 0, 6};

        for (int indiceObjetivo : ordenVisual) {

            TextView txtMarca = new TextView(this);

            txtMarca.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            convertirDp(27)
                    )
            );

            txtMarca.setGravity(Gravity.CENTER);
            txtMarca.setIncludeFontPadding(false);
            txtMarca.setTextSize(
                    nombresJugadores.length >= 5 ? 8 : 10
            );

            txtMarca.setText("○ ○ ○");
            txtMarca.setTextColor(Color.WHITE);

            columna.addView(txtMarca);

            txtMarcasJugadores[indiceJugador][indiceObjetivo] =
                    txtMarca;
        }

        TextView txtPuntos = new TextView(this);

        txtPuntos.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        convertirDp(27)
                )
        );

        txtPuntos.setGravity(Gravity.CENTER);
        txtPuntos.setIncludeFontPadding(false);
        txtPuntos.setTextSize(
                nombresJugadores.length >= 5 ? 9 : 10
        );

        txtPuntos.setTypeface(
                txtPuntos.getTypeface(),
                android.graphics.Typeface.BOLD
        );

        txtPuntos.setText("0 PTS");
        txtPuntos.setTextColor(Color.WHITE);

        columna.addView(txtPuntos);
        txtPuntosJugadores[indiceJugador] = txtPuntos;

        return columna;
    }

    // Configuración de listeners -----------------------------------------------

    private void configurarListeners() {

        btnFuera.setOnClickListener(
                view -> registrarFuera()
        );

        btnX1.setOnClickListener(
                view -> seleccionarMultiplicador(1)
        );

        btnX2.setOnClickListener(
                view -> seleccionarMultiplicador(2)
        );

        btnX3.setOnClickListener(
                view -> seleccionarMultiplicador(3)
        );

        btnNumero15.setOnClickListener(
                view -> registrarObjetivo(0)
        );

        btnNumero16.setOnClickListener(
                view -> registrarObjetivo(1)
        );

        btnNumero17.setOnClickListener(
                view -> registrarObjetivo(2)
        );

        btnNumero18.setOnClickListener(
                view -> registrarObjetivo(3)
        );

        btnNumero19.setOnClickListener(
                view -> registrarObjetivo(4)
        );

        btnNumero20.setOnClickListener(
                view -> registrarObjetivo(5)
        );

        btnBull.setOnClickListener(
                view -> registrarObjetivo(6)
        );

        btnDeshacerTirada.setOnClickListener(
                view -> deshacerUltimaTirada()
        );

        btnVerMarcador.setOnClickListener(
                view -> mostrarDialogoMarcador()
        );

        /*
         * Permite pasar turno aunque no se hayan usado los tres dardos.
         * Es útil cuando el jugador falla el resto o no desea registrarlos.
         */
        btnSiguienteTurno.setOnClickListener(
                view -> pasarSiguienteTurnoManual()
        );

        btnSalirPartida.setOnClickListener(
                view -> mostrarDialogoSalir()
        );
    }

    private void configurarBotonAtras() {

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        mostrarDialogoSalir();
                    }
                }
        );
    }

    // Selección del multiplicador ----------------------------------------------

    private void seleccionarMultiplicador(int multiplicador) {

        if (partidaFinalizada) {
            return;
        }

        multiplicadorSeleccionado = multiplicador;
        actualizarMultiplicadores();
        guardarPartidaEnCurso();
    }

    // Registro de dardos -------------------------------------------------------

    private void registrarFuera() {

        if (!puedeRegistrarDardo()) {
            return;
        }

        guardarEstadoActual();

        tiradasTurno[numeroDardo - 1] = "0";

        finalizarRegistroDardo();
    }

    private void registrarObjetivo(int indiceObjetivo) {

        if (!puedeRegistrarDardo()) {
            return;
        }

        if (indiceObjetivo == 6
                && multiplicadorSeleccionado == 3) {

            Toast.makeText(
                    this,
                    "La Diana solo puede ser simple o doble",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        guardarEstadoActual();

        aplicarImpactos(
                jugadorActual,
                indiceObjetivo,
                multiplicadorSeleccionado
        );

        tiradasTurno[numeroDardo - 1] =
                crearTextoTirada(
                        indiceObjetivo,
                        multiplicadorSeleccionado
                );

        if (!mantenerMultiplicador) {
            multiplicadorSeleccionado = 1;
        }

        /*
         * Se comprueba después de cada dardo y antes de cambiar de turno.
         * buscarGanador() revisa a todos los jugadores.
         */
        int indiceGanador = buscarGanador();

        if (indiceGanador != -1) {

            partidaFinalizada = true;

            GestorPartidaEnCurso.eliminarPartida(
                    this
            );

            actualizarInterfazCompleta();
            abrirResultadoActivity(indiceGanador);

            return;
        }

        finalizarRegistroDardo();
    }

    private boolean puedeRegistrarDardo() {

        return !partidaFinalizada
                && numeroDardo >= 1
                && numeroDardo <= numeroDardosTurno;
    }

    private String crearTextoTirada(
            int indiceObjetivo,
            int multiplicador) {

        if (multiplicador == 1) {
            return ETIQUETAS_OBJETIVOS[indiceObjetivo];
        }

        return "X"
                + multiplicador
                + " "
                + ETIQUETAS_OBJETIVOS[indiceObjetivo];
    }

    private void finalizarRegistroDardo() {

        if (numeroDardo < numeroDardosTurno) {

            numeroDardo++;
            actualizarInterfazCompleta();
            guardarPartidaEnCurso();

        } else {

            pasarSiguienteTurnoAutomatico();
        }
    }

    // Lógica de puntuación -----------------------------------------------------

    private void aplicarImpactos(
            int indiceJugador,
            int indiceObjetivo,
            int impactos) {

        int marcasAnteriores =
                marcasJugadores[indiceJugador][indiceObjetivo];

        int totalMarcas =
                marcasAnteriores + impactos;

        marcasJugadores[indiceJugador][indiceObjetivo] =
                Math.min(
                        MARCAS_PARA_CERRAR,
                        totalMarcas
                );

        int impactosSobrantes =
                Math.max(
                        0,
                        totalMarcas - MARCAS_PARA_CERRAR
                );

        if (impactosSobrantes == 0) {
            return;
        }

        if (modoCriquet == ModoCriquet.CRIQUET) {

            aplicarPuntosCriquet(
                    indiceJugador,
                    indiceObjetivo,
                    impactosSobrantes
            );

        } else {

            aplicarPuntosCutThroat(
                    indiceJugador,
                    indiceObjetivo,
                    impactosSobrantes
            );
        }
    }

    private void aplicarPuntosCriquet(
            int indiceJugador,
            int indiceObjetivo,
            int impactosSobrantes) {

        if (!existeRivalConObjetivoAbierto(
                indiceJugador,
                indiceObjetivo)) {

            return;
        }

        puntuacionesJugadores[indiceJugador] +=
                impactosSobrantes
                        * OBJETIVOS[indiceObjetivo];
    }

    private void aplicarPuntosCutThroat(
            int indiceJugador,
            int indiceObjetivo,
            int impactosSobrantes) {

        int puntos =
                impactosSobrantes
                        * OBJETIVOS[indiceObjetivo];

        for (int rival = 0;
             rival < nombresJugadores.length;
             rival++) {

            if (rival == indiceJugador) {
                continue;
            }

            if (marcasJugadores[rival][indiceObjetivo]
                    < MARCAS_PARA_CERRAR) {

                puntuacionesJugadores[rival] += puntos;
            }
        }
    }

    private boolean existeRivalConObjetivoAbierto(
            int indiceJugador,
            int indiceObjetivo) {

        for (int rival = 0;
             rival < nombresJugadores.length;
             rival++) {

            if (rival != indiceJugador
                    && marcasJugadores[rival][indiceObjetivo]
                    < MARCAS_PARA_CERRAR) {

                return true;
            }
        }

        return false;
    }

    // Cambio de turno ----------------------------------------------------------

    private void pasarSiguienteTurnoAutomatico() {

        pasarAlSiguienteJugador();
    }

    private void pasarSiguienteTurnoManual() {

        if (partidaFinalizada) {
            return;
        }

        /*
         * Se guarda también el cambio manual de turno.
         * De esta forma, DESHACER permite regresar al jugador,
         * ronda, dardo, puntuaciones y cierres anteriores.
         */
        guardarEstadoActual();

        pasarAlSiguienteJugador();
    }

    private void pasarAlSiguienteJugador() {

        jugadorActual++;

        if (jugadorActual >= nombresJugadores.length) {

            jugadorActual = 0;
            rondaActual++;
        }

        numeroDardo = 1;
        multiplicadorSeleccionado = 1;

        Arrays.fill(tiradasTurno, "");

        if (maxRondas > 0 && rondaActual > maxRondas) {

            int indiceGanador =
                    obtenerGanadorPorLimiteRondas();

            partidaFinalizada = true;

            GestorPartidaEnCurso.eliminarPartida(
                    this
            );

            abrirResultadoActivity(indiceGanador);

            return;
        }

        actualizarInterfazCompleta();
        guardarPartidaEnCurso();
    }

    private int obtenerGanadorPorLimiteRondas() {

        int indiceGanador = 0;

        for (int i = 1;
             i < puntuacionesJugadores.length;
             i++) {

            if (modoCriquet == ModoCriquet.CRIQUET) {

                if (puntuacionesJugadores[i]
                        > puntuacionesJugadores[indiceGanador]) {

                    indiceGanador = i;
                }

            } else {

                if (puntuacionesJugadores[i]
                        < puntuacionesJugadores[indiceGanador]) {

                    indiceGanador = i;
                }
            }
        }

        return indiceGanador;
    }

    // Condición de victoria ----------------------------------------------------

    private int buscarGanador() {

        for (int jugador = 0;
             jugador < nombresJugadores.length;
             jugador++) {

            if (comprobarGanador(jugador)) {
                return jugador;
            }
        }

        return -1;
    }

    private boolean comprobarGanador(int indiceJugador) {

        if (!haCerradoTodosLosObjetivos(indiceJugador)) {
            return false;
        }

        if (modoCriquet == ModoCriquet.CRIQUET) {

            return tienePuntuacionMayorOIgual(
                    indiceJugador
            );
        }

        return tienePuntuacionMenorOIgual(
                indiceJugador
        );
    }

    private boolean haCerradoTodosLosObjetivos(
            int indiceJugador) {

        for (int marcas :
                marcasJugadores[indiceJugador]) {

            if (marcas < MARCAS_PARA_CERRAR) {
                return false;
            }
        }

        return true;
    }

    private boolean tienePuntuacionMayorOIgual(
            int indiceJugador) {

        int puntuacion =
                puntuacionesJugadores[indiceJugador];

        for (int i = 0;
             i < puntuacionesJugadores.length;
             i++) {

            if (i != indiceJugador
                    && puntuacion
                    < puntuacionesJugadores[i]) {

                return false;
            }
        }

        return true;
    }

    private boolean tienePuntuacionMenorOIgual(
            int indiceJugador) {

        int puntuacion =
                puntuacionesJugadores[indiceJugador];

        for (int i = 0;
             i < puntuacionesJugadores.length;
             i++) {

            if (i != indiceJugador
                    && puntuacion
                    > puntuacionesJugadores[i]) {

                return false;
            }
        }

        return true;
    }

    // Deshacer -----------------------------------------------------------------

    private void guardarEstadoActual() {

        historialEstados.push(
                new EstadoPartida(
                        copiarMatriz(marcasJugadores),
                        puntuacionesJugadores.clone(),
                        jugadorActual,
                        rondaActual,
                        numeroDardo,
                        multiplicadorSeleccionado,
                        tiradasTurno.clone()
                )
        );
    }

    private void deshacerUltimaTirada() {

        if (historialEstados.isEmpty()
                || partidaFinalizada) {

            Toast.makeText(
                    this,
                    "No hay ninguna tirada para deshacer",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        EstadoPartida estadoAnterior =
                historialEstados.pop();

        marcasJugadores =
                copiarMatriz(
                        estadoAnterior.marcasJugadores
                );

        puntuacionesJugadores =
                estadoAnterior
                        .puntuacionesJugadores
                        .clone();

        jugadorActual =
                estadoAnterior.jugadorActual;

        rondaActual =
                estadoAnterior.rondaActual;

        numeroDardo =
                estadoAnterior.numeroDardo;

        multiplicadorSeleccionado =
                estadoAnterior.multiplicadorSeleccionado;

        System.arraycopy(
                estadoAnterior.tiradasTurno,
                0,
                tiradasTurno,
                0,
                tiradasTurno.length
        );

        actualizarInterfazCompleta();
        guardarPartidaEnCurso();
    }

    private int[][] copiarMatriz(int[][] matrizOriginal) {

        int[][] copia =
                new int[matrizOriginal.length][];

        for (int i = 0;
             i < matrizOriginal.length;
             i++) {

            copia[i] =
                    matrizOriginal[i].clone();
        }

        return copia;
    }

    // Actualización de interfaz ------------------------------------------------

    private void actualizarInterfazCompleta() {

        actualizarCabecera();
        actualizarInformacionTirada();
        actualizarMarcador();
        actualizarMultiplicadores();
        actualizarEstadoBotones();
        actualizarDardosVisuales();
    }

    private void actualizarCabecera() {

        txtModoJuego.setText(
                obtenerNombreModoMostrado()
        );

        txtJugadorActual.setText(
                nombresJugadores[jugadorActual]
                        .toUpperCase(Locale.ROOT)
        );

        txtJugadorActual.setTextColor(
                coloresJugadores[jugadorActual]
        );
    }

    private void actualizarInformacionTirada() {

        txtRondaActual.setText(
                String.valueOf(rondaActual)
        );

        if (maxRondas > 0) {

            txtMaxRondas.setVisibility(View.VISIBLE);
            txtMaxRondas.setText("/" + maxRondas);

        } else {

            txtMaxRondas.setVisibility(View.GONE);
        }

        txtNumeroDardo.setText(
                numeroDardo + " / " + numeroDardosTurno
        );

        txtTirada1.setText(tiradasTurno[0]);
        txtTirada2.setText(tiradasTurno[1]);
        txtTirada3.setText(tiradasTurno[2]);
        txtTirada4.setText(tiradasTurno[3]);

        actualizarVisibilidadDardos();
    }

    private void actualizarMarcador() {

        for (int jugador = 0;
             jugador < nombresJugadores.length;
             jugador++) {

            txtNombresJugadores[jugador].setText(
                    abreviarNombre(
                            nombresJugadores[jugador]
                                    .toUpperCase(Locale.ROOT)
                    )
            );

            txtNombresJugadores[jugador].setTextColor(
                    coloresJugadores[jugador]
            );

            txtPuntosJugadores[jugador].setText(
                    puntuacionesJugadores[jugador]
                            + " PTS"
            );

            txtPuntosJugadores[jugador].setTextColor(
                    jugador == jugadorActual
                            ? coloresJugadores[jugador]
                            : Color.WHITE
            );

            for (int objetivo = 0;
                 objetivo < OBJETIVOS.length;
                 objetivo++) {

                txtMarcasJugadores[jugador][objetivo]
                        .setText(
                                obtenerTextoMarcas(
                                        marcasJugadores[jugador][objetivo]
                                )
                        );

                txtMarcasJugadores[jugador][objetivo]
                        .setTextColor(
                                jugador == jugadorActual
                                        ? coloresJugadores[jugador]
                                        : Color.WHITE
                        );
            }
        }
    }

    private String obtenerTextoMarcas(int numeroMarcas) {

        switch (numeroMarcas) {

            case 1:
                return "● ○ ○";

            case 2:
                return "● ● ○";

            case 3:
                return "● ● ●";

            default:
                return "○ ○ ○";
        }
    }

    private String abreviarNombre(String nombre) {

        int maximo;

        if (nombresJugadores.length >= 5) {
            maximo = 5;
        } else if (nombresJugadores.length == 4) {
            maximo = 7;
        } else {
            maximo = 10;
        }

        if (nombre.length() <= maximo) {
            return nombre;
        }

        return nombre.substring(0, maximo - 1) + ".";
    }

    private void actualizarMultiplicadores() {

        for (int i = 0;
             i < botonesMultiplicadores.size();
             i++) {

            Button boton =
                    botonesMultiplicadores.get(i);

            boolean seleccionado =
                    i + 1 == multiplicadorSeleccionado;

            boton.setSelected(seleccionado);

            if (seleccionado) {

                boton.setBackgroundTintList(
                        ColorStateList.valueOf(
                                getColor(R.color.gold)
                        )
                );

                boton.setTextColor(Color.BLACK);

            } else {

                boton.setBackgroundTintList(
                        tintesOriginalesMultiplicadores[i]
                );

                boton.setTextColor(
                        coloresTextoOriginalesMultiplicadores[i]
                );
            }

            boton.setAlpha(
                    seleccionado ? 1.0f : 0.82f
            );
        }
    }

    private void actualizarEstadoBotones() {

        boolean puedeJugar =
                !partidaFinalizada;

        btnFuera.setEnabled(puedeJugar);

        for (Button boton : botonesPuntuacion) {

            boton.setEnabled(puedeJugar);
            boton.setAlpha(
                    puedeJugar ? 1.0f : 0.45f
            );
        }

        for (Button boton : botonesMultiplicadores) {
            boton.setEnabled(puedeJugar);
        }

        btnFuera.setAlpha(
                puedeJugar ? 1.0f : 0.45f
        );

        boolean puedeDeshacer =
                !historialEstados.isEmpty()
                        && !partidaFinalizada;

        btnDeshacerTirada.setEnabled(
                puedeDeshacer
        );

        btnDeshacerTirada.setAlpha(
                puedeDeshacer ? 1.0f : 0.45f
        );

        /*
         * Permanece visible y disponible durante todo el turno.
         */
        btnSiguienteTurno.setVisibility(View.VISIBLE);
        btnSiguienteTurno.setEnabled(puedeJugar);
        btnSiguienteTurno.setAlpha(
                puedeJugar ? 1.0f : 0.45f
        );
    }

    private void actualizarVisibilidadDardos() {

        imgDardo1.setVisibility(View.VISIBLE);
        txtTirada1.setVisibility(View.VISIBLE);

        boolean mostrarDardo2 = numeroDardosTurno >= 2;
        boolean mostrarDardo3 = numeroDardosTurno >= 3;
        boolean mostrarDardo4 = numeroDardosTurno >= 4;

        imgDardo2.setVisibility(
                mostrarDardo2 ? View.VISIBLE : View.GONE
        );
        txtTirada2.setVisibility(
                mostrarDardo2 ? View.VISIBLE : View.GONE
        );

        imgDardo3.setVisibility(
                mostrarDardo3 ? View.VISIBLE : View.GONE
        );
        txtTirada3.setVisibility(
                mostrarDardo3 ? View.VISIBLE : View.GONE
        );

        imgDardo4.setVisibility(
                mostrarDardo4 ? View.VISIBLE : View.GONE
        );
        txtTirada4.setVisibility(
                mostrarDardo4 ? View.VISIBLE : View.GONE
        );
    }

    private void actualizarDardosVisuales() {

        actualizarVisibilidadDardos();

        int dardoActualVisual =
                Math.max(
                        0,
                        numeroDardo - 1
                );

        imgDardo1.setAlpha(
                dardoActualVisual == 0 ? 1.0f : 0.35f
        );

        imgDardo2.setAlpha(
                dardoActualVisual == 1 ? 1.0f : 0.35f
        );

        imgDardo3.setAlpha(
                dardoActualVisual == 2 ? 1.0f : 0.35f
        );

        imgDardo4.setAlpha(
                dardoActualVisual == 3 ? 1.0f : 0.35f
        );

        if (numeroDardo > numeroDardosTurno) {

            imgDardo1.setAlpha(1.0f);
            imgDardo2.setAlpha(1.0f);
            imgDardo3.setAlpha(1.0f);
            imgDardo4.setAlpha(1.0f);
        }
    }

    // Marcador emergente -------------------------------------------------------

    private void mostrarDialogoMarcador() {

        StringBuilder mensaje =
                new StringBuilder();

        ArrayList<Integer> clasificacion =
                construirClasificacionMarcador();

        for (int posicion = 0;
             posicion < clasificacion.size();
             posicion++) {

            int indiceJugador =
                    clasificacion.get(posicion);

            mensaje.append(posicion + 1)
                    .append(". ")
                    .append(nombresJugadores[indiceJugador])
                    .append(": ")
                    .append(puntuacionesJugadores[indiceJugador])
                    .append(" puntos");

            if (haCerradoTodosLosObjetivos(indiceJugador)) {
                mensaje.append(" · Cerrado");
            }

            if (posicion < clasificacion.size() - 1) {
                mensaje.append("\n");
            }
        }

        new MaterialAlertDialogBuilder(this, R.style.TemaDialogoDardos)
                .setTitle("Marcador")
                .setMessage(mensaje.toString())
                .setPositiveButton("CERRAR", null)
                .show();
    }

    /**
     * Clasificación provisional utilizada por el botón VER MARCADOR.
     *
     * Cricket: mayor puntuación primero.
     * Cut Throat: menor puntuación primero.
     *
     * En empate se prioriza al jugador que tenga más objetivos cerrados
     * y después se conserva el orden original.
     */
    private ArrayList<Integer> construirClasificacionMarcador() {

        ArrayList<Integer> clasificacion =
                new ArrayList<>();

        for (int i = 0;
             i < nombresJugadores.length;
             i++) {

            clasificacion.add(i);
        }

        clasificacion.sort(
                (indice1, indice2) -> {

                    int comparacionPuntos;

                    if (modoCriquet == ModoCriquet.CRIQUET) {

                        comparacionPuntos =
                                Integer.compare(
                                        puntuacionesJugadores[indice2],
                                        puntuacionesJugadores[indice1]
                                );

                    } else {

                        comparacionPuntos =
                                Integer.compare(
                                        puntuacionesJugadores[indice1],
                                        puntuacionesJugadores[indice2]
                                );
                    }

                    if (comparacionPuntos != 0) {
                        return comparacionPuntos;
                    }

                    int objetivosCerrados1 =
                            contarObjetivosCerrados(indice1);

                    int objetivosCerrados2 =
                            contarObjetivosCerrados(indice2);

                    int comparacionCierres =
                            Integer.compare(
                                    objetivosCerrados2,
                                    objetivosCerrados1
                            );

                    if (comparacionCierres != 0) {
                        return comparacionCierres;
                    }

                    return Integer.compare(
                            indice1,
                            indice2
                    );
                }
        );

        return clasificacion;
    }

    private int contarObjetivosCerrados(
            int indiceJugador) {

        int objetivosCerrados = 0;

        for (int marcas :
                marcasJugadores[indiceJugador]) {

            if (marcas >= MARCAS_PARA_CERRAR) {
                objetivosCerrados++;
            }
        }

        return objetivosCerrados;
    }

    private String obtenerNombreModoMostrado() {

        if (modoCriquet == ModoCriquet.CUT_THROAT) {
            return "CUT THROAT CRICKET";
        }

        return "CRICKET";
    }

    // ResultadoActivity --------------------------------------------------------

    private void abrirResultadoActivity(
            int indiceGanador) {

        /*
         * Construimos la clasificación final usando el mismo formato
         * que PartidaPuntosActivity y ResultadoActivity.
         */
        ArrayList<Integer> clasificacion =
                construirClasificacionFinal(indiceGanador);

        ArrayList<String> nombresOrdenados =
                new ArrayList<>();

        ArrayList<Integer> puntuacionesOrdenadas =
                new ArrayList<>();

        ArrayList<Integer> coloresOrdenados =
                new ArrayList<>();

        ArrayList<Integer> posiciones =
                new ArrayList<>();

        ArrayList<Integer> indicesOriginales =
                new ArrayList<>();

        for (int posicion = 0;
             posicion < clasificacion.size();
             posicion++) {

            int indiceJugador =
                    clasificacion.get(posicion);

            nombresOrdenados.add(
                    nombresJugadores[indiceJugador]
            );

            puntuacionesOrdenadas.add(
                    puntuacionesJugadores[indiceJugador]
            );

            coloresOrdenados.add(
                    coloresJugadores[indiceJugador]
            );

            posiciones.add(
                    posicion + 1
            );

            indicesOriginales.add(
                    indiceJugador
            );
        }

        String nombreGanador =
                nombresOrdenados.isEmpty()
                        ? ""
                        : nombresOrdenados.get(0);

        /*
         * Guardamos también la tabla de marcas en el mismo orden
         * que la clasificación enviada a ResultadoActivity.
         */
        int[] marcasOrdenadas =
                aplanarMarcasClasificacion(
                        clasificacion
                );

        String motivoFinalizacion =
                maxRondas > 0 && rondaActual > maxRondas
                        ? "LIMITE_RONDAS"
                        : "VICTORIA";

        Intent intent = new Intent(
                PartidaCriquetActivity.this,
                ResultadoActivity.class
        );

        intent.putExtra(
                ResultadoActivity.EXTRA_ORDEN_ALEATORIO,
                ordenAleatorio
        );

        // Datos generales de la partida
        intent.putExtra(
                ResultadoActivity.EXTRA_MODO_JUEGO,
                obtenerNombreModoMostrado()
        );

        intent.putExtra(
                ResultadoActivity.EXTRA_MOTIVO_FINALIZACION,
                motivoFinalizacion
        );

        intent.putExtra(
                ResultadoActivity.EXTRA_NUMERO_JUGADORES,
                nombresJugadores.length
        );

        intent.putExtra(
                ResultadoActivity.EXTRA_RONDAS_JUGADAS,
                obtenerRondasJugadasResultado()
        );

        intent.putExtra(
                ResultadoActivity.EXTRA_MAX_RONDAS,
                maxRondas
        );

        intent.putExtra(
                ResultadoActivity.EXTRA_NOMBRE_GANADOR,
                nombreGanador
        );

        intent.putExtra(
                ResultadoActivity.EXTRA_NUMERO_DARDOS,
                numeroDardosTurno
        );

        // Datos ordenados de los jugadores
        intent.putStringArrayListExtra(
                ResultadoActivity.EXTRA_NOMBRES_JUGADORES,
                nombresOrdenados
        );

        intent.putIntegerArrayListExtra(
                ResultadoActivity.EXTRA_PUNTUACIONES,
                puntuacionesOrdenadas
        );

        intent.putIntegerArrayListExtra(
                ResultadoActivity.EXTRA_COLORES,
                coloresOrdenados
        );

        intent.putIntegerArrayListExtra(
                ResultadoActivity.EXTRA_POSICIONES,
                posiciones
        );

        intent.putIntegerArrayListExtra(
                ResultadoActivity.EXTRA_INDICES_ORIGINALES,
                indicesOriginales
        );

        /*
         * Extras opcionales específicos de Cricket.
         * No afectan a ResultadoActivity mientras no los lea.
         */
        intent.putExtra(
                EXTRA_MARCAS_CRIQUET,
                marcasOrdenadas
        );

        intent.putExtra(
                EXTRA_OBJETIVOS_CRIQUET,
                OBJETIVOS.clone()
        );

        startActivity(intent);
        finish();
    }

    private int[] aplanarMarcasClasificacion(
            ArrayList<Integer> clasificacion) {

        int cantidadObjetivos =
                OBJETIVOS.length;

        int[] resultado =
                new int[
                        clasificacion.size()
                                * cantidadObjetivos
                        ];

        int posicion = 0;

        for (int indiceJugador : clasificacion) {

            for (int indiceObjetivo = 0;
                 indiceObjetivo < cantidadObjetivos;
                 indiceObjetivo++) {

                resultado[posicion] =
                        marcasJugadores[indiceJugador]
                                [indiceObjetivo];

                posicion++;
            }
        }

        return resultado;
    }

    private int obtenerRondasJugadasResultado() {

        if (maxRondas > 0 && rondaActual > maxRondas) {
            return maxRondas;
        }

        return Math.max(1, rondaActual);
    }

    /**
     * En Criquet normal se ordena de mayor a menor puntuación.
     * En Cut Throat se ordena de menor a mayor puntuación.
     *
     * El ganador detectado se fuerza siempre a la primera posición.
     */
    private ArrayList<Integer> construirClasificacionFinal(
            int indiceGanador) {

        ArrayList<Integer> clasificacion =
                new ArrayList<>();

        for (int i = 0;
             i < nombresJugadores.length;
             i++) {

            clasificacion.add(i);
        }

        if (modoCriquet == ModoCriquet.CRIQUET) {

            clasificacion.sort(
                    (indice1, indice2) ->
                            Integer.compare(
                                    puntuacionesJugadores[indice2],
                                    puntuacionesJugadores[indice1]
                            )
            );

        } else {

            clasificacion.sort(
                    (indice1, indice2) ->
                            Integer.compare(
                                    puntuacionesJugadores[indice1],
                                    puntuacionesJugadores[indice2]
                            )
            );
        }

        clasificacion.remove(
                Integer.valueOf(indiceGanador)
        );

        clasificacion.add(
                0,
                indiceGanador
        );

        return clasificacion;
    }

    // Salida -------------------------------------------------------------------

    private void mostrarDialogoSalir() {

        new MaterialAlertDialogBuilder(
                this,
                R.style.TemaDialogoDardos
        )
                .setTitle("Salir de la partida")
                .setMessage(
                        "Puedes conservar la partida para continuarla "
                                + "más adelante o abandonarla definitivamente."
                )
                .setPositiveButton(
                        "Guardar y salir",
                        (dialog, which) -> {

                            guardarPartidaEnCurso();
                            volverMainActivity();
                        }
                )
                .setNeutralButton(
                        "Abandonar partida",
                        (dialog, which) -> {

                            partidaFinalizada = true;

                            GestorPartidaEnCurso.eliminarPartida(this);

                            volverMainActivity();
                        }
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .show();
    }


    private void volverMainActivity() {

        Intent intent =
                new Intent(
                        this,
                        MainActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
        finish();
    }


    //Guardado de seguridad -------------------------------------------------------

    @Override
    protected void onPause() {
        super.onPause();

        if (partidaCargadaCorrectamente
                && !partidaFinalizada) {

            guardarPartidaEnCurso();
        }
    }

    private int convertirDp(int dp) {

        float densidad =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(dp * densidad);
    }

    // Estado para deshacer -----------------------------------------------------

    private static class EstadoPartida {

        private final int[][] marcasJugadores;
        private final int[] puntuacionesJugadores;

        private final int jugadorActual;
        private final int rondaActual;
        private final int numeroDardo;

        private final int multiplicadorSeleccionado;
        private final String[] tiradasTurno;

        private EstadoPartida(
                int[][] marcasJugadores,
                int[] puntuacionesJugadores,
                int jugadorActual,
                int rondaActual,
                int numeroDardo,
                int multiplicadorSeleccionado,
                String[] tiradasTurno) {

            this.marcasJugadores =
                    marcasJugadores;

            this.puntuacionesJugadores =
                    puntuacionesJugadores;

            this.jugadorActual =
                    jugadorActual;

            this.rondaActual =
                    rondaActual;

            this.numeroDardo =
                    numeroDardo;

            this.multiplicadorSeleccionado =
                    multiplicadorSeleccionado;

            this.tiradasTurno =
                    tiradasTurno;
        }
    }
}
