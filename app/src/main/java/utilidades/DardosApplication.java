package utilidades;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import java.util.WeakHashMap;

import activities.AcercaDeActivity;
import activities.AjustesActivity;
import activities.AyudaActivity;
import activities.PartidaRondasActivity;
import activities.ReglasActivity;

/**
 * Proteccion comun frente a las barras del sistema (edge-to-edge).
 * No cambia las Activity existentes: se aplica despues de que carguen su layout.
 */
public final class DardosApplication extends Application
        implements Application.ActivityLifecycleCallbacks {

    private final WeakHashMap<Activity, Boolean> actividadesPreparadas = new WeakHashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        // Ajustes tiene su solucion propia, ya probada: no se modifica.
        if (activity instanceof AjustesActivity) return;

        // En Android 14 o inferior solo aplicamos el ajuste a pantallas que
        // ya activaban expresamente EdgeToEdge, evitando dobles margenes.
        boolean edgeToEdgeExplicito = activity instanceof AyudaActivity
                || activity instanceof AcercaDeActivity
                || activity instanceof ReglasActivity;
        if (Build.VERSION.SDK_INT < 35 && !edgeToEdgeExplicito) return;

        if (actividadesPreparadas.put(activity, Boolean.TRUE) != null) return;

        View decor = activity.getWindow().getDecorView();
        decor.post(() -> {
            if (activity.isFinishing() || activity.isDestroyed()) return;
            InsetsSistema.aplicar(activity);
            if (activity instanceof PartidaRondasActivity) {
                EspacioCompactoRondas.ajustarSiNecesario(activity);
            }
        });
    }

    @Override public void onActivityCreated(Activity activity, Bundle state) { }
    @Override public void onActivityStarted(Activity activity) { }
    @Override public void onActivityPaused(Activity activity) { }
    @Override public void onActivityStopped(Activity activity) { }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) { }
    @Override public void onActivityDestroyed(Activity activity) {
        actividadesPreparadas.remove(activity);
    }
}
