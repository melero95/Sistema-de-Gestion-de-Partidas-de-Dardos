package modelos;

import java.util.Locale;

/**
 * Datos propios de un jugador registrado.
 * Las estadísticas históricas se calculan mediante consultas a SQLite.
 */
public class Jugador {

    // Id = 0: jugador nuevo, todavía no insertado en SQLite.
    private long id;
    private String nombre;
    private boolean activo = true;

    // Color ARGB de Android (valor real), no identificador R.color.
    private int color;

    public Jugador() {
        // Necesario para ciertas operaciones de serialización.
    }

    public Jugador(String nombre, int color) {
        this(0L, nombre, color, true);
    }

    public Jugador(long id, String nombre, int color, boolean activo) {
        setId(id);
        setNombre(nombre);
        this.color = color;
        this.activo = activo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("El ID del jugador no puede ser negativo");
        }
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    /** Normaliza espacios y conserva el nombre en mayúsculas. */
    public void setNombre(String nombre) {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre del jugador no puede ser nulo");
        }

        String nombreNormalizado = nombre.trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);

        if (nombreNormalizado.isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
        }

        this.nombre = nombreNormalizado;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return nombre == null ? "" : nombre;
    }
}
