package sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.pm.ApplicationInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import modelos.ConfiguracionPartida;
import utilidades.SerializadorConfiguracionPartida;

/** Población de datos de demostración, exclusivamente para builds depurables.
 * Se ejecuta manualmente desde Ajustes, una vez sobre una BD completamente vacía.
 * Todas las inserciones se realizan en una única transacción SQLite.
 */
public final class PobladorDatosPrueba {
    private PobladorDatosPrueba() { }
    private static final String[] NOMBRES = {"ANA TEST", "LUIS TEST", "MARTA TEST", "PABLO TEST", "SARA TEST", "DIEGO TEST"};
    private static final int[] COLORES = {0xFFF44336,0xFF2196F3,0xFF4CAF50,0xFFFFC107,0xFF9C27B0,0xFFFF9800};
    private static final String[] MODOS = {
            ConfiguracionPartida.MODO_301, ConfiguracionPartida.MODO_501,
            ConfiguracionPartida.MODO_CRICKET, ConfiguracionPartida.MODO_CUT_THROAT,
            ConfiguracionPartida.MODO_DOUBLE_DOWN, ConfiguracionPartida.MODO_AROUND_CLOCK,
            ConfiguracionPartida.MODO_SHANGHAI
    };
    private static final String PREFS = "seed_dardos_debug";
    private static final String CLAVE = "seed_ejecutado_v1";

