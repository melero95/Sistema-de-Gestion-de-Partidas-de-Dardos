package sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import modelos.EstadisticaPartida;

/**
 * Acceso a estadísticas por participación. Las columnas de otros modos quedan
 * en NULL; el valor 0 significa que el contador aplica pero no ocurrió.
 */
public class EstadisticaPartidaDao implements AutoCloseable {

    private static final String TABLA = BaseDatosHelper.TABLA_ESTADISTICAS;
    private static final String[] COLUMNAS = {
            "id_estadistica", "id_resultado",
            "dardos_lanzados",
            "turnos_jugados",
            "simples",
            "dobles",
            "triples",
            "fallos",
            "busts",
            "puntos_validos",
            "mejor_turno",
            "turnos_180",
            "dardos_cierre",
            "marcas_validas",
            "objetivos_cerrados",
            "puntos_generados",
            "puntos_recibidos",
            "rondas_penalizadas",
            "mejor_ronda_dd",
            "puntos_conseguidos_dd",
            "objetivos_superados",
            "aciertos_objetivo",
            "partida_completada",
            "puntos_validos_shanghai",
            "mejor_ronda_shanghai",
            "rondas_con_puntos"
    };

    private final BaseDatosHelper helper;
    private final boolean propietarioHelper;

    public EstadisticaPartidaDao(Context context) {
        this(new BaseDatosHelper(context.getApplicationContext()), true);
    }

    EstadisticaPartidaDao(BaseDatosHelper helper) {
        this(helper, false);
    }

    private EstadisticaPartidaDao(BaseDatosHelper helper, boolean propietarioHelper) {
        if (helper == null) {
            throw new IllegalArgumentException("El helper es obligatorio");
        }
        this.helper = helper;
        this.propietarioHelper = propietarioHelper;
    }

    public long insertar(EstadisticaPartida estadistica) {
        return insertar(helper.getWritableDatabase(), estadistica);
    }

    long insertar(SQLiteDatabase db, EstadisticaPartida estadistica) {
        if (estadistica == null) {
            throw new IllegalArgumentException("La estadística es obligatoria");
        }
        long id = insertar(db, estadistica, estadistica.getIdResultado());
        estadistica.setId(id);
        return id;
    }

    /** Inserta con un resultado provisional sin modificar el modelo de origen. */
    long insertar(SQLiteDatabase db, EstadisticaPartida estadistica, long idResultado) {
        if (estadistica == null || estadistica.getId() != 0L || idResultado <= 0L) {
            throw new IllegalArgumentException("Se requiere una estadística nueva con resultado válido");
        }
        ContentValues valores = new ContentValues();
        valores.put("id_resultado", idResultado);
        valores.put("dardos_lanzados", estadistica.getDardosLanzados());
        valores.put("turnos_jugados", estadistica.getTurnosJugados());
        valores.put("simples", estadistica.getSimples());
        valores.put("dobles", estadistica.getDobles());
        valores.put("triples", estadistica.getTriples());
        valores.put("fallos", estadistica.getFallos());
        if (estadistica.getBusts() == null) {
            valores.putNull("busts");
        } else {
            valores.put("busts", estadistica.getBusts());
        }
        if (estadistica.getPuntosValidos() == null) {
            valores.putNull("puntos_validos");
        } else {
            valores.put("puntos_validos", estadistica.getPuntosValidos());
        }
        if (estadistica.getMejorTurno() == null) {
            valores.putNull("mejor_turno");
        } else {
            valores.put("mejor_turno", estadistica.getMejorTurno());
        }
        if (estadistica.getTurnos180() == null) {
            valores.putNull("turnos_180");
        } else {
            valores.put("turnos_180", estadistica.getTurnos180());
        }
        if (estadistica.getDardosCierre() == null) {
            valores.putNull("dardos_cierre");
        } else {
            valores.put("dardos_cierre", estadistica.getDardosCierre());
        }
        if (estadistica.getMarcasValidas() == null) {
            valores.putNull("marcas_validas");
        } else {
            valores.put("marcas_validas", estadistica.getMarcasValidas());
        }
        if (estadistica.getObjetivosCerrados() == null) {
            valores.putNull("objetivos_cerrados");
        } else {
            valores.put("objetivos_cerrados", estadistica.getObjetivosCerrados());
        }
        if (estadistica.getPuntosGenerados() == null) {
            valores.putNull("puntos_generados");
        } else {
            valores.put("puntos_generados", estadistica.getPuntosGenerados());
        }
        if (estadistica.getPuntosRecibidos() == null) {
            valores.putNull("puntos_recibidos");
        } else {
            valores.put("puntos_recibidos", estadistica.getPuntosRecibidos());
        }
        if (estadistica.getRondasPenalizadas() == null) {
            valores.putNull("rondas_penalizadas");
        } else {
            valores.put("rondas_penalizadas", estadistica.getRondasPenalizadas());
        }
        if (estadistica.getMejorRondaDd() == null) {
            valores.putNull("mejor_ronda_dd");
        } else {
            valores.put("mejor_ronda_dd", estadistica.getMejorRondaDd());
        }
        if (estadistica.getPuntosConseguidosDd() == null) {
            valores.putNull("puntos_conseguidos_dd");
        } else {
            valores.put("puntos_conseguidos_dd", estadistica.getPuntosConseguidosDd());
        }
        if (estadistica.getObjetivosSuperados() == null) {
            valores.putNull("objetivos_superados");
        } else {
            valores.put("objetivos_superados", estadistica.getObjetivosSuperados());
        }
        if (estadistica.getAciertosObjetivo() == null) {
            valores.putNull("aciertos_objetivo");
        } else {
            valores.put("aciertos_objetivo", estadistica.getAciertosObjetivo());
        }
        if (estadistica.getPartidaCompletada() == null) {
            valores.putNull("partida_completada");
        } else {
            valores.put("partida_completada", estadistica.getPartidaCompletada() ? 1 : 0);
        }
        if (estadistica.getPuntosValidosShanghai() == null) {
            valores.putNull("puntos_validos_shanghai");
        } else {
            valores.put("puntos_validos_shanghai", estadistica.getPuntosValidosShanghai());
        }
        if (estadistica.getMejorRondaShanghai() == null) {
            valores.putNull("mejor_ronda_shanghai");
        } else {
            valores.put("mejor_ronda_shanghai", estadistica.getMejorRondaShanghai());
        }
        if (estadistica.getRondasConPuntos() == null) {
            valores.putNull("rondas_con_puntos");
        } else {
            valores.put("rondas_con_puntos", estadistica.getRondasConPuntos());
        }
        return db.insertOrThrow(TABLA, null, valores);
    }

