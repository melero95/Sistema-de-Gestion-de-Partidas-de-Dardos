package sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import modelos.Partida;

/**
 * Acceso a PARTIDAS. Solo registra partidas finalizadas.
 * No expone borrado ni SQL a las Activities.
 */
public class PartidaDao implements AutoCloseable {

    private static final String TABLA = BaseDatosHelper.TABLA_PARTIDAS;
    private static final String[] COLUMNAS = {
            "id_partida", "identificador_unico", "fecha", "modo",
            "rondas_configuradas", "rondas_jugadas", "numero_jugadores",
            "id_ganador", "motivo_finalizacion", "configuracion_json"
    };

    private final BaseDatosHelper helper;
    private final boolean propietarioHelper;

    public PartidaDao(Context context) {
        this(new BaseDatosHelper(context.getApplicationContext()), true);
    }

    // Permite usar el MISMO helper/SQLiteDatabase en un guardado transaccional.
    PartidaDao(BaseDatosHelper helper) {
        this(helper, false);
    }

    private PartidaDao(BaseDatosHelper helper, boolean propietarioHelper) {
        if (helper == null) {
            throw new IllegalArgumentException("El helper es obligatorio");
        }
        this.helper = helper;
        this.propietarioHelper = propietarioHelper;
    }

    /** Inserción simple. Para insertar con resultados usar RepositorioPartidas. */
    public long insertar(Partida partida) {
        return insertar(helper.getWritableDatabase(), partida);
    }

    /** Usado por el repositorio dentro de una transacción ya abierta. */
    long insertar(SQLiteDatabase db, Partida partida) {
        if (partida == null || partida.getId() != 0L) {
            throw new IllegalArgumentException("Se requiere una partida nueva con ID 0");
        }
        ContentValues valores = new ContentValues();
        valores.put("identificador_unico", partida.getIdentificadorUnico());
        valores.put("fecha", partida.getFecha());
        valores.put("modo", partida.getModo());
        valores.put("rondas_configuradas", partida.getRondasConfiguradas());
        valores.put("rondas_jugadas", partida.getRondasJugadas());
        valores.put("numero_jugadores", partida.getNumeroJugadores());
        if (partida.getIdGanador() == null) {
            valores.putNull("id_ganador");
        } else {
            valores.put("id_ganador", partida.getIdGanador());
        }
        valores.put("motivo_finalizacion", partida.getMotivoFinalizacion());
        valores.put("configuracion_json", partida.getConfiguracionJson());

        long id = db.insertOrThrow(TABLA, null, valores);
        partida.setId(id);
        return id;
    }

    /** Devuelve null cuando no existe. */
    public Partida obtenerPorId(long idPartida) {
        validarId(idPartida);
        return consultarUna(helper.getReadableDatabase(), "id_partida = ?",
                new String[]{String.valueOf(idPartida)});
    }

    /** El UUID permite detectar intentos repetidos de guardado. */
    public Partida obtenerPorIdentificadorUnico(String identificadorUnico) {
        return obtenerPorIdentificadorUnico(helper.getReadableDatabase(), identificadorUnico);
    }

    Partida obtenerPorIdentificadorUnico(SQLiteDatabase db, String identificadorUnico) {
        if (identificadorUnico == null || identificadorUnico.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador único es obligatorio");
        }
        return consultarUna(db, "identificador_unico = ?",
                new String[]{identificadorUnico.trim()});
    }

    /** De más reciente a más antigua; desempate por ID. */
    public List<Partida> listarRecientes() {
        return consultarLista(helper.getReadableDatabase(), null, null);
    }

    public List<Partida> listarPorModo(String modo) {
        if (modo == null || modo.trim().isEmpty()) {
            throw new IllegalArgumentException("El modo es obligatorio");
        }
        return consultarLista(helper.getReadableDatabase(), "modo = ?",
                new String[]{modo.trim()});
    }

    private Partida consultarUna(SQLiteDatabase db, String seleccion, String[] argumentos) {
        try (Cursor cursor = db.query(TABLA, COLUMNAS, seleccion, argumentos,
                null, null, null, "1")) {
            return cursor.moveToFirst() ? convertir(cursor) : null;
        }
    }

    private List<Partida> consultarLista(SQLiteDatabase db, String seleccion, String[] argumentos) {
        List<Partida> salida = new ArrayList<>();
        try (Cursor cursor = db.query(TABLA, COLUMNAS, seleccion, argumentos,
                null, null, "fecha DESC, id_partida DESC")) {
            while (cursor.moveToNext()) {
                salida.add(convertir(cursor));
            }
        }
        return salida;
    }

    private Partida convertir(Cursor cursor) {
        Partida partida = new Partida();
        partida.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id_partida")));
        partida.setIdentificadorUnico(cursor.getString(cursor.getColumnIndexOrThrow("identificador_unico")));
        partida.setFecha(cursor.getLong(cursor.getColumnIndexOrThrow("fecha")));
        partida.setModo(cursor.getString(cursor.getColumnIndexOrThrow("modo")));
        partida.setRondasConfiguradas(cursor.getInt(cursor.getColumnIndexOrThrow("rondas_configuradas")));
        partida.setRondasJugadas(cursor.getInt(cursor.getColumnIndexOrThrow("rondas_jugadas")));
        partida.setNumeroJugadores(cursor.getInt(cursor.getColumnIndexOrThrow("numero_jugadores")));
        int indiceGanador = cursor.getColumnIndexOrThrow("id_ganador");
        partida.setIdGanador(cursor.isNull(indiceGanador) ? null : cursor.getLong(indiceGanador));
        partida.setMotivoFinalizacion(cursor.getString(cursor.getColumnIndexOrThrow("motivo_finalizacion")));
        partida.setConfiguracionJson(cursor.getString(cursor.getColumnIndexOrThrow("configuracion_json")));
        return partida;
    }

    private static void validarId(long id) {
        if (id <= 0L) {
            throw new IllegalArgumentException("El ID debe ser positivo");
        }
    }

    /** Solo cierra los recursos creados por este DAO. */
    @Override
    public void close() {
        if (propietarioHelper) {
            helper.close();
        }
    }
}
