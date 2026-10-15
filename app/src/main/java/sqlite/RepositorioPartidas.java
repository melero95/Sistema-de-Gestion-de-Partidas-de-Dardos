package sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelos.EstadisticaPartida;
import modelos.Partida;
import modelos.ResultadoPartida;

/**
 * Coordina el guardado DEFINITIVO y ATÓMICO de una partida finalizada.
 *
 * IMPORTANTE: resultados.get(i) y estadisticas.get(i) deben corresponder
 * al MISMO jugador. Cada resultado y cada estadística deben ser nuevos (ID 0).
 * El objeto Partida debe conservar su identificadorUnico entre reintentos.
 *
 * Esta fase prepara la infraestructura: NO se llama aún desde las Activities.
 */
public class RepositorioPartidas implements AutoCloseable {

    private final BaseDatosHelper helper;
    private final boolean propietarioHelper;
    private final PartidaDao partidaDao;
    private final ResultadoPartidaDao resultadoDao;
    private final EstadisticaPartidaDao estadisticaDao;

    public RepositorioPartidas(Context context) {
        this(new BaseDatosHelper(context.getApplicationContext()), true);
    }

    // Inyección de una BD independiente en las pruebas instrumentales.
    RepositorioPartidas(BaseDatosHelper helper) {
        this(helper, false);
    }

    private RepositorioPartidas(BaseDatosHelper helper, boolean propietarioHelper) {
        if (helper == null) {
            throw new IllegalArgumentException("El helper es obligatorio");
        }
        this.helper = helper;
        this.propietarioHelper = propietarioHelper;
        this.partidaDao = new PartidaDao(helper);
        this.resultadoDao = new ResultadoPartidaDao(helper);
        this.estadisticaDao = new EstadisticaPartidaDao(helper);
    }

    /**
     * Inserta PARTIDAS -> RESULTADOS_PARTIDA -> ESTADISTICAS_PARTIDA
     * dentro de UNA sola transacción. Devuelve el ID persistido.
     *
     * Si ya existe el identificador único, devuelve su ID sin crear duplicados.
     * Si falla la operación, SQLite hace ROLLBACK y los objetos nuevos
     * recuperan sus identificadores previos (0) para permitir reintento.
     *
     * La comprobación previa de UUID no reemplaza la restricción UNIQUE en BD.
     */
    public long guardarPartidaCompleta(Partida partida,
                                       List<ResultadoPartida> resultados,
                                       List<EstadisticaPartida> estadisticas) {
        // Segundo intento sobre los mismos objetos tras un COMMIT confirmado.
        if (partida != null && partida.getId() > 0L) {
            Partida existente = partidaDao.obtenerPorIdentificadorUnico(
                    partida.getIdentificadorUnico());
            if (existente != null && existente.getId() == partida.getId()) {
                return existente.getId();
            }
            throw new IllegalArgumentException("El ID y el UUID de la partida no coinciden en SQLite");
        }
        validarEntrada(partida, resultados, estadisticas);

        // No modificamos los objetos del llamador hasta confirmar COMMIT.
        SQLiteDatabase db = helper.getWritableDatabase();
        long idPartida;
        long[] idsResultados = new long[resultados.size()];
        long[] idsEstadisticas = new long[estadisticas.size()];
        boolean yaExistia = false;

        db.beginTransaction();
        try {
            Partida existente = partidaDao.obtenerPorIdentificadorUnico(
                    db, partida.getIdentificadorUnico());
            if (existente != null) {
                idPartida = existente.getId();
                yaExistia = true;
            } else {
                // Copia para evitar IDs huérfanos en los objetos originales
                // si falla una inserción posterior y se deshace la transacción.
                Partida temporal = new Partida(
                        partida.getModo(), partida.getRondasConfiguradas(),
                        partida.getRondasJugadas(), partida.getNumeroJugadores(),
                        partida.getIdGanador(), partida.getMotivoFinalizacion(),
                        partida.getConfiguracionJson());
                temporal.setIdentificadorUnico(partida.getIdentificadorUnico());
                temporal.setFecha(partida.getFecha());
                idPartida = partidaDao.insertar(db, temporal);

                for (int i = 0; i < resultados.size(); i++) {
                    ResultadoPartida original = resultados.get(i);
                    ResultadoPartida temporalResultado = new ResultadoPartida(
                            idPartida, original.getIdJugador(), original.getPosicion(),
                            original.getPuntuacionFinal(), original.getOrdenTurno());
                    idsResultados[i] = resultadoDao.insertar(db, temporalResultado);
                    idsEstadisticas[i] = estadisticaDao.insertar(db,
                            estadisticas.get(i), idsResultados[i]);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            // Ante una excepción, aquí se ejecuta el ROLLBACK automático.
            db.endTransaction();
        }

        if (!yaExistia) {
            partida.setId(idPartida);
            for (int i = 0; i < resultados.size(); i++) {
                ResultadoPartida resultado = resultados.get(i);
                EstadisticaPartida estadistica = estadisticas.get(i);
                resultado.setIdPartida(idPartida);
                resultado.setId(idsResultados[i]);
                estadistica.setIdResultado(idsResultados[i]);
                estadistica.setId(idsEstadisticas[i]);
            }
        }
        return idPartida;
    }

    private static void validarEntrada(Partida partida,
                                      List<ResultadoPartida> resultados,
                                      List<EstadisticaPartida> estadisticas) {
        if (partida == null || resultados == null || estadisticas == null) {
            throw new IllegalArgumentException("Partida, resultados y estadísticas son obligatorios");
        }
        if (partida.getId() != 0L || partida.getIdentificadorUnico() == null
                || partida.getIdentificadorUnico().trim().isEmpty()
                || partida.getModo() == null || partida.getMotivoFinalizacion() == null
                || partida.getConfiguracionJson() == null) {
            throw new IllegalArgumentException("La partida debe ser nueva y contener sus metadatos");
        }
        if (resultados.isEmpty() || resultados.size() > 6
                || resultados.size() != partida.getNumeroJugadores()
                || estadisticas.size() != resultados.size()) {
            throw new IllegalArgumentException("Debe existir un resultado y una estadística por jugador");
        }

        Set<Long> participantes = new HashSet<>();
        Set<Integer> ordenes = new HashSet<>();
        for (int i = 0; i < resultados.size(); i++) {
            ResultadoPartida resultado = resultados.get(i);
            EstadisticaPartida estadistica = estadisticas.get(i);
            if (resultado == null || estadistica == null
                    || resultado.getId() != 0L || resultado.getIdPartida() != 0L
                    || estadistica.getId() != 0L || estadistica.getIdResultado() != 0L
                    || resultado.getIdJugador() <= 0L
                    || resultado.getPosicion() < 1 || resultado.getOrdenTurno() < 1
                    || resultado.getOrdenTurno() > resultados.size()) {
                throw new IllegalArgumentException("Cada fila debe ser nueva y tener valores válidos");
            }
            if (!participantes.add(resultado.getIdJugador())
                    || !ordenes.add(resultado.getOrdenTurno())) {
                throw new IllegalArgumentException("No se permiten participantes u órdenes repetidos");
            }
        }
        if (partida.getIdGanador() != null && !participantes.contains(partida.getIdGanador())) {
            throw new IllegalArgumentException("El ganador debe pertenecer a la partida");
        }
    }

    @Override
    public void close() {
        if (propietarioHelper) {
            helper.close();
        }
    }
}
