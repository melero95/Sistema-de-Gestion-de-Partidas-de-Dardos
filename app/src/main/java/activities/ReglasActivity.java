package activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.productos.juegosdedardos.R;

import java.util.ArrayList;

public class ReglasActivity extends AppCompatActivity {

    // Componentes
    private Spinner spinnerModoReglas;
    private TextView txtReglas;
    private Button btnVolverMenu;

    // Lista de modos
    private final ArrayList<String> modosJuego = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reglas);

        EdgeToEdge.enable(this);

        inicializarComponentes();
        configurarSpinner();
        configurarBotones();

        // Por defecto mostramos todas las reglas
        mostrarReglas(0);
    }

    // ---------------------------------------------------------
    // INICIALIZAR COMPONENTES
    // ---------------------------------------------------------

    private void inicializarComponentes() {

        spinnerModoReglas = findViewById(R.id.spinnerModoReglas);
        txtReglas = findViewById(R.id.txtReglas);
        btnVolverMenu = findViewById(R.id.btnVolverMenu);
    }

    // ---------------------------------------------------------
    // CONFIGURAR SPINNER
    // ---------------------------------------------------------

    private void configurarSpinner() {

        modosJuego.clear();

        modosJuego.add("Todos");
        modosJuego.add("301 / 501");
        modosJuego.add("Cricket");
        modosJuego.add("Cut Throat Cricket");
        modosJuego.add("Double Down");
        modosJuego.add("Around the Clock");
        modosJuego.add("Shanghai");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                modosJuego
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerModoReglas.setAdapter(adapter);

        spinnerModoReglas.setSelection(0);

        spinnerModoReglas.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {

                        mostrarReglas(position);
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                        // No es necesario hacer nada
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // MOSTRAR REGLAS SEGÚN EL MODO
    // ---------------------------------------------------------

    private void mostrarReglas(int posicion) {

        String reglas;

        switch (posicion) {

            case 1:
                reglas = obtenerReglas301501();
                break;

            case 2:
                reglas = obtenerReglasCricket();
                break;

            case 3:
                reglas = obtenerReglasCutThroat();
                break;

            case 4:
                reglas = obtenerReglasDoubleDown();
                break;

            case 5:
                reglas = obtenerReglasAroundTheClock();
                break;

            case 6:
                reglas = obtenerReglasShanghai();
                break;

            case 0:
            default:
                reglas = obtenerTodasLasReglas();
                break;
        }

        mostrarTextoFormateado(reglas);
    }

    // ---------------------------------------------------------
    // MOSTRAR HTML EN EL TEXTVIEW
    // ---------------------------------------------------------

    private void mostrarTextoFormateado(String texto) {

        if (android.os.Build.VERSION.SDK_INT
                >= android.os.Build.VERSION_CODES.N) {

            txtReglas.setText(
                    Html.fromHtml(
                            texto,
                            Html.FROM_HTML_MODE_LEGACY
                    )
            );

        } else {

            txtReglas.setText(
                    Html.fromHtml(texto)
            );
        }
    }

    // ---------------------------------------------------------
    // TODAS LAS REGLAS
    // ---------------------------------------------------------

    private String obtenerTodasLasReglas() {

        return obtenerReglas301501()
                + separador()
                + obtenerReglasCricket()
                + separador()
                + obtenerReglasCutThroat()
                + separador()
                + obtenerReglasDoubleDown()
                + separador()
                + obtenerReglasAroundTheClock()
                + separador()
                + obtenerReglasShanghai();
    }

    // ---------------------------------------------------------
    // 301 / 501
    // ---------------------------------------------------------

    private String obtenerReglas301501() {

        return titulo("301 / 501")

                + subtitulo("Objetivo")

                + parrafo(
                "El objetivo es reducir la puntuación inicial hasta llegar "
                        + "exactamente a 0 puntos."
        )

                + parrafo(
                "En el modo 301 cada jugador comienza con 301 puntos, "
                        + "mientras que en 501 comienza con 501 puntos."
        )

                + subtitulo("Cómo se juega")

                + parrafo(
                "Los jugadores realizan sus turnos de forma consecutiva. "
                        + "En cada turno pueden lanzar el número de dardos "
                        + "configurado para la partida."
        )

                + parrafo(
                "La puntuación obtenida con cada lanzamiento se resta "
                        + "de la puntuación restante del jugador."
        )

                + subtitulo("Multiplicadores")

                + parrafo(
                "Los lanzamientos pueden realizarse como simples, dobles "
                        + "o triples. El multiplicador seleccionado modifica "
                        + "el valor obtenido."
        )

                + subtitulo("Final de la partida")

                + parrafo(
                "Para finalizar es necesario alcanzar exactamente 0 puntos."
        )

                + parrafo(
                "Si está activada la opción de cierre con doble, "
                        + "el último lanzamiento deberá ser un doble."
        )

                + parrafo(
                "Si el jugador supera la puntuación necesaria para llegar "
                        + "a 0, el turno termina sin poder cerrar la partida."
        );
    }

    // ---------------------------------------------------------
    // CRICKET
    // ---------------------------------------------------------

    private String obtenerReglasCricket() {

        return titulo("Cricket")

                + subtitulo("Objetivo")

                + parrafo(
                "El objetivo es cerrar todos los números de Cricket "
                        + "y conseguir una puntuación igual o superior "
                        + "a la de los demás jugadores."
        )

                + subtitulo("Números utilizados")

                + parrafo(
                "Se utilizan los números 20, 19, 18, 17, 16, 15 "
                        + "y la diana (Bull)."
        )

                + subtitulo("Cerrar un número")

                + parrafo(
                "Para cerrar un número es necesario conseguir tres marcas."
        )

                + parrafo(
                "Un simple suma una marca, un doble suma dos marcas "
                        + "y un triple suma tres marcas."
        )

                + subtitulo("Puntuación")

                + parrafo(
                "Una vez cerrado un número, un jugador puede obtener puntos "
                        + "adicionales sobre ese número mientras algún rival "
                        + "todavía no lo haya cerrado."
        )

                + subtitulo("Final de la partida")

                + parrafo(
                "La partida termina cuando un jugador ha cerrado todos "
                        + "los números y además tiene una puntuación igual "
                        + "o superior a la de sus rivales."
        );
    }

    // ---------------------------------------------------------
    // CUT THROAT CRICKET
    // ---------------------------------------------------------

    private String obtenerReglasCutThroat() {

        return titulo("Cut Throat Cricket")

                + subtitulo("Objetivo")

                + parrafo(
                "El funcionamiento para cerrar los números es similar "
                        + "al Cricket tradicional."
        )

                + parrafo(
                "La principal diferencia está en la forma de gestionar "
                        + "los puntos."
        )

                + subtitulo("Puntuación")

                + parrafo(
                "Cuando un jugador consigue puntos sobre un número que "
                        + "ya ha cerrado, esos puntos se asignan a los "
                        + "rivales que todavía tengan ese número abierto."
        )

                + parrafo(
                "Por este motivo, en Cut Throat Cricket interesa terminar "
                        + "con la menor cantidad de puntos posible."
        )

                + subtitulo("Final de la partida")

                + parrafo(
                "Para ganar es necesario cerrar todos los números "
                        + "y tener una puntuación igual o inferior "
                        + "a la de los demás jugadores."
        );
    }

    // ---------------------------------------------------------
    // DOUBLE DOWN
    // ---------------------------------------------------------

    private String obtenerReglasDoubleDown() {

        return titulo("Double Down")

                + subtitulo("Objetivo")

                + parrafo(
                "El objetivo es conseguir la mayor cantidad de puntos "
                        + "posible superando las diferentes rondas."
        )

                + subtitulo("Inicio")

                + parrafo(
                "Todos los jugadores comienzan con 50 puntos."
        )

                + subtitulo("Rondas")

                + parrafo(
                "La partida está formada por 9 rondas. "
                        + "En cada ronda se establece un objetivo concreto "
                        + "que los jugadores deben intentar alcanzar."
        )

                + parrafo(
                "Los puntos conseguidos durante la ronda se añaden "
                        + "a la puntuación acumulada."
        )

                + subtitulo("Fallo de ronda")

                + parrafo(
                "Si un jugador no consigue ningún acierto válido durante "
                        + "una ronda, su puntuación total se divide por dos."
        )

                + subtitulo("Final de la partida")

                + parrafo(
                "Después de completar todas las rondas, gana el jugador "
                        + "que haya conseguido la mayor puntuación."
        );
    }

    // ---------------------------------------------------------
    // AROUND THE CLOCK
    // ---------------------------------------------------------

    private String obtenerReglasAroundTheClock() {

        return titulo("Around the Clock")

                + subtitulo("Objetivo")

                + parrafo(
                "Around the Clock es una carrera en la que cada jugador "
                        + "debe avanzar por los diferentes objetivos "
                        + "hasta completar el recorrido."
        )

                + subtitulo("Cómo se juega")

                + parrafo(
                "Cada jugador comienza en el primer objetivo."
        )

                + parrafo(
                "Cuando consigue acertar el objetivo correspondiente, "
                        + "avanza al siguiente."
        )

                + subtitulo("Multiplicadores")

                + parrafo(
                "Un lanzamiento simple permite avanzar una posición."
        )

                + parrafo(
                "Un lanzamiento doble permite avanzar dos posiciones."
        )

                + parrafo(
                "Los lanzamientos fallados no permiten avanzar."
        )

                + subtitulo("Final de la partida")

                + parrafo(
                "Los jugadores se clasifican según el orden en el que "
                        + "completan todos los objetivos."
        );
    }

    // ---------------------------------------------------------
    // SHANGHAI
    // ---------------------------------------------------------

    private String obtenerReglasShanghai() {

        return titulo("Shanghai")

                + subtitulo("Objetivo")

                + parrafo(
                "El objetivo es conseguir la mayor puntuación posible "
                        + "durante las diferentes rondas."
        )

                + subtitulo("Cómo se juega")

                + parrafo(
                "Cada ronda tiene asignado un número objetivo."
        )

                + parrafo(
                "Los jugadores deben intentar acertar ese número "
                        + "durante sus lanzamientos."
        )

                + subtitulo("Puntuación")

                + parrafo(
                "Un simple suma el valor del número objetivo."
        )

                + parrafo(
                "Un doble suma dos veces el valor del objetivo."
        )

                + parrafo(
                "Un triple suma tres veces el valor del objetivo."
        )

                + parrafo(
                "Los impactos en números diferentes al objetivo "
                        + "no aportan puntos."
        )

                + subtitulo("Final de la partida")

                + parrafo(
                "Al terminar todas las rondas, gana el jugador "
                        + "con mayor puntuación acumulada."
        );
    }

    // ---------------------------------------------------------
    // FORMATO DEL TEXTO
    // ---------------------------------------------------------

    private String titulo(String texto) {

        return "<h2>"
                + "<font color='#FFC107'>"
                + texto
                + "</font>"
                + "</h2>";
    }


    private String subtitulo(String texto) {

        return "<b>"
                + "<font color='#FFC107'>"
                + texto
                + "</font>"
                + "</b><br>";
    }


    private String parrafo(String texto) {

        return "<font color='#FFFFFF'>"
                + texto
                + "</font><br><br>";
    }

    private String separador() {

        return "<br>"
                + "<hr>"
                + "<br>";
    }

    // ---------------------------------------------------------
    // BOTONES
    // ---------------------------------------------------------

    private void configurarBotones() {

        btnVolverMenu.setOnClickListener(v -> volverAlMenu());
    }

    // ---------------------------------------------------------
    // VOLVER AL MENÚ PRINCIPAL
    // ---------------------------------------------------------

    private void volverAlMenu() {

        Intent intent = new Intent(
                ReglasActivity.this,
                MainActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
        finish();
    }
}