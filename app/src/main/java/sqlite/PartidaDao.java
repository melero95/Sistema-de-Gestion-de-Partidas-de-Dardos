package sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import modelos.Partida;

/** DAO de PARTIDAS: conserva la API de fase 6 y añade gestión de visibilidad. */
public class PartidaDao implements AutoCloseable {
    private static final String TABLA = BaseDatosHelper.TABLA_PARTIDAS;
    private static final String[] COLUMNAS = {"id_partida", "identificador_unico", "fecha", "modo",
            "rondas_configuradas", "rondas_jugadas", "numero_jugadores", "id_ganador",
            "motivo_finalizacion", "configuracion_json"};
    private final BaseDatosHelper helper;
    private final boolean propietarioHelper;
    public PartidaDao(Context context) { this(new BaseDatosHelper(context.getApplicationContext()), true); }
    PartidaDao(BaseDatosHelper helper) { this(helper, false); }
    private PartidaDao(BaseDatosHelper helper, boolean propio) {
        if (helper == null) throw new IllegalArgumentException("El helper es obligatorio");
        this.helper = helper; propietarioHelper = propio;
    }
    public long insertar(Partida partida) { return insertar(helper.getWritableDatabase(), partida); }
    long insertar(SQLiteDatabase db, Partida partida) {
        if (partida == null || partida.getId() != 0L) throw new IllegalArgumentException("Se requiere una partida nueva con ID 0");
        ContentValues v = new ContentValues();
        v.put("identificador_unico", partida.getIdentificadorUnico());
        v.put("fecha", partida.getFecha()); v.put("modo", partida.getModo());
        v.put("rondas_configuradas", partida.getRondasConfiguradas());
        v.put("rondas_jugadas", partida.getRondasJugadas());
        v.put("numero_jugadores", partida.getNumeroJugadores());
        if (partida.getIdGanador() == null) v.putNull("id_ganador");
        else v.put("id_ganador", partida.getIdGanador());
        v.put("motivo_finalizacion", partida.getMotivoFinalizacion());
        v.put("configuracion_json", partida.getConfiguracionJson());
        long id = db.insertOrThrow(TABLA, null, v); partida.setId(id); return id;
    }
    public Partida obtenerPorId(long id) { validarId(id); return consultarUna(helper.getReadableDatabase(), "id_partida = ?", new String[]{String.valueOf(id)}); }
    public Partida obtenerPorIdentificadorUnico(String uuid) { return obtenerPorIdentificadorUnico(helper.getReadableDatabase(), uuid); }
    Partida obtenerPorIdentificadorUnico(SQLiteDatabase db, String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) throw new IllegalArgumentException("Identificador único obligatorio");
        return consultarUna(db, "identificador_unico = ?", new String[]{uuid.trim()});
    }
    public List<Partida> listarRecientes() { return consultarLista(helper.getReadableDatabase(), null, null); }
    public List<Partida> listarPorModo(String modo) {
        if (modo == null || modo.trim().isEmpty()) throw new IllegalArgumentException("Modo obligatorio");
        return consultarLista(helper.getReadableDatabase(), "modo = ?", new String[]{modo.trim()});
    }
    /** Permite filtrar las ocultas sin borrar ni modificar sus estadísticas. */
    public List<Partida> listarHistorial(String modo, boolean incluirOcultas) {
        boolean filtrarModo = modo != null && !modo.trim().isEmpty();
        String filtro = (incluirOcultas ? "" : "oculta = 0") + (filtrarModo ? (incluirOcultas ? "" : " AND ") + "modo = ?" : "");
        return consultarLista(helper.getReadableDatabase(), filtro.isEmpty() ? null : filtro,
                filtrarModo ? new String[]{modo.trim()} : null);
    }
    public boolean estaOculta(long id) {
        validarId(id);
        try (Cursor c = helper.getReadableDatabase().query(TABLA, new String[]{"oculta"}, "id_partida = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() && c.getInt(0) == 1;
        }
    }
    public boolean cambiarOculta(long id, boolean oculta) {
        validarId(id); ContentValues v = new ContentValues(); v.put("oculta", oculta ? 1 : 0);
        return helper.getWritableDatabase().update(TABLA, v, "id_partida = ?", new String[]{String.valueOf(id)}) == 1;
    }
    public int contarOcultas() {
        try (Cursor c = helper.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + TABLA + " WHERE oculta = 1", null)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }
    public int restaurarTodas() {
        ContentValues v = new ContentValues(); v.put("oculta", 0);
        return helper.getWritableDatabase().update(TABLA, v, "oculta = 1", null);
    }
    private Partida consultarUna(SQLiteDatabase db, String filtro, String[] args) {
        try (Cursor c = db.query(TABLA, COLUMNAS, filtro, args, null, null, null, "1")) {
            return c.moveToFirst() ? convertir(c) : null;
        }
    }
    private List<Partida> consultarLista(SQLiteDatabase db, String filtro, String[] args) {
        List<Partida> l = new ArrayList<>();
        try (Cursor c = db.query(TABLA, COLUMNAS, filtro, args, null, null, "fecha DESC, id_partida DESC")) {
            while (c.moveToNext()) l.add(convertir(c));
        }
        return l;
    }
    private Partida convertir(Cursor c) {
        Partida p = new Partida();
        p.setId(c.getLong(c.getColumnIndexOrThrow("id_partida")));
        p.setIdentificadorUnico(c.getString(c.getColumnIndexOrThrow("identificador_unico")));
        p.setFecha(c.getLong(c.getColumnIndexOrThrow("fecha")));
        p.setModo(c.getString(c.getColumnIndexOrThrow("modo")));
        p.setRondasConfiguradas(c.getInt(c.getColumnIndexOrThrow("rondas_configuradas")));
        p.setRondasJugadas(c.getInt(c.getColumnIndexOrThrow("rondas_jugadas")));
        p.setNumeroJugadores(c.getInt(c.getColumnIndexOrThrow("numero_jugadores")));
        int g = c.getColumnIndexOrThrow("id_ganador");
        p.setIdGanador(c.isNull(g) ? null : c.getLong(g));
        p.setMotivoFinalizacion(c.getString(c.getColumnIndexOrThrow("motivo_finalizacion")));
        p.setConfiguracionJson(c.getString(c.getColumnIndexOrThrow("configuracion_json")));
        return p;
    }
    private static void validarId(long id) { if (id <= 0L) throw new IllegalArgumentException("El ID debe ser positivo"); }
    @Override public void close() { if (propietarioHelper) helper.close(); }
}
