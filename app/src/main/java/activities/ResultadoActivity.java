package activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.productos.juegosdedardos.R;

import java.util.ArrayList;
import java.util.Collections;

import adapters.ClasificacionResultadoAdapter;
import modelos.ResultadoJugador;
import modelos.EstadisticasJugadorEnCurso;
import sqlite.ServicioFinalizacionPartida;
import modelos.ConfiguracionPartida;
import utilidades.SerializadorConfiguracionPartida;
import utilidades.DatosFinalizacionPartida;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import preferencias.GestorResultadoPendiente;

import androidx.activity.OnBackPressedCallback;

public class ResultadoActivity extends AppCompatActivity {

    //Claves del Intent de ResultadoActivity ---------------------------------------

    public static final String EXTRA_MODO_JUEGO =
            "resultado_modo_juego";

    public static final String EXTRA_MOTIVO_FINALIZACION =
            "resultado_motivo_finalizacion";

    public static final String EXTRA_NUMERO_JUGADORES =
            "resultado_numero_jugadores";

    public static final String EXTRA_RONDAS_JUGADAS =
            "resultado_rondas_jugadas";

    public static final String EXTRA_MAX_RONDAS =
            "resultado_max_rondas";

    public static final String EXTRA_NOMBRE_GANADOR =
            "resultado_nombre_ganador";

    public static final String EXTRA_NOMBRES_JUGADORES =
            "resultado_nombres_jugadores";

    public static final String EXTRA_PUNTUACIONES =
            "resultado_puntuaciones";

    public static final String EXTRA_COLORES =
            "resultado_colores";

    public static final String EXTRA_POSICIONES =
            "resultado_posiciones";

    public static final String EXTRA_INDICES_ORIGINALES =
            "resultado_indices_originales";
    public static final String EXTRA_NUMERO_DARDOS = "resultado_numero_dardos";
    public static final String EXTRA_CIERRE_DOBLE = "resultado_cierre_doble";
    public static final String EXTRA_ORDEN_ALEATORIO = "resultado_orden_aleatorio";

    //Claves de entrada para iniciar una nueva partida ------------------------------

    private static final String EXTRA_ENTRADA_MODO_JUEGO =
            "modoJuego";

    private static final String EXTRA_ENTRADA_MAX_RONDAS =
            "maxRondas";

    private static final String EXTRA_ENTRADA_NUMERO_JUGADORES =
            "numeroJugadores";

    private static final String EXTRA_ENTRADA_NOMBRES_JUGADORES =
            "nombresJugadores";

    private static final String EXTRA_ENTRADA_COLOR_JUGADOR =
            "colorJugador";

    //Datos generales recibidos ----------------------------------------------------

    private String modoJuego;
    private String motivoFinalizacion;
    private String nombreGanador;
    private boolean resultadoEmpatado;

    private int numeroJugadores;
    private int rondasJugadas;
    private int maxRondas;
    private int numeroDardos = 3;
    private boolean cierreDoble;
    private boolean ordenAleatorio;

    //Listas recibidas -------------------------------------------------------------

    private ArrayList<String> nombresJugadores;
    private ArrayList<Integer> puntuacionesJugadores;
    private ArrayList<Integer> coloresJugadores;
    private ArrayList<Integer> posicionesJugadores;
    private ArrayList<Integer> indicesOriginales;

    //Datos preparados para la pantalla --------------------------------------------

    private ArrayList<ResultadoJugador> jugadoresResultado;
    private ArrayList<ResultadoJugador> jugadoresClasificacion;

    private ResultadoJugador jugadorGanador;

    //Elementos principales --------------------------------------------------------

    private ImageView imgTrofeo;

    private TextView txtTituloResultado;
    private TextView txtNombreGanador;
    private TextView txtPuntuacionGanador;
    private TextView txtDetalleGanador;
    private TextView txtTituloClasificacion;

    private LinearLayout contenedorGanador;
    private LinearLayout filaGanador;
    private LinearLayout contenedorClasificacion;
    private LinearLayout contenedorBotonesResultado;

    private RecyclerView recyclerClasificacion;

