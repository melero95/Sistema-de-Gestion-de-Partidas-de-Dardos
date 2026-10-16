package activities;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.productos.juegosdedardos.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/** Pruebas básicas de navegación, sin modificar los datos de SQLite. */
@RunWith(AndroidJUnit4.class)
public class JugadoresActivityTest {

    @Rule
    public ActivityScenarioRule<JugadoresActivity> escenario =
            new ActivityScenarioRule<>(JugadoresActivity.class);

    @Test
    public void muestraPantallaYBotonesPrincipales() {
        onView(withId(R.id.txtTituloJugadores))
                .check(matches(withText("Gestión de jugadores")));
        onView(withId(R.id.btnNuevoJugador))
                .check(matches(isDisplayed()));
        onView(withId(R.id.btnVolverJugadores))
                .check(matches(isDisplayed()));
    }

    @Test
    public void abreDialogoDeAltaJugador() {
        onView(withId(R.id.btnNuevoJugador)).perform(click());
        onView(withId(R.id.editNombreJugador))
                .check(matches(isDisplayed()));
        onView(withId(R.id.gridColoresJugador))
                .check(matches(isDisplayed()));
    }
}
