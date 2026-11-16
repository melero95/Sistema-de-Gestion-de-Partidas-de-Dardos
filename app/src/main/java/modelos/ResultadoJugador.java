package modelos;

import java.io.Serializable;

public class ResultadoJugador implements Serializable {

    private String nombre;
    private int puntuacion;
    private int posicion;
    private boolean ganador;
    private boolean mostrarPuntuacion;
    // Textos adicionales para la pantalla final; no alteran la puntuación persistida.
    private String textoResultadoPrincipal;
    private String textoResultadoSecundario;

    //Constructor --------------------
    public ResultadoJugador(String nombre, int puntuacion, int posicion,
                            boolean ganador, boolean mostrarPuntuacion) {

        this.nombre = nombre;
        this.puntuacion = puntuacion;
        this.posicion = posicion;
        this.ganador = ganador;
        this.mostrarPuntuacion = mostrarPuntuacion;
    }

    public void setTextosResultado(String principal, String secundario) {
        this.textoResultadoPrincipal = principal;
        this.textoResultadoSecundario = secundario;
    }

    public String getTextoResultadoPrincipal() { return textoResultadoPrincipal; }
    public String getTextoResultadoSecundario() { return textoResultadoSecundario; }

    //Getters --------------------
    public String getNombre() {
        return nombre;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public int getPosicion() {
        return posicion;
    }

    public boolean isGanador() {
        return ganador;
    }

    public boolean isMostrarPuntuacion() {
        return mostrarPuntuacion;
    }

    //Setters --------------------
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public void setGanador(boolean ganador) {
        this.ganador = ganador;
    }

    public void setMostrarPuntuacion(boolean mostrarPuntuacion) {
        this.mostrarPuntuacion = mostrarPuntuacion;
    }
}