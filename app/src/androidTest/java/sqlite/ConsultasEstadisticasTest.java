package sqlite;

import static org.junit.Assert.*;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import modelos.EstadisticasJugadorDTO;
import modelos.EstadisticasResumenDTO;
import modelos.MetricasModalidadDTO;
import java.util.List;

/** Pruebas de la fase 08, sobre una BD independiente del usuario. */
@RunWith(AndroidJUnit4.class)
public class ConsultasEstadisticasTest {
    private static final String BD = "pruebas_fase08_estadisticas.db";
    private Context contexto;
    private BaseDatosHelper helper;
    private ConsultasEstadisticas consultas;

    @Before public void preparar() {
        contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();
        contexto.deleteDatabase(BD);
        helper = new BaseDatosHelper(contexto, BD);
        consultas = new ConsultasEstadisticas(helper);
        SQLiteDatabase d = helper.getWritableDatabase();
        d.execSQL("INSERT INTO JUGADORES(id_jugador,nombre,activo,color) VALUES(1,'ANA',1,-65536)");
        d.execSQL("INSERT INTO JUGADORES(id_jugador,nombre,activo,color) VALUES(2,'LUIS',0,-16776961)");
        d.execSQL("INSERT INTO PARTIDAS(id_partida,identificador_unico,fecha,modo,rondas_configuradas,rondas_jugadas,numero_jugadores,id_ganador,motivo_finalizacion,configuracion_json,oculta) " +
                "VALUES(1,'uuid-1',1234567,'501',10,8,2,1,'VICTORIA','{}',1)");
        d.execSQL("INSERT INTO RESULTADOS_PARTIDA(id_resultado,id_partida,id_jugador,posicion,puntuacion_final,orden_turno) " +
                "VALUES(11,1,1,1,0,1)");
        d.execSQL("INSERT INTO RESULTADOS_PARTIDA(id_resultado,id_partida,id_jugador,posicion,puntuacion_final,orden_turno) " +
                "VALUES(12,1,2,2,88,2)");
        d.execSQL("INSERT INTO ESTADISTICAS_PARTIDA(id_resultado,dardos_lanzados,puntos_validos,mejor_turno,turnos_180,busts,dardos_cierre) " +
                "VALUES(11,30,501,120,1,2,2)");
        d.execSQL("INSERT INTO ESTADISTICAS_PARTIDA(id_resultado,dardos_lanzados,puntos_validos,mejor_turno,turnos_180,busts,dardos_cierre) " +
                "VALUES(12,30,413,100,0,1,0)");
    }
    @After public void limpiar() {
        if (consultas != null) consultas.close();
        if (helper != null) helper.close();
        if (contexto != null) contexto.deleteDatabase(BD);
    }
    @Test public void ocultasCuentanEnGeneralYVictorias() {
        EstadisticasResumenDTO r = consultas.resumenGeneral();
        assertEquals(1, r.totalPartidas);
        assertEquals(2, r.totalJugadores);
        assertEquals("501", r.modalidadMasJugada);
        EstadisticasJugadorDTO j = consultas.resumenJugador(1L);
        assertEquals(1, j.victorias);
        assertEquals(100d, j.porcentajeVictorias, .001);
        assertEquals(1, j.ultimasPartidas.size());
    }
    @Test public void jugadoresInactivosIncluidos() {
        EstadisticasJugadorDTO j = consultas.resumenJugador(2L);
        assertNotNull(j);
        assertFalse(j.activo);
        assertEquals(1, j.segundos);
        assertEquals(1, j.partidas);
    }
    @Test public void mediasAgregadasYCierres() {
        MetricasModalidadDTO m = consultas.metricasJugador(1L,"501");
        assertEquals(501d/30d, m.puntosPorDardo, .001);
        assertEquals(120, (int) m.maximos.get("mejor_turno"));
        assertEquals(1L, (long) m.acumulados.get("cierres_realizados"));
        assertEquals(1L, (long) m.acumulados.get("turnos_180"));
    }
    @Test public void recordsNoExigenJugadoresActivos() {
        assertEquals(1, consultas.recordMasVictorias().size());
        assertEquals(1, consultas.recordMasVictorias().get(0).idJugador);
        assertEquals(2, consultas.recordMasPartidas().size());
        assertTrue(consultas.recordPorcentajeVictorias().isEmpty()); // mínimo 10 partidas
        assertEquals(1, consultas.recordMas180().get(0).idJugador);
    }
}
