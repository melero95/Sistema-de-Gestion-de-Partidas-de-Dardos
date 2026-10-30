package activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.productos.juegosdedardos.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import modelos.EstadisticasJugadorDTO;
import modelos.EstadisticasResumenDTO;
import modelos.Jugador;
import modelos.MetricasModalidadDTO;
import modelos.RecordEstadisticoDTO;
import sqlite.ConsultasEstadisticas;
import sqlite.JugadorDao;

/**
 * Fase 08: tres apartados en una sola Activity, sin duplicar datos históricos.
 * Las partidas ocultas y los jugadores inactivos siempre se contabilizan.
 */
public class EstadisticasActivity extends AppCompatActivity {
    public static final String EXTRA_ID_JUGADOR = "estadisticas_id_jugador";
    private static final String[] MODOS = {
            "301", "501", "Cricket", "Cut Throat Cricket", "Double Down", "Around the Clock", "Shanghai"
    };
    private static final String[] MODOS_SELECTOR = {
            "301", "501", "Cricket", "Cut Throat Cricket", "Double Down", "Around the Clock", "Shanghai"
    };
    private static final int GENERAL = 0, JUGADORES = 1, RECORDS = 2;
    private final Locale idioma = new Locale("es", "ES");
    private int pestaña = GENERAL;
    private long jugadorSeleccionado = 0L;
    private String modoSeleccionado = "301";
    private ConsultasEstadisticas consultas;
    private LinearLayout contenido;
    private ScrollView scroll;
    private TextView tabGeneral, tabJugadores, tabRecords;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.blue));
        setContentView(R.layout.activity_estadisticas);
        consultas = new ConsultasEstadisticas(this);
        contenido = findViewById(R.id.contenidoEstadisticas);
        scroll = findViewById(R.id.scrollEstadisticas);
        tabGeneral = findViewById(R.id.tabEstadisticasGeneral);
        tabJugadores = findViewById(R.id.tabEstadisticasJugadores);
        tabRecords = findViewById(R.id.tabEstadisticasRecords);
        tabGeneral.setOnClickListener(v -> cambiarPestana(GENERAL));
        tabJugadores.setOnClickListener(v -> cambiarPestana(JUGADORES));
        tabRecords.setOnClickListener(v -> cambiarPestana(RECORDS));
        findViewById(R.id.btnVolverEstadisticas).setOnClickListener(v -> finish());
        if (state != null) {
            pestaña = state.getInt("pestana", GENERAL);
            jugadorSeleccionado = state.getLong("jugador", 0L);
            modoSeleccionado = state.getString("modo", "301");
        } else if (getIntent().getLongExtra(EXTRA_ID_JUGADOR, 0L) > 0L) {
            jugadorSeleccionado = getIntent().getLongExtra(EXTRA_ID_JUGADOR, 0L);
            pestaña = JUGADORES;
        }
        mostrarActual();
    }

    @Override protected void onResume() {
        super.onResume();
        if (contenido != null && consultas != null) mostrarActual();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        out.putInt("pestana", pestaña);
        out.putLong("jugador", jugadorSeleccionado);
        out.putString("modo", modoSeleccionado);
        super.onSaveInstanceState(out);
    }
    @Override protected void onDestroy() {
        if (consultas != null) consultas.close();
        super.onDestroy();
    }

    private void cambiarPestana(int nueva) {
        if (pestaña == nueva) return;
        pestaña = nueva;
        mostrarActual();
    }
    private void mostrarActual() {
        if (contenido == null || consultas == null) return;
        contenido.removeAllViews();
        TextView[] tabs = {tabGeneral, tabJugadores, tabRecords};
        for (int i = 0; i < tabs.length; i++) {
            boolean activa = i == pestaña;
            tabs[i].setBackgroundResource(activa
                    ? R.drawable.fondo_pestana_estadisticas_activa
                    : R.drawable.fondo_pestana_estadisticas_inactiva);
            tabs[i].setTextColor(ContextCompat.getColor(this, activa ? R.color.blue : R.color.gold));
        }
        try {
            if (pestaña == GENERAL) mostrarGeneral();
            else if (pestaña == JUGADORES) mostrarJugadores();
            else mostrarRecords();
        } catch (Exception error) {
            android.util.Log.e("EstadisticasActivity", "Error consultando estadísticas", error);
            bloqueTexto(contenido, "No se han podido consultar las estadísticas. Comprueba los datos guardados.");
            Toast.makeText(this, "Error al leer estadísticas", Toast.LENGTH_SHORT).show();
        }
        scroll.post(() -> scroll.scrollTo(0, 0));
    }

    // ---------- GENERAL ----------
    private void mostrarGeneral() {
        EstadisticasResumenDTO resumen = consultas.resumenGeneral();
        LinearLayout panel = tarjeta(contenido, "RESUMEN GENERAL");
        linea(panel, "Partidas finalizadas", String.valueOf(resumen.totalPartidas));
        linea(panel, "Jugadores registrados", String.valueOf(resumen.totalJugadores));
        linea(panel, "Modalidad más jugada", resumen.modalidadMasJugada == null ? "Sin partidas" : resumen.modalidadMasJugada);
        linea(panel, "Modos utilizados", String.valueOf(resumen.partidasPorModo.size()) + " / 7");
        LinearLayout modos = tarjeta(contenido, "PARTIDAS POR MODALIDAD");
        Map<String, Long> recuento = new HashMap<>();
        for (EstadisticasResumenDTO.Modalidad m : resumen.partidasPorModo) recuento.put(m.modo, m.partidas);
        for (String modo : MODOS) linea(modos, modo, String.valueOf(recuento.containsKey(modo) ? recuento.get(modo) : 0L));
        if (resumen.totalPartidas == 0L) bloqueTexto(contenido, "Aún no hay partidas finalizadas. Las estadísticas aparecerán al jugar.");
    }

    // ---------- JUGADORES ----------
    private void mostrarJugadores() {
        List<Jugador> jugadores;
        try (JugadorDao dao = new JugadorDao(this)) { jugadores = dao.listarTodos(); }
        if (jugadores.isEmpty()) {
            bloqueTexto(contenido, "No hay jugadores registrados.");
            return;
        }
        List<String> nombres = new ArrayList<>();
        int seleccion = 0;
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            nombres.add(j.getNombre() + (j.isActivo() ? "" : " (inactivo)"));
            if (j.getId() == jugadorSeleccionado) seleccion = i;
        }
        jugadorSeleccionado = jugadores.get(seleccion).getId();
        LinearLayout selector = tarjeta(contenido, "SELECCIONAR JUGADOR");
        Spinner spinner = spinner(nombres);
        selector.addView(spinner);
        LinearLayout detalle = vertical();
        contenido.addView(detalle);
        spinner.setSelection(seleccion);
        // onItemSelected puede ejecutarse más tarde, al mostrar la vista.
        actualizarJugador(detalle, jugadorSeleccionado);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                jugadorSeleccionado = jugadores.get(position).getId();
                actualizarJugador(detalle, jugadorSeleccionado);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
    private void actualizarJugador(LinearLayout detalle, long id) {
        detalle.removeAllViews();
        EstadisticasJugadorDTO jugador = consultas.resumenJugador(id);
        if (jugador == null) { bloqueTexto(detalle, "Jugador no encontrado."); return; }
        LinearLayout resumen = tarjeta(detalle, jugador.nombre + (jugador.activo ? "" : " · INACTIVO"));
        linea(resumen, "Partidas jugadas", String.valueOf(jugador.partidas));
        linea(resumen, "Victorias", String.valueOf(jugador.victorias));
        linea(resumen, "Porcentaje de victorias", porcentaje(jugador.porcentajeVictorias));
        linea(resumen, "Segundos puestos", String.valueOf(jugador.segundos));
        linea(resumen, "Terceros puestos", String.valueOf(jugador.terceros));
        LinearLayout porModo = tarjeta(detalle, "PARTIDAS POR MODALIDAD");
        Map<String, Long> recuento = new HashMap<>();
        for (EstadisticasResumenDTO.Modalidad modo : jugador.partidasPorModo) recuento.put(modo.modo, modo.partidas);
        for (String modo : MODOS) linea(porModo, modo, String.valueOf(recuento.containsKey(modo) ? recuento.get(modo) : 0L));
        LinearLayout historial = tarjeta(detalle, "ÚLTIMAS 5 PARTIDAS");
        if (jugador.ultimasPartidas.isEmpty()) textoVacio(historial, "Todavía no ha disputado partidas.");
        for (EstadisticasJugadorDTO.UltimaPartida partida : jugador.ultimasPartidas) {
            String fecha = DateFormat.getDateInstance(DateFormat.SHORT, idioma).format(new Date(partida.fecha));
            String titulo = fecha + " · " + partida.modo;
            String desc = (partida.victoria ? "Victoria" : "Posición " + partida.posicion) + "   ·   Ver detalle ›";
            LinearLayout fila = vertical();
            TextView t = texto(titulo, 15, R.color.gold, true);
            fila.addView(t);
            fila.addView(texto(desc, 13, android.R.color.white, false));
            fila.setPadding(dp(6), dp(8), dp(6), dp(8));
            fila.setOnClickListener(v -> {
                Intent intent = new Intent(this, DetallePartidaActivity.class);
                intent.putExtra(DetallePartidaActivity.EXTRA_ID_PARTIDA, partida.idPartida);
                startActivity(intent);
            });
            historial.addView(fila);
        }
        LinearLayout modo = tarjeta(detalle, "ESTADÍSTICAS POR MODALIDAD");
        Spinner spinnerModo = spinner(Arrays.asList(MODOS_SELECTOR));
        modo.addView(spinnerModo);
        LinearLayout metricas = vertical();
        modo.addView(metricas);
        int seleccionado = Math.max(0, Arrays.asList(MODOS_SELECTOR).indexOf(modoSeleccionado));
        spinnerModo.setSelection(seleccionado);
        mostrarMetricas(metricas, id, MODOS_SELECTOR[seleccionado]);
        spinnerModo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long idSpinner) {
                modoSeleccionado = MODOS_SELECTOR[position];
                mostrarMetricas(metricas, id, modoSeleccionado);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void mostrarMetricas(LinearLayout contenedor, long idJugador, String modo) {
        contenedor.removeAllViews();
        MetricasModalidadDTO datos = consultas.metricasJugador(idJugador, modo);
        if (datos.partidas == 0L) { textoVacio(contenedor, "Sin partidas en " + modo + "."); return; }
        linea(contenedor, "Partidas disputadas", String.valueOf(datos.partidas));
        Map<String, Long> sumas = datos.acumulados;
        Map<String, Integer> mejores = datos.maximos;
        switch (modo) {
            case "301": case "501":
                if (sumas.containsKey("puntos_validos") && sumas.containsKey("dardos_lanzados")) {
                    if (sumas.get("dardos_lanzados") > 0L) linea(contenedor, "Media de puntos por dardo", decimal(datos.puntosPorDardo));
                    else linea(contenedor, "Media de puntos por dardo", "—");
                }
                valorMax(contenedor, "Mejor turno", mejores, "mejor_turno");
                valorSuma(contenedor, "Turnos de 180", sumas, "turnos_180");
                valorSuma(contenedor, "Busts", sumas, "busts");
                valorSuma(contenedor, "Cierres realizados", sumas, "cierres_realizados");
                valorSuma(contenedor, "Dardos empleados en cierres", sumas, "dardos_cierre");
                break;
            case "Cricket": case "Cut Throat Cricket":
                valorSuma(contenedor, "Marcas válidas", sumas, "marcas_validas");
                valorSuma(contenedor, "Objetivos cerrados", sumas, "objetivos_cerrados");
                valorSuma(contenedor, "Puntos generados", sumas, "puntos_generados");
                valorSuma(contenedor, "Puntos recibidos", sumas, "puntos_recibidos");
                break;
            case "Double Down":
                valorSuma(contenedor, "Puntos conseguidos", sumas, "puntos_conseguidos_dd");
                valorMax(contenedor, "Mejor ronda", mejores, "mejor_ronda_dd");
                valorSuma(contenedor, "Rondas penalizadas", sumas, "rondas_penalizadas");
                break;
            case "Around the Clock":
                valorSuma(contenedor, "Objetivos superados", sumas, "objetivos_superados");
                valorSuma(contenedor, "Aciertos de objetivo", sumas, "aciertos_objetivo");
                valorSuma(contenedor, "Partidas completadas", sumas, "partida_completada");
                break;
            case "Shanghai":
                valorSuma(contenedor, "Puntos acumulados", sumas, "puntos_validos_shanghai");
                valorMax(contenedor, "Mejor ronda", mejores, "mejor_ronda_shanghai");
                valorSuma(contenedor, "Rondas con puntos", sumas, "rondas_con_puntos");
                break;
            default: break;
        }
        if (sumas.isEmpty() && mejores.isEmpty()) textoVacio(contenedor, "No hay contadores guardados para esta modalidad.");
    }
    private void valorSuma(LinearLayout parent, String titulo, Map<String, Long> datos, String campo) {
        linea(parent, titulo, datos.containsKey(campo) ? String.valueOf(datos.get(campo)) : "—");
    }
    private void valorMax(LinearLayout parent, String titulo, Map<String, Integer> datos, String campo) {
        linea(parent, titulo, datos.containsKey(campo) ? String.valueOf(datos.get(campo)) : "—");
    }

    // ---------- RÉCORDS ----------
    private void mostrarRecords() {
        textoVacio(contenido, "Las marcas incluyen partidas ocultas y jugadores inactivos. El porcentaje exige al menos 10 partidas.");
        mostrarRecord("MÁS VICTORIAS", consultas.recordMasVictorias(), false, "victorias");
        mostrarRecord("MÁS PARTIDAS JUGADAS", consultas.recordMasPartidas(), false, "partidas");
        mostrarRecord("MAYOR PORCENTAJE DE VICTORIAS", consultas.recordPorcentajeVictorias(), true, "");
        mostrarRecord("MÁS TURNOS DE 180 (301 / 501)", consultas.recordMas180(), false, "");
        mostrarRecord("MAYOR PUNTUACIÓN · DOUBLE DOWN", consultas.recordMayorPuntuacion("Double Down"), false, "puntos");
        mostrarRecord("MAYOR PUNTUACIÓN · SHANGHAI", consultas.recordMayorPuntuacion("Shanghai"), false, "puntos");
    }
    private void mostrarRecord(String titulo, List<RecordEstadisticoDTO> registros, boolean esPorcentaje, String unidad) {
        LinearLayout panel = tarjeta(contenido, titulo);
        if (registros.isEmpty() || (registros.get(0).valor <= 0d && !esPorcentaje)) {
            textoVacio(panel, "Todavía no hay una marca registrada.");
            return;
        }
        for (RecordEstadisticoDTO r : registros) {
            String marca = esPorcentaje ? porcentaje(r.valor) : String.valueOf((long) r.valor) + (unidad.isEmpty() ? "" : " " + unidad);
            linea(panel, r.nombre, marca);
        }
    }

    // ---------- Componentes visuales compartidos ----------
    private LinearLayout vertical() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return l;
    }
    private LinearLayout tarjeta(LinearLayout parent, String titulo) {
        LinearLayout l = vertical();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12);
        l.setLayoutParams(lp);
        l.setBackgroundResource(R.drawable.fondo_panel_estadisticas);
        l.setPadding(dp(14), dp(12), dp(14), dp(12));
        TextView encabezado = texto(titulo, 16, R.color.gold, true);
        encabezado.setPadding(0, 0, 0, dp(9));
        l.addView(encabezado);
        parent.addView(l);
        return l;
    }
    private void linea(LinearLayout parent, String etiqueta, String valor) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(7), 0, dp(7));
        TextView izq = texto(etiqueta, 14, android.R.color.white, false);
        izq.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView der = texto(valor, 15, R.color.gold, true);
        der.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        der.setPadding(dp(8), 0, 0, 0);
        row.addView(izq);
        row.addView(der);
        parent.addView(row);
    }
    private void bloqueTexto(LinearLayout parent, String mensaje) {
        LinearLayout card = tarjeta(parent, "INFORMACIÓN");
        textoVacio(card, mensaje);
    }
    private void textoVacio(LinearLayout parent, String mensaje) {
        TextView texto = texto(mensaje, 14, android.R.color.white, false);
        texto.setPadding(dp(3), dp(8), dp(3), dp(8));
        parent.addView(texto);
    }
    private TextView texto(String valor, int tamaño, int color, boolean negrita) {
        TextView t = new TextView(this);
        t.setText(valor);
        t.setTextSize(tamaño);
        t.setTextColor(ContextCompat.getColor(this, color));
        if (negrita) t.setTypeface(null, android.graphics.Typeface.BOLD);
        return t;
    }
    private Spinner spinner(List<String> valores) {
        Spinner sp = new Spinner(this);
        ArrayAdapter<String> a = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valores) {
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                TextView txt = (TextView) super.getView(position, convertView, parent);
                txt.setTextColor(ContextCompat.getColor(EstadisticasActivity.this, R.color.gold));
                txt.setTextSize(16);
                return txt;
            }
            @Override public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView txt = (TextView) super.getDropDownView(position, convertView, parent);
                txt.setTextColor(Color.WHITE);
                txt.setBackgroundColor(Color.rgb(26, 35, 126));
                txt.setPadding(dp(12), dp(12), dp(12), dp(12));
                return txt;
            }
        };
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(a);
        sp.setMinimumHeight(dp(48));
        sp.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gold));
        return sp;
    }
    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
    private String decimal(double v) { return String.format(idioma, "%.2f", v); }
    private String porcentaje(double v) { return decimal(v) + " %"; }
}