    public static boolean esDepurable(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
    public static boolean yaEjecutado(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(CLAVE, false);
    }
    public static boolean estaVacia(Context context) {
        try (BaseDatosHelper helper = new BaseDatosHelper(context)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            return contar(db, BaseDatosHelper.TABLA_JUGADORES) == 0
                    && contar(db, BaseDatosHelper.TABLA_PARTIDAS) == 0
                    && contar(db, BaseDatosHelper.TABLA_RESULTADOS) == 0
                    && contar(db, BaseDatosHelper.TABLA_ESTADISTICAS) == 0;
        }
    }
    private static long contar(SQLiteDatabase db, String tabla) {
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + tabla, null)) {
            return c.moveToFirst() ? c.getLong(0) : -1;
        }
    }
    public static final class Resumen {
        public final int jugadores;
        public final int partidas;
        public final int participaciones;
        public Resumen(int jugadores, int partidas, int participaciones) {
            this.jugadores = jugadores;
            this.partidas = partidas;
            this.participaciones = participaciones;
        }
    }
    public static Resumen poblar(Context context) {
        if (!esDepurable(context)) throw new IllegalStateException("Función exclusiva de Debug");
        if (yaEjecutado(context)) throw new IllegalStateException("Los datos de prueba ya se generaron antes");
        try (BaseDatosHelper helper = new BaseDatosHelper(context)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();
            int participaciones = 0;
            try {
                if (contar(db, BaseDatosHelper.TABLA_JUGADORES) != 0 ||
                        contar(db, BaseDatosHelper.TABLA_PARTIDAS) != 0 ||
                        contar(db, BaseDatosHelper.TABLA_RESULTADOS) != 0 ||
                        contar(db, BaseDatosHelper.TABLA_ESTADISTICAS) != 0)
                    throw new IllegalStateException("La base de datos no está vacía. No se modifica nada.");
                long[] ids = new long[NOMBRES.length];
                for (int i=0;i<ids.length;i++) {
                    ContentValues v = new ContentValues();
                    v.put("nombre",NOMBRES[i]);v.put("color",COLORES[i]);v.put("activo",i==5?0:1);
                    ids[i] = db.insertOrThrow(BaseDatosHelper.TABLA_JUGADORES,null,v);
                }
                // 70 partidas distribuidas entre los siete modos. Orden y valores deterministas.
                // Los dos primeros jugadores participan en todas, el resto en rotación.
                for (int num=0;num<70;num++) {
                    int modoIndice = num%7;
                    String modo=MODOS[modoIndice];
                    int totalJugadores = modoIndice==2 || modoIndice==3 ? 2 : 2+(num%4);
                    int[] participantes = new int[totalJugadores];
                    participantes[0]=0;
                    participantes[1]=1;
                    for(int j=2;j<totalJugadores;j++) participantes[j]=2+((num+j-2)%4);
                    int ganadorIndice = num%5==0 ? 1 : (num%3==0 ? totalJugadores-1 : 0);
                    // Cinco partidas ganadas por LUIS y suficientes juegos de ANA para el umbral de 10.
                    int rondas = modoIndice==4?9:(modoIndice>=5?21:15);
                    int rondasJugadas = modoIndice==4?9:((num%8)+5);
                    List<ConfiguracionPartida.Participante> jugadores = new ArrayList<>();
                    for(int j=0;j<totalJugadores;j++) {
                        int indice=participantes[j];
                        jugadores.add(new ConfiguracionPartida.Participante(ids[indice],NOMBRES[indice],COLORES[indice],j+1));
                    }
                    ConfiguracionPartida cfg = new ConfiguracionPartida(modo,rondas,3,
                            modoIndice<2 && num%2==0,false,false,jugadores);
                    ContentValues p = new ContentValues();
                    p.put("identificador_unico",String.format(Locale.ROOT,"seed-f08-%03d",num));
                    p.put("fecha",System.currentTimeMillis()-(long)(70-num)*86400000L);
                    p.put("modo",modo);p.put("rondas_configuradas",rondas);
                    p.put("rondas_jugadas",rondasJugadas);p.put("numero_jugadores",totalJugadores);
                    p.put("id_ganador",ids[participantes[ganadorIndice]]);
                    p.put("motivo_finalizacion", "VICTORIA");
                    p.put("configuracion_json",SerializadorConfiguracionPartida.aJson(cfg));
                    p.put("oculta",num%11==0?1:0);
                    long idPartida=db.insertOrThrow(BaseDatosHelper.TABLA_PARTIDAS,null,p);
                    for(int j=0;j<totalJugadores;j++) {
                        boolean ganador=j==ganadorIndice;
                        int posicion=ganador?1:(j<ganadorIndice?j+2:j+1);
                        int puntos;
                        switch(modoIndice) {
                            case 0: case 1: puntos=ganador?0:50+j*31+num%30;break;
                            case 4: puntos=50+num*3+j*22;break;
                            case 5: puntos=ganador?21:8+(num+j)%12;break;
                            case 6: puntos=70+num*4+j*17;break;
                            default: puntos=20+num+j*10;
                        }
                        ContentValues resultado=new ContentValues();
                        resultado.put("id_partida",idPartida);
                        resultado.put("id_jugador",ids[participantes[j]]);
                        resultado.put("posicion",posicion);resultado.put("orden_turno",j+1);
                        resultado.put("puntuacion_final",puntos);
                        long idResultado=db.insertOrThrow(BaseDatosHelper.TABLA_RESULTADOS,null,resultado);
                        ContentValues e=new ContentValues();
                        e.put("id_resultado",idResultado);
                        e.put("turnos_jugados",rondasJugadas);
                        e.put("dardos_lanzados",rondasJugadas*3);
                        e.put("simples",rondasJugadas+j);
                        e.put("dobles",(num+j)%7);
                        e.put("triples",(num+j)%5);
                        e.put("fallos",(num+j)%6);
                        if(modoIndice<2) {
                            e.put("busts",(num+j)%4);
                            e.put("puntos_validos",(modoIndice==0?301:501)-puntos);
                            e.put("mejor_turno",num%5==0?180:60+(num+j)%90);
                            e.put("turnos_180",num%5==0?1:0);
                            e.put("dardos_cierre",ganador?1+(num%3):0);
                        } else if(modoIndice==2 || modoIndice==3) {
                            e.put("marcas_validas",15+(num+j)%9);
                            e.put("objetivos_cerrados",ganador?7:3+(num+j)%4);
                            e.put("puntos_generados",20+num+j*4);
                            e.put("puntos_recibidos",(num+j*3)%60);
                        } else if(modoIndice==4) {
                            e.put("rondas_penalizadas",(num+j)%4);
                            e.put("mejor_ronda_dd",10+(num+j)%38);
                            e.put("puntos_conseguidos_dd",25+(num*3+j*7)%140);
                        } else if(modoIndice==5) {
                            e.put("objetivos_superados",ganador?21:8+(num+j)%12);
                            e.put("aciertos_objetivo",8+(num+j)%15);
                            e.put("partida_completada",ganador?1:0);
                        } else {
                            e.put("puntos_validos_shanghai",puntos);
                            e.put("mejor_ronda_shanghai",18+(num+j)%80);
                            e.put("rondas_con_puntos",5+(num+j)%14);
                        }
                        db.insertOrThrow(BaseDatosHelper.TABLA_ESTADISTICAS,null,e);
                        participaciones++;
                    }
                }
                db.setTransactionSuccessful();
            } finally { db.endTransaction(); }
            if (!context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putBoolean(CLAVE,true).commit())
                throw new IllegalStateException("Datos insertados, pero no se pudo guardar la marca de ejecución");
            return new Resumen(NOMBRES.length,70,participaciones);
        }
    }
}
