package activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.productos.juegosdedardos.R;

import modelos.EstadoPartidaRondas;
import modelos.PartidaEnCurso;
import preferencias.GestorPartidaEnCurso;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Locale;

/**
 * Gestiona los modos:
 *
 * - Around the Clock:
 *   Carrera individual desde el 1 hasta la Diana.
 *   Cada jugador tiene su propio objetivo.
 *   Simple = 1 avance, doble = 2 avances y triple = 3 avances.
 *   Los jugadores finalizados dejan de participar y se conserva
 *   el orden completo de llegada.
 *
 * - Shanghai:
 *   Cada ronda tiene un único objetivo, desde el 1 hasta el 20 y Diana.
 *   Solo puntúan los impactos en el objetivo de la ronda:
 *   simple = valor del objetivo, doble = x2 y triple = x3.
 *
 * - Double Down:
 *   Siempre tiene 9 rondas:
 *   15, 16, Dobles, 17, 18, Triples, 19, 20 y Bull.
 *   Todos los jugadores empiezan con 50 puntos.
 *   Si un jugador no consigue ningún impacto válido durante su turno,
 *   su puntuación se divide entre dos redondeando hacia arriba.
 */
public class PartidaRondasActivity extends AppCompatActivity {

    // Extras recibidos ---------------------------------------------------------

    public static final String EXTRA_MODO_JUEGO = "modoJuego";
    public static final String EXTRA_MAX_RONDAS = "maxRondas";
    public static final String EXTRA_NUMERO_JUGADORES = "numeroJugadores";
    public static final String EXTRA_JUGADOR = "jugador";
    public static final String EXTRA_NOMBRES_JUGADORES = "nombresJugadores";
    public static final String EXTRA_COLOR_JUGADOR = "colorJugador";
    public static final String EXTRA_REANUDAR_PARTIDA = "reanudarPartida";

    // Motivo de finalización ---------------------------------------------------

    private static final String MOTIVO_MAX_RONDAS = "max_rondas";

    // Constantes generales -----------------------------------------------------

    private static final int MAX_JUGADORES = 6;
    private static final int CAPACIDAD_MAX_DARDOS = 4;
    private static final String PREF_CONFIG_PARTIDA = "configuracion_dardos_partida";
    private static final String CLAVE_DARDOS_RONDAS = "numero_dardos_rondas";

    private static final int PUNTUACION_INICIAL_DOUBLE_DOWN = 50;
    private static final int RONDAS_DOUBLE_DOWN = 9;
    private static final int OBJETIVO_INICIAL_AROUND_CLOCK = 1;
    private static final int OBJETIVO_DIANA_AROUND_CLOCK = 21;
    private static final int OBJETIVO_FINALIZADO_AROUND_CLOCK = 22;

    /*
     * Tipos de objetivo de Double Down:
     *
     * NUMERO  -> hay que acertar el número concreto.
     * DOBLES  -> vale cualquier doble.
     * TRIPLES -> vale cualquier triple.
     * BULL    -> vale Bull simple o Bull doble.
     */
    private enum TipoObjetivo {
        NUMERO,
        DOBLES,
        TRIPLES,
        BULL
    }

    private static final TipoObjetivo[] TIPOS_DOUBLE_DOWN = {
            TipoObjetivo.NUMERO,
            TipoObjetivo.NUMERO,
            TipoObjetivo.DOBLES,
            TipoObjetivo.NUMERO,
            TipoObjetivo.NUMERO,
            TipoObjetivo.TRIPLES,
            TipoObjetivo.NUMERO,
            TipoObjetivo.NUMERO,
            TipoObjetivo.BULL
    };

    private static final int[] VALORES_DOUBLE_DOWN = {
            15, 16, 0, 17, 18, 0, 19, 20, 25
    };

    private static final String[] ETIQUETAS_DOUBLE_DOWN = {
            "15",
            "16",
            "DOBLES",
            "17",
            "18",
            "TRIPLES",
            "19",
            "20",
            "BULL"
    };

    private enum ModoRondas {
        AROUND_THE_CLOCK,
        SHANGHAI,
        DOUBLE_DOWN
    }

    // Configuración de la partida ---------------------------------------------

    private ModoRondas modoRondas;
    private String modoJuego;

    /*
     * En Around the Clock:
     * ultimoNumeroAroundClock es el último número configurado.
     * maxRondas incluye también la ronda final de Diana.
     *
     * En Double Down:
     * maxRondas siempre vale 9.
     */
    private int ultimoNumeroAroundClock;
    private int maxRondas;
    private int numeroDardosTurno = 3;
    private boolean ordenAleatorio;

    private String[] nombresJugadores;

    /*
     * Se conservan por separado:
     *
     * - coloresRecursosJugadores: R.color... para enviarlos a ResultadoActivity.
     * - coloresJugadores: colores resueltos para mostrarlos en pantalla.
     */
    private int[] coloresRecursosJugadores;
    private int[] coloresJugadores;

    // Estado de la partida -----------------------------------------------------

    private int[] puntuacionesJugadores;
    private int[] objetivosJugadores;
    private boolean[] jugadoresFinalizados;
    private final ArrayList<Integer> ordenFinalizacion =
            new ArrayList<>();

    private int jugadorActual;
    private int rondaActual;
    private int dardoActual;

    private int puntosValidosTurno;
    private boolean haAcertadoObjetivoTurno;

    private boolean partidaFinalizada;
    private boolean partidaCargadaCorrectamente;
    private boolean animacionCambioTurnoActiva;

    private final String[] textosDardos =
            new String[CAPACIDAD_MAX_DARDOS];

    /*
     * Guarda el estado anterior a cada dardo y a cada cambio manual de turno.
     * Permite deshacer incluso cuando el tercer dardo ya cambió de jugador.
     */
    private final Deque<EstadoPartida> historialEstados =
            new ArrayDeque<>();

    // Vistas generales ---------------------------------------------------------

    private TextView txtModoJuego;
    private TextView txtTurnoDe;

    private TextView txtRondaActual;
    private TextView txtMaxRondas;
    private TextView txtNumeroDardos;

    private TextView txtTiradaDardo1;
    private TextView txtTiradaDardo2;
    private TextView txtTiradaDardo3;
    private TextView txtTiradaDardo4;

    private ImageView imgDardo1;
    private ImageView imgDardo2;
    private ImageView imgDardo3;
    private ImageView imgDardo4;

    private TextView txtObjetivoRonda;

    // Contenedores para adaptar la distribución -------------------------------

    private LinearLayout contenedorContenidoPartida;
    private LinearLayout contenedorMarcadores;
    private LinearLayout contenedorObjetivoRonda;

    private View panelInformacionRonda;

    // Marcador central ---------------------------------------------------------

    private TextView txtNombreJugadorActual;
    private TextView txtPuntuacionJugadorActual;

    // Marcadores laterales -----------------------------------------------------

    private final LinearLayout[] panelesJugadores =
            new LinearLayout[MAX_JUGADORES];

    private final TextView[] txtNombresJugadores =
            new TextView[MAX_JUGADORES];

    private final TextView[] txtPuntuacionesJugadores =
            new TextView[MAX_JUGADORES];

    // Botoneras ---------------------------------------------------------------

    private LinearLayout contenedorBotoneraReducida;
    private LinearLayout contenedorBotoneraCompleta;

    private Button btnFueraRonda;
    private Button btnSimpleRonda;
    private Button btnDobleRonda;
    private Button btnTripleRonda;

    private Button btnFueraNumerico;
    private Button btnBull;

    private final Button[] botonesNumericos =
            new Button[20];

    // Botones inferiores -------------------------------------------------------

    private Button btnDeshacerTirada;
    private Button btnVerMarcador;
    private Button btnSiguienteTurno;
    private Button btnSalirPartida;

