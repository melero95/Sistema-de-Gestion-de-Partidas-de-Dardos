package preferencias;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import activities.ResultadoActivity;
import utilidades.DatosFinalizacionPartida;

/**
 * Copia de seguridad del RESULTADO TERMINADO, independiente del motor de juego.
 * Funciona para Puntos, Cricket y Rondas; no escribe en las tablas SQLite.
 * Se conserva hasta que la transacción de resultado se confirma con éxito.
 */
public final class GestorResultadoPendiente {
    private static final String TAG = "GestorResultadoPendiente";
    private static final String PREFERENCIAS = "resultado_final_pendiente";
    private static final String CLAVE_RESULTADO = "fotografia_final_json";
    private static final int VERSION = 1;

    private GestorResultadoPendiente() { }

    private static SharedPreferences preferencias(Context contexto) {
        return contexto.getApplicationContext()
                .getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
    }

    public static boolean existe(Context contexto) {
        return contexto != null && preferencias(contexto)
                .contains(CLAVE_RESULTADO);
    }

    /**
     * No permite sobrescribir el resultado pendiente de OTRA partida.
     * Almacena tipos JSON explícitos para no depender de las firmas genéricas de Gson/R8.
     */
    public static boolean guardar(Context contexto, Intent intent) {
        if (contexto == null || intent == null) return false;
        try {
            String datosFinales = intent.getStringExtra(DatosFinalizacionPartida.EXTRA_DATOS);
            String uuid = uuidDatos(datosFinales);
            if (uuid == null) return false;

            JSONObject copia = new JSONObject();
            copia.put("version", VERSION);
            copia.put("uuid", uuid);
            copia.put("datos", datosFinales);
            copia.put("modo", intent.getStringExtra(ResultadoActivity.EXTRA_MODO_JUEGO));
            copia.put("motivo", intent.getStringExtra(ResultadoActivity.EXTRA_MOTIVO_FINALIZACION));
            copia.put("ganador", intent.getStringExtra(ResultadoActivity.EXTRA_NOMBRE_GANADOR));
            copia.put("numJugadores", intent.getIntExtra(ResultadoActivity.EXTRA_NUMERO_JUGADORES, 0));
            copia.put("rondas", intent.getIntExtra(ResultadoActivity.EXTRA_RONDAS_JUGADAS, 0));
            copia.put("maxRondas", intent.getIntExtra(ResultadoActivity.EXTRA_MAX_RONDAS, 0));
            copia.put("dardos", intent.getIntExtra(ResultadoActivity.EXTRA_NUMERO_DARDOS, 3));
            copia.put("doble", intent.getBooleanExtra(ResultadoActivity.EXTRA_CIERRE_DOBLE, false));
            copia.put("aleatorio", intent.getBooleanExtra(ResultadoActivity.EXTRA_ORDEN_ALEATORIO, false));
            copia.put("nombres", convertirLista(intent.getStringArrayListExtra(
                    ResultadoActivity.EXTRA_NOMBRES_JUGADORES)));
            copia.put("puntos", convertirLista(intent.getIntegerArrayListExtra(
                    ResultadoActivity.EXTRA_PUNTUACIONES)));
            copia.put("colores", convertirLista(intent.getIntegerArrayListExtra(
                    ResultadoActivity.EXTRA_COLORES)));
            copia.put("posiciones", convertirLista(intent.getIntegerArrayListExtra(
                    ResultadoActivity.EXTRA_POSICIONES)));
            copia.put("indices", convertirLista(intent.getIntegerArrayListExtra(
                    ResultadoActivity.EXTRA_INDICES_ORIGINALES)));

            if (copia.isNull("modo") || copia.isNull("motivo")
                    || copia.getJSONArray("nombres").length() == 0) {
                return false;
            }

            String anterior = preferencias(contexto).getString(CLAVE_RESULTADO, null);
            if (anterior != null && !anterior.trim().isEmpty()) {
                String uuidAnterior = new JSONObject(anterior).optString("uuid", "");
                if (!uuidAnterior.equals(uuid)) {
                    Log.e(TAG, "Hay un resultado anterior pendiente; no se sobrescribe");
                    return false;
                }
            }
            return preferencias(contexto).edit()
                    .putString(CLAVE_RESULTADO, copia.toString()).commit();
        } catch (Exception error) {
            Log.e(TAG, "No se pudo conservar el resultado final", error);
            return false;
        }
    }

