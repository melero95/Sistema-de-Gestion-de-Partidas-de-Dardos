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
import sqlite.ServicioFinalizacionPartida;
import modelos.ConfiguracionPartida;
import utilidades.SerializadorConfiguracionPartida;
import utilidades.DatosFinalizacionPartida;
import androidx.appcompat.app.AlertDialog;

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

        if (nombreGanador.isEmpty()) {
            nombreGanador =
                    nombresJugadores.get(0);
        }

        return true;
    }

    private boolean persistirResultado() {
        try {
            ServicioFinalizacionPartida.guardar(this, getIntent(), rondasJugadas,
                    motivoFinalizacion, posicionesJugadores, puntuacionesJugadores,
                    indicesOriginales);
            return true;
        } catch (Exception error) {
            android.util.Log.e("ResultadoActivity", "Error al guardar partida", error);
            new AlertDialog.Builder(this)
                    .setTitle("No se ha guardado la partida")
                    .setMessage("No se pudo registrar la partida en SQLite. "
                            + "Puedes reintentar el guardado. No se eliminará el estado temporal.")
                    .setCancelable(false)
                    .setPositiveButton("Reintentar", (dialog, which) -> {
                        if (persistirResultado()) {
                            prepararDatosResultado();
                            prepararRecyclerView();
                            configurarListeners();
                            actualizarPantalla();
                            mostrarAnimacionResultado();
                        }
                    })
                    .setNegativeButton("Volver al menú", (dialog, which) -> volverMenuPrincipal())
                    .show();
            return false;
        }
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
                    posicion == 1;

            ResultadoJugador resultadoJugador =
                    new ResultadoJugador(
                            nombre,
                            puntuacion,
                            posicion,
                            esGanador,
                            true
                    );

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
        if (jugadorGanador == null
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

        if (modoJuego.trim().isEmpty()) {

            txtTituloResultado.setText(
                    "RESULTADO"
            );

        } else {

            txtTituloResultado.setText(
                    "RESULTADO - "
                            + modoJuego.toUpperCase()
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

            txtPuntuacionGanador.setText(
                    jugadorGanador.getPuntuacion()
                            + " puntos"
            );

        } else {

            txtPuntuacionGanador.setVisibility(
                    View.GONE
            );
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

        clasificacionAdapter.notifyDataSetChanged();
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

        Toast.makeText(
                this,
                "No se han podido cargar los resultados de la partida",
                Toast.LENGTH_LONG
        ).show();

        volverMenuPrincipal();
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

                        volverMenuPrincipal();
                    }
                }
        );
    }

}
