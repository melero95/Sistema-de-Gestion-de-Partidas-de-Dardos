package sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelos.EstadisticasJugadorDTO;
import modelos.EstadisticasResumenDTO;
import modelos.MetricasModalidadDTO;
import modelos.RecordEstadisticoDTO;

/**
 * Fase 08.1: consultas de SOLO LECTURA, sin contadores duplicados.
 * Las partidas ocultas SIEMPRE participan: ninguna consulta filtra p.oculta.
 * Los jugadores inactivos SIEMPRE participan en las clasificaciones.
 */
public final class ConsultasEstadisticas implements AutoCloseable {
    private static final String P = BaseDatosHelper.TABLA_PARTIDAS;
    private static final String J = BaseDatosHelper.TABLA_JUGADORES;
    private static final String R = BaseDatosHelper.TABLA_RESULTADOS;
    private static final String E = BaseDatosHelper.TABLA_ESTADISTICAS;
    private static final int MINIMO_PARTIDAS = 10;

    private final BaseDatosHelper helper;
    private final boolean propietario;

    public ConsultasEstadisticas(Context context) {
        this(new BaseDatosHelper(context.getApplicationContext()), true);
    }
    ConsultasEstadisticas(BaseDatosHelper helper) { this(helper, false); }
    private ConsultasEstadisticas(BaseDatosHelper helper, boolean propietario) {
        if (helper == null) throw new IllegalArgumentException("BaseDatosHelper obligatorio");
        this.helper = helper;
        this.propietario = propietario;
    }

    private SQLiteDatabase db() { return helper.getReadableDatabase(); }
    private long escalar(String sql, String[] args) {
        try (Cursor c = db().rawQuery(sql, args)) {
            return c.moveToFirst() ? c.getLong(0) : 0;
        }
    }
    private List<EstadisticasResumenDTO.Modalidad> conteoModos(String condicion, String[] args) {
        List<EstadisticasResumenDTO.Modalidad> out = new ArrayList<>();
        String sql = "SELECT p.modo, COUNT(*) n FROM " + P + " p " +
                (condicion == null ? "" : "JOIN " + R + " r ON r.id_partida=p.id_partida WHERE " + condicion) +
                " GROUP BY p.modo ORDER BY n DESC, p.modo COLLATE NOCASE ASC";
        try (Cursor c = db().rawQuery(sql, args)) {
            while (c.moveToNext()) out.add(new EstadisticasResumenDTO.Modalidad(c.getString(0), c.getLong(1)));
        }
        return out;
    }
    public EstadisticasResumenDTO resumenGeneral() {
        List<EstadisticasResumenDTO.Modalidad> modalidades = conteoModos(null, null);
        return new EstadisticasResumenDTO(
                escalar("SELECT COUNT(*) FROM " + P, null),
                escalar("SELECT COUNT(*) FROM " + J, null),
                modalidades.isEmpty() ? null : modalidades.get(0).modo, modalidades);
    }

    public EstadisticasJugadorDTO resumenJugador(long idJugador) {
        validarJugador(idJugador);
        String nombre;
        int color;
        boolean activo;
        try (Cursor c = db().rawQuery("SELECT nombre,color,activo FROM " + J + " WHERE id_jugador=?",
                new String[]{String.valueOf(idJugador)})) {
            if (!c.moveToFirst()) return null;
            nombre = c.getString(0);
            color = c.getInt(1);
            activo = c.getInt(2) == 1;
        }
        long partidas = 0, victorias = 0, segundos = 0, terceros = 0;
        String sql = "SELECT COUNT(*), COALESCE(SUM(CASE WHEN p.id_ganador=r.id_jugador THEN 1 ELSE 0 END),0)," +
                " COALESCE(SUM(CASE WHEN r.posicion=2 THEN 1 ELSE 0 END),0)," +
                " COALESCE(SUM(CASE WHEN r.posicion=3 THEN 1 ELSE 0 END),0)" +
                " FROM " + R + " r JOIN " + P + " p ON p.id_partida=r.id_partida WHERE r.id_jugador=?";
        String[] args = {String.valueOf(idJugador)};
        try (Cursor c = db().rawQuery(sql, args)) {
            if (c.moveToFirst()) { partidas=c.getLong(0); victorias=c.getLong(1); segundos=c.getLong(2); terceros=c.getLong(3); }
        }
        List<EstadisticasJugadorDTO.UltimaPartida> recientes = new ArrayList<>();
        sql = "SELECT p.id_partida,p.fecha,p.modo,r.posicion," +
                "CASE WHEN p.id_ganador=r.id_jugador THEN 1 ELSE 0 END" +
                " FROM " + R + " r JOIN " + P + " p ON p.id_partida=r.id_partida" +
                " WHERE r.id_jugador=? ORDER BY p.fecha DESC,p.id_partida DESC LIMIT 5";
        try (Cursor c = db().rawQuery(sql, args)) {
            while (c.moveToNext()) recientes.add(new EstadisticasJugadorDTO.UltimaPartida(
                    c.getLong(0), c.getLong(1), c.getString(2), c.getInt(3), c.getInt(4)==1));
        }
        return new EstadisticasJugadorDTO(idJugador, nombre, color,
                activo, partidas, victorias, segundos, terceros,
                conteoModos("r.id_jugador=?", args), recientes);
    }

