package utilidades;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.productos.juegosdedardos.R;

/** Aplica las barras del sistema sin eliminar los paddings originales del XML. */
public final class InsetsSistema {

    private InsetsSistema() { }

    public static void aplicar(Activity actividad) {
        View contenido = actividad.findViewById(android.R.id.content);
        if (!(contenido instanceof ViewGroup)) return;
        ViewGroup marco = (ViewGroup) contenido;
        if (marco.getChildCount() == 0) return;

        View raiz = marco.getChildAt(0);
        // El menu principal usa DrawerLayout: adaptar solo el contenido, no
        // el DrawerLayout entero, para respetar la gestion de sus cajones.
        if (raiz instanceof androidx.drawerlayout.widget.DrawerLayout) {
            View principal = actividad.findViewById(R.id.main);
            if (principal != null) raiz = principal;
        }

        final View objetivo = raiz;
        final int izquierda = objetivo.getPaddingLeft();
        final int arriba = objetivo.getPaddingTop();
        final int derecha = objetivo.getPaddingRight();
        final int abajo = objetivo.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(objetivo, (vista, windowInsets) -> {
            Insets barras = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());
            vista.setPadding(izquierda + barras.left,
                    arriba + barras.top,
                    derecha + barras.right,
                    abajo + barras.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(objetivo);
    }
}
