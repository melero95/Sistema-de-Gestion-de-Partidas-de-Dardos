package utilidades;

import android.content.Intent;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import modelos.ConfiguracionPartida;
import modelos.EstadisticasJugadorEnCurso;
import modelos.PartidaEnCurso;

/** Fotografía inmutable por contrato, transmitida a ResultadoActivity como JSON. */
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
        // Los guardados anteriores a la fase 06 carecen de UUID; uno nuevo será
        // estable desde el próximo guardado temporal, no antes.
        if (d.identificadorUnico == null || d.identificadorUnico.isEmpty())
            d.identificadorUnico = UUID.randomUUID().toString();
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
    public static DatosFinalizacionPartida desdeIntent(Intent intent) {
        String json = intent.getStringExtra(EXTRA_DATOS);
        if (json == null) return null;
        return GSON.fromJson(json, DatosFinalizacionPartida.class);
    }
    public ConfiguracionPartida validarConfiguracion() {
        if (identificadorUnico == null || identificadorUnico.trim().isEmpty()
                || estadisticas == null) throw new IllegalArgumentException("Faltan datos de finalización");
        return SerializadorConfiguracionPartida.desdeJson(configuracionJson);
    }
}
