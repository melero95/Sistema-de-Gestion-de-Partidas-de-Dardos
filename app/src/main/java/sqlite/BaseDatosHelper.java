package sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Creación y mantenimiento del esquema SQLite de la aplicación.
 * Las Activities no deben ejecutar consultas SQL directamente: se usarán DAO.
 */
public class BaseDatosHelper extends SQLiteOpenHelper {

    public static final String NOMBRE_BASE_DATOS = "gestor_dardos.db";
    public static final int VERSION_BASE_DATOS = 1;

    public static final String TABLA_JUGADORES = "JUGADORES";
    public static final String TABLA_PARTIDAS = "PARTIDAS";
    public static final String TABLA_RESULTADOS = "RESULTADOS_PARTIDA";
    public static final String TABLA_ESTADISTICAS = "ESTADISTICAS_PARTIDA";

    public BaseDatosHelper(Context context) {
        this(context, NOMBRE_BASE_DATOS);
    }

    // Constructor de ámbito de paquete: permite usar una BD aislada en pruebas.
    BaseDatosHelper(Context context, String nombreBaseDatos) {
        super(context, nombreBaseDatos, null, VERSION_BASE_DATOS);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        crearTablaJugadores(db);
        crearTablaPartidas(db);
        crearTablaResultados(db);
        crearTablaEstadisticas(db);
        crearIndices(db);
    }

    private void crearTablaJugadores(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA_JUGADORES + " (" +
                "id_jugador INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL COLLATE NOCASE UNIQUE " +
                "CHECK (length(trim(nombre)) > 0), " +
                "activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)), " +
                "color INTEGER NOT NULL" +
                ")");
    }

    private void crearTablaPartidas(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA_PARTIDAS + " (" +
                "id_partida INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "identificador_unico TEXT NOT NULL UNIQUE, " +
                "fecha INTEGER NOT NULL, " +
                "modo TEXT NOT NULL, " +
                "rondas_configuradas INTEGER NOT NULL CHECK (rondas_configuradas >= 0), " +
                "rondas_jugadas INTEGER NOT NULL CHECK (rondas_jugadas >= 0), " +
                "numero_jugadores INTEGER NOT NULL " +
                "CHECK (numero_jugadores BETWEEN 1 AND 6), " +
                "id_ganador INTEGER, " +
                "motivo_finalizacion TEXT NOT NULL, " +
                "configuracion_json TEXT NOT NULL, " +
                "FOREIGN KEY (id_ganador) REFERENCES " +
                TABLA_JUGADORES + "(id_jugador)" +
                ")");
    }

    private void crearTablaResultados(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA_RESULTADOS + " (" +
                "id_resultado INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_partida INTEGER NOT NULL, " +
                "id_jugador INTEGER NOT NULL, " +
                "posicion INTEGER NOT NULL CHECK (posicion >= 1), " +
                "puntuacion_final INTEGER, " +
                "orden_turno INTEGER NOT NULL CHECK (orden_turno BETWEEN 1 AND 6), " +
                "UNIQUE (id_partida, id_jugador), " +
                "UNIQUE (id_partida, orden_turno), " +
                "FOREIGN KEY (id_partida) REFERENCES " +
                TABLA_PARTIDAS + "(id_partida) ON DELETE CASCADE, " +
                "FOREIGN KEY (id_jugador) REFERENCES " +
                TABLA_JUGADORES + "(id_jugador)" +
                ")");
    }

    private void crearTablaEstadisticas(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA_ESTADISTICAS + " (" +
                "id_estadistica INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_resultado INTEGER NOT NULL UNIQUE, " +

                // Estadísticas comunes (todos los modos).
                "dardos_lanzados INTEGER NOT NULL DEFAULT 0 CHECK (dardos_lanzados >= 0), " +
                "turnos_jugados INTEGER NOT NULL DEFAULT 0 CHECK (turnos_jugados >= 0), " +
                "simples INTEGER NOT NULL DEFAULT 0 CHECK (simples >= 0), " +
                "dobles INTEGER NOT NULL DEFAULT 0 CHECK (dobles >= 0), " +
                "triples INTEGER NOT NULL DEFAULT 0 CHECK (triples >= 0), " +
                "fallos INTEGER NOT NULL DEFAULT 0 CHECK (fallos >= 0), " +

                // 301 / 501.
                "busts INTEGER CHECK (busts >= 0), " +
                "puntos_validos INTEGER CHECK (puntos_validos >= 0), " +
                "mejor_turno INTEGER CHECK (mejor_turno >= 0), " +
                "turnos_180 INTEGER CHECK (turnos_180 >= 0), " +
                "dardos_cierre INTEGER CHECK (dardos_cierre >= 0), " +

                // Cricket / Cut Throat Cricket.
                "marcas_validas INTEGER CHECK (marcas_validas >= 0), " +
                "objetivos_cerrados INTEGER CHECK (objetivos_cerrados >= 0), " +
                "puntos_generados INTEGER CHECK (puntos_generados >= 0), " +
                "puntos_recibidos INTEGER CHECK (puntos_recibidos >= 0), " +

                // Double Down.
                "rondas_penalizadas INTEGER CHECK (rondas_penalizadas >= 0), " +
                "mejor_ronda_dd INTEGER CHECK (mejor_ronda_dd >= 0), " +
                "puntos_conseguidos_dd INTEGER CHECK (puntos_conseguidos_dd >= 0), " +

                // Around the Clock.
                "objetivos_superados INTEGER CHECK (objetivos_superados >= 0), " +
                "aciertos_objetivo INTEGER CHECK (aciertos_objetivo >= 0), " +
                "partida_completada INTEGER CHECK (partida_completada IN (0, 1)), " +

                // Shanghai.
                "puntos_validos_shanghai INTEGER CHECK (puntos_validos_shanghai >= 0), " +
                "mejor_ronda_shanghai INTEGER CHECK (mejor_ronda_shanghai >= 0), " +
                "rondas_con_puntos INTEGER CHECK (rondas_con_puntos >= 0), " +

                "FOREIGN KEY (id_resultado) REFERENCES " +
                TABLA_RESULTADOS + "(id_resultado) ON DELETE CASCADE" +
                ")");
    }

    private void crearIndices(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX idx_partidas_fecha ON " +
                TABLA_PARTIDAS + "(fecha DESC)");
        db.execSQL("CREATE INDEX idx_partidas_modo_fecha ON " +
                TABLA_PARTIDAS + "(modo, fecha DESC)");
        db.execSQL("CREATE INDEX idx_partidas_ganador ON " +
                TABLA_PARTIDAS + "(id_ganador)");
        db.execSQL("CREATE INDEX idx_resultados_jugador ON " +
                TABLA_RESULTADOS + "(id_jugador)");
        db.execSQL("CREATE INDEX idx_resultados_clasificacion ON " +
                TABLA_RESULTADOS + "(id_partida, posicion)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        // No destruir tablas: cuando exista la versión 2, definir migraciones.
        throw new IllegalStateException("Migración SQLite pendiente: versión " +
                versionAnterior + " -> " + versionNueva);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        throw new IllegalStateException("No se admite reducir la versión SQLite " +
                versionAnterior + " -> " + versionNueva);
    }
}
