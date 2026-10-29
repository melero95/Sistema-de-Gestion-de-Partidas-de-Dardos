package utilidades;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import activities.JugadoresActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import modelos.ConfiguracionPartida;
import modelos.Partida;
import modelos.Jugador;
import sqlite.JugadorDao;
import activities.ConfigurarNuevaPartidaActivity;
import activities.PartidaCriquetActivity;
import activities.PartidaPuntosActivity;
import activities.PartidaRondasActivity;
import com.productos.juegosdedardos.R;

/** Reconstruye una partida histórica sin alterar su registro original. */
public final class AccionesPartidaHistorica {
    private AccionesPartidaHistorica() { }
    public static void editar(Activity actividad, Partida partida) {
        Intent intent = new Intent(actividad, ConfigurarNuevaPartidaActivity.class);
        intent.putExtra(ConfigurarNuevaPartidaActivity.EXTRA_CONFIGURACION_JSON, partida.getConfiguracionJson());
        actividad.startActivity(intent);
    }
    public static void repetir(Activity actividad, Partida partida) {
        final ConfiguracionPartida cfg;
        try { cfg = SerializadorConfiguracionPartida.desdeJson(partida.getConfiguracionJson()); }
        catch (IllegalArgumentException ex) {
            Toast.makeText(actividad, "Configuración histórica incompatible", Toast.LENGTH_LONG).show(); return;
        }
        // Comprobar todos los participantes e informar solo de los no disponibles.
        List<String> noDisponibles = new ArrayList<>();
        try (JugadorDao dao = new JugadorDao(actividad)) {
            for (ConfiguracionPartida.Participante p : cfg.getParticipantes()) {
                Jugador jugador = dao.obtenerPorId(p.getIdJugador());
                if (jugador == null || !jugador.isActivo()) {
                    // Nombre histórico, incluso si el jugador ya no existe.
                    noDisponibles.add(p.getNombre());
                }
            }
        }
        if (!noDisponibles.isEmpty()) {
            StringBuilder mensaje = new StringBuilder(
                    "Para repetir esta partida, revisa estos participantes:\n\n");
            for (String nombre : noDisponibles) {
                mensaje.append("• ").append(nombre).append("\n");
            }
            mensaje.append("\nActívalos desde Gestión de jugadores y vuelve a intentarlo. ")
                   .append("Si alguno ya no existe, utiliza Recuperar configuración.");
            new MaterialAlertDialogBuilder(actividad, R.style.TemaDialogoDardos)
                    .setTitle("Participantes no disponibles")
                    .setMessage(mensaje.toString())
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Gestionar jugadores", (dialogo, opcion) ->
                            actividad.startActivity(new Intent(actividad, JugadoresActivity.class)))
                    .show();
            return;
        }
        Intent intent;
        switch(cfg.getModo()) {
            case ConfiguracionPartida.MODO_301:
            case ConfiguracionPartida.MODO_501: intent = new Intent(actividad, PartidaPuntosActivity.class); break;
            case ConfiguracionPartida.MODO_CRICKET:
            case ConfiguracionPartida.MODO_CUT_THROAT: intent = new Intent(actividad, PartidaCriquetActivity.class); break;
            default: intent = new Intent(actividad, PartidaRondasActivity.class);
        }
        intent.putExtra("modoJuego", cfg.getModo());
        intent.putExtra("maxRondas", cfg.getRondas());
        intent.putExtra("numeroJugadores", cfg.getParticipantes().size());
        intent.putExtra("numeroDardos", cfg.getDardosPorTurno());
        intent.putExtra("cierreDoble", cfg.isCierreDoble());
        intent.putExtra("mantenerMultiplicador", cfg.isMantenerMultiplicador());
        // Orden histórico ya resuelto; no sortear de nuevo.
        intent.putExtra("ordenAleatorio", cfg.isOrdenAleatorio());
        long[] ids = new long[cfg.getParticipantes().size()];
        ArrayList<String> nombres = new ArrayList<>();
        ArrayList<Integer> colores = new ArrayList<>();
        ArrayList<ConfiguracionPartida.Participante> jugadores = new ArrayList<>();
        int i = 0;
        for (ConfiguracionPartida.Participante p : cfg.getParticipantes()) {
            i++;
            int recurso = colorRecurso(actividad, p.getColorArgb());
            ids[i - 1] = p.getIdJugador(); nombres.add(p.getNombre()); colores.add(recurso);
            intent.putExtra("idJugador" + i, p.getIdJugador());
            intent.putExtra("jugador" + i, p.getNombre());
            intent.putExtra("colorJugador" + i, recurso);
            jugadores.add(new ConfiguracionPartida.Participante(p.getIdJugador(), p.getNombre(),
                    p.getColorArgb(), i));
        }
        intent.putExtra("idsJugadores", ids);
        intent.putStringArrayListExtra("nombresJugadores", nombres);
        intent.putStringArrayListExtra(PartidaPuntosActivity.EXTRA_NOMBRES_JUGADORES, nombres);
        intent.putIntegerArrayListExtra("colorJugador", colores);
        ConfiguracionPartida nueva = new ConfiguracionPartida(cfg.getModo(), cfg.getRondas(),
                cfg.getDardosPorTurno(), cfg.isCierreDoble(), cfg.isMantenerMultiplicador(),
                cfg.isOrdenAleatorio(), jugadores);
        intent.putExtra(ConfigurarNuevaPartidaActivity.EXTRA_CONFIGURACION_JSON,
                SerializadorConfiguracionPartida.aJson(nueva));
        actividad.startActivity(intent);
    }
    private static int colorRecurso(Activity actividad, int argb) {
        int[] candidatos = {R.color.jugador_rojo, R.color.jugador_verde, R.color.jugador_azul,
                R.color.jugador_amarillo, R.color.jugador_naranja, R.color.jugador_morado,
                R.color.jugador_cyan, R.color.jugador_blanco};
        for (int recurso : candidatos) if (ContextCompat.getColor(actividad, recurso) == argb) return recurso;
        return R.color.jugador_blanco;
    }
}
