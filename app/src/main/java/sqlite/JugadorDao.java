package sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import modelos.Jugador;

/**
 * Acceso a la tabla JUGADORES. Las Activities no deben ejecutar SQL.
 *
 * Un jugador nunca se elimina físicamente: se desactiva para impedir que
 * aparezca en la selección de nuevas partidas, conservando su historial.
 *
 * Los nombres únicos se garantizan también mediante la restricción UNIQUE
 * de SQLite (COLLATE NOCASE). Una colisión lanza SQLiteConstraintException.
 */
public class JugadorDao implements AutoCloseable {

    private static final String COLUMNA_ID = "id_jugador";
    private static final String COLUMNA_NOMBRE = "nombre";
    private static final String COLUMNA_ACTIVO = "activo";
    private static final String COLUMNA_COLOR = "color";

    private static final String[] COLUMNAS = {
            COLUMNA_ID, COLUMNA_NOMBRE, COLUMNA_ACTIVO, COLUMNA_COLOR
    };

    private final BaseDatosHelper helper;

    /** Constructor para uso normal en la aplicación. */
    public JugadorDao(Context context) {
        this(new BaseDatosHelper(context.getApplicationContext()));
    }

    /** Constructor accesible desde pruebas del paquete sqlite. */
    JugadorDao(BaseDatosHelper helper) {
        if (helper == null) {
            throw new IllegalArgumentException("El helper de base de datos es obligatorio");
        }
        this.helper = helper;
    }

    /**
     * Inserta un jugador nuevo y asigna a su objeto el ID generado en SQLite.
     * El objeto debe tener id=0. Ante un nombre repetido, SQLite lanza una
     * excepción de restricción y el ID del objeto permanece inalterado.
     */
    public long insertar(Jugador jugador) {
        if (jugador == null || jugador.getId() != 0L) {
            throw new IllegalArgumentException("Para insertar se requiere un jugador nuevo con ID 0");
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        long nuevoId = db.insertOrThrow(
                BaseDatosHelper.TABLA_JUGADORES,
                null,
                valoresJugador(jugador)
        );
        jugador.setId(nuevoId);
        return nuevoId;
    }

    /** Devuelve null si no existe un jugador con ese ID. */
    public Jugador obtenerPorId(long idJugador) {
        validarId(idJugador);
        SQLiteDatabase db = helper.getReadableDatabase();

        try (Cursor cursor = db.query(
                BaseDatosHelper.TABLA_JUGADORES,
                COLUMNAS,
                COLUMNA_ID + " = ?",
                new String[]{String.valueOf(idJugador)},
                null,
                null,
                null
        )) {
            return cursor.moveToFirst() ? convertirJugador(cursor) : null;
        }
    }

    /**
     * Busca por nombre, reutilizando la normalización del modelo Jugador.
     * Devuelve null cuando no hay coincidencias.
     */
    public Jugador obtenerPorNombre(String nombre) {
        String nombreNormalizado = new Jugador(nombre, 0).getNombre();
        SQLiteDatabase db = helper.getReadableDatabase();

        try (Cursor cursor = db.query(
                BaseDatosHelper.TABLA_JUGADORES,
                COLUMNAS,
                COLUMNA_NOMBRE + " = ?",
                new String[]{nombreNormalizado},
                null,
                null,
                null
        )) {
            return cursor.moveToFirst() ? convertirJugador(cursor) : null;
        }
    }

    /** Incluye jugadores activos e inactivos, ordenados por nombre. */
    public List<Jugador> listarTodos() {
        return listar(null, null);
    }

    /** Solo jugadores disponibles para configurar partidas nuevas. */
    public List<Jugador> listarActivos() {
        return listar(COLUMNA_ACTIVO + " = ?", new String[]{"1"});
    }

    /**
     * Actualiza nombre, color y estado del jugador indicado.
     * Devuelve false si el ID no existe; no modifica su identificador.
     */
    public boolean actualizar(Jugador jugador) {
        if (jugador == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo");
        }
        validarId(jugador.getId());

        SQLiteDatabase db = helper.getWritableDatabase();
        int filas = db.update(
                BaseDatosHelper.TABLA_JUGADORES,
                valoresJugador(jugador),
                COLUMNA_ID + " = ?",
                new String[]{String.valueOf(jugador.getId())}
        );
        return filas == 1;
    }

    /**
     * Activa o desactiva sin borrar el registro ni cambiar su nombre o color.
     * Devuelve false si no existe ese jugador.
     */
    public boolean cambiarActivo(long idJugador, boolean activo) {
        validarId(idJugador);
        ContentValues valores = new ContentValues();
        valores.put(COLUMNA_ACTIVO, activo ? 1 : 0);

        SQLiteDatabase db = helper.getWritableDatabase();
        int filas = db.update(
                BaseDatosHelper.TABLA_JUGADORES,
                valores,
                COLUMNA_ID + " = ?",
                new String[]{String.valueOf(idJugador)}
        );
        return filas == 1;
    }

    private List<Jugador> listar(String seleccion, String[] argumentos) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Jugador> jugadores = new ArrayList<>();

        try (Cursor cursor = db.query(
                BaseDatosHelper.TABLA_JUGADORES,
                COLUMNAS,
                seleccion,
                argumentos,
                null,
                null,
                COLUMNA_NOMBRE + " COLLATE NOCASE ASC, " + COLUMNA_ID + " ASC"
        )) {
            while (cursor.moveToNext()) {
                jugadores.add(convertirJugador(cursor));
            }
        }
        return jugadores;
    }

    private ContentValues valoresJugador(Jugador jugador) {
        ContentValues valores = new ContentValues();
        valores.put(COLUMNA_NOMBRE, jugador.getNombre());
        valores.put(COLUMNA_ACTIVO, jugador.isActivo() ? 1 : 0);
        valores.put(COLUMNA_COLOR, jugador.getColor());
        return valores;
    }

    private Jugador convertirJugador(Cursor cursor) {
        return new Jugador(
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMNA_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_NOMBRE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMNA_COLOR)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMNA_ACTIVO)) == 1
        );
    }

    private void validarId(long idJugador) {
        if (idJugador <= 0L) {
            throw new IllegalArgumentException("El ID del jugador debe ser mayor que cero");
        }
    }

    /** Cierra el SQLiteOpenHelper propio del DAO al dejar de utilizarlo. */
    @Override
    public void close() {
        helper.close();
    }
}
