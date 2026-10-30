package modelos;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Métricas agregadas para un modo concreto, NULL si la métrica no aplica. */
public final class MetricasModalidadDTO {
    public final String modo;
    public final long partidas;
    public final double puntosPorDardo;
    public final Map<String, Long> acumulados;
    public final Map<String, Integer> maximos;

    public MetricasModalidadDTO(String modo, long partidas, double puntosPorDardo,
                               Map<String, Long> acumulados, Map<String, Integer> maximos) {
        this.modo = modo;
        this.partidas = partidas;
        this.puntosPorDardo = puntosPorDardo;
        this.acumulados = Collections.unmodifiableMap(new LinkedHashMap<>(acumulados));
        this.maximos = Collections.unmodifiableMap(new LinkedHashMap<>(maximos));
    }
}