    // Ciclo de vida ------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(
                ContextCompat.getColor(this, R.color.blue)
        );

        getWindow().setNavigationBarColor(
                ContextCompat.getColor(this, R.color.blue)
        );

        setContentView(R.layout.activity_partida_rondas);

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

        actualizarInterfazCompleta();
        guardarPartidaEnCurso();
    }

    // Inicialización de vistas -------------------------------------------------

    private void inicializarVistas() {

        txtModoJuego = findViewById(R.id.txtModoJuego);
        txtTurnoDe = findViewById(R.id.txtTurnoDe);

        txtRondaActual = findViewById(R.id.txtRondaActual);
        txtMaxRondas = findViewById(R.id.txtMaxRondas);
        txtNumeroDardos = findViewById(R.id.txtNumeroDardos);

        txtTiradaDardo1 = findViewById(R.id.txtTiradaDardo1);
        txtTiradaDardo2 = findViewById(R.id.txtTiradaDardo2);
        txtTiradaDardo3 = findViewById(R.id.txtTiradaDardo3);
        txtTiradaDardo4 = findViewById(R.id.txtTiradaDardo4);

        imgDardo1 = findViewById(R.id.imgDardo1);
        imgDardo2 = findViewById(R.id.imgDardo2);
        imgDardo3 = findViewById(R.id.imgDardo3);
        imgDardo4 = findViewById(R.id.imgDardo4);

        txtObjetivoRonda = findViewById(R.id.txtObjetivoRonda);

        contenedorContenidoPartida =
                findViewById(R.id.contenedorContenidoPartida);

        contenedorMarcadores =
                findViewById(R.id.contenedorMarcadores);

        contenedorObjetivoRonda =
                findViewById(R.id.contenedorObjetivoRonda);

        panelInformacionRonda =
                findViewById(R.id.panelInformacionRonda);

        txtNombreJugadorActual =
                findViewById(R.id.txtNombreJugadorActual);

        txtPuntuacionJugadorActual =
                findViewById(R.id.txtPuntuacionJugadorActual);

        // Orden visual utilizado también en PartidaPuntosActivity:
        // izquierda: 1, 3 y 5; derecha: 2, 4 y 6.

        panelesJugadores[0] = findViewById(R.id.tarjetaJugador1);
        txtNombresJugadores[0] = findViewById(R.id.txtNombreJugador1);
        txtPuntuacionesJugadores[0] = findViewById(R.id.txtPuntuacionJugador1);

        panelesJugadores[1] = findViewById(R.id.tarjetaJugador3);
        txtNombresJugadores[1] = findViewById(R.id.txtNombreJugador3);
        txtPuntuacionesJugadores[1] = findViewById(R.id.txtPuntuacionJugador3);

        panelesJugadores[2] = findViewById(R.id.tarjetaJugador2);
        txtNombresJugadores[2] = findViewById(R.id.txtNombreJugador2);
        txtPuntuacionesJugadores[2] = findViewById(R.id.txtPuntuacionJugador2);

        panelesJugadores[3] = findViewById(R.id.tarjetaJugador4);
        txtNombresJugadores[3] = findViewById(R.id.txtNombreJugador4);
        txtPuntuacionesJugadores[3] = findViewById(R.id.txtPuntuacionJugador4);

        panelesJugadores[4] = findViewById(R.id.tarjetaJugador5);
        txtNombresJugadores[4] = findViewById(R.id.txtNombreJugador5);
        txtPuntuacionesJugadores[4] = findViewById(R.id.txtPuntuacionJugador5);

        panelesJugadores[5] = findViewById(R.id.tarjetaJugador6);
        txtNombresJugadores[5] = findViewById(R.id.txtNombreJugador6);
        txtPuntuacionesJugadores[5] = findViewById(R.id.txtPuntuacionJugador6);

        contenedorBotoneraReducida =
                findViewById(R.id.contenedorBotoneraReducida);

        contenedorBotoneraCompleta =
                findViewById(R.id.contenedorBotoneraCompleta);

        btnFueraRonda = findViewById(R.id.btnFueraRonda);
        btnSimpleRonda = findViewById(R.id.btnSimpleRonda);
        btnDobleRonda = findViewById(R.id.btnDobleRonda);
        btnTripleRonda = findViewById(R.id.btnTripleRonda);

        btnFueraNumerico = findViewById(R.id.btnFueraNumerico);
        btnBull = findViewById(R.id.btnBull);

        botonesNumericos[0] = findViewById(R.id.btn1);
        botonesNumericos[1] = findViewById(R.id.btn2);
        botonesNumericos[2] = findViewById(R.id.btn3);
        botonesNumericos[3] = findViewById(R.id.btn4);
        botonesNumericos[4] = findViewById(R.id.btn5);
        botonesNumericos[5] = findViewById(R.id.btn6);
        botonesNumericos[6] = findViewById(R.id.btn7);
        botonesNumericos[7] = findViewById(R.id.btn8);
        botonesNumericos[8] = findViewById(R.id.btn9);
        botonesNumericos[9] = findViewById(R.id.btn10);
        botonesNumericos[10] = findViewById(R.id.btn11);
        botonesNumericos[11] = findViewById(R.id.btn12);
        botonesNumericos[12] = findViewById(R.id.btn13);
        botonesNumericos[13] = findViewById(R.id.btn14);
        botonesNumericos[14] = findViewById(R.id.btn15);
        botonesNumericos[15] = findViewById(R.id.btn16);
        botonesNumericos[16] = findViewById(R.id.btn17);
        botonesNumericos[17] = findViewById(R.id.btn18);
        botonesNumericos[18] = findViewById(R.id.btn19);
        botonesNumericos[19] = findViewById(R.id.btn20);

        btnDeshacerTirada = findViewById(R.id.btnDeshacerTirada);
        btnVerMarcador = findViewById(R.id.btnVerMarcador);
        btnSiguienteTurno = findViewById(R.id.btnSiguienteTurno);
        btnSalirPartida = findViewById(R.id.btnSalirPartida);
    }

    // Recepción de datos -------------------------------------------------------

    private void recibirDatosPartida() {

        Intent intent = getIntent();

        modoJuego =
                intent.getStringExtra(EXTRA_MODO_JUEGO);

        if (modoJuego == null || modoJuego.trim().isEmpty()) {
            modoJuego = "Around the Clock";
        }

        modoRondas =
                interpretarModoJuego(modoJuego);

        recibirNombresJugadores(intent);
        recibirColoresJugadores(intent);

        numeroDardosTurno = Math.max(
                1,
                Math.min(
                        CAPACIDAD_MAX_DARDOS,
                        intent.getIntExtra(
                                ConfigurarNuevaPartidaActivity.EXTRA_NUMERO_DARDOS,
                                3
                        )
                )
        );

        ordenAleatorio = intent.getBooleanExtra(
                ConfigurarNuevaPartidaActivity.EXTRA_ORDEN_ALEATORIO,
                false
        );

        configurarNumeroRondas(intent);
    }

    private ModoRondas interpretarModoJuego(String modoRecibido) {

        String modoNormalizado =
                modoRecibido
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replace("_", " ")
                        .replace("-", " ");

        if (modoNormalizado.contains("DOUBLE")) {
            return ModoRondas.DOUBLE_DOWN;
        }

        if (modoNormalizado.contains("SHANGHAI")) {
            return ModoRondas.SHANGHAI;
        }

        return ModoRondas.AROUND_THE_CLOCK;
    }

    private void recibirNombresJugadores(Intent intent) {

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

            int numeroJugadores =
                    intent.getIntExtra(
                            EXTRA_NUMERO_JUGADORES,
                            1
                    );

            numeroJugadores =
                    Math.max(
                            1,
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

                nombresJugadores[i] =
                        nombre.trim();
            }
        }

        if (nombresJugadores.length > MAX_JUGADORES) {

            nombresJugadores =
                    Arrays.copyOf(
                            nombresJugadores,
                            MAX_JUGADORES
                    );
        }

        for (int i = 0;
             i < nombresJugadores.length;
             i++) {

            if (nombresJugadores[i] == null
                    || nombresJugadores[i].trim().isEmpty()) {

                nombresJugadores[i] =
                        "Jugador " + (i + 1);

            } else {

                nombresJugadores[i] =
                        nombresJugadores[i].trim();
            }
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

        coloresRecursosJugadores =
                new int[nombresJugadores.length];

        coloresJugadores =
                new int[nombresJugadores.length];

        for (int i = 0;
             i < nombresJugadores.length;
             i++) {

            int recursoColor =
                    intent.getIntExtra(
                            EXTRA_COLOR_JUGADOR + (i + 1),
                            coloresPorDefecto[
                                    i % coloresPorDefecto.length
                                    ]
                    );

            coloresRecursosJugadores[i] =
                    recursoColor;

            coloresJugadores[i] =
                    ContextCompat.getColor(
                            this,
                            recursoColor
                    );
        }
    }

    private void configurarNumeroRondas(Intent intent) {

        if (modoRondas == ModoRondas.DOUBLE_DOWN) {

            ultimoNumeroAroundClock = 0;
            maxRondas = RONDAS_DOUBLE_DOWN;

            return;
        }

        ultimoNumeroAroundClock =
                intent.getIntExtra(
                        EXTRA_MAX_RONDAS,
                        20
                );

        ultimoNumeroAroundClock =
                Math.max(
                        1,
                        Math.min(
                                ultimoNumeroAroundClock,
                                20
                        )
                );

        /*
         * Las rondas numéricas se completan con una ronda final de Diana.
         */
        maxRondas =
                ultimoNumeroAroundClock + 1;
    }

    // Preparación inicial ------------------------------------------------------

    private void inicializarPartida() {

        puntuacionesJugadores =
                new int[nombresJugadores.length];

        objetivosJugadores =
                new int[nombresJugadores.length];

        jugadoresFinalizados =
                new boolean[nombresJugadores.length];

        Arrays.fill(
                objetivosJugadores,
                OBJETIVO_INICIAL_AROUND_CLOCK
        );

        Arrays.fill(
                jugadoresFinalizados,
                false
        );

        ordenFinalizacion.clear();

        if (modoRondas == ModoRondas.DOUBLE_DOWN) {

            Arrays.fill(
                    puntuacionesJugadores,
                    PUNTUACION_INICIAL_DOUBLE_DOWN
            );

        } else {

            Arrays.fill(
                    puntuacionesJugadores,
                    0
            );
        }

        jugadorActual = 0;
        rondaActual = 1;
        dardoActual = 0;

        puntosValidosTurno = 0;
        haAcertadoObjetivoTurno = false;

        partidaFinalizada = false;
        animacionCambioTurnoActiva = false;

        Arrays.fill(textosDardos, "");
        historialEstados.clear();
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
                PartidaEnCurso.TIPO_RONDAS
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

        partida.setColoresJugadores(
                convertirArrayIntALista(
                        coloresRecursosJugadores
                )
        );

        EstadoPartidaRondas estado =
                new EstadoPartidaRondas();

        estado.setUltimoNumeroAroundClock(
                ultimoNumeroAroundClock
        );

        estado.setMaxRondas(maxRondas);
        estado.setNumeroDardosTurno(numeroDardosTurno);
        estado.setOrdenAleatorio(ordenAleatorio);

        estado.setColoresRecursosJugadores(
                convertirArrayIntALista(
                        coloresRecursosJugadores
                )
        );

        estado.setColoresJugadores(
                convertirArrayIntALista(
                        coloresJugadores
                )
        );

        estado.setPuntuacionesJugadores(
                convertirArrayIntALista(
                        puntuacionesJugadores
                )
        );

        estado.setObjetivosJugadores(
                convertirArrayIntALista(
                        objetivosJugadores
                )
        );

        estado.setJugadoresFinalizados(
                convertirArrayBooleanALista(
                        jugadoresFinalizados
                )
        );

        estado.setOrdenFinalizacion(
                new ArrayList<>(
                        ordenFinalizacion
                )
        );

        estado.setDardoActual(
                dardoActual
        );

        estado.setPuntosValidosTurno(
                puntosValidosTurno
        );

        estado.setHaAcertadoObjetivoTurno(
                haAcertadoObjetivoTurno
        );

        estado.setTextosDardos(
                new ArrayList<>(
                        Arrays.asList(textosDardos)
                )
        );

        estado.setHistorialEstados(
                construirHistorialGuardado()
        );

        partida.setEstadoRondas(estado);

        getSharedPreferences(
                PREF_CONFIG_PARTIDA,
                MODE_PRIVATE
        ).edit()
                .putInt(
                        CLAVE_DARDOS_RONDAS,
                        numeroDardosTurno
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
                || !PartidaEnCurso.TIPO_RONDAS.equals(
                partida.getTipoPartida()
        )
                || partida.getEstadoRondas() == null
                || partida.getNombresJugadores() == null
                || partida.getNombresJugadores().isEmpty()) {

            return false;
        }

        EstadoPartidaRondas estado =
                partida.getEstadoRondas();

        modoJuego = partida.getModoJuego();
        modoRondas = interpretarModoJuego(modoJuego);

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

        for (int i = 0;
             i < nombresJugadores.length;
             i++) {

            if (nombresJugadores[i] == null
                    || nombresJugadores[i].trim().isEmpty()) {

                nombresJugadores[i] =
                        "Jugador " + (i + 1);

            } else {

                nombresJugadores[i] =
                        nombresJugadores[i].trim();
            }
        }

        ultimoNumeroAroundClock =
                Math.max(
                        0,
                        estado.getUltimoNumeroAroundClock()
                );

        maxRondas =
                estado.getMaxRondas() > 0
                        ? estado.getMaxRondas()
                        : (
                        modoRondas == ModoRondas.DOUBLE_DOWN
                        ? RONDAS_DOUBLE_DOWN
                        : Math.max(
                                2,
                                ultimoNumeroAroundClock + 1
                        )
                );

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
                        Math.min(
                                partida.getRondaActual(),
                                maxRondas
                        )
                );

        int dardosGuardados =
                estado.getNumeroDardosTurno();

        if (dardosGuardados <= 0) {

            dardosGuardados =
                    getSharedPreferences(
                            PREF_CONFIG_PARTIDA,
                            MODE_PRIVATE
                    ).getInt(
                            CLAVE_DARDOS_RONDAS,
                            3
                    );
        }

        numeroDardosTurno =
                Math.max(
                        1,
                        Math.min(
                                CAPACIDAD_MAX_DARDOS,
                                dardosGuardados
                        )
                );

        ordenAleatorio =
                estado.isOrdenAleatorio();

        dardoActual =
                Math.max(
                        0,
                        Math.min(
                                estado.getDardoActual(),
                                numeroDardosTurno
                        )
                );

        puntosValidosTurno =
                Math.max(
                        0,
                        estado.getPuntosValidosTurno()
                );

        haAcertadoObjetivoTurno =
                estado.isHaAcertadoObjetivoTurno();

        ArrayList<Integer> recursosGuardados =
                estado.getColoresRecursosJugadores();

        if (recursosGuardados == null
                || recursosGuardados.isEmpty()) {

            recursosGuardados =
                    partida.getColoresJugadores();
        }

        coloresRecursosJugadores =
                convertirListaAArrayInt(
                        recursosGuardados,
                        numeroJugadores,
                        R.color.jugador_blanco
                );

        coloresJugadores =
                convertirListaAArrayInt(
                        estado.getColoresJugadores(),
                        numeroJugadores,
                        Color.WHITE
                );

        /*
         * Compatibilidad adicional: si faltan colores resueltos,
         * se reconstruyen a partir de los recursos guardados.
         */
        for (int i = 0; i < coloresJugadores.length; i++) {

            if (coloresJugadores[i] == Color.WHITE) {

                try {

                    coloresJugadores[i] =
                            ContextCompat.getColor(
                                    this,
                                    coloresRecursosJugadores[i]
                            );

                } catch (Exception ignored) {
                    //Se conserva Color.WHITE como respaldo.
                }
            }
        }

        int puntuacionPorDefecto =
                modoRondas == ModoRondas.DOUBLE_DOWN
                        ? PUNTUACION_INICIAL_DOUBLE_DOWN
                        : 0;

        puntuacionesJugadores =
                convertirListaAArrayInt(
                        estado.getPuntuacionesJugadores(),
                        numeroJugadores,
                        puntuacionPorDefecto
                );

        objetivosJugadores =
                convertirListaAArrayInt(
                        estado.getObjetivosJugadores(),
                        numeroJugadores,
                        OBJETIVO_INICIAL_AROUND_CLOCK
                );

        jugadoresFinalizados =
                convertirListaAArrayBoolean(
                        estado.getJugadoresFinalizados(),
                        numeroJugadores
                );

        ordenFinalizacion.clear();

        if (estado.getOrdenFinalizacion() != null) {

            for (Integer indice :
                    estado.getOrdenFinalizacion()) {

                if (indice != null
                        && indice >= 0
                        && indice < numeroJugadores
                        && !ordenFinalizacion.contains(indice)) {

                    ordenFinalizacion.add(indice);
                }
            }
        }

        if (modoRondas == ModoRondas.AROUND_THE_CLOCK) {

            for (int i = 0; i < numeroJugadores; i++) {

                objetivosJugadores[i] =
                        Math.max(
                                OBJETIVO_INICIAL_AROUND_CLOCK,
                                Math.min(
                                        objetivosJugadores[i],
                                        OBJETIVO_FINALIZADO_AROUND_CLOCK
                                )
                        );

                if (objetivosJugadores[i]
                        >= OBJETIVO_FINALIZADO_AROUND_CLOCK) {

                    jugadoresFinalizados[i] = true;

                    if (!ordenFinalizacion.contains(i)) {
                        ordenFinalizacion.add(i);
                    }
                }
            }
        }

        Arrays.fill(textosDardos, "");

        if (estado.getTextosDardos() != null) {

            int limite =
                    Math.min(
                            textosDardos.length,
                            estado.getTextosDardos().size()
                    );

            for (int i = 0; i < limite; i++) {

                String textoDardo =
                        estado.getTextosDardos().get(i);

                textosDardos[i] =
                        textoDardo == null
                                ? ""
                                : textoDardo;
            }
        }

        historialEstados.clear();

        restaurarHistorialEstados(
                estado.getHistorialEstados()
        );

        partidaFinalizada = false;
        animacionCambioTurnoActiva = false;

        return true;
    }

    //Conversión del historial ----------------------------------------------------

    private ArrayList<EstadoPartidaRondas.EstadoDeshacerRondas>
    construirHistorialGuardado() {

        ArrayList<EstadoPartidaRondas.EstadoDeshacerRondas> resultado =
                new ArrayList<>();

        for (EstadoPartida estado :
                historialEstados) {

            EstadoPartidaRondas.EstadoDeshacerRondas guardado =
                    new EstadoPartidaRondas.EstadoDeshacerRondas();

            guardado.setPuntuacionesJugadores(
                    convertirArrayIntALista(
                            estado.puntuacionesJugadores
                    )
            );

            guardado.setObjetivosJugadores(
                    convertirArrayIntALista(
                            estado.objetivosJugadores
                    )
            );

            guardado.setJugadoresFinalizados(
                    convertirArrayBooleanALista(
                            estado.jugadoresFinalizados
                    )
            );

            guardado.setOrdenFinalizacion(
                    new ArrayList<>(
                            estado.ordenFinalizacion
                    )
            );

            guardado.setJugadorActual(
                    estado.jugadorActual
            );

            guardado.setRondaActual(
                    estado.rondaActual
            );

            guardado.setDardoActual(
                    estado.dardoActual
            );

            guardado.setPuntosValidosTurno(
                    estado.puntosValidosTurno
            );

            guardado.setHaAcertadoObjetivoTurno(
                    estado.haAcertadoObjetivoTurno
            );

            guardado.setTextosDardos(
                    new ArrayList<>(
                            Arrays.asList(
                                    estado.textosDardos
                            )
                    )
            );

            resultado.add(guardado);
        }

        return resultado;
    }

    private void restaurarHistorialEstados(
            ArrayList<EstadoPartidaRondas.EstadoDeshacerRondas> estados
    ) {

        if (estados == null) {
            return;
        }

        for (EstadoPartidaRondas.EstadoDeshacerRondas guardado :
                estados) {

            if (guardado == null) {
                continue;
            }

            historialEstados.addLast(
                    new EstadoPartida(
                            convertirListaAArrayInt(
                                    guardado.getPuntuacionesJugadores(),
                                    nombresJugadores.length,
                                    modoRondas == ModoRondas.DOUBLE_DOWN
                                            ? PUNTUACION_INICIAL_DOUBLE_DOWN
                                            : 0
                            ),
                            convertirListaAArrayInt(
                                    guardado.getObjetivosJugadores(),
                                    nombresJugadores.length,
                                    OBJETIVO_INICIAL_AROUND_CLOCK
                            ),
                            convertirListaAArrayBoolean(
                                    guardado.getJugadoresFinalizados(),
                                    nombresJugadores.length
                            ),
                            guardado.getOrdenFinalizacion() == null
                                    ? new ArrayList<>()
                                    : new ArrayList<>(
                                    guardado.getOrdenFinalizacion()
                            ),
                            guardado.getJugadorActual(),
                            guardado.getRondaActual(),
                            guardado.getDardoActual(),
                            guardado.getPuntosValidosTurno(),
                            guardado.isHaAcertadoObjetivoTurno(),
                            convertirListaAArrayString(
                                    guardado.getTextosDardos(),
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

    private ArrayList<Boolean> convertirArrayBooleanALista(
            boolean[] valores
    ) {

        ArrayList<Boolean> resultado =
                new ArrayList<>();

        if (valores != null) {

            for (boolean valor : valores) {
                resultado.add(valor);
            }
        }

        return resultado;
    }

    private boolean[] convertirListaAArrayBoolean(
            ArrayList<Boolean> valores,
            int tamano
    ) {

        boolean[] resultado =
                new boolean[tamano];

        if (valores == null) {
            return resultado;
        }

        int limite =
                Math.min(
                        valores.size(),
                        tamano
                );

        for (int i = 0; i < limite; i++) {

            Boolean valor =
                    valores.get(i);

            resultado[i] =
                    valor != null && valor;
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

    // Configuración de listeners ----------------------------------------------

    private void configurarListeners() {

        btnFueraRonda.setOnClickListener(
                view -> registrarFallo()
        );

        btnSimpleRonda.setOnClickListener(
                view -> registrarImpactoObjetivoReducido(1)
        );

        btnDobleRonda.setOnClickListener(
                view -> registrarImpactoObjetivoReducido(2)
        );

        btnTripleRonda.setOnClickListener(
                view -> registrarImpactoObjetivoReducido(3)
        );

        btnFueraNumerico.setOnClickListener(
                view -> registrarFallo()
        );

        for (int i = 0;
             i < botonesNumericos.length;
             i++) {

            final int numero = i + 1;

            botonesNumericos[i].setOnClickListener(
                    view -> registrarImpactoBotoneraCompleta(numero)
            );
        }

        btnBull.setOnClickListener(
                view -> registrarBullBotoneraCompleta()
        );

        btnDeshacerTirada.setOnClickListener(
                view -> deshacerUltimaTirada()
        );

        btnVerMarcador.setOnClickListener(
                view -> mostrarDialogoMarcador()
        );

        btnSiguienteTurno.setOnClickListener(
                view -> finalizarTurnoManual()
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

    // Registro de dardos -------------------------------------------------------

    private void registrarFallo() {

        if (!puedeRegistrarDardo()) {
            return;
        }

        guardarEstadoActual();

        textosDardos[dardoActual] = "0";

        finalizarRegistroDardo();
    }

    /**
     * Botonera reducida:
     *
     * - Around the Clock: simple/doble/triple del objetivo actual.
     * - Double Down numérico: simple/doble/triple del número objetivo.
     * - Double Down Bull: Bull simple o Bull doble.
     */
    private void registrarImpactoObjetivoReducido(
            int multiplicador) {

        if (!puedeRegistrarDardo()) {
            return;
        }

        if (esObjetivoBull()
                && multiplicador == 3) {

            Toast.makeText(
                    this,
                    "La Diana no admite triple",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        guardarEstadoActual();

        int puntosValidos;
        String texto;

        if (modoRondas == ModoRondas.AROUND_THE_CLOCK) {

            int objetivoActual =
                    objetivosJugadores[jugadorActual];

            if (objetivoActual
                    == OBJETIVO_DIANA_AROUND_CLOCK) {

                texto =
                        multiplicador == 1
                                ? "BULL"
                                : "D-BULL";

            } else {

                texto =
                        crearTextoMultiplicador(
                                objetivoActual,
                                multiplicador
                        );
            }

            avanzarObjetivoAroundClock(
                    multiplicador
            );

            textosDardos[dardoActual] = texto;
            haAcertadoObjetivoTurno = true;

            finalizarRegistroDardo();
            return;

        } else if (modoRondas == ModoRondas.SHANGHAI) {

            if (esRondaDianaSecuencial()) {

                puntosValidos =
                        multiplicador == 1
                                ? 25
                                : 50;

                texto =
                        multiplicador == 1
                                ? "BULL"
                                : "D-BULL";

            } else {

                int numeroObjetivo =
                        rondaActual;

                puntosValidos =
                        numeroObjetivo
                                * multiplicador;

                texto =
                        crearTextoMultiplicador(
                                numeroObjetivo,
                                multiplicador
                        );
            }

        } else {

            TipoObjetivo tipoObjetivo =
                    obtenerTipoObjetivoActual();

            if (tipoObjetivo == TipoObjetivo.BULL) {

                puntosValidos =
                        multiplicador == 1
                                ? 25
                                : 50;

                texto =
                        multiplicador == 1
                                ? "BULL"
                                : "D-BULL";

            } else {

                int numeroObjetivo =
                        obtenerValorObjetivoActual();

                puntosValidos =
                        numeroObjetivo
                                * multiplicador;

                texto =
                        crearTextoMultiplicador(
                                numeroObjetivo,
                                multiplicador
                        );
            }
        }

        aplicarImpactoValido(
                puntosValidos,
                texto
        );
    }

    /**
     * Botonera completa utilizada en las rondas de cualquier doble
     * o cualquier triple de Double Down.
     */
    private void registrarImpactoBotoneraCompleta(
            int numero) {

        if (!puedeRegistrarDardo()) {
            return;
        }

        TipoObjetivo tipoObjetivo =
                obtenerTipoObjetivoActual();

        if (tipoObjetivo != TipoObjetivo.DOBLES
                && tipoObjetivo != TipoObjetivo.TRIPLES) {

            return;
        }

        guardarEstadoActual();

        int multiplicador =
                tipoObjetivo == TipoObjetivo.DOBLES
                        ? 2
                        : 3;

        int puntosValidos =
                numero * multiplicador;

        String texto =
                crearTextoMultiplicador(
                        numero,
                        multiplicador
                );

        aplicarImpactoValido(
                puntosValidos,
                texto
        );
    }

    private void registrarBullBotoneraCompleta() {

        if (!puedeRegistrarDardo()) {
            return;
        }

        TipoObjetivo tipoObjetivo =
                obtenerTipoObjetivoActual();

        if (tipoObjetivo == TipoObjetivo.TRIPLES) {

            Toast.makeText(
                    this,
                    "No existe triple Diana",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (tipoObjetivo != TipoObjetivo.DOBLES) {
            return;
        }

        guardarEstadoActual();

        /*
         * En la ronda de dobles, el botón Bull representa Bull doble.
         */
        aplicarImpactoValido(
                50,
                "D-BULL"
        );
    }

    private void avanzarObjetivoAroundClock(
            int posiciones) {

        if (modoRondas != ModoRondas.AROUND_THE_CLOCK
                || jugadoresFinalizados[jugadorActual]) {

            return;
        }

        int nuevoObjetivo =
                objetivosJugadores[jugadorActual]
                        + Math.max(1, posiciones);

        if (nuevoObjetivo
                >= OBJETIVO_FINALIZADO_AROUND_CLOCK) {

            objetivosJugadores[jugadorActual] =
                    OBJETIVO_FINALIZADO_AROUND_CLOCK;

            jugadoresFinalizados[jugadorActual] = true;

            if (!ordenFinalizacion.contains(jugadorActual)) {

                ordenFinalizacion.add(
                        jugadorActual
                );
            }

        } else {

            objetivosJugadores[jugadorActual] =
                    nuevoObjetivo;
        }
    }

    private void aplicarImpactoValido(
            int puntos,
            String texto) {

        puntosValidosTurno += puntos;
        haAcertadoObjetivoTurno = true;

        textosDardos[dardoActual] = texto;

        finalizarRegistroDardo();
    }

    private boolean puedeRegistrarDardo() {

        if (partidaFinalizada
                || animacionCambioTurnoActiva) {

            return false;
        }

        if (modoRondas == ModoRondas.AROUND_THE_CLOCK
                && jugadoresFinalizados[jugadorActual]) {

            return false;
        }

        if (dardoActual >= numeroDardosTurno) {

            Toast.makeText(
                    this,
                    "Ya se han lanzado todos los dardos del turno",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        return true;
    }

    private void finalizarRegistroDardo() {

        dardoActual++;

        actualizarInterfazCompleta();
        guardarPartidaEnCurso();

        if (modoRondas == ModoRondas.AROUND_THE_CLOCK
                && jugadoresFinalizados[jugadorActual]) {

            finalizarTurnoConAnimacion();
            return;
        }

        if (dardoActual >= numeroDardosTurno) {

            finalizarTurnoConAnimacion();
        }
    }

    // Finalización del turno ---------------------------------------------------

    private void finalizarTurnoManual() {

        if (partidaFinalizada
                || animacionCambioTurnoActiva) {

            return;
        }

        /*
         * Se guarda también cuando el jugador pasa sin lanzar.
         * Así el cambio manual completo se puede deshacer.
         */
        guardarEstadoActual();

        finalizarTurnoConAnimacion();
    }

    private void finalizarTurnoConAnimacion() {

        if (partidaFinalizada
                || animacionCambioTurnoActiva) {

            return;
        }

        animacionCambioTurnoActiva = true;
        bloquearBotonesPartida();

        txtNombreJugadorActual.animate()
                .alpha(0f)
                .setDuration(230)
                .start();

        txtPuntuacionJugadorActual.animate()
                .alpha(0f)
                .setDuration(230)
                .withEndAction(() -> {

                    aplicarResultadoTurno();

                    boolean ultimaRondaCompletada =
                            avanzarJugadorYRonda();

                    if (ultimaRondaCompletada) {

                        animacionCambioTurnoActiva = false;

                        txtNombreJugadorActual.setAlpha(1f);
                        txtPuntuacionJugadorActual.setAlpha(1f);

                        finalizarPartida();
                        return;
                    }

                    prepararNuevoTurno();
                    actualizarInterfazCompleta();
                    guardarPartidaEnCurso();

                    txtNombreJugadorActual.setAlpha(0f);
                    txtPuntuacionJugadorActual.setAlpha(0f);

                    txtNombreJugadorActual.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start();

                    txtPuntuacionJugadorActual.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .withEndAction(() -> {

                                animacionCambioTurnoActiva = false;
                                actualizarEstadoBotones();
                            })
                            .start();
                })
                .start();
    }

    private void aplicarResultadoTurno() {

        if (modoRondas == ModoRondas.DOUBLE_DOWN) {

            aplicarResultadoTurnoDoubleDown();

        } else if (modoRondas == ModoRondas.SHANGHAI) {

            puntuacionesJugadores[jugadorActual] +=
                    puntosValidosTurno;
        }
    }

    private void aplicarResultadoTurnoDoubleDown() {

        int puntuacionAnterior =
                puntuacionesJugadores[jugadorActual];

        if (haAcertadoObjetivoTurno) {

            puntuacionesJugadores[jugadorActual] =
                    puntuacionAnterior
                            + puntosValidosTurno;

        } else {

            /*
             * División entre dos redondeando hacia arriba:
             *
             * 25 -> 13
             * 13 -> 7
             * 7  -> 4
             */
            puntuacionesJugadores[jugadorActual] =
                    (puntuacionAnterior + 1) / 2;
        }
    }

    /**
     * @return true si todos los jugadores ya completaron la última ronda.
     */
    private boolean avanzarJugadorYRonda() {

        if (modoRondas == ModoRondas.AROUND_THE_CLOCK
                && todosJugadoresFinalizados()) {

            return true;
        }

        int jugadoresComprobados = 0;

        do {

            jugadorActual++;

            if (jugadorActual >= nombresJugadores.length) {

                jugadorActual = 0;
                rondaActual++;
            }

            jugadoresComprobados++;

        } while (
                modoRondas == ModoRondas.AROUND_THE_CLOCK
                        && jugadoresFinalizados[jugadorActual]
                        && jugadoresComprobados
                        < nombresJugadores.length
        );

        if (modoRondas == ModoRondas.AROUND_THE_CLOCK) {
            return todosJugadoresFinalizados();
        }

        return rondaActual > maxRondas;
    }

    private boolean todosJugadoresFinalizados() {

        if (modoRondas != ModoRondas.AROUND_THE_CLOCK) {
            return false;
        }

        for (boolean finalizado :
                jugadoresFinalizados) {

            if (!finalizado) {
                return false;
            }
        }

        return true;
    }

    private void prepararNuevoTurno() {

        dardoActual = 0;
        puntosValidosTurno = 0;
        haAcertadoObjetivoTurno = false;

        Arrays.fill(textosDardos, "");
    }

    // Objetivo actual ----------------------------------------------------------

    private TipoObjetivo obtenerTipoObjetivoActual() {

        if (modoRondas == ModoRondas.AROUND_THE_CLOCK) {

            return objetivosJugadores[jugadorActual]
                    == OBJETIVO_DIANA_AROUND_CLOCK
                    ? TipoObjetivo.BULL
                    : TipoObjetivo.NUMERO;
        }

        if (modoRondas == ModoRondas.SHANGHAI) {

            return esRondaDianaSecuencial()
                    ? TipoObjetivo.BULL
                    : TipoObjetivo.NUMERO;
        }

        int indice =
                Math.max(
                        0,
                        Math.min(
                                rondaActual - 1,
                                TIPOS_DOUBLE_DOWN.length - 1
                        )
                );

        return TIPOS_DOUBLE_DOWN[indice];
    }

    private int obtenerValorObjetivoActual() {

        if (modoRondas == ModoRondas.AROUND_THE_CLOCK) {

            int objetivo =
                    objetivosJugadores[jugadorActual];

            return objetivo
                    == OBJETIVO_DIANA_AROUND_CLOCK
                    ? 25
                    : objetivo;
        }

        if (modoRondas == ModoRondas.SHANGHAI) {

            return esRondaDianaSecuencial()
                    ? 25
                    : rondaActual;
        }

        int indice =
                Math.max(
                        0,
                        Math.min(
                                rondaActual - 1,
                                VALORES_DOUBLE_DOWN.length - 1
                        )
                );

        return VALORES_DOUBLE_DOWN[indice];
    }

    private String obtenerTextoObjetivoActual() {

        if (modoRondas == ModoRondas.AROUND_THE_CLOCK) {

            int objetivo =
                    objetivosJugadores[jugadorActual];

            if (objetivo
                    >= OBJETIVO_FINALIZADO_AROUND_CLOCK) {

                return "FINALIZADO";
            }

            if (objetivo
                    == OBJETIVO_DIANA_AROUND_CLOCK) {

                return "DIANA";
            }

            return String.valueOf(objetivo);
        }

        if (modoRondas == ModoRondas.SHANGHAI) {

            if (esRondaDianaSecuencial()) {
                return "DIANA";
            }

            return String.valueOf(rondaActual);
        }

        int indice =
                Math.max(
                        0,
                        Math.min(
                                rondaActual - 1,
                                ETIQUETAS_DOUBLE_DOWN.length - 1
                        )
                );

        return ETIQUETAS_DOUBLE_DOWN[indice];
    }

    private boolean esRondaDianaSecuencial() {

        return modoRondas != ModoRondas.DOUBLE_DOWN
                && rondaActual == maxRondas;
    }

    private boolean esObjetivoBull() {

        return obtenerTipoObjetivoActual()
                == TipoObjetivo.BULL;
    }

    private String crearTextoMultiplicador(
            int numero,
            int multiplicador) {

        if (multiplicador == 2) {
            return "D" + numero;
        }

        if (multiplicador == 3) {
            return "T" + numero;
        }

        return String.valueOf(numero);
    }

    // Deshacer -----------------------------------------------------------------

    private void guardarEstadoActual() {

        historialEstados.push(
                new EstadoPartida(
                        puntuacionesJugadores.clone(),
                        objetivosJugadores.clone(),
                        jugadoresFinalizados.clone(),
                        new ArrayList<>(ordenFinalizacion),
                        jugadorActual,
                        rondaActual,
                        dardoActual,
                        puntosValidosTurno,
                        haAcertadoObjetivoTurno,
                        textosDardos.clone()
                )
        );
    }

    private void deshacerUltimaTirada() {

        if (partidaFinalizada
                || animacionCambioTurnoActiva) {

            return;
        }

        if (historialEstados.isEmpty()) {

            Toast.makeText(
                    this,
                    "No hay ninguna tirada para deshacer",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        EstadoPartida estadoAnterior =
                historialEstados.pop();

        puntuacionesJugadores =
                estadoAnterior
                        .puntuacionesJugadores
                        .clone();

        objetivosJugadores =
                estadoAnterior
                        .objetivosJugadores
                        .clone();

        jugadoresFinalizados =
                estadoAnterior
                        .jugadoresFinalizados
                        .clone();

        ordenFinalizacion.clear();
        ordenFinalizacion.addAll(
                estadoAnterior.ordenFinalizacion
        );

        jugadorActual =
                estadoAnterior.jugadorActual;

        rondaActual =
                estadoAnterior.rondaActual;

        dardoActual =
                estadoAnterior.dardoActual;

        puntosValidosTurno =
                estadoAnterior.puntosValidosTurno;

        haAcertadoObjetivoTurno =
                estadoAnterior.haAcertadoObjetivoTurno;

        System.arraycopy(
                estadoAnterior.textosDardos,
                0,
                textosDardos,
                0,
                textosDardos.length
        );

        actualizarInterfazCompleta();
        guardarPartidaEnCurso();
    }

    // Actualización completa ---------------------------------------------------

    private void actualizarInterfazCompleta() {

        actualizarCabecera();
        actualizarPanelesJugadores();
        actualizarMarcadores();
        actualizarJugadorActivo();
        actualizarInformacionRonda();
        actualizarInformacionTurno();
        actualizarObjetivoRonda();
        actualizarBotoneras();
        actualizarEstadoBotones();
    }

    private void actualizarCabecera() {

        txtModoJuego.setText(
                obtenerNombreModoMostrado()
        );
    }

    private String obtenerNombreModoMostrado() {

        if (modoRondas == ModoRondas.DOUBLE_DOWN) {
            return "DOUBLE DOWN";
        }

        if (modoRondas == ModoRondas.SHANGHAI) {
            return "SHANGHAI";
        }

        return "AROUND THE CLOCK";
    }

    private void actualizarPanelesJugadores() {

        for (int i = 0;
             i < MAX_JUGADORES;
             i++) {

            if (i < nombresJugadores.length) {

                panelesJugadores[i].setVisibility(
                        View.VISIBLE
                );

                txtNombresJugadores[i].setText(
                        nombresJugadores[i]
                );

                txtNombresJugadores[i].setTextColor(
                        coloresJugadores[i]
                );

            } else {

                panelesJugadores[i].setVisibility(
                        View.GONE
                );
            }
        }
    }

    private void actualizarMarcadores() {

        for (int i = 0;
             i < nombresJugadores.length;
             i++) {

            if (modoRondas == ModoRondas.AROUND_THE_CLOCK) {

                txtPuntuacionesJugadores[i].setText(
                        obtenerTextoObjetivoJugador(i)
                );

            } else {

                txtPuntuacionesJugadores[i].setText(
                        String.valueOf(
                                puntuacionesJugadores[i]
                        )
                );
            }

            panelesJugadores[i].setAlpha(
                    i == jugadorActual
                            ? 1.0f
                            : 0.62f
            );

            txtPuntuacionesJugadores[i].setTextColor(
                    i == jugadorActual
                            ? coloresJugadores[i]
                            : Color.WHITE
            );
        }
    }

    private String obtenerTextoObjetivoJugador(
            int indiceJugador) {

        if (modoRondas != ModoRondas.AROUND_THE_CLOCK) {
            return String.valueOf(
                    puntuacionesJugadores[indiceJugador]
            );
        }

        int objetivo =
                objetivosJugadores[indiceJugador];

        if (objetivo
                >= OBJETIVO_FINALIZADO_AROUND_CLOCK) {

            return "FIN";
        }

        if (objetivo
                == OBJETIVO_DIANA_AROUND_CLOCK) {

            return "DIANA";
        }

        return String.valueOf(objetivo);
    }

    private void actualizarJugadorActivo() {

        String nombreActual =
                nombresJugadores[jugadorActual];

        int colorActual =
                coloresJugadores[jugadorActual];

        txtTurnoDe.setText(
                nombreActual.toUpperCase(Locale.ROOT)
        );

        txtTurnoDe.setTextColor(
                colorActual
        );

        txtNombreJugadorActual.setText(
                nombreActual
        );

        txtNombreJugadorActual.setTextColor(
                colorActual
        );

        if (modoRondas == ModoRondas.AROUND_THE_CLOCK) {

            txtPuntuacionJugadorActual.setText(
                    obtenerTextoObjetivoJugador(
                            jugadorActual
                    )
            );

        } else {

            txtPuntuacionJugadorActual.setText(
                    String.valueOf(
                            obtenerPuntuacionProvisionalTurno()
                    )
            );
        }
    }

    private int obtenerPuntuacionProvisionalTurno() {

        if (modoRondas == ModoRondas.AROUND_THE_CLOCK) {

            return objetivosJugadores[jugadorActual]
                    >= OBJETIVO_FINALIZADO_AROUND_CLOCK
                    ? OBJETIVO_DIANA_AROUND_CLOCK
                    : objetivosJugadores[jugadorActual];
        }

        int puntuacionActual =
                puntuacionesJugadores[jugadorActual];

        /*
         * La penalización de Double Down se aplica únicamente al terminar
         * el turno. Mientras se lanzan los dardos se muestra la puntuación
         * actual o la suma provisional si ya hubo algún acierto.
         */
        return puntuacionActual
                + puntosValidosTurno;
    }

    private void actualizarInformacionRonda() {

        txtRondaActual.setText(
                String.valueOf(rondaActual)
        );

        if (modoRondas == ModoRondas.AROUND_THE_CLOCK) {

            txtMaxRondas.setText("");

        } else {

            txtMaxRondas.setText(
                    "/" + maxRondas
            );
        }
    }

    private void actualizarInformacionTurno() {

        txtTiradaDardo1.setText(textosDardos[0]);
        txtTiradaDardo2.setText(textosDardos[1]);
        txtTiradaDardo3.setText(textosDardos[2]);
        txtTiradaDardo4.setText(textosDardos[3]);

        int numeroDardoMostrado =
                Math.min(
                        dardoActual + 1,
                        numeroDardosTurno
                );

        txtNumeroDardos.setText(
                numeroDardoMostrado
                        + " / "
                        + numeroDardosTurno
        );

        actualizarVisibilidadDardos();

        imgDardo1.setAlpha(
                dardoActual == 0 ? 1.0f : 0.35f
        );

        imgDardo2.setAlpha(
                dardoActual == 1 ? 1.0f : 0.35f
        );

        imgDardo3.setAlpha(
                dardoActual == 2 ? 1.0f : 0.35f
        );

        imgDardo4.setAlpha(
                dardoActual == 3 ? 1.0f : 0.35f
        );

        if (dardoActual >= numeroDardosTurno) {

            imgDardo1.setAlpha(1.0f);
            imgDardo2.setAlpha(1.0f);
            imgDardo3.setAlpha(1.0f);
            imgDardo4.setAlpha(1.0f);
        }
    }

    private void actualizarVisibilidadDardos() {

        imgDardo1.setVisibility(View.VISIBLE);
        txtTiradaDardo1.setVisibility(View.VISIBLE);

        imgDardo2.setVisibility(
                numeroDardosTurno >= 2
                        ? View.VISIBLE
                        : View.GONE
        );

        txtTiradaDardo2.setVisibility(
                numeroDardosTurno >= 2
                        ? View.VISIBLE
                        : View.GONE
        );

        imgDardo3.setVisibility(
                numeroDardosTurno >= 3
                        ? View.VISIBLE
                        : View.GONE
        );

        txtTiradaDardo3.setVisibility(
                numeroDardosTurno >= 3
                        ? View.VISIBLE
                        : View.GONE
        );

        imgDardo4.setVisibility(
                numeroDardosTurno >= 4
                        ? View.VISIBLE
                        : View.GONE
        );

        txtTiradaDardo4.setVisibility(
                numeroDardosTurno >= 4
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void actualizarObjetivoRonda() {

        txtObjetivoRonda.setText(
                obtenerTextoObjetivoActual()
        );
    }

    private void actualizarBotoneras() {

        TipoObjetivo tipoObjetivo =
                obtenerTipoObjetivoActual();

        boolean usarBotoneraCompleta =
                modoRondas == ModoRondas.DOUBLE_DOWN
                        && (
                        tipoObjetivo == TipoObjetivo.DOBLES
                                || tipoObjetivo == TipoObjetivo.TRIPLES
                );

        contenedorBotoneraReducida.setVisibility(
                usarBotoneraCompleta
                        ? View.GONE
                        : View.VISIBLE
        );

        contenedorBotoneraCompleta.setVisibility(
                usarBotoneraCompleta
                        ? View.VISIBLE
                        : View.GONE
        );

        ajustarDistribucionSegunBotonera(
                usarBotoneraCompleta
        );

        boolean objetivoBull =
                tipoObjetivo == TipoObjetivo.BULL;

        btnTripleRonda.setEnabled(
                !objetivoBull
                        && !partidaFinalizada
                        && !animacionCambioTurnoActiva
        );

        btnTripleRonda.setAlpha(
                objetivoBull
                        ? 0.35f
                        : 1.0f
        );

        /*
         * En la ronda de Triples no existe triple Bull.
         */
        btnBull.setEnabled(
                usarBotoneraCompleta
                        && tipoObjetivo == TipoObjetivo.DOBLES
                        && !partidaFinalizada
                        && !animacionCambioTurnoActiva
        );

        btnBull.setAlpha(
                tipoObjetivo == TipoObjetivo.DOBLES
                        ? 1.0f
                        : 0.35f
        );
    }

    /**
     * Ajusta la pantalla según la botonera que esté visible.
     *
     * Con la botonera completa se oculta el objetivo porque ya aparece
     * representado por los botones numéricos. También se compactan
     * marcadores y panel de ronda para evitar que la pantalla quede
     * excesivamente alta en dispositivos reales.
     */
    private void ajustarDistribucionSegunBotonera(
            boolean usarBotoneraCompleta) {

        if (usarBotoneraCompleta) {

            /*
             * El objetivo sigue visible para indicar claramente
             * si la ronda actual es de DOBLES o TRIPLES.
             */
            contenedorObjetivoRonda.setVisibility(
                    View.VISIBLE
            );

            contenedorContenidoPartida.setPadding(
                    dpToPx(8),
                    dpToPx(6),
                    dpToPx(8),
                    0
            );

            cambiarAlturaVista(
                    contenedorMarcadores,
                    dpToPx(112)
            );

            cambiarMargenSuperior(
                    panelInformacionRonda,
                    dpToPx(5)
            );

            cambiarAlturaVista(
                    contenedorObjetivoRonda,
                    dpToPx(52)
            );

            cambiarMargenSuperior(
                    contenedorObjetivoRonda,
                    dpToPx(5)
            );

            cambiarMargenInferior(
                    contenedorObjetivoRonda,
                    dpToPx(2)
            );

        } else {

            contenedorObjetivoRonda.setVisibility(
                    View.VISIBLE
            );

            contenedorContenidoPartida.setPadding(
                    dpToPx(8),
                    dpToPx(28),
                    dpToPx(8),
                    dpToPx(2)
            );

            cambiarAlturaVista(
                    contenedorMarcadores,
                    dpToPx(145)
            );

            cambiarMargenSuperior(
                    panelInformacionRonda,
                    dpToPx(18)
            );

            cambiarAlturaVista(
                    contenedorObjetivoRonda,
                    dpToPx(64)
            );

            cambiarMargenSuperior(
                    contenedorObjetivoRonda,
                    dpToPx(30)
            );

            cambiarMargenInferior(
                    contenedorObjetivoRonda,
                    dpToPx(2)
            );
        }
    }

    private void cambiarAlturaVista(
            View vista,
            int nuevaAltura) {

        ViewGroup.LayoutParams parametros =
                vista.getLayoutParams();

        if (parametros == null) {
            return;
        }

        if (parametros.height == nuevaAltura) {
            return;
        }

        parametros.height = nuevaAltura;
        vista.setLayoutParams(parametros);
    }

    private void cambiarMargenSuperior(
            View vista,
            int margenSuperior) {

        ViewGroup.LayoutParams parametrosGenerales =
                vista.getLayoutParams();

        if (!(parametrosGenerales
                instanceof ViewGroup.MarginLayoutParams)) {

            return;
        }

        ViewGroup.MarginLayoutParams parametros =
                (ViewGroup.MarginLayoutParams)
                        parametrosGenerales;

        if (parametros.topMargin == margenSuperior) {
            return;
        }

        parametros.topMargin = margenSuperior;
        vista.setLayoutParams(parametros);
    }

    private void cambiarMargenInferior(
            View vista,
            int margenInferior) {

        ViewGroup.LayoutParams parametrosGenerales =
                vista.getLayoutParams();

        if (!(parametrosGenerales
                instanceof ViewGroup.MarginLayoutParams)) {

            return;
        }

        ViewGroup.MarginLayoutParams parametros =
                (ViewGroup.MarginLayoutParams)
                        parametrosGenerales;

        if (parametros.bottomMargin == margenInferior) {
            return;
        }

        parametros.bottomMargin = margenInferior;
        vista.setLayoutParams(parametros);
    }

    private int dpToPx(int dp) {

        return Math.round(
                dp
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private void actualizarEstadoBotones() {

        if (partidaFinalizada
                || animacionCambioTurnoActiva) {

            bloquearBotonesPartida();
            return;
        }

        boolean puedeLanzar =
                dardoActual < numeroDardosTurno;

        btnFueraRonda.setEnabled(puedeLanzar);
        btnSimpleRonda.setEnabled(puedeLanzar);
        btnDobleRonda.setEnabled(puedeLanzar);

        btnTripleRonda.setEnabled(
                puedeLanzar
                        && !esObjetivoBull()
        );

        btnFueraNumerico.setEnabled(puedeLanzar);

        TipoObjetivo tipoObjetivo =
                obtenerTipoObjetivoActual();

        for (Button boton : botonesNumericos) {
            boton.setEnabled(puedeLanzar);
        }

        btnBull.setEnabled(
                puedeLanzar
                        && tipoObjetivo == TipoObjetivo.DOBLES
        );

        btnDeshacerTirada.setEnabled(
                !historialEstados.isEmpty()
        );

        btnDeshacerTirada.setAlpha(
                historialEstados.isEmpty()
                        ? 0.45f
                        : 1.0f
        );

        btnSiguienteTurno.setEnabled(true);
    }

    private void bloquearBotonesPartida() {

        btnFueraRonda.setEnabled(false);
        btnSimpleRonda.setEnabled(false);
        btnDobleRonda.setEnabled(false);
        btnTripleRonda.setEnabled(false);

        btnFueraNumerico.setEnabled(false);
        btnBull.setEnabled(false);

        for (Button boton : botonesNumericos) {
            boton.setEnabled(false);
        }

        btnDeshacerTirada.setEnabled(false);
        btnSiguienteTurno.setEnabled(false);
    }

    // Marcador emergente -------------------------------------------------------

    private void mostrarDialogoMarcador() {

        StringBuilder mensaje =
                new StringBuilder();

        boolean mostrarPuntosJugador =
                debeMostrarPuntosEnMarcador();

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
                    .append(": ");

            if (mostrarPuntosJugador) {

                mensaje.append(
                        obtenerPuntuacionMarcador(
                                indiceJugador
                        )
                ).append(" puntos");

            } else {

                mensaje.append("Objetivo ")
                        .append(
                                obtenerObjetivoActualMarcador(
                                        indiceJugador
                                )
                        );
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

    private boolean debeMostrarPuntosEnMarcador() {

        switch (modoRondas) {

            case AROUND_THE_CLOCK:
                return false;

            case SHANGHAI:
            case DOUBLE_DOWN:
            default:
                return true;
        }
    }

    private String obtenerObjetivoActualMarcador(
            int indiceJugador
    ) {

        switch (modoRondas) {

            case AROUND_THE_CLOCK:
                return obtenerTextoObjetivoJugador(
                        indiceJugador
                );

            case SHANGHAI:
            case DOUBLE_DOWN:
            default:
                return obtenerTextoObjetivoActual();
        }
    }

    private int obtenerPuntuacionMarcador(
            int indiceJugador
    ) {

        int puntuacion =
                puntuacionesJugadores[indiceJugador];

        /*
         * En Shanghai, la puntuación del turno se guarda definitivamente
         * al cambiar de turno. Para que el marcador emergente se vea
         * actualizado durante la ronda, al jugador actual se le suma
         * provisionalmente lo conseguido en el turno.
         */
        if (modoRondas == ModoRondas.SHANGHAI
                && indiceJugador == jugadorActual) {

            puntuacion += puntosValidosTurno;
        }

        return puntuacion;
    }

    private ArrayList<Integer> construirClasificacionMarcador() {

        ArrayList<Integer> clasificacion =
                new ArrayList<>();

        for (int i = 0;
             i < nombresJugadores.length;
             i++) {

            clasificacion.add(i);
        }

        if (modoRondas == ModoRondas.AROUND_THE_CLOCK) {

            clasificacion.clear();

            for (Integer indice :
                    ordenFinalizacion) {

                if (indice != null
                        && indice >= 0
                        && indice < nombresJugadores.length
                        && !clasificacion.contains(indice)) {

                    clasificacion.add(indice);
                }
            }

            ArrayList<Integer> restantes =
                    new ArrayList<>();

            for (int i = 0;
                 i < nombresJugadores.length;
                 i++) {

                if (!clasificacion.contains(i)) {
                    restantes.add(i);
                }
            }

            restantes.sort(
                    (indice1, indice2) ->
                            Integer.compare(
                                    objetivosJugadores[indice2],
                                    objetivosJugadores[indice1]
                            )
            );

            clasificacion.addAll(restantes);

        } else {

            /*
             * Para Shanghai usamos obtenerPuntuacionMarcador(),
             * así el orden también tiene en cuenta los puntos
             * provisionales del jugador actual.
             */
            clasificacion.sort(
                    (indice1, indice2) ->
                            Integer.compare(
                                    obtenerPuntuacionMarcador(indice2),
                                    obtenerPuntuacionMarcador(indice1)
                            )
            );
        }

        return clasificacion;
    }
    // Finalización y ResultadoActivity ----------------------------------------

    private void finalizarPartida() {

        if (partidaFinalizada) {
            return;
        }

        partidaFinalizada = true;

        GestorPartidaEnCurso.eliminarPartida(
                this
        );

        bloquearBotonesPartida();

        Toast.makeText(
                this,
                "Partida finalizada",
                Toast.LENGTH_SHORT
        ).show();

        abrirResultadoActivity();
    }

    private ArrayList<Integer> construirClasificacionFinal() {

        ArrayList<Integer> clasificacion =
                new ArrayList<>();

        for (int i = 0;
             i < nombresJugadores.length;
             i++) {

            clasificacion.add(i);
        }

        if (modoRondas == ModoRondas.AROUND_THE_CLOCK) {

            clasificacion.clear();

            for (Integer indice :
                    ordenFinalizacion) {

                if (indice != null
                        && indice >= 0
                        && indice < nombresJugadores.length
                        && !clasificacion.contains(indice)) {

                    clasificacion.add(indice);
                }
            }

            ArrayList<Integer> restantes =
                    new ArrayList<>();

            for (int i = 0;
                 i < nombresJugadores.length;
                 i++) {

                if (!clasificacion.contains(i)) {
                    restantes.add(i);
                }
            }

            restantes.sort(
                    (indice1, indice2) ->
                            Integer.compare(
                                    objetivosJugadores[indice2],
                                    objetivosJugadores[indice1]
                            )
            );

            clasificacion.addAll(restantes);

        } else {

            clasificacion.sort(
                    (indice1, indice2) ->
                            Integer.compare(
                                    puntuacionesJugadores[indice2],
                                    puntuacionesJugadores[indice1]
                            )
            );
        }

        return clasificacion;
    }

    private void abrirResultadoActivity() {

        ArrayList<Integer> clasificacion =
                construirClasificacionFinal();

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

            if (modoRondas == ModoRondas.AROUND_THE_CLOCK) {

                puntuacionesOrdenadas.add(
                        objetivosJugadores[indiceJugador]
                );

            } else {

                puntuacionesOrdenadas.add(
                        puntuacionesJugadores[indiceJugador]
                );
            }

            coloresOrdenados.add(
                    coloresRecursosJugadores[indiceJugador]
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

        Intent intent = new Intent(
                PartidaRondasActivity.this,
                ResultadoActivity.class
        );

        intent.putExtra(
                ResultadoActivity.EXTRA_ORDEN_ALEATORIO,
                ordenAleatorio
        );

        intent.putExtra(
                ResultadoActivity.EXTRA_MODO_JUEGO,
                obtenerNombreModoMostrado()
        );

        intent.putExtra(
                ResultadoActivity.EXTRA_MOTIVO_FINALIZACION,
                MOTIVO_MAX_RONDAS
        );

        intent.putExtra(
                ResultadoActivity.EXTRA_NUMERO_JUGADORES,
                nombresJugadores.length
        );

        intent.putExtra(
                ResultadoActivity.EXTRA_RONDAS_JUGADAS,
                rondaActual
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

        startActivity(intent);
        finish();
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

    // Estado completo para deshacer -------------------------------------------

    private static class EstadoPartida {

        private final int[] puntuacionesJugadores;
        private final int[] objetivosJugadores;
        private final boolean[] jugadoresFinalizados;
        private final ArrayList<Integer> ordenFinalizacion;

        private final int jugadorActual;
        private final int rondaActual;
        private final int dardoActual;

        private final int puntosValidosTurno;
        private final boolean haAcertadoObjetivoTurno;

        private final String[] textosDardos;

        private EstadoPartida(
                int[] puntuacionesJugadores,
                int[] objetivosJugadores,
                boolean[] jugadoresFinalizados,
                ArrayList<Integer> ordenFinalizacion,
                int jugadorActual,
                int rondaActual,
                int dardoActual,
                int puntosValidosTurno,
                boolean haAcertadoObjetivoTurno,
                String[] textosDardos) {

            this.puntuacionesJugadores =
                    puntuacionesJugadores;

            this.objetivosJugadores =
                    objetivosJugadores;

            this.jugadoresFinalizados =
                    jugadoresFinalizados;

            this.ordenFinalizacion =
                    ordenFinalizacion;

            this.jugadorActual =
                    jugadorActual;

            this.rondaActual =
                    rondaActual;

            this.dardoActual =
                    dardoActual;

            this.puntosValidosTurno =
                    puntosValidosTurno;

            this.haAcertadoObjetivoTurno =
                    haAcertadoObjetivoTurno;

            this.textosDardos =
                    textosDardos;
        }
    }
}
