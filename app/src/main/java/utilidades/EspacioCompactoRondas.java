package utilidades;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.widget.NestedScrollView;

import com.productos.juegosdedardos.R;

/**
 * Ajuste excepcional del espacio central compartido por Double Down,
 * Around the Clock y Shanghai. Conserva las botoneras fijas y sus botones.
 */
public final class EspacioCompactoRondas {

    private EspacioCompactoRondas() { }

    public static void ajustarSiNecesario(Activity actividad) {
        NestedScrollView scroll = actividad.findViewById(R.id.scrollPartidaRondas);
        if (scroll == null) return;
        scroll.post(() -> {
            if (scroll.getChildCount() == 0) return;
            int altoVisible = scroll.getHeight() - scroll.getPaddingTop()
                    - scroll.getPaddingBottom();
            if (altoVisible <= 0) return;

            // Compactacion solo en pantallas realmente pequenas: en equipos
            // normales la apariencia del XML no cambia.
            if (altoVisible >= dp(actividad, 350)) return;
            View contenido = scroll.getChildAt(0);
            if (contenido.getHeight() <= altoVisible) return;

            reducirAltura(actividad.findViewById(R.id.cardModoJuego), dp(actividad, 38));
            reducirAltura(actividad.findViewById(R.id.cardTurnoJugador), dp(actividad, 30));
            View objetivo = actividad.findViewById(R.id.marcoObjetivoRonda);
            reducirAltura(objetivo, dp(actividad, 80));
            if (objetivo != null
                    && objetivo.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams lp =
                        (ViewGroup.MarginLayoutParams) objetivo.getLayoutParams();
                int margen = dp(actividad, 8);
                if (lp.topMargin > margen) {
                    lp.topMargin = margen;
                    objetivo.setLayoutParams(lp);
                }
            }
            // El contenido central conserva su desplazamiento vertical.
        });
    }

    private static void reducirAltura(View vista, int nuevaAltura) {
        if (vista == null) return;
        ViewGroup.LayoutParams lp = vista.getLayoutParams();
        if (lp.height > nuevaAltura) {
            lp.height = nuevaAltura;
            vista.setLayoutParams(lp);
        }
    }

    private static int dp(Activity actividad, int valor) {
        return (int) (valor * actividad.getResources().getDisplayMetrics().density + 0.5f);
    }
}
