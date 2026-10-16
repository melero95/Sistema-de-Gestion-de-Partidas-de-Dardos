package activities;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.productos.juegosdedardos.R;

import java.util.List;

import adapters.JugadorAdapter;
import modelos.Jugador;
import sqlite.JugadorDao;

/**
 * Gestión de jugadores almacenados en SQLite.
 * Nunca elimina registros: los jugadores se desactivan para nuevas partidas.
 */
public class JugadoresActivity extends AppCompatActivity {

    private static final int[] COLORES_RECURSO = {
            R.color.jugador_rojo, R.color.jugador_verde,
            R.color.jugador_azul, R.color.jugador_amarillo,
            R.color.jugador_naranja, R.color.jugador_morado,
            R.color.jugador_cyan, R.color.jugador_blanco
    };

    private static final String[] NOMBRES_COLORES = {
            "Rojo", "Verde", "Azul", "Amarillo",
            "Naranja", "Morado", "Cian", "Blanco"
    };

    private JugadorDao jugadorDao;
    private JugadorAdapter adaptador;
    private TextView txtResumenJugadores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jugadores);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.blue));

        jugadorDao = new JugadorDao(this);
        txtResumenJugadores = findViewById(R.id.txtResumenJugadores);
        ListView listaJugadores = findViewById(R.id.listaJugadores);
        TextView txtSinJugadores = findViewById(R.id.txtSinJugadores);

        adaptador = new JugadorAdapter(this, new JugadorAdapter.AccionesJugador() {
            @Override
            public void onEditar(Jugador jugador) {
                mostrarDialogoJugador(jugador);
            }

            @Override
            public void onCambiarEstado(Jugador jugador) {
                solicitarCambioEstado(jugador);
            }
        });
        listaJugadores.setAdapter(adaptador);
        listaJugadores.setEmptyView(txtSinJugadores);

        Button btnNuevoJugador = findViewById(R.id.btnNuevoJugador);
        Button btnVolverJugadores = findViewById(R.id.btnVolverJugadores);
        btnNuevoJugador.setOnClickListener(v -> mostrarDialogoJugador(null));
        btnVolverJugadores.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarJugadores();
    }

    private void cargarJugadores() {
        try {
            List<Jugador> jugadores = jugadorDao.listarTodos();
            adaptador.actualizarJugadores(jugadores);

            int activos = 0;
            for (Jugador jugador : jugadores) {
                if (jugador.isActivo()) {
                    activos++;
                }
            }
            txtResumenJugadores.setText(
                    activos + " activos · " + (jugadores.size() - activos) + " inactivos"
            );
        } catch (SQLiteException e) {
            mostrarError("No se ha podido cargar la lista de jugadores");
        }
    }

    private void mostrarDialogoJugador(Jugador existente) {
        boolean editando = existente != null;
        View contenido = getLayoutInflater().inflate(R.layout.dialogo_jugador, null);
        EditText editNombre = contenido.findViewById(R.id.editNombreJugador);
        GridLayout cuadrículaColores = contenido.findViewById(R.id.gridColoresJugador);

        int colorInicial = editando
                ? existente.getColor()
                : ContextCompat.getColor(this, COLORES_RECURSO[0]);
        final int[] colorElegido = {colorInicial};

        if (editando) {
            editNombre.setText(existente.getNombre());
            editNombre.setSelection(editNombre.length());
        }

        pintarColores(cuadrículaColores, colorElegido);

        AlertDialog dialogo = new MaterialAlertDialogBuilder(this, R.style.TemaDialogoDardos)
                .setTitle(editando ? "Editar jugador" : "Añadir jugador")
                .setView(contenido)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        // Se controla el clic manualmente para no cerrar el diálogo ante un error.
        dialogo.setOnShowListener(ignorado ->
                dialogo.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String nombre = editNombre.getText().toString();
                    try {
                        if (editando) {
                            Jugador actualizado = new Jugador(
                                    existente.getId(), nombre, colorElegido[0], existente.isActivo()
                            );
                            if (!jugadorDao.actualizar(actualizado)) {
                                mostrarError("No se ha encontrado el jugador");
                                return;
                            }
                        } else {
                            jugadorDao.insertar(new Jugador(nombre, colorElegido[0]));
                        }

                        dialogo.dismiss();
                        cargarJugadores();
                        Toast.makeText(this,
                                editando ? "Jugador actualizado" : "Jugador añadido",
                                Toast.LENGTH_SHORT).show();

                    } catch (IllegalArgumentException e) {
                        editNombre.setError("Introduce un nombre válido");
                        editNombre.requestFocus();
                    } catch (SQLiteConstraintException e) {
                        editNombre.setError("Ya existe un jugador con ese nombre");
                        editNombre.requestFocus();
                    } catch (SQLiteException e) {
                        mostrarError("No se han podido guardar los cambios");
                    }
                })
        );
        dialogo.show();
    }

    private void pintarColores(GridLayout cuadrícula, int[] colorElegido) {
        cuadrícula.removeAllViews();
        cuadrícula.setColumnCount(4);

        for (int i = 0; i < COLORES_RECURSO.length; i++) {
            final int color = ContextCompat.getColor(this, COLORES_RECURSO[i]);
            final boolean seleccionado = color == colorElegido[0];

            TextView muestra = new TextView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dp(48);
            params.height = dp(48);
            params.setMargins(dp(7), dp(7), dp(7), dp(7));
            muestra.setLayoutParams(params);
            muestra.setGravity(Gravity.CENTER);
            muestra.setText(seleccionado ? "✓" : "");
            muestra.setTextSize(22);
            muestra.setTextColor(colorClaro(color) ? Color.BLACK : Color.WHITE);
            muestra.setContentDescription(NOMBRES_COLORES[i] +
                    (seleccionado ? ", seleccionado" : ""));
            muestra.setSelected(seleccionado);
            muestra.setClickable(true);
            muestra.setFocusable(true);

            GradientDrawable fondo = new GradientDrawable();
            fondo.setShape(GradientDrawable.OVAL);
            fondo.setColor(color);
            fondo.setStroke(dp(seleccionado ? 4 : 1),
                    ContextCompat.getColor(this, seleccionado
                            ? R.color.gold : R.color.white));
            muestra.setBackground(fondo);

            muestra.setOnClickListener(v -> {
                colorElegido[0] = color;
                pintarColores(cuadrícula, colorElegido);
            });
            cuadrícula.addView(muestra);
        }
    }

    private boolean colorClaro(int color) {
        return (Color.red(color) * 299 + Color.green(color) * 587
                + Color.blue(color) * 114) / 1000 >= 150;
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    private void solicitarCambioEstado(Jugador jugador) {
        if (!jugador.isActivo()) {
            cambiarEstado(jugador, true);
            return;
        }

        new MaterialAlertDialogBuilder(this, R.style.TemaDialogoDardos)
                .setTitle("Desactivar jugador")
                .setMessage("¿Quieres desactivar a " + jugador.getNombre() + "? " +
                        "No podrá seleccionarse en partidas nuevas, pero " +
                        "conservará su historial y sus estadísticas.")
                .setPositiveButton("Desactivar", (dialogo, cual) ->
                        cambiarEstado(jugador, false))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cambiarEstado(Jugador jugador, boolean activo) {
        try {
            if (jugadorDao.cambiarActivo(jugador.getId(), activo)) {
                cargarJugadores();
                Toast.makeText(this,
                        activo ? "Jugador reactivado" : "Jugador desactivado",
                        Toast.LENGTH_SHORT).show();
            } else {
                mostrarError("No se ha encontrado el jugador");
            }
        } catch (SQLiteException e) {
            mostrarError("No se ha podido cambiar el estado del jugador");
        }
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        if (jugadorDao != null) {
            jugadorDao.close();
        }
        super.onDestroy();
    }
}
