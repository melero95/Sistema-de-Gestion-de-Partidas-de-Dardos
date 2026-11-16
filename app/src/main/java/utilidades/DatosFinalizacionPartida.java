package utilidades;

import android.content.Intent;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import modelos.ConfiguracionPartida;
import modelos.EstadisticasJugadorEnCurso;
import modelos.PartidaEnCurso;

/** Datos finales versionados por UUID para guardar sin duplicar registros. */
public final class DatosFinalizacionPartida {
    public static final String EXTRA_DATOS = "datos_finalizacion_fase06";
    private static final Gson GSON = new Gson();
    public String identificadorUnico;
    public String configuracionJson;
    public ArrayList<EstadisticasJugadorEnCurso> estadisticas;

    public static DatosFinalizacionPartida nueva(Intent intent) {
        DatosFinalizacionPartida d = new DatosFinalizacionPartida();
        d.identificadorUnico = UUID.randomUUID().toString();
        d.configuracionJson = intent.getStringExtra("configuracionJson");
        return d;
    }

    public static DatosFinalizacionPartida restaurar(PartidaEnCurso estado) {
        DatosFinalizacionPartida d = new DatosFinalizacionPartida();
        d.identificadorUnico = estado.getIdentificadorUnico();
        d.configuracionJson = estado.getConfiguracionJson();
        if (d.identificadorUnico == null || d.identificadorUnico.isEmpty()) {
            d.identificadorUnico = UUID.randomUUID().toString();
        }
        return d;
    }

    public void guardarMetadatos(PartidaEnCurso destino) {
        destino.setIdentificadorUnico(identificadorUnico);
        destino.setConfiguracionJson(configuracionJson);
    }

    public void adjuntar(Intent resultado, List<EstadisticasJugadorEnCurso> lista) {
        estadisticas = EstadisticasJugadorEnCurso.copiarLista(lista);
        resultado.putExtra(EXTRA_DATOS, GSON.toJson(this));
    }

    /**
     * La versión minificada puede restaurar una lista de mapas si R8 ha
     * eliminado la firma genérica. Reconstruir cada DTO de forma explícita
     * evita el ClassCastException LinkedHashMap -> EstadisticasJugadorEnCurso.
     */
    public static DatosFinalizacionPartida desdeIntent(Intent intent) {
        String json = intent.getStringExtra(EXTRA_DATOS);
        if (json == null) return null;
        DatosFinalizacionPartida datos = GSON.fromJson(json, DatosFinalizacionPartida.class);
        if (datos == null || datos.estadisticas == null) return datos;

        ArrayList<EstadisticasJugadorEnCurso> normalizadas = new ArrayList<>();
        for (Object elemento : datos.estadisticas) {
            EstadisticasJugadorEnCurso estadistica;
            if (elemento instanceof EstadisticasJugadorEnCurso) {
                estadistica = (EstadisticasJugadorEnCurso) elemento;
            } else if (elemento instanceof Map) {
                estadistica = GSON.fromJson(GSON.toJson(elemento),
                        EstadisticasJugadorEnCurso.class);
            } else {
                throw new IllegalArgumentException("Estadística final con tipo incompatible");
            }
            if (estadistica == null) {
                throw new IllegalArgumentException("Estadística final vacía");
            }
            normalizadas.add(estadistica);
        }
        datos.estadisticas = normalizadas;
        return datos;
    }

    public ConfiguracionPartida validarConfiguracion() {
        if (identificadorUnico == null || identificadorUnico.trim().isEmpty()
                || estadisticas == null) {
            throw new IllegalArgumentException("Faltan datos de finalización");
        }
        return SerializadorConfiguracionPartida.desdeJson(configuracionJson);
    }
}