    /** Si el respaldo está corrupto, devuelve null y NO lo elimina. */
    public static Intent crearIntentRecuperacion(Context contexto) {
        if (contexto == null) return null;
        String texto = preferencias(contexto).getString(CLAVE_RESULTADO, null);
        if (texto == null || texto.trim().isEmpty()) return null;
        try {
            JSONObject datos = new JSONObject(texto);
            if (datos.getInt("version") != VERSION
                    || !datos.getString("uuid").equals(uuidDatos(datos.getString("datos")))) {
                return null;
            }
            Intent resultado = new Intent(contexto, ResultadoActivity.class);
            resultado.putExtra(DatosFinalizacionPartida.EXTRA_DATOS, datos.getString("datos"));
            resultado.putExtra(ResultadoActivity.EXTRA_MODO_JUEGO, datos.getString("modo"));
            resultado.putExtra(ResultadoActivity.EXTRA_MOTIVO_FINALIZACION, datos.getString("motivo"));
            resultado.putExtra(ResultadoActivity.EXTRA_NOMBRE_GANADOR, datos.optString("ganador", ""));
            resultado.putExtra(ResultadoActivity.EXTRA_NUMERO_JUGADORES, datos.getInt("numJugadores"));
            resultado.putExtra(ResultadoActivity.EXTRA_RONDAS_JUGADAS, datos.getInt("rondas"));
            resultado.putExtra(ResultadoActivity.EXTRA_MAX_RONDAS, datos.getInt("maxRondas"));
            resultado.putExtra(ResultadoActivity.EXTRA_NUMERO_DARDOS, datos.optInt("dardos", 3));
            resultado.putExtra(ResultadoActivity.EXTRA_CIERRE_DOBLE, datos.optBoolean("doble", false));
            resultado.putExtra(ResultadoActivity.EXTRA_ORDEN_ALEATORIO,
                    datos.optBoolean("aleatorio", false));
            resultado.putStringArrayListExtra(ResultadoActivity.EXTRA_NOMBRES_JUGADORES,
                    listaString(datos.getJSONArray("nombres")));
            resultado.putIntegerArrayListExtra(ResultadoActivity.EXTRA_PUNTUACIONES,
                    listaInt(datos.getJSONArray("puntos")));
            resultado.putIntegerArrayListExtra(ResultadoActivity.EXTRA_COLORES,
                    listaInt(datos.getJSONArray("colores")));
            resultado.putIntegerArrayListExtra(ResultadoActivity.EXTRA_POSICIONES,
                    listaInt(datos.getJSONArray("posiciones")));
            resultado.putIntegerArrayListExtra(ResultadoActivity.EXTRA_INDICES_ORIGINALES,
                    listaInt(datos.getJSONArray("indices")));
            return resultado;
        } catch (Exception error) {
            Log.e(TAG, "El resultado pendiente no se puede interpretar; no se elimina", error);
            return null;
        }
    }

    /** Nunca elimina el pendiente de una partida distinta. */
    public static boolean eliminarSiCoincide(Context contexto, Intent intent) {
        if (!existe(contexto)) return true;
        try {
            String guardado = preferencias(contexto).getString(CLAVE_RESULTADO, null);
            JSONObject anterior = new JSONObject(guardado);
            String actual = uuidDatos(intent.getStringExtra(DatosFinalizacionPartida.EXTRA_DATOS));
            if (actual == null || !actual.equals(anterior.optString("uuid", ""))) return false;
            return eliminar(contexto);
        } catch (Exception error) {
            Log.e(TAG, "No se pudo comprobar la copia pendiente", error);
            return false;
        }
    }

    /** Solo tras confirmación explícita del usuario si hay incompatibilidad. */
    public static boolean eliminar(Context contexto) {
        if (contexto == null) return false;
        return preferencias(contexto).edit().remove(CLAVE_RESULTADO).commit();
    }

    private static String uuidDatos(String json) throws JSONException {
        if (json == null || json.trim().isEmpty()) return null;
        String uuid = new JSONObject(json).optString("identificadorUnico", "");
        return uuid.trim().isEmpty() ? null : uuid;
    }

    private static JSONArray convertirLista(ArrayList<?> valores) {
        JSONArray lista = new JSONArray();
        if (valores != null) {
            for (Object valor : valores) lista.put(valor);
        }
        return lista;
    }

    private static ArrayList<String> listaString(JSONArray datos) throws JSONException {
        ArrayList<String> lista = new ArrayList<>();
        for (int i = 0; i < datos.length(); i++) lista.add(datos.getString(i));
        return lista;
    }

    private static ArrayList<Integer> listaInt(JSONArray datos) throws JSONException {
        ArrayList<Integer> lista = new ArrayList<>();
        for (int i = 0; i < datos.length(); i++) lista.add(datos.getInt(i));
        return lista;
    }
}
