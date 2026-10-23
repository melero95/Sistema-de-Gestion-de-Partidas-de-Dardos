package modelos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fotografía de las reglas y los participantes de una partida.
 * No representa el marcador ni los dardos ya lanzados.
 *
 * El orden de la lista de participantes es el orden REAL de la partida;
 * si la opción aleatoria estaba activa, se construye DESPUÉS del sorteo.
 */
public final class ConfiguracionPartida {

    public static final String MODO_301 = "301";
    public static final String MODO_501 = "501";
    public static final String MODO_CRICKET = "Cricket";
    public static final String MODO_CUT_THROAT = "Cut Throat Cricket";
    public static final String MODO_DOUBLE_DOWN = "Double Down";
    public static final String MODO_AROUND_CLOCK = "Around the Clock";
    public static final String MODO_SHANGHAI = "Shanghai";

    private final String modo;
    private final int rondas;
    private final int dardosPorTurno;
    private final boolean cierreDoble;
    private final boolean mantenerMultiplicador;
    private final boolean ordenAleatorio;
    private final List<Participante> participantes;

    public ConfiguracionPartida(String modo, int rondas, int dardosPorTurno,
                               boolean cierreDoble, boolean mantenerMultiplicador,
                               boolean ordenAleatorio, List<Participante> participantes) {
        if (!esModoValido(modo)) {
            throw new IllegalArgumentException("Modalidad no admitida: " + modo);
        }
        if (rondas < 1) {
            throw new IllegalArgumentException("Las rondas deben ser mayores que cero");
        }
        if (MODO_DOUBLE_DOWN.equals(modo) && rondas != 9) {
            throw new IllegalArgumentException("Double Down siempre tiene 9 rondas");
        }
        if ((MODO_AROUND_CLOCK.equals(modo) || MODO_SHANGHAI.equals(modo))
                && rondas != 21) {
            throw new IllegalArgumentException("Este modo tiene 21 rondas/objetivos");
        }
        if (dardosPorTurno < 1 || dardosPorTurno > 4) {
            throw new IllegalArgumentException("Debe haber entre 1 y 4 dardos por turno");
        }
        if (participantes == null || participantes.isEmpty() || participantes.size() > 6) {
            throw new IllegalArgumentException("Se requieren de 1 a 6 participantes");
        }
        if ((MODO_CRICKET.equals(modo) || MODO_CUT_THROAT.equals(modo))
                && participantes.size() < 2) {
            throw new IllegalArgumentException("Cricket requiere al menos dos participantes");
        }

        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < participantes.size(); i++) {
            Participante participante = participantes.get(i);
            if (participante == null || !ids.add(participante.getIdJugador())) {
                throw new IllegalArgumentException("Participante nulo o ID de jugador repetido");
            }
            if (participante.getOrdenTurno() != i + 1) {
                throw new IllegalArgumentException("El orden de turno debe ser correlativo");
            }
        }

        this.modo = modo;
        this.rondas = rondas;
        this.dardosPorTurno = dardosPorTurno;
        this.cierreDoble = cierreDoble;
        this.mantenerMultiplicador = mantenerMultiplicador;
        this.ordenAleatorio = ordenAleatorio;
        this.participantes = Collections.unmodifiableList(new ArrayList<>(participantes));
    }

    public String getModo() { return modo; }
    public int getRondas() { return rondas; }
    public int getDardosPorTurno() { return dardosPorTurno; }
    public boolean isCierreDoble() { return cierreDoble; }
    public boolean isMantenerMultiplicador() { return mantenerMultiplicador; }
    public boolean isOrdenAleatorio() { return ordenAleatorio; }
    public List<Participante> getParticipantes() { return participantes; }

    private static boolean esModoValido(String modo) {
        return MODO_301.equals(modo) || MODO_501.equals(modo)
                || MODO_CRICKET.equals(modo) || MODO_CUT_THROAT.equals(modo)
                || MODO_DOUBLE_DOWN.equals(modo) || MODO_AROUND_CLOCK.equals(modo)
                || MODO_SHANGHAI.equals(modo);
    }

    /** Conserva el nombre y color presentes en el MOMENTO de jugar. */
    public static final class Participante {
        private final long idJugador;
        private final String nombre;
        // Color ARGB real, no un identificador R.color.*.
        private final int colorArgb;
        private final int ordenTurno;

        public Participante(long idJugador, String nombre, int colorArgb, int ordenTurno) {
            if (idJugador <= 0) {
                throw new IllegalArgumentException("El participante debe tener un ID de SQLite");
            }
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del participante es obligatorio");
            }
            if (ordenTurno < 1 || ordenTurno > 6) {
                throw new IllegalArgumentException("Orden de turno fuera de rango");
            }
            this.idJugador = idJugador;
            this.nombre = nombre.trim();
            this.colorArgb = colorArgb;
            this.ordenTurno = ordenTurno;
        }

        public long getIdJugador() { return idJugador; }
        public String getNombre() { return nombre; }
        public int getColorArgb() { return colorArgb; }
        public int getOrdenTurno() { return ordenTurno; }
    }
}