    public EstadisticaPartida obtenerPorId(long idEstadistica) {
        validarId(idEstadistica);
        try (Cursor cursor = helper.getReadableDatabase().query(TABLA, COLUMNAS,
                "id_estadistica = ?", new String[]{String.valueOf(idEstadistica)},
                null, null, null, "1")) {
            return cursor.moveToFirst() ? convertir(cursor) : null;
        }
    }

    public EstadisticaPartida obtenerPorResultado(long idResultado) {
        validarId(idResultado);
        try (Cursor cursor = helper.getReadableDatabase().query(TABLA, COLUMNAS,
                "id_resultado = ?", new String[]{String.valueOf(idResultado)},
                null, null, null, "1")) {
            return cursor.moveToFirst() ? convertir(cursor) : null;
        }
    }

    /** Devuelve las estadísticas en el orden de clasificación final. */
    public List<EstadisticaPartida> listarPorPartida(long idPartida) {
        validarId(idPartida);
        List<EstadisticaPartida> salida = new ArrayList<>();
        String sql = "SELECT e.* FROM " + TABLA + " e INNER JOIN "
                + BaseDatosHelper.TABLA_RESULTADOS
                + " r ON r.id_resultado = e.id_resultado"
                + " WHERE r.id_partida = ? ORDER BY r.posicion ASC, r.orden_turno ASC";
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(idPartida)})) {
            while (cursor.moveToNext()) {
                salida.add(convertir(cursor));
            }
        }
        return salida;
    }

    private EstadisticaPartida convertir(Cursor cursor) {
        EstadisticaPartida estadistica = new EstadisticaPartida();
        estadistica.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id_estadistica")));
        estadistica.setIdResultado(cursor.getLong(cursor.getColumnIndexOrThrow("id_resultado")));
        estadistica.setDardosLanzados(cursor.getInt(cursor.getColumnIndexOrThrow("dardos_lanzados")));
        estadistica.setTurnosJugados(cursor.getInt(cursor.getColumnIndexOrThrow("turnos_jugados")));
        estadistica.setSimples(cursor.getInt(cursor.getColumnIndexOrThrow("simples")));
        estadistica.setDobles(cursor.getInt(cursor.getColumnIndexOrThrow("dobles")));
        estadistica.setTriples(cursor.getInt(cursor.getColumnIndexOrThrow("triples")));
        estadistica.setFallos(cursor.getInt(cursor.getColumnIndexOrThrow("fallos")));
        int idxBusts = cursor.getColumnIndexOrThrow("busts");
        estadistica.setBusts(cursor.isNull(idxBusts) ? null : cursor.getInt(idxBusts));
        int idxPuntosValidos = cursor.getColumnIndexOrThrow("puntos_validos");
        estadistica.setPuntosValidos(cursor.isNull(idxPuntosValidos) ? null : cursor.getInt(idxPuntosValidos));
        int idxMejorTurno = cursor.getColumnIndexOrThrow("mejor_turno");
        estadistica.setMejorTurno(cursor.isNull(idxMejorTurno) ? null : cursor.getInt(idxMejorTurno));
        int idxTurnos180 = cursor.getColumnIndexOrThrow("turnos_180");
        estadistica.setTurnos180(cursor.isNull(idxTurnos180) ? null : cursor.getInt(idxTurnos180));
        int idxDardosCierre = cursor.getColumnIndexOrThrow("dardos_cierre");
        estadistica.setDardosCierre(cursor.isNull(idxDardosCierre) ? null : cursor.getInt(idxDardosCierre));
        int idxMarcasValidas = cursor.getColumnIndexOrThrow("marcas_validas");
        estadistica.setMarcasValidas(cursor.isNull(idxMarcasValidas) ? null : cursor.getInt(idxMarcasValidas));
        int idxObjetivosCerrados = cursor.getColumnIndexOrThrow("objetivos_cerrados");
        estadistica.setObjetivosCerrados(cursor.isNull(idxObjetivosCerrados) ? null : cursor.getInt(idxObjetivosCerrados));
        int idxPuntosGenerados = cursor.getColumnIndexOrThrow("puntos_generados");
        estadistica.setPuntosGenerados(cursor.isNull(idxPuntosGenerados) ? null : cursor.getInt(idxPuntosGenerados));
        int idxPuntosRecibidos = cursor.getColumnIndexOrThrow("puntos_recibidos");
        estadistica.setPuntosRecibidos(cursor.isNull(idxPuntosRecibidos) ? null : cursor.getInt(idxPuntosRecibidos));
        int idxRondasPenalizadas = cursor.getColumnIndexOrThrow("rondas_penalizadas");
        estadistica.setRondasPenalizadas(cursor.isNull(idxRondasPenalizadas) ? null : cursor.getInt(idxRondasPenalizadas));
        int idxMejorRondaDd = cursor.getColumnIndexOrThrow("mejor_ronda_dd");
        estadistica.setMejorRondaDd(cursor.isNull(idxMejorRondaDd) ? null : cursor.getInt(idxMejorRondaDd));
        int idxPuntosConseguidosDd = cursor.getColumnIndexOrThrow("puntos_conseguidos_dd");
        estadistica.setPuntosConseguidosDd(cursor.isNull(idxPuntosConseguidosDd) ? null : cursor.getInt(idxPuntosConseguidosDd));
        int idxObjetivosSuperados = cursor.getColumnIndexOrThrow("objetivos_superados");
        estadistica.setObjetivosSuperados(cursor.isNull(idxObjetivosSuperados) ? null : cursor.getInt(idxObjetivosSuperados));
        int idxAciertosObjetivo = cursor.getColumnIndexOrThrow("aciertos_objetivo");
        estadistica.setAciertosObjetivo(cursor.isNull(idxAciertosObjetivo) ? null : cursor.getInt(idxAciertosObjetivo));
        int idxPartidaCompletada = cursor.getColumnIndexOrThrow("partida_completada");
        estadistica.setPartidaCompletada(cursor.isNull(idxPartidaCompletada) ? null : cursor.getInt(idxPartidaCompletada) == 1);
        int idxPuntosValidosShanghai = cursor.getColumnIndexOrThrow("puntos_validos_shanghai");
        estadistica.setPuntosValidosShanghai(cursor.isNull(idxPuntosValidosShanghai) ? null : cursor.getInt(idxPuntosValidosShanghai));
        int idxMejorRondaShanghai = cursor.getColumnIndexOrThrow("mejor_ronda_shanghai");
        estadistica.setMejorRondaShanghai(cursor.isNull(idxMejorRondaShanghai) ? null : cursor.getInt(idxMejorRondaShanghai));
        int idxRondasConPuntos = cursor.getColumnIndexOrThrow("rondas_con_puntos");
        estadistica.setRondasConPuntos(cursor.isNull(idxRondasConPuntos) ? null : cursor.getInt(idxRondasConPuntos));
        return estadistica;
    }

    private static void validarId(long id) {
        if (id <= 0L) {
            throw new IllegalArgumentException("El ID debe ser positivo");
        }
    }

    @Override
    public void close() {
        if (propietarioHelper) {
            helper.close();
        }
    }
}