    /**
     * Datos específicos por jugador y modalidad. Se devuelve un mapa de métricas por nombres de columnas.
     * Evitamos AVG(media): los puntos/dardo proceden de SUM(puntos)/SUM(dardos).
     * Para Cricket y Cut Throat, y para los restantes modos, solo se incluyen sus campos aplicables.
     */
    public MetricasModalidadDTO metricasJugador(long idJugador, String modo) {
        validarJugador(idJugador);
        String[] sumas;
        String[] maximos;
        switch (modo == null ? "" : modo) {
            case "301": case "501":
                sumas = new String[]{"busts", "puntos_validos", "turnos_180", "dardos_cierre", "cierres_realizados", "dardos_lanzados"};
                maximos = new String[]{"mejor_turno"}; break;
            case "Cricket": case "Cut Throat Cricket":
                sumas = new String[]{"marcas_validas", "objetivos_cerrados", "puntos_generados", "puntos_recibidos"};
                maximos = new String[0]; break;
            case "Double Down":
                sumas = new String[]{"puntos_conseguidos_dd", "rondas_penalizadas"};
                maximos = new String[]{"mejor_ronda_dd"}; break;
            case "Around the Clock":
                sumas = new String[]{"objetivos_superados", "aciertos_objetivo", "partida_completada"};
                maximos = new String[0]; break;
            case "Shanghai":
                sumas = new String[]{"puntos_validos_shanghai", "rondas_con_puntos"};
                maximos = new String[]{"mejor_ronda_shanghai"}; break;
            default: throw new IllegalArgumentException("Modo no reconocido: " + modo);
        }
        // Columnas construidas solo desde listas constantes internas, nunca desde texto de usuario.
        StringBuilder query = new StringBuilder("SELECT COUNT(*), SUM(e.id_estadistica IS NOT NULL)");
        for (String columna : sumas) {
            if ("cierres_realizados".equals(columna))
                query.append(",SUM(CASE WHEN e.dardos_cierre > 0 THEN 1 ELSE 0 END)");
            else query.append(",SUM(e.").append(columna).append(")");
        }
        for (String columna : maximos) query.append(",MAX(e.").append(columna).append(")");
        query.append(" FROM ").append(R).append(" r JOIN ").append(P)
                .append(" p ON p.id_partida=r.id_partida LEFT JOIN ").append(E)
                .append(" e ON e.id_resultado=r.id_resultado WHERE r.id_jugador=? AND p.modo=?");
        Map<String,Long> totales = new LinkedHashMap<>();
        Map<String,Integer> mejores = new LinkedHashMap<>();
        long partidas = 0;
        try (Cursor c = db().rawQuery(query.toString(), new String[]{String.valueOf(idJugador), modo})) {
            if (c.moveToFirst()) {
                partidas = c.getLong(0);
                int index=2;
                for (String columna:sumas) {
                    if (!c.isNull(index)) totales.put(columna, c.getLong(index));
                    index++;
                }
                for (String columna:maximos) {
                    if (!c.isNull(index)) mejores.put(columna, c.getInt(index));
                    index++;
                }
            }
        }
        long dardos = totales.containsKey("dardos_lanzados") ? totales.get("dardos_lanzados") : 0;
        long puntos = totales.containsKey("puntos_validos") ? totales.get("puntos_validos") : 0;
        return new MetricasModalidadDTO(modo, partidas, dardos == 0 ? 0d : (double)puntos/dardos,
                totales, mejores);
    }

