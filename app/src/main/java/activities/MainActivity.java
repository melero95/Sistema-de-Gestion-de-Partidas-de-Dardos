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

import com.google.android.material.navigation.NavigationView;
import com.productos.juegosdedardos.R;

import modelos.PartidaEnCurso;
import preferencias.GestorPartidaEnCurso;

public class MainActivity extends AppCompatActivity {

    //Elementos de la pantalla principal -----------------------------------------

    private Button btnContinuar;
    private Button btnNuevaPartida;
    private Button btnHistorialPartidas;
    private Button btnAjustes;

    //Toolbar y menú lateral ------------------------------------------------------

    private Toolbar toolbarMain;
    private DrawerLayout drawerMain;
    private NavigationView navigationMain;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Configuro que la parte superior sea del mismo color
        getWindow().setStatusBarColor(
                ContextCompat.getColor(
                        this,
                        R.color.blue
                )
        );

        getWindow().setNavigationBarColor(
                ContextCompat.getColor(
                        this,
                        R.color.blue
                )
        );

        //No uso EdgeToEdge para evitar que el Toolbar se meta en la barra superior
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

        /*
         * Se vuelve a comprobar siempre que MainActivity recupera el foco.
         * Así el botón desaparece si la partida terminó o fue abandonada,
         * y aparece después de guardar una partida y volver al menú.
         */
        comprobarPartidaGuardada();
    }

    //Inicialización de componentes ----------------------------------------------

    private void iniciarComponentes() {

        toolbarMain =
                findViewById(R.id.toolbarMain);

        drawerMain =
                findViewById(R.id.drawerMain);

        navigationMain =
                findViewById(R.id.navigationMain);

        btnContinuar =
                findViewById(R.id.btnContinuar);

        btnNuevaPartida =
                findViewById(R.id.btnNuevaPartida);

        btnHistorialPartidas =
                findViewById(R.id.btnHistorialPartidas);

        btnAjustes =
                findViewById(R.id.btnAjustes);
    }

    //Configuración del Toolbar ---------------------------------------------------

    private void configurarToolbar() {

        setSupportActionBar(toolbarMain);

        if (getSupportActionBar() != null) {

            getSupportActionBar()
                    .setDisplayShowTitleEnabled(false);
        }
    }

    //Configuración del menú lateral ---------------------------------------------

    private void configurarMenuLateral() {

        //Mantiene los colores originales de los iconos del menú lateral
        navigationMain.setItemIconTintList(null);

        toggle = new ActionBarDrawerToggle(
                this,
                drawerMain,
                toolbarMain,
                R.string.abrir_menu,
                R.string.cerrar_menu
        );

        drawerMain.addDrawerListener(toggle);
        toggle.syncState();

        navigationMain.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.menuEstadisticas) {

                Intent intent = new Intent(
                        MainActivity.this,
                        EstadisticasActivity.class
                );

                startActivity(intent);

            } else if (id == R.id.menuRecords) {

                Intent intent = new Intent(
                        MainActivity.this,
                        RecordsActivity.class
                );
                startActivity(intent);

            } else if (id == R.id.menuReglas) {

                Intent intent = new Intent(
                        MainActivity.this,
                        ReglasActivity.class
                );
                startActivity(intent);

            } else if (id == R.id.menuAyuda) {

                Intent intent = new Intent(
                        MainActivity.this,
                        AyudaActivity.class
                );

                startActivity(intent);

            } else if (id == R.id.menuAcercaDe) {

                Intent intent = new Intent(
                        MainActivity.this,
                        AcercaDeActivity.class
                );
                startActivity(intent);
            }

            drawerMain.closeDrawers();
            return true;
        });
    }

    //Configuración de listeners --------------------------------------------------

    private void configurarListeners() {

        btnContinuar.setOnClickListener(
                view -> continuarPartidaGuardada()
        );

        btnNuevaPartida.setOnClickListener(view -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    ConfigurarNuevaPartidaActivity.class
            );

            startActivity(intent);
        });

        btnHistorialPartidas.setOnClickListener(view -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    HistorialPartidasActivity.class
            );

            startActivity(intent);
        });

        btnAjustes.setOnClickListener(view -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    AjustesActivity.class
            );

            startActivity(intent);
        });
    }

    //Continuar partida guardada --------------------------------------------------

    private void continuarPartidaGuardada() {

        PartidaEnCurso partida =
                GestorPartidaEnCurso.cargarPartida(this);

        if (partida == null) {

            Toast.makeText(
                    this,
                    "No se ha encontrado una partida válida para continuar",
                    Toast.LENGTH_SHORT
            ).show();

            comprobarPartidaGuardada();
            return;
        }

        Intent intent;

        switch (partida.getTipoPartida()) {

            case PartidaEnCurso.TIPO_PUNTOS:

                intent = new Intent(
                        MainActivity.this,
                        PartidaPuntosActivity.class
                );

                intent.putExtra(
                        PartidaPuntosActivity.EXTRA_REANUDAR_PARTIDA,
                        true
                );

                break;

            case PartidaEnCurso.TIPO_CRIQUET:

                intent = new Intent(
                        MainActivity.this,
                        PartidaCriquetActivity.class
                );

                intent.putExtra(
                        PartidaCriquetActivity.EXTRA_REANUDAR_PARTIDA,
                        true
                );

                break;

            case PartidaEnCurso.TIPO_RONDAS:

                intent = new Intent(
                        MainActivity.this,
                        PartidaRondasActivity.class
                );

                intent.putExtra(
                        PartidaRondasActivity.EXTRA_REANUDAR_PARTIDA,
                        true
                );

                break;

            default:

                GestorPartidaEnCurso.eliminarPartida(this);

                Toast.makeText(
                        this,
                        "La partida guardada no es compatible",
                        Toast.LENGTH_SHORT
                ).show();

                comprobarPartidaGuardada();
                return;
        }

        startActivity(intent);
    }

    //Comprobación de partida guardada -------------------------------------------

    private void comprobarPartidaGuardada() {

        boolean existePartida =
                GestorPartidaEnCurso.existePartida(this);

        btnContinuar.setVisibility(
                existePartida
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    //Control del botón Atrás -----------------------------------------------------

    private void configurarBotonAtras() {

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (drawerMain.isDrawerOpen(
                                GravityCompat.START
                        )) {

                            drawerMain.closeDrawer(
                                    GravityCompat.START
                            );

                        } else {

                            setEnabled(false);

                            getOnBackPressedDispatcher()
                                    .onBackPressed();
                        }
                    }
                }
        );
    }
}
