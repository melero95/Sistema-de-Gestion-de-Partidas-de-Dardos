package activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.productos.juegosdedardos.R;

import modelos.PartidaEnCurso;
import preferencias.GestorPartidaEnCurso;
import preferencias.GestorResultadoPendiente;

public class MainActivity extends AppCompatActivity {

    private Button btnContinuar;
    private Button btnNuevaPartida;
    private Button btnHistorialPartidas;
    private Button btnAjustes;

    private Toolbar toolbarMain;
    private DrawerLayout drawerMain;
    private NavigationView navigationMain;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.blue));
        setContentView(R.layout.activity_main);
        iniciarComponentes();
        configurarToolbar();
        configurarMenuLateral();
        configurarListeners();
        configurarBotonAtras();
        comprobarPartidaGuardada();
    }

    @Override
    protected void onResume() {
        super.onResume();
        comprobarPartidaGuardada();
    }

    private void iniciarComponentes() {
        toolbarMain = findViewById(R.id.toolbarMain);
        drawerMain = findViewById(R.id.drawerMain);
        navigationMain = findViewById(R.id.navigationMain);
        btnContinuar = findViewById(R.id.btnContinuar);
        btnNuevaPartida = findViewById(R.id.btnNuevaPartida);
        btnHistorialPartidas = findViewById(R.id.btnHistorialPartidas);
        btnAjustes = findViewById(R.id.btnAjustes);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbarMain);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void configurarMenuLateral() {
        navigationMain.setItemIconTintList(null);
        toggle = new ActionBarDrawerToggle(this, drawerMain, toolbarMain,
                R.string.abrir_menu, R.string.cerrar_menu);
        drawerMain.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, R.color.gold));

        navigationMain.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            if (id == R.id.menuJugadores) {
                intent = new Intent(this, JugadoresActivity.class);
            } else if (id == R.id.menuEstadisticas) {
                intent = new Intent(this, EstadisticasActivity.class);
            } else if (id == R.id.menuHistorial) {
                intent = new Intent(this, HistorialPartidasActivity.class);
            } else if (id == R.id.menuReglas) {
                intent = new Intent(this, ReglasActivity.class);
            } else if (id == R.id.menuAyuda) {
                intent = new Intent(this, AyudaActivity.class);
            } else if (id == R.id.menuAcercaDe) {
                intent = new Intent(this, AcercaDeActivity.class);
            }
            if (intent != null) startActivity(intent);
            drawerMain.closeDrawers();
            return true;
        });
    }

    private void configurarListeners() {
        btnContinuar.setOnClickListener(view -> continuarPartidaGuardada());
        btnNuevaPartida.setOnClickListener(view ->
                startActivity(new Intent(this, ConfigurarNuevaPartidaActivity.class)));
        btnHistorialPartidas.setOnClickListener(view ->
                startActivity(new Intent(this, HistorialPartidasActivity.class)));
        btnAjustes.setOnClickListener(view ->
                startActivity(new Intent(this, AjustesActivity.class)));
    }

    /** Primero se recupera un resultado FINAL; nunca volver a lanzar sus dardos. */
    private void continuarPartidaGuardada() {
        if (GestorResultadoPendiente.existe(this)) {
            Intent resultado = GestorResultadoPendiente.crearIntentRecuperacion(this);
            if (resultado == null) {
                mostrarGuardadoIncompatible(true);
            } else {
                startActivity(resultado);
            }
            return;
        }

        PartidaEnCurso partida = GestorPartidaEnCurso.cargarPartida(this);
        if (partida == null) {
            if (GestorPartidaEnCurso.existePartida(this)) {
                mostrarGuardadoIncompatible(false);
            } else {
                Toast.makeText(this, "No existe ninguna partida para continuar",
                        Toast.LENGTH_SHORT).show();
                comprobarPartidaGuardada();
            }
            return;
        }

        Intent intent;
        switch (partida.getTipoPartida()) {
            case PartidaEnCurso.TIPO_PUNTOS:
                intent = new Intent(this, PartidaPuntosActivity.class);
                intent.putExtra(PartidaPuntosActivity.EXTRA_REANUDAR_PARTIDA, true);
                break;
            case PartidaEnCurso.TIPO_CRIQUET:
                intent = new Intent(this, PartidaCriquetActivity.class);
                intent.putExtra(PartidaCriquetActivity.EXTRA_REANUDAR_PARTIDA, true);
                break;
            case PartidaEnCurso.TIPO_RONDAS:
                intent = new Intent(this, PartidaRondasActivity.class);
                intent.putExtra(PartidaRondasActivity.EXTRA_REANUDAR_PARTIDA, true);
                break;
            default:
                // Nunca eliminar un estado que desconocemos automáticamente.
                mostrarGuardadoIncompatible(false);
                return;
        }
        startActivity(intent);
    }

    private void mostrarGuardadoIncompatible(boolean esResultadoFinal) {
        String titulo = esResultadoFinal
                ? "Resultado pendiente incompatible" : "Partida guardada incompatible";
        String mensaje = "La versión actual no ha podido interpretar "
                + (esResultadoFinal ? "el resultado final" : "la partida temporal")
                + ". Puedes reintentar o descartarlo expresamente. "
                + "El historial y las estadísticas ya guardadas en SQLite NO se borrarán.";

        new MaterialAlertDialogBuilder(this, R.style.TemaDialogoDardos)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setCancelable(true)
                .setPositiveButton("Reintentar", (dialog, cual) -> continuarPartidaGuardada())
                .setNeutralButton("Descartar", (dialog, cual) ->
                        confirmarDescarte(esResultadoFinal))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarDescarte(boolean esResultadoFinal) {
        new MaterialAlertDialogBuilder(this, R.style.TemaDialogoDardos)
                .setTitle("¿Descartar datos temporales?")
                .setMessage("Esta acción eliminará únicamente "
                        + (esResultadoFinal ? "el resultado pendiente" : "la partida en curso")
                        + ". No se podrá recuperar su progreso, pero NO se borrarán "
                        + "los jugadores, el historial ni las estadísticas guardadas.")
                .setPositiveButton("Descartar definitivamente", (dialog, cual) -> {
                    boolean eliminado = esResultadoFinal
                            ? GestorResultadoPendiente.eliminar(this)
                            : GestorPartidaEnCurso.eliminarPartida(this);
                    if (!eliminado) {
                        Toast.makeText(this, "No se pudieron descartar los datos",
                                Toast.LENGTH_LONG).show();
                    }
                    comprobarPartidaGuardada();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void comprobarPartidaGuardada() {
        boolean existe = GestorPartidaEnCurso.existePartida(this)
                || GestorResultadoPendiente.existe(this);
        btnContinuar.setVisibility(existe ? View.VISIBLE : View.GONE);
    }

    private void configurarBotonAtras() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerMain.isDrawerOpen(GravityCompat.START)) {
                    drawerMain.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}
