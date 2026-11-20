package preferencias;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import modelos.PartidaEnCurso;

public final class GestorPartidaEnCurso {
    private static final String NOMBRE_PREFERENCIAS = "preferencias_partida_en_curso";
    private static final String CLAVE_PARTIDA = "partida_guardada";
    private static final Gson GSON = new Gson();
    private static final String TAG = "GestorPartidaEnCurso";

    private GestorPartidaEnCurso() { }

    public static boolean guardarPartida(Context context, PartidaEnCurso partida) {
        if (context == null || partida == null) return false;
        try {
            partida.setVersionGuardado(PartidaEnCurso.VERSION_ACTUAL);
            partida.setFechaGuardado(System.currentTimeMillis());
            String jsonPartida = GSON.toJson(partida);
            return obtenerPreferencias(context).edit()
                    .putString(CLAVE_PARTIDA, jsonPartida).commit();
        } catch (Exception e) {
            Log.e(TAG, "No se pudo crear el respaldo temporal", e);
            return false;
        }
    }

    public static PartidaEnCurso cargarPartida(Context context) {
        if (context == null) return null;
        String jsonPartida = obtenerPreferencias(context).getString(CLAVE_PARTIDA, null);
        if (jsonPartida == null || jsonPartida.trim().isEmpty()) return null;
        try {
            PartidaEnCurso partida = GSON.fromJson(jsonPartida, PartidaEnCurso.class);
            if (!esPartidaValida(partida)) {
                // Nunca eliminar de forma automática un JSON guardado por otra
                // versión ofuscada: puede ser recuperable tras una migración.
                Log.e(TAG, "Partida guardada con formato incompatible; se conserva");
                return null;
            }
            return partida;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "JSON temporal inválido; se conserva", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "No se pudo recuperar el JSON temporal; se conserva", e);
            return null;
        }
    }

    public static boolean existePartida(Context context) {
        if (context == null) return false;
        String json = obtenerPreferencias(context).getString(CLAVE_PARTIDA, null);
        return json != null && !json.trim().isEmpty();
    }

    public static boolean eliminarPartida(Context context) {
        if (context == null) return false;
        return obtenerPreferencias(context).edit().remove(CLAVE_PARTIDA).commit();
    }

    private static boolean esPartidaValida(PartidaEnCurso partida) {
        if (partida == null || partida.getVersionGuardado() != PartidaEnCurso.VERSION_ACTUAL
                || partida.getTipoPartida() == null || partida.getTipoPartida().trim().isEmpty()
                || partida.getModoJuego() == null || partida.getModoJuego().trim().isEmpty()
                || partida.getNombresJugadores() == null || partida.getNombresJugadores().isEmpty()
                || partida.getNumeroJugadores() <= 0) return false;

        switch (partida.getTipoPartida()) {
            case PartidaEnCurso.TIPO_PUNTOS:
                return partida.getEstadoPuntos() != null;
            case PartidaEnCurso.TIPO_CRICKET:
                return partida.getEstadoCricket() != null;
            case PartidaEnCurso.TIPO_RONDAS:
                return partida.getEstadoRondas() != null;
            default:
                return false;
        }
    }

    private static SharedPreferences obtenerPreferencias(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE);
    }
}
