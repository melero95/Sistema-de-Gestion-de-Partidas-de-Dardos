package sqlite;

import static org.junit.Assert.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import modelos.Jugador;

/** Fase 09: pruebas aisladas; nunca utiliza la BD real del usuario. */
@RunWith(AndroidJUnit4.class)
public class Fase09IntegridadSQLiteTest {
    private static final String NOMBRE = "fase09_integridad_aislada.db";
    private Context contexto;
    private BaseDatosHelper helper;
    private SQLiteDatabase db;

    @Before public void preparar() {
        contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();
        contexto.deleteDatabase(NOMBRE);
        helper = new BaseDatosHelper(contexto, NOMBRE);
        db = helper.getWritableDatabase();
    }

    @After public void limpiar() {
        if (helper != null) helper.close();
        if (contexto != null) contexto.deleteDatabase(NOMBRE);
    }

    @Test public void clavesForaneasActivadasYSinViolaciones() {
        try (Cursor c = db.rawQuery("PRAGMA foreign_keys", null)) {
            assertTrue(c.moveToFirst());
            assertEquals(1, c.getInt(0));
        }
        try (Cursor c = db.rawQuery("PRAGMA foreign_key_check", null)) {
            assertEquals(0, c.getCount());
        }
    }

    @Test public void rechazaNumeroJugadoresFueraDeUnoASeisEnTablaHistorica() {
        for (int numero : new int[]{0, 7, 8}) {
            ContentValues v = partida(numero);
            try {
                db.insertOrThrow(BaseDatosHelper.TABLA_PARTIDAS, null, v);
                fail("SQLite debe rechazar " + numero + " jugadores");
            } catch (SQLiteConstraintException esperado) {
                // CHECK(numero_jugadores BETWEEN 1 AND 6)
            }
        }
    }

    @Test public void noPermiteGanadorQueNoExiste() {
        ContentValues valores = partida(2);
        valores.put("id_ganador", 99999999L);
        try {
            db.insertOrThrow(BaseDatosHelper.TABLA_PARTIDAS, null, valores);
            fail("Debe fallar la FK del ganador");
        } catch (SQLiteConstraintException esperado) {
            // No se debe crear una partida con ganador inexistente.
        }
    }

    @Test public void ochoJugadoresRegistradosNoLimitanLaTablaJugadores() {
        JugadorDao dao = new JugadorDao(helper);
        for (int i = 1; i <= 8; i++) {
            assertTrue(dao.insertar(new Jugador("JUGADOR " + i, 0xFF223344)) > 0);
        }
        assertEquals(8, dao.listarActivos().size());
    }

    private ContentValues partida(int participantes) {
        ContentValues v = new ContentValues();
        v.put("identificador_unico", java.util.UUID.randomUUID().toString());
        v.put("fecha", 1L);
        v.put("modo", "501");
        v.put("rondas_configuradas", 10);
        v.put("rondas_jugadas", 1);
        v.put("numero_jugadores", participantes);
        v.put("motivo_finalizacion", "TEST");
        v.put("configuracion_json", "{}");
        return v;
    }
}
