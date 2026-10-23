package utilidades;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import modelos.ConfiguracionPartida;

/**
 * Formato histórico independiente del JSON de una partida en curso.
 * La versión es obligatoria. Las versiones desconocidas se rechazan
 * explícitamente para no reinterpretar silenciosamente datos antiguos.
 */
public final class SerializadorConfiguracionPartida {

    public static final int VERSION_ACTUAL = 1;
    private static final Gson GSON = new Gson();

    private SerializadorConfiguracionPartida() { }

    public static String aJson(ConfiguracionPartida config) {
        if (config == null) {
            throw new IllegalArgumentException("La configuración no puede ser nula");
        }
        JsonObject raiz = new JsonObject();
        raiz.addProperty("version", VERSION_ACTUAL);
        raiz.addProperty("modo", config.getModo());
        raiz.addProperty("rondas", config.getRondas());
        raiz.addProperty("dardosPorTurno", config.getDardosPorTurno());
        raiz.addProperty("cierreDoble", config.isCierreDoble());
        raiz.addProperty("mantenerMultiplicador", config.isMantenerMultiplicador());
        raiz.addProperty("ordenAleatorio", config.isOrdenAleatorio());

        JsonArray lista = new JsonArray();
        for (ConfiguracionPartida.Participante participante : config.getParticipantes()) {
            JsonObject item = new JsonObject();
            item.addProperty("idJugador", participante.getIdJugador());
            item.addProperty("nombre", participante.getNombre());
            item.addProperty("colorArgb", participante.getColorArgb());
            item.addProperty("ordenTurno", participante.getOrdenTurno());
            lista.add(item);
        }
        raiz.add("participantes", lista);
        return GSON.toJson(raiz);
    }

    public static ConfiguracionPartida desdeJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("No se ha recibido configuración JSON");
        }
        try {
            JsonElement raizLeida = JsonParser.parseString(json);
            if (!raizLeida.isJsonObject()) {
                throw new IllegalArgumentException("La configuración debe ser un objeto JSON");
            }
            JsonObject raiz = raizLeida.getAsJsonObject();
            int version = entero(raiz, "version");
            if (version != VERSION_ACTUAL) {
                throw new IllegalArgumentException("Versión de configuración no compatible: " + version);
            }

            JsonElement listaLeida = obligatorio(raiz, "participantes");
            if (!listaLeida.isJsonArray()) {
                throw new IllegalArgumentException("participantes debe ser una lista");
            }
            List<ConfiguracionPartida.Participante> participantes = new ArrayList<>();
            JsonArray lista = listaLeida.getAsJsonArray();
            for (int i = 0; i < lista.size(); i++) {
                JsonElement item = lista.get(i);
                if (!item.isJsonObject()) {
                    throw new IllegalArgumentException("Participante no válido en posición " + i);
                }
                JsonObject p = item.getAsJsonObject();
                participantes.add(new ConfiguracionPartida.Participante(
                        largo(p, "idJugador"), texto(p, "nombre"),
                        entero(p, "colorArgb"), entero(p, "ordenTurno")));
            }
            return new ConfiguracionPartida(
                    texto(raiz, "modo"), entero(raiz, "rondas"),
                    entero(raiz, "dardosPorTurno"), booleano(raiz, "cierreDoble"),
                    booleano(raiz, "mantenerMultiplicador"),
                    booleano(raiz, "ordenAleatorio"), participantes);
        } catch (JsonSyntaxException | IllegalStateException | NumberFormatException ex) {
            throw new IllegalArgumentException("La configuración JSON es inválida", ex);
        }
    }

    private static JsonElement obligatorio(JsonObject objeto, String nombre) {
        JsonElement valor = objeto.get(nombre);
        if (valor == null || valor.isJsonNull()) {
            throw new IllegalArgumentException("Falta el campo " + nombre);
        }
        return valor;
    }

    private static String texto(JsonObject objeto, String nombre) {
        JsonElement valor = obligatorio(objeto, nombre);
        if (!valor.isJsonPrimitive() || !valor.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("No es un texto válido: " + nombre);
        }
        return valor.getAsString();
    }

    private static boolean booleano(JsonObject objeto, String nombre) {
        JsonElement valor = obligatorio(objeto, nombre);
        if (!valor.isJsonPrimitive() || !valor.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("No es un booleano válido: " + nombre);
        }
        return valor.getAsBoolean();
    }

    private static int entero(JsonObject objeto, String nombre) {
        BigDecimal decimal = numero(objeto, nombre);
        try {
            return decimal.intValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Valor entero fuera de rango: " + nombre, ex);
        }
    }

    private static long largo(JsonObject objeto, String nombre) {
        BigDecimal decimal = numero(objeto, nombre);
        try {
            return decimal.longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Valor long fuera de rango: " + nombre, ex);
        }
    }

    private static BigDecimal numero(JsonObject objeto, String nombre) {
        JsonElement valor = obligatorio(objeto, nombre);
        if (!valor.isJsonPrimitive()) {
            throw new IllegalArgumentException("No es un número: " + nombre);
        }
        JsonPrimitive primitivo = valor.getAsJsonPrimitive();
        if (!primitivo.isNumber()) {
            throw new IllegalArgumentException("No es un número: " + nombre);
        }
        return new BigDecimal(primitivo.getAsString());
    }
}
