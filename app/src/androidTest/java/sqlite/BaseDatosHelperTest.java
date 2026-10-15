package sqlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public class BaseDatosHelperTest {

    @Test
    public void crearBaseDatosConCuatroTablasYClavesForaneas() {
        Context contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String nombrePrueba = "gestor_dardos_prueba.db";

        // La prueba utiliza su propia base de datos, no la de los jugadores.
        contexto.deleteDatabase(nombrePrueba);
        try (BaseDatosHelper helper = new BaseDatosHelper(contexto, nombrePrueba)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            assertEquals(BaseDatosHelper.VERSION_BASE_DATOS, db.getVersion());

            Set<String> tablasEsperadas = new HashSet<>(Arrays.asList(
                    BaseDatosHelper.TABLA_JUGADORES,
                    BaseDatosHelper.TABLA_PARTIDAS,
                    BaseDatosHelper.TABLA_RESULTADOS,
                    BaseDatosHelper.TABLA_ESTADISTICAS
            ));
            Set<String> tablasEncontradas = new HashSet<>();

            try (Cursor cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type = 'table' " +
                            "AND name IN (?, ?, ?, ?)",
                    tablasEsperadas.toArray(new String[0])
            )) {
                while (cursor.moveToNext()) {
                    tablasEncontradas.add(cursor.getString(0));
                }
            }
            assertEquals(tablasEsperadas, tablasEncontradas);

            try (Cursor cursor = db.rawQuery("PRAGMA foreign_keys", null)) {
                assertTrue(cursor.moveToFirst());
                assertEquals(1, cursor.getInt(0));
            }
        } finally {
            contexto.deleteDatabase(nombrePrueba);
        }
    }
}