    /** Registros empatados en el primer puesto se conservan; SIN filtrar jugadores inactivos. */
    public List<RecordEstadisticoDTO> recordMasVictorias() {
        return ranking("COUNT(p.id_ganador)", "JOIN " + P + " p ON p.id_ganador=j.id_jugador", null, null, false);
    }
    public List<RecordEstadisticoDTO> recordMasPartidas() {
        return ranking("COUNT(r.id_resultado)", "JOIN " + R + " r ON r.id_jugador=j.id_jugador", null, null, false);
    }
    public List<RecordEstadisticoDTO> recordPorcentajeVictorias() {
        String joins = "JOIN " + R + " r ON r.id_jugador=j.id_jugador JOIN " + P + " p ON p.id_partida=r.id_partida";
        return ranking("100.0*SUM(CASE WHEN p.id_ganador=j.id_jugador THEN 1 ELSE 0 END)/COUNT(*)",
                joins, "HAVING COUNT(*)>=CAST(? AS INTEGER)", new String[]{String.valueOf(MINIMO_PARTIDAS)}, false);
    }
    public List<RecordEstadisticoDTO> recordMas180() {
        String joins="JOIN " + R + " r ON r.id_jugador=j.id_jugador JOIN " + P + " p ON p.id_partida=r.id_partida" +
                " JOIN " + E + " e ON e.id_resultado=r.id_resultado";
        return ranking("SUM(e.turnos_180)", joins,
                "WHERE p.modo IN ('301','501') HAVING COUNT(e.turnos_180)>0", null, false);
    }
    /** Mayor puntuación final: se limita a Double Down y Shanghai. */
    public List<RecordEstadisticoDTO> recordMayorPuntuacion(String modo) {
        if (!"Double Down".equals(modo) && !"Shanghai".equals(modo))
            throw new IllegalArgumentException("No se puede comparar puntuación final en este modo");
        String joins="JOIN " + R + " r ON r.id_jugador=j.id_jugador JOIN " + P + " p ON p.id_partida=r.id_partida";
        return ranking("MAX(r.puntuacion_final)", joins,
                "WHERE p.modo=? HAVING COUNT(r.puntuacion_final)>0", new String[]{modo}, true);
    }

    private List<RecordEstadisticoDTO> ranking(String expresion, String joins,
                                                String filtros, String[] argumentos, boolean conModo) {
        // SQLite exige WHERE -> GROUP BY -> HAVING -> ORDER BY.
        // Los filtros que reciben estas consultas son constantes internas.
        String where = "";
        String having = "";
        if (filtros != null && !filtros.trim().isEmpty()) {
            String filtro = filtros.trim();
            int indiceHaving = filtro.indexOf("HAVING ");
            if (indiceHaving >= 0) {
                where = filtro.substring(0, indiceHaving).trim();
                having = filtro.substring(indiceHaving).trim();
            } else {
                where = filtro;
            }
        }
        String columnaPartida = joins.contains(" p ON") ? "p.id_partida" : "r.id_partida";
        String sql = "SELECT j.id_jugador,j.nombre,j.color," + expresion
                + " AS valor,COUNT(DISTINCT " + columnaPartida + ") AS partidas"
                + " FROM " + J + " j " + joins
                + (where.isEmpty() ? "" : " " + where)
                + " GROUP BY j.id_jugador"
                + (having.isEmpty() ? "" : " " + having)
                + " ORDER BY valor DESC,j.nombre COLLATE NOCASE ASC";
        List<RecordEstadisticoDTO> lista = new ArrayList<>();
        try (Cursor c=db().rawQuery(sql, argumentos)) {
            Double marca = null;
            while (c.moveToNext()) {
                if (c.isNull(3)) continue;
                double valor = c.getDouble(3);
                if (marca != null && Double.compare(marca,valor)!=0) break;
                marca = valor;
                lista.add(new RecordEstadisticoDTO(c.getLong(0),c.getString(1),c.getInt(2),
                        conModo && argumentos!=null ? argumentos[0] : null,valor,c.getLong(4)));
            }
        }
        return lista;
    }
    private static void validarJugador(long id) {
        if (id<=0) throw new IllegalArgumentException("ID de jugador inválido");
    }
    @Override public void close() { if (propietario) helper.close(); }
}