    private Button btnEstadisticas;
    private Button btnRevancha;
    private Button btnMenuPrincipal;

    //Adaptador --------------------------------------------------------------------

    private ClasificacionResultadoAdapter clasificacionAdapter;

    // SQLite ha confirmado que la partida se ha guardado.
    private boolean resultadoPersistido = false;

    //Crear Activity ---------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_resultado);

        iniciarElementos();
        configurarBotonAtras();
        recibirDatosIntent();

        if (!validarDatosRecibidos()) {
            mostrarErrorDatos();
            return;
        }

        // Guardar en SQLite antes de habilitar la navegación a otra partida.
        // Si el guardado falla, se ofrecen reintentos sin eliminar SharedPreferences.
        if (!persistirResultado()) return;

        prepararDatosResultado();
        prepararRecyclerView();
        configurarListeners();
        actualizarPantalla();

        mostrarAnimacionResultado();
    }

    //Iniciar elementos ------------------------------------------------------------

    private void iniciarElementos() {

        imgTrofeo = findViewById(
                R.id.imgTrofeo
        );

        txtTituloResultado = findViewById(
                R.id.txtTituloResultado
        );

        txtNombreGanador = findViewById(
                R.id.txtNombreGanador
        );

        txtPuntuacionGanador = findViewById(
                R.id.txtPuntuacionGanador
        );

        txtDetalleGanador = findViewById(R.id.txtDetalleGanador);

        txtTituloClasificacion = findViewById(
                R.id.txtTituloClasificacion
        );

        contenedorGanador = findViewById(
                R.id.contenedorGanador
        );

        filaGanador = findViewById(
                R.id.filaGanador
        );

        contenedorClasificacion = findViewById(
                R.id.contenedorClasificacion
        );

        contenedorBotonesResultado = findViewById(
                R.id.contenedorBotonesResultado
        );

        recyclerClasificacion = findViewById(
                R.id.recyclerClasificacion
        );

        btnEstadisticas = findViewById(
                R.id.btnEstadisticas
        );

        btnRevancha = findViewById(
                R.id.btnRevancha
        );

        btnMenuPrincipal = findViewById(
                R.id.btnMenuPrincipal
        );
    }

    //Recibir datos del Intent ------------------------------------------------------

    private void recibirDatosIntent() {

        Intent intent = getIntent();

        if (intent == null) {
            inicializarDatosVacios();
            return;
        }

        modoJuego = intent.getStringExtra(
                EXTRA_MODO_JUEGO
        );

        motivoFinalizacion = intent.getStringExtra(
                EXTRA_MOTIVO_FINALIZACION
        );

        numeroJugadores = intent.getIntExtra(
                EXTRA_NUMERO_JUGADORES,
                0
        );

        rondasJugadas = intent.getIntExtra(
                EXTRA_RONDAS_JUGADAS,
                0
        );

        maxRondas = intent.getIntExtra(
                EXTRA_MAX_RONDAS,
                0
        );
        numeroDardos = Math.max(1, Math.min(4, intent.getIntExtra(EXTRA_NUMERO_DARDOS, 3)));
        cierreDoble = intent.getBooleanExtra(EXTRA_CIERRE_DOBLE, false);
        ordenAleatorio = intent.getBooleanExtra(EXTRA_ORDEN_ALEATORIO, false);

        nombreGanador = intent.getStringExtra(
                EXTRA_NOMBRE_GANADOR
        );

        nombresJugadores =
                intent.getStringArrayListExtra(
                        EXTRA_NOMBRES_JUGADORES
                );

        puntuacionesJugadores =
                intent.getIntegerArrayListExtra(
                        EXTRA_PUNTUACIONES
                );

        coloresJugadores =
                intent.getIntegerArrayListExtra(
                        EXTRA_COLORES
                );

        posicionesJugadores =
                intent.getIntegerArrayListExtra(
                        EXTRA_POSICIONES
                );

        indicesOriginales =
                intent.getIntegerArrayListExtra(
                        EXTRA_INDICES_ORIGINALES
                );

        corregirDatosNulos();
        resultadoEmpatado = "EMPATE".equalsIgnoreCase(motivoFinalizacion);
    }

    //Inicializar datos vacíos ------------------------------------------------------

    private void inicializarDatosVacios() {

        modoJuego = "";
        motivoFinalizacion = "";
        nombreGanador = "";

        numeroJugadores = 0;
        rondasJugadas = 0;
        maxRondas = 0;

        nombresJugadores = new ArrayList<>();
        puntuacionesJugadores = new ArrayList<>();
        coloresJugadores = new ArrayList<>();
        posicionesJugadores = new ArrayList<>();
        indicesOriginales = new ArrayList<>();

        jugadoresResultado = new ArrayList<>();
        jugadoresClasificacion = new ArrayList<>();
    }

    //Corregir valores nulos --------------------------------------------------------

    private void corregirDatosNulos() {

        if (modoJuego == null) {
            modoJuego = "";
        }

        if (motivoFinalizacion == null) {
            motivoFinalizacion = "";
        }

        if (nombreGanador == null) {
            nombreGanador = "";
        }

        if (nombresJugadores == null) {
            nombresJugadores = new ArrayList<>();
        }

        if (puntuacionesJugadores == null) {
            puntuacionesJugadores = new ArrayList<>();
        }

        if (coloresJugadores == null) {
            coloresJugadores = new ArrayList<>();
        }

        if (posicionesJugadores == null) {
            posicionesJugadores = new ArrayList<>();
        }

        if (indicesOriginales == null) {
            indicesOriginales = new ArrayList<>();
        }
    }

    //Validar datos recibidos -------------------------------------------------------

    private boolean validarDatosRecibidos() {

        if (nombresJugadores.isEmpty()) {
            return false;
        }

        int cantidadJugadores =
                nombresJugadores.size();

        if (puntuacionesJugadores.size()
                != cantidadJugadores) {

            return false;
        }

        if (coloresJugadores.size()
                != cantidadJugadores) {

            return false;
        }

        if (posicionesJugadores.size()
                != cantidadJugadores) {

            return false;
        }

        if (indicesOriginales.size()
                != cantidadJugadores) {

            return false;
        }

        numeroJugadores = cantidadJugadores;

        if (nombreGanador.isEmpty() && !resultadoEmpatado) {
            nombreGanador =
                    nombresJugadores.get(0);
        }

        return true;
    }

    /**
     * Copia el resultado final en SharedPreferences ANTES de escribir en SQLite.
     * Se utiliza en 301/501, Cricket, Cut Throat y los tres modos de rondas.
     * El UUID de la foto final permite repetir la inserción sin duplicados.
     */
    private boolean persistirResultado() {
        boolean copiaSegura = GestorResultadoPendiente.guardar(this, getIntent());
        if (!copiaSegura) {
            mostrarDialogoErrorGuardado(false,
                    new IllegalStateException("No se pudo proteger el resultado final "
                            + "antes de escribir en SQLite"));
            return false;
        }

        try {
            ServicioFinalizacionPartida.guardar(this, getIntent(), rondasJugadas,
                    motivoFinalizacion, posicionesJugadores, puntuacionesJugadores,
                    indicesOriginales);
            resultadoPersistido = true;
            if (!GestorResultadoPendiente.eliminarSiCoincide(this, getIntent())) {
                android.util.Log.w("ResultadoActivity", "El guardado final se completó, "
                        + "pero no se pudo quitar su copia temporal");
            }
            return true;
        } catch (Exception error) {
            android.util.Log.e("ResultadoActivity", "Error al guardar partida", error);
            mostrarDialogoErrorGuardado(true, error);
            return false;
        }
    }

    private void mostrarDialogoErrorGuardado(boolean respaldoDisponible, Exception error) {
        String detalle = error.getClass().getSimpleName();
        if (error.getMessage() != null && !error.getMessage().trim().isEmpty()) {
            String mensajeTecnico = error.getMessage();
            detalle += " — " + mensajeTecnico.substring(0,
                    Math.min(160, mensajeTecnico.length()));
        }
        String mensaje = respaldoDisponible
                ? "La partida ha finalizado, pero no se ha podido guardar en SQLite. "
                  + "El resultado está protegido y puedes reintentarlo desde «Continuar»."
                : "No se ha podido crear una copia segura del resultado. "
                  + "No cierres esta pantalla: reintenta el guardado para no perderlo.";

        MaterialAlertDialogBuilder dialogo = new MaterialAlertDialogBuilder(
                this, R.style.TemaDialogoDardos)
                .setTitle("Resultado pendiente de guardar")
                .setMessage(mensaje + "\n\nDetalle: " + detalle)
                .setCancelable(false)
                .setPositiveButton("Reintentar", (ventana, opcion) -> {
                    if (persistirResultado()) {
                        prepararDatosResultado();
                        prepararRecyclerView();
                        configurarListeners();
                        actualizarPantalla();
                        mostrarAnimacionResultado();
                    }
                });
        if (respaldoDisponible) {
            dialogo.setNegativeButton("Volver al menú", (ventana, opcion) ->
                    volverMenuPrincipal());
        }
        dialogo.show();
    }

    //Preparar objetos ResultadoJugador --------------------------------------------

    private void prepararDatosResultado() {

        jugadoresResultado = new ArrayList<>();
        jugadoresClasificacion = new ArrayList<>();

        jugadorGanador = null;

        for (int i = 0;
             i < nombresJugadores.size();
             i++) {

            String nombre =
                    nombresJugadores.get(i);

            int puntuacion =
                    puntuacionesJugadores.get(i);

            int posicion =
                    posicionesJugadores.get(i);

            boolean esGanador =
                    posicion == 1 && !resultadoEmpatado;

            ResultadoJugador resultadoJugador =
                    new ResultadoJugador(
                            nombre,
                            puntuacion,
                            posicion,
                            esGanador,
                            true
                    );

            configurarTextosResultado(resultadoJugador, i);

            jugadoresResultado.add(
                    resultadoJugador
            );

            if (esGanador) {

                jugadorGanador =
                        resultadoJugador;

            } else {

                jugadoresClasificacion.add(
                        resultadoJugador
                );
            }
        }

        //Seguridad por si no llegó la posición 1
        if (!resultadoEmpatado && jugadorGanador == null
                && !jugadoresResultado.isEmpty()) {

            jugadorGanador =
                    jugadoresResultado.get(0);

            jugadoresClasificacion.clear();

            for (int i = 1;
                 i < jugadoresResultado.size();
                 i++) {

                jugadoresClasificacion.add(
                        jugadoresResultado.get(i)
                );
            }
        }
    }

    /**
     * La lista de estadísticas del Intent está en orden ORIGINAL de jugadores.
     * Las puntuaciones y posiciones ya están en orden de CLASIFICACIÓN.
     */
    private void configurarTextosResultado(ResultadoJugador jugador, int indiceClasificado) {
        String modo = modoJuego.trim().toLowerCase(java.util.Locale.ROOT);
        int puntos = jugador.getPuntuacion();
        String principal = getResources().getQuantityString(
                R.plurals.lint06_puntos_ganador, puntos, puntos);
        String secundario = null;

        DatosFinalizacionPartida datos = DatosFinalizacionPartida.desdeIntent(getIntent());
        EstadisticasJugadorEnCurso estadistica = null;
        int indiceOriginal = obtenerIndiceOriginalSeguro(indiceClasificado);
        if (datos != null && datos.estadisticas != null
                && indiceOriginal >= 0 && indiceOriginal < datos.estadisticas.size()) {
            estadistica = datos.estadisticas.get(indiceOriginal);
        }

        if (modo.equals("301") || modo.equals("501")) {
            // Un jugador cerrado en cero deja de participar; sus turnos no aumentan
            // aunque los demás sigan lanzando.
            if (estadistica != null) {
                secundario = textoRondas(estadistica.getTurnosJugados());
            }
        } else if (modo.equals("around the clock") || modo.equals("round the clock")) {
            // 1..21 representa el SIGUIENTE objetivo pendiente; 22 = completado.
            if (puntos >= 22 || (estadistica != null && estadistica.isPartidaCompletada())) {
                principal = "Completado";
            } else if (puntos == 21) {
                principal = "Pendiente: Diana";
            } else if (puntos >= 1 && puntos <= 20) {
                principal = "Pendiente: " + puntos;
            } else {
                principal = "Progreso no disponible";
            }
            if (estadistica != null) {
                secundario = textoRondas(estadistica.getTurnosJugados()) + " · "
                        + textoDardos(estadistica.getDardosLanzados());
            }
        } else if (modo.equals("shanghai")) {
            secundario = textoRondas(rondasJugadas);
        }
        // Cricket, Cut Throat y Double Down conservan la puntuación actual.
        jugador.setTextosResultado(principal, secundario);
    }

    private String textoRondas(int cantidad) {
        return cantidad + (cantidad == 1 ? " ronda" : " rondas");
    }

    private String textoDardos(int cantidad) {
        return cantidad + (cantidad == 1 ? " dardo" : " dardos");
    }

    //Preparar RecyclerView ---------------------------------------------------------

    private void prepararRecyclerView() {

        recyclerClasificacion.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerClasificacion.setNestedScrollingEnabled(
                false
        );

        clasificacionAdapter =
                new ClasificacionResultadoAdapter(
                        jugadoresClasificacion
                );

        recyclerClasificacion.setAdapter(
                clasificacionAdapter
        );
    }

    //Actualizar pantalla completa -------------------------------------------------

    private void actualizarPantalla() {

        actualizarTitulo();
        actualizarGanador();
        actualizarClasificacion();
        actualizarBotones();
    }

    //Actualizar título -------------------------------------------------------------

    private void actualizarTitulo() {

        if (resultadoEmpatado) {
            txtTituloResultado.setText(getString(R.string.resultado_empate_modo,
                    modoJuego.toUpperCase(java.util.Locale.ROOT)));
            return;
        }

        if (modoJuego.trim().isEmpty()) {

            txtTituloResultado.setText(
                    R.string.resultado_titulo_simple
            );

        } else {

            txtTituloResultado.setText(
                    getString(R.string.resultado_titulo_modo,
                            modoJuego.toUpperCase(java.util.Locale.ROOT))
            );
        }
    }

    //Actualizar ganador ------------------------------------------------------------

    private void actualizarGanador() {

        if (jugadorGanador == null) {

            contenedorGanador.setVisibility(
                    View.GONE
            );

            return;
        }

        contenedorGanador.setVisibility(
                View.VISIBLE
        );

        txtNombreGanador.setText(
                jugadorGanador.getNombre()
        );

        if (jugadorGanador.isMostrarPuntuacion()) {

            txtPuntuacionGanador.setVisibility(
                    View.VISIBLE
            );

            txtPuntuacionGanador.setText(jugadorGanador.getTextoResultadoPrincipal());

        } else {

            txtPuntuacionGanador.setVisibility(View.GONE);
        }

        String detalle = jugadorGanador.getTextoResultadoSecundario();
        if (detalle == null || detalle.isEmpty()) {
            txtDetalleGanador.setVisibility(View.GONE);
        } else {
            txtDetalleGanador.setText(detalle);
            txtDetalleGanador.setVisibility(View.VISIBLE);
        }
    }

    //Actualizar clasificación -----------------------------------------------------

    private void actualizarClasificacion() {

        if (jugadoresClasificacion.isEmpty()) {

            contenedorClasificacion.setVisibility(
                    View.GONE
            );

            return;
        }

        contenedorClasificacion.setVisibility(
                View.VISIBLE
        );

        clasificacionAdapter.actualizarLista(jugadoresClasificacion);
    }

    //Actualizar botones ------------------------------------------------------------

    private void actualizarBotones() {

        boolean hayResultados =
                !jugadoresResultado.isEmpty();

        btnEstadisticas.setEnabled(
                hayResultados
        );

        btnRevancha.setEnabled(
                hayResultados
        );
    }

    //Mostrar animación del resultado ----------------------------------------------

    private void mostrarAnimacionResultado() {

        imgTrofeo.setScaleX(0f);
        imgTrofeo.setScaleY(0f);
        imgTrofeo.setAlpha(0f);

        filaGanador.setAlpha(0f);
        filaGanador.setTranslationY(30f);

        contenedorClasificacion.setAlpha(0f);
        contenedorClasificacion.setTranslationY(30f);

        contenedorBotonesResultado.setAlpha(0f);
        contenedorBotonesResultado.setTranslationY(30f);

        imgTrofeo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(1200)
                .setInterpolator(
                        new DecelerateInterpolator()
                )
                .withEndAction(() -> filaGanador.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(800)
                        .setInterpolator(
                                new DecelerateInterpolator()
                        )
                        .withEndAction(
                                this::mostrarClasificacionAnimada
                        )
                        .start())
                .start();
    }

    //Mostrar clasificación y botones de forma animada -----------------------------

    private void mostrarClasificacionAnimada() {

        if (contenedorClasificacion.getVisibility()
                == View.VISIBLE) {

            contenedorClasificacion.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(700)
                    .setInterpolator(
                            new DecelerateInterpolator()
                    )
                    .withEndAction(
                            this::mostrarBotonesAnimados
                    )
                    .start();

        } else {

            mostrarBotonesAnimados();
        }
    }

    //Mostrar botones de forma animada ---------------------------------------------

    private void mostrarBotonesAnimados() {

        contenedorBotonesResultado.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setInterpolator(
                        new DecelerateInterpolator()
                )
                .start();
    }

    //Configurar botones ------------------------------------------------------------

    private void configurarListeners() {

        btnEstadisticas.setOnClickListener(
                view -> abrirEstadisticas()
        );

        btnRevancha.setOnClickListener(
                view -> realizarRevancha()
        );

        btnMenuPrincipal.setOnClickListener(
                view -> volverMenuPrincipal()
        );
    }

    //Abrir estadísticas ------------------------------------------------------------

    private void abrirEstadisticas() {
        // Fase 08: consultar estadísticas históricas tras el guardado confirmado.
        startActivity(new Intent(this, EstadisticasActivity.class));
    }

    //Realizar revancha -------------------------------------------------------------

    private void realizarRevancha() {

        if (nombresJugadores == null ||
                nombresJugadores.isEmpty()) {

            Toast.makeText(
                    this,
                    "No se puede iniciar la revancha",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intentRevancha =
                crearIntentRevancha();

        if (intentRevancha == null) {

            Toast.makeText(
                    this,
                    "No se reconoce el modo de juego",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        ArrayList<String> nombresRevancha =
                reconstruirNombresOrdenOriginal();

        ArrayList<Integer> coloresRevancha =
                reconstruirColoresOrdenOriginal();

        if (ordenAleatorio) {
            ArrayList<Integer> orden = new ArrayList<>();
            for (int i = 0; i < nombresRevancha.size(); i++) orden.add(i);
            Collections.shuffle(orden);
            ArrayList<String> nombresMezclados = new ArrayList<>();
            ArrayList<Integer> coloresMezclados = new ArrayList<>();
            for (Integer indice : orden) {
                nombresMezclados.add(nombresRevancha.get(indice));
                coloresMezclados.add(coloresRevancha.get(indice));
            }
            nombresRevancha = nombresMezclados;
            coloresRevancha = coloresMezclados;
        }

        intentRevancha.putExtra(
                EXTRA_ENTRADA_MODO_JUEGO,
                modoJuego
        );

        intentRevancha.putExtra(
                EXTRA_ENTRADA_MAX_RONDAS,
                maxRondas
        );

        intentRevancha.putExtra(
                EXTRA_ENTRADA_NUMERO_JUGADORES,
                nombresRevancha.size()
        );

        intentRevancha.putExtra(ConfigurarNuevaPartidaActivity.EXTRA_NUMERO_DARDOS, numeroDardos);
        intentRevancha.putExtra(ConfigurarNuevaPartidaActivity.EXTRA_CIERRE_DOBLE, cierreDoble);
        intentRevancha.putExtra(ConfigurarNuevaPartidaActivity.EXTRA_ORDEN_ALEATORIO, ordenAleatorio);

        intentRevancha.putStringArrayListExtra(
                EXTRA_ENTRADA_NOMBRES_JUGADORES,
                nombresRevancha
        );

        intentRevancha.putIntegerArrayListExtra(
                EXTRA_ENTRADA_COLOR_JUGADOR,
                coloresRevancha
        );

        /*
         * También enviamos nombres y colores mediante extras individuales.
         * De esta forma, las Activities antiguas y las nuevas pueden
         * reconstruir la revancha sin depender de un único formato.
         */
        for (int i = 0;
             i < nombresRevancha.size();
             i++) {

            intentRevancha.putExtra(
                    PartidaCriquetActivity.EXTRA_JUGADOR + (i + 1),
                    nombresRevancha.get(i)
            );

            intentRevancha.putExtra(
                    PartidaCriquetActivity.EXTRA_COLOR_JUGADOR + (i + 1),
                    coloresRevancha.get(i)
            );
        }

        // Revancha con IDs reales y nueva fotografía versionada (fase 06).
        // Se identifica cada jugador por su nombre normalizado dentro de la
        // configuración histórica (los nombres son únicos en JUGADORES).
        try {
            DatosFinalizacionPartida datos = DatosFinalizacionPartida.desdeIntent(getIntent());
            ConfiguracionPartida anterior = datos.validarConfiguracion();
            ArrayList<ConfiguracionPartida.Participante> nuevos = new ArrayList<>();
            for (int i = 0; i < nombresRevancha.size(); i++) {
                String nombre = nombresRevancha.get(i);
                ConfiguracionPartida.Participante encontrado = null;
                for (ConfiguracionPartida.Participante participante : anterior.getParticipantes()) {
                    if (participante.getNombre().equalsIgnoreCase(nombre)) {
                        encontrado = participante;
                        break;
                    }
                }
                if (encontrado == null) throw new IllegalArgumentException("Jugador de revancha no encontrado");
                nuevos.add(new ConfiguracionPartida.Participante(
                        encontrado.getIdJugador(), nombre, encontrado.getColorArgb(), i + 1));
            }
            ConfiguracionPartida nueva = new ConfiguracionPartida(
                    anterior.getModo(), anterior.getRondas(), anterior.getDardosPorTurno(),
                    anterior.isCierreDoble(), anterior.isMantenerMultiplicador(),
                    anterior.isOrdenAleatorio(), nuevos);
            intentRevancha.putExtra(EXTRA_ENTRADA_MODO_JUEGO, anterior.getModo());
            intentRevancha.putExtra(ConfigurarNuevaPartidaActivity.EXTRA_CONFIGURACION_JSON,
                    SerializadorConfiguracionPartida.aJson(nueva));
            intentRevancha.putExtra(ConfigurarNuevaPartidaActivity.EXTRA_MANTENER_MULTIPLICADOR,
                    anterior.isMantenerMultiplicador());
        } catch (Exception error) {
            Toast.makeText(this, "No se puede reconstruir la revancha: " + error.getMessage(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        startActivity(intentRevancha);
        finish();
    }

    //Crear Intent de revancha según el modo ---------------------------------------

    private Intent crearIntentRevancha() {

        String modo =
                modoJuego.trim();

        if (modo.equalsIgnoreCase("301") ||
                modo.equalsIgnoreCase("501")) {

            return new Intent(
                    ResultadoActivity.this,
                    PartidaPuntosActivity.class
            );
        }

        if (modo.equalsIgnoreCase("Cricket") ||
                modo.equalsIgnoreCase("Cut Throat Cricket")) {

            return new Intent(
                    ResultadoActivity.this,
                    PartidaCriquetActivity.class
            );
        }

        if (modo.equalsIgnoreCase("Double Down") ||
                modo.equalsIgnoreCase("Around the Clock") ||
                modo.equalsIgnoreCase("Round the Clock") ||
                modo.equalsIgnoreCase("Shanghai")) {

            return new Intent(
                    ResultadoActivity.this,
                    PartidaRondasActivity.class
            );
        }

        return null;
    }

    //Reconstruir nombres en el orden original -------------------------------------

    private ArrayList<String> reconstruirNombresOrdenOriginal() {

        ArrayList<String> nombresOrdenOriginal =
                crearListaNombresPorDefecto();

        for (int i = 0;
             i < nombresJugadores.size();
             i++) {

            int indiceOriginal =
                    obtenerIndiceOriginalSeguro(i);

            if (indiceOriginal >= 0 &&
                    indiceOriginal < nombresOrdenOriginal.size()) {

                nombresOrdenOriginal.set(
                        indiceOriginal,
                        nombresJugadores.get(i)
                );
            }
        }

        return nombresOrdenOriginal;
    }

    //Reconstruir colores en el orden original -------------------------------------

    private ArrayList<Integer> reconstruirColoresOrdenOriginal() {

        ArrayList<Integer> coloresOrdenOriginal =
                crearListaColoresPorDefecto();

        for (int i = 0;
             i < coloresJugadores.size();
             i++) {

            int indiceOriginal =
                    obtenerIndiceOriginalSeguro(i);

            if (indiceOriginal >= 0 &&
                    indiceOriginal < coloresOrdenOriginal.size()) {

                coloresOrdenOriginal.set(
                        indiceOriginal,
                        coloresJugadores.get(i)
                );
            }
        }

        return coloresOrdenOriginal;
    }

    //Crear lista de nombres por defecto -------------------------------------------

    private ArrayList<String> crearListaNombresPorDefecto() {

        ArrayList<String> nombresOrdenOriginal =
                new ArrayList<>();

        for (int i = 0;
             i < nombresJugadores.size();
             i++) {

            nombresOrdenOriginal.add(
                    nombresJugadores.get(i)
            );
        }

        return nombresOrdenOriginal;
    }

    //Crear lista de colores por defecto -------------------------------------------

    private ArrayList<Integer> crearListaColoresPorDefecto() {

        ArrayList<Integer> coloresOrdenOriginal =
                new ArrayList<>();

        for (int i = 0;
             i < nombresJugadores.size();
             i++) {

            if (i < coloresJugadores.size()) {

                coloresOrdenOriginal.add(
                        coloresJugadores.get(i)
                );

            } else {

                coloresOrdenOriginal.add(
                        getColor(android.R.color.white)
                );
            }
        }

        return coloresOrdenOriginal;
    }

    //Obtener índice original seguro -----------------------------------------------

    private int obtenerIndiceOriginalSeguro(
            int indiceClasificacion
    ) {

        if (indicesOriginales == null ||
                indiceClasificacion < 0 ||
                indiceClasificacion
                        >= indicesOriginales.size()) {

            return indiceClasificacion;
        }

        return indicesOriginales.get(
                indiceClasificacion
        );
    }

    //Mostrar error al recibir datos ------------------------------------------------

    private void mostrarErrorDatos() {
        // Mismo diálogo Material que el resto del proyecto. No eliminar aquí
        // un resultado previo: puede ser recuperable tras una actualización.
        new MaterialAlertDialogBuilder(this, R.style.TemaDialogoDardos)
                .setTitle("No se pudieron recuperar los resultados")
                .setMessage("Los datos finales están incompletos o no son compatibles "
                        + "con esta versión. Si hay una copia pendiente, puedes "
                        + "reintentar desde «Continuar» o descartarla expresamente "
                        + "en el menú. El historial de SQLite no se modificará.")
                .setCancelable(false)
                .setPositiveButton("Volver al menú", (dialogo, opcion) ->
                        volverMenuPrincipal())
                .show();
    }

    //Volver al menú principal ------------------------------------------------------

    private void volverMenuPrincipal() {

        Intent intent = new Intent(
                ResultadoActivity.this,
                MainActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    //Configurar botón atrás ----------------------------------------------------------

    private void configurarBotonAtras() {

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {
                        // Si no existe una copia segura, no abandonar el resultado.
                        if (!resultadoPersistido &&
                                !GestorResultadoPendiente.existe(ResultadoActivity.this)) {
                            new MaterialAlertDialogBuilder(ResultadoActivity.this,
                                    R.style.TemaDialogoDardos)
                                    .setTitle("Guardado sin confirmar")
                                    .setMessage("El resultado aún no se ha guardado. "
                                            + "Utiliza «Reintentar» para no perderlo.")
                                    .setPositiveButton("Entendido", null)
                                    .show();
                            return;
                        }
                        volverMenuPrincipal();
                    }
                }
        );
    }

}
