package sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import modelos.ResultadoPartida;

/** Una fila por participante de una partida finalizada. */
public class ResultadoPartidaDao implements AutoCloseable {

    private static final String TABLA = BaseDatosHelper.TABLA_RESULTADOS;
    private static final String[] COLUMNAS = {
            "id_resultado", "id_partida", "id_jugador", "posicion",
            "puntuacion_final", "orden_turno"
    };
    private final BaseDatosHelper helper;
    private final boolean propietarioHelper;

    public ResultadoPartidaDao(Context context) {
        this(new BaseDatosHelper(context.getApplicationContext()), true);
    }

    ResultadoPartidaDao(BaseDatosHelper helper) {
        this(helper, false);
    }

    private ResultadoPartidaDao(BaseDatosHelper helper, boolean propietarioHelper) {
        if (helper == null) {
            throw new IllegalArgumentException("El helper es obligatorio");
        }
        this.helper = helper;
        this.propietarioHelper = propietarioHelper;
    }

    public long insertar(ResultadoPartida resultado) {
        return insertar(helper.getWritableDatabase(), resultado);
    }

    long insertar(SQLiteDatabase db, ResultadoPartida resultado) {
        if (resultado == null || resultado.getId() != 0L
                || resultado.getIdPartida() <= 0 || resultado.getIdJugador() <= 0
                || resultado.getPosicion() <= 0 || resultado.getOrdenTurno() <= 0) {
            throw new IllegalArgumentException("Resultado nuevo incompleto o con ID existente");
        }
        ContentValues valores = new ContentValues();
        valores.put("id_partida", resultado.getIdPartida());
        valores.put("id_jugador", resultado.getIdJugador());
        valores.put("posicion", resultado.getPosicion());
        if (resultado.getPuntuacionFinal() == null) {
            valores.putNull("puntuacion_final");
        } else {
            valores.put("puntuacion_final", resultado.getPuntuacionFinal());
        }
        valores.put("orden_turno", resultado.getOrdenTurno());
        long id = db.insertOrThrow(TABLA, null, valores);
        resultado.setId(id);
        return id;
    }

    public ResultadoPartida obtenerPorId(long idResultado) {
        validarId(idResultado);
        try (Cursor cursor = helper.getReadableDatabase().query(TABLA, COLUMNAS,
                "id_resultado = ?", new String[]{String.valueOf(idResultado)},
                null, null, null, "1")) {
            return cursor.moveToFirst() ? convertir(cursor) : null;
        }
    }

    public ResultadoPartida obtenerPorPartidaYJugador(long idPartida, long idJugador) {
        validarId(idPartida);
        validarId(idJugador);
        try (Cursor cursor = helper.getReadableDatabase().query(TABLA, COLUMNAS,
                "id_partida = ? AND id_jugador = ?",
                new String[]{String.valueOf(idPartida), String.valueOf(idJugador)},
                null, null, null, "1")) {
            return cursor.moveToFirst() ? convertir(cursor) : null;
        }
    }

    /** Orden de clasificación, con empate resuelto visualmente por turno. */
    public List<ResultadoPartida> listarPorPartida(long idPartida) {
        validarId(idPartida);
        List<ResultadoPartida> salida = new ArrayList<>();
        try (Cursor cursor = helper.getReadableDatabase().query(TABLA, COLUMNAS,
                "id_partida = ?", new String[]{String.valueOf(idPartida)},
                null, null, "posicion ASC, orden_turno ASC")) {
            while (cursor.moveToNext()) {
                salida.add(convertir(cursor));
            }
        }
        return salida;
    }

    /** Permite a futuras estadísticas localizar las participaciones de un jugador. */
    public List<ResultadoPartida> listarPorJugador(long idJugador) {
        validarId(idJugador);
        List<ResultadoPartida> salida = new ArrayList<>();
        String sql = "SELECT r.* FROM " + TABLA + " r INNER JOIN "
                + BaseDatosHelper.TABLA_PARTIDAS + " p ON p.id_partida = r.id_partida"
                + " WHERE r.id_jugador = ? ORDER BY p.fecha DESC, p.id_partida DESC";
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(idJugador)})) {
            while (cursor.moveToNext()) {
                salida.add(convertir(cursor));
            }
        }
        return salida;
    }

    private ResultadoPartida convertir(Cursor cursor) {
        ResultadoPartida resultado = new ResultadoPartida();
        resultado.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id_resultado")));
        resultado.setIdPartida(cursor.getLong(cursor.getColumnIndexOrThrow("id_partida")));
        resultado.setIdJugador(cursor.getLong(cursor.getColumnIndexOrThrow("id_jugador")));
        resultado.setPosicion(cursor.getInt(cursor.getColumnIndexOrThrow("posicion")));
        int indicePuntuacion = cursor.getColumnIndexOrThrow("puntuacion_final");
        resultado.setPuntuacionFinal(cursor.isNull(indicePuntuacion)
                ? null : cursor.getInt(indicePuntuacion));
        resultado.setOrdenTurno(cursor.getInt(cursor.getColumnIndexOrThrow("orden_turno")));
        return resultado;
    }

    private static void validarId(long id) {
        if (id <= 0) {
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
