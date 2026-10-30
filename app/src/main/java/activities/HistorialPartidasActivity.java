
package activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.productos.juegosdedardos.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import adapters.PartidaHistorialAdapter;
import modelos.Jugador;
import modelos.PartidaResumenDTO;
import sqlite.ConsultasHistorial;
import utilidades.AccionesPartidaHistorica;

public class HistorialPartidasActivity
        extends AppCompatActivity {

    private final String[] modos = {
            "Todos los modos",
            "301",
            "501",
            "Cricket",
            "Cut Throat Cricket",
            "Double Down",
            "Around the Clock",
            "Shanghai"
    };

    private static final String[] ORDENES = {
            "Fecha (más recientes)",
            "Fecha (más antiguas)",
            "Modo (A-Z)",
            "Ganador (A-Z)",
            "Rondas (mayor a menor)",
            "Rondas (menor a mayor)"
    };

    private ConsultasHistorial consultas;

    private Spinner spinner;
    private Spinner spinnerJugador;
    private Spinner spinnerOrden;

    private Button btnRestaurar;
    private TextView txtVacio;

    private PartidaHistorialAdapter adaptador;

    /**
     * El primer elemento representa todos los jugadores
     * y utiliza null como identificador.
     */
    private final List<Long> idsJugadores =
            new ArrayList<>();

    private boolean cargandoJugadores = false;

    @Override
    protected void onCreate(Bundle state) {

        super.onCreate(state);

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

        setContentView(
                R.layout.activity_historial_partidas
        );

        consultas = new ConsultasHistorial(this);

        spinner = findViewById(
                R.id.spnModoHistorial
        );

        spinnerJugador = findViewById(
                R.id.spnJugadorHistorial
        );

        spinnerOrden = findViewById(
                R.id.spnOrdenHistorial
        );

        txtVacio = findViewById(
                R.id.txtHistorialVacio
        );

        btnRestaurar = findViewById(
                R.id.btnRestaurarTodas
        );

        RecyclerView recycler = findViewById(
                R.id.recyclerHistorial
        );

        recycler.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adaptador = new PartidaHistorialAdapter(
                new PartidaHistorialAdapter.Eventos() {

                    @Override
                    public void abrir(PartidaResumenDTO p) {
                        abrirDetalle(p.getId());
                    }

                    @Override
                    public void menu(
                            View ancla,
                            PartidaResumenDTO p) {

                        abrirMenu(ancla, p);
                    }
                }
        );

        recycler.setAdapter(adaptador);

        configurarSpinnerModos();
        configurarSpinnerOrden();
        configurarSpinnerJugadores();

        btnRestaurar.setOnClickListener(
                v -> confirmarRestaurar()
        );

        findViewById(
                R.id.btnVolverHistorial
        ).setOnClickListener(v -> finish());

        if (state != null) {

            spinner.setSelection(
                    state.getInt("modo", 0)
            );

            spinnerOrden.setSelection(
                    state.getInt("orden", 0)
            );

            long idJugador = state.getLong(
                    "jugador",
                    0L
            );

            seleccionarJugadorPorId(
                    idJugador == 0L ? null : idJugador
            );
        }

        recargar();
    }

    // Configuración de filtros ------------------------------------

    private void configurarSpinnerModos() {

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        modos
                );

        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View v,
                            int position,
                            long id) {

                        recargar();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );
    }

    private void configurarSpinnerOrden() {

        ArrayAdapter<String> ordenAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        ORDENES
                );

        ordenAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerOrden.setAdapter(ordenAdapter);

        spinnerOrden.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View v,
                            int position,
                            long id) {

                        recargar();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );
    }

    private void configurarSpinnerJugadores() {

        spinnerJugador.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View v,
                            int position,
                            long id) {

                        if (!cargandoJugadores) {
                            recargar();
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );

        cargarJugadores(null);
    }

    /**
     * Recupera todos los jugadores de SQLite,
     * incluidos los que estén desactivados.
     *
     * Conserva el jugador seleccionado cuando
     * se vuelve a cargar el Spinner.
     */
    private void cargarJugadores(Long idSeleccionado) {

        cargandoJugadores = true;

        List<String> nombres = new ArrayList<>();

        idsJugadores.clear();

        nombres.add("Todos los jugadores");
        idsJugadores.add(null);

        List<Jugador> jugadores =
                consultas.listarJugadores();

        for (Jugador jugador : jugadores) {

            nombres.add(jugador.getNombre());

            idsJugadores.add(
                    jugador.getId()
            );
        }

        ArrayAdapter<String> adaptadorJugadores =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        nombres
                );

        adaptadorJugadores.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerJugador.setAdapter(
                adaptadorJugadores
        );

        seleccionarJugadorPorId(idSeleccionado);

        cargandoJugadores = false;
    }

    private Long obtenerJugadorSeleccionado() {

        if (spinnerJugador == null
                || idsJugadores.isEmpty()) {

            return null;
        }

        int posicion =
                spinnerJugador.getSelectedItemPosition();

        if (posicion < 0
                || posicion >= idsJugadores.size()) {

            return null;
        }

        return idsJugadores.get(posicion);
    }

    private void seleccionarJugadorPorId(
            Long idJugador) {

        if (spinnerJugador == null) {
            return;
        }

        if (idJugador == null) {

            spinnerJugador.setSelection(0);
            return;
        }

        for (int i = 1;
             i < idsJugadores.size();
             i++) {

            if (idJugador.equals(
                    idsJugadores.get(i))) {

                spinnerJugador.setSelection(i);
                return;
            }
        }

        spinnerJugador.setSelection(0);
    }

    // Ciclo de vida -----------------------------------------------

    @Override
    protected void onResume() {

        super.onResume();

        if (consultas != null && adaptador != null) {

            Long idSeleccionado =
                    obtenerJugadorSeleccionado();

            cargarJugadores(idSeleccionado);

            recargar();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {

        out.putInt(
                "modo",
                spinner.getSelectedItemPosition()
        );

        out.putInt(
                "orden",
                spinnerOrden.getSelectedItemPosition()
        );

        Long idJugador =
                obtenerJugadorSeleccionado();

        out.putLong(
                "jugador",
                idJugador == null ? 0L : idJugador
        );

        super.onSaveInstanceState(out);
    }

    @Override
    protected void onDestroy() {

        if (consultas != null) {
            consultas.close();
        }

        super.onDestroy();
    }

    // Listado y filtrado ------------------------------------------

    private void recargar() {

        if (adaptador == null || consultas == null) {
            return;
        }

        int pos = spinner.getSelectedItemPosition();

        String modo =
                pos <= 0 ? null : modos[pos];

        Long idJugador =
                obtenerJugadorSeleccionado();

        List<PartidaResumenDTO> datos =
                consultas.listar(
                        modo,
                        idJugador,
                        false
                );

        ordenar(datos);

        adaptador.actualizar(datos);

        txtVacio.setVisibility(
                datos.isEmpty()
                        ? View.VISIBLE
                        : View.GONE
        );

        txtVacio.setText(
                "No hay partidas visibles para los filtros seleccionados"
        );

        btnRestaurar.setVisibility(
                consultas.contarOcultas() > 0
                        ? View.VISIBLE
                        : View.INVISIBLE
        );
    }

    // Acciones del historial --------------------------------------

    private void abrirDetalle(long id) {

        Intent intent = new Intent(
                this,
                DetallePartidaActivity.class
        );

        intent.putExtra(
                DetallePartidaActivity.EXTRA_ID_PARTIDA,
                id
        );

        startActivity(intent);
    }

    private void abrirMenu(
            View ancla,
            PartidaResumenDTO item) {

        PopupMenu menu = new PopupMenu(
                this,
                ancla
        );

        menu.getMenuInflater().inflate(
                R.menu.menu_partida_historial,
                menu.getMenu()
        );

        boolean oculta =
                consultas.oculta(item.getId());

        menu.getMenu()
                .findItem(R.id.accionOcultar)
                .setVisible(!oculta);

        menu.getMenu()
                .findItem(R.id.accionMostrar)
                .setVisible(oculta);

        menu.setOnMenuItemClickListener(op -> {

            int id = op.getItemId();

            if (id == R.id.accionDetalle) {

                abrirDetalle(item.getId());

            } else if (id == R.id.accionRepetir) {

                AccionesPartidaHistorica.repetir(
                        this,
                        item.getPartida()
                );

            } else if (id == R.id.accionEditar) {

                AccionesPartidaHistorica.editar(
                        this,
                        item.getPartida()
                );

            } else if (id == R.id.accionOcultar
                    || id == R.id.accionMostrar) {

                consultas.cambiarVisibilidad(
                        item.getId(),
                        id == R.id.accionOcultar
                );

                recargar();

            } else {

                return false;
            }

            return true;
        });

        menu.show();
    }

    // Ordenación --------------------------------------------------

    private void ordenar(List<PartidaResumenDTO> datos) {

        int criterio =
                spinnerOrden.getSelectedItemPosition();

        Comparator<PartidaResumenDTO> comparador;

        switch (criterio) {

            case 1:
                // Fecha ascendente
                comparador = Comparator.comparingLong(
                        p -> p.getPartida().getFecha()
                );
                break;

            case 2:
                // Modo alfabético
                comparador = (a, b) ->
                        a.getPartida()
                                .getModo()
                                .compareToIgnoreCase(
                                        b.getPartida().getModo()
                                );
                break;

            case 3:
                // Ganador alfabético
                comparador = (a, b) ->
                        a.getGanador().compareToIgnoreCase(
                                b.getGanador()
                        );
                break;

            case 4:
                // Rondas jugadas descendente
                comparador = (a, b) ->
                        Integer.compare(
                                b.getPartida().getRondasJugadas(),
                                a.getPartida().getRondasJugadas()
                        );
                break;

            case 5:
                // Rondas jugadas ascendente
                comparador = Comparator.comparingInt(
                        p -> p.getPartida().getRondasJugadas()
                );
                break;

            default:
                // Fecha descendente
                comparador = (a, b) ->
                        Long.compare(
                                b.getPartida().getFecha(),
                                a.getPartida().getFecha()
                        );
        }

        datos.sort(
                comparador.thenComparing(
                        (a, b) -> Long.compare(
                                b.getId(),
                                a.getId()
                        )
                )
        );
    }

    // Restauración de partidas ocultas ----------------------------

    private void confirmarRestaurar() {

        new MaterialAlertDialogBuilder(
                this,
                R.style.TemaDialogoDardos
        )
                .setTitle("Restaurar partidas")
                .setMessage(
                        "¿Volver a mostrar todas las partidas ocultas?"
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton(
                        "Restaurar",
                        (d, w) -> {

                            consultas.restaurarTodas();

                            recargar();
                        }
                )
                .show();
    }
}
