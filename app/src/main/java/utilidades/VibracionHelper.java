package utilidades;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import activities.AjustesActivity;

/**
 * Avisos hápticos de las partidas de dardos.
 * Solo vibra si Ajustes > Vibración está activado y el móvil lo permite.
 * minSdk 28: todos los dispositivos soportados disponen de VibrationEffect.
 */
public final class VibracionHelper {

    // Duraciones algo más largas para que sean perceptibles en móviles reales.
    private static final long ERROR_MS = 300L;
    // Dos pulsos: resultado/fin de partida.
    private static final long[] VICTORIA = {0L, 240L, 150L, 340L};
    // Tres pulsos: 180 (tres triples al 20).
    private static final long[] CIENTO_OCHENTA =
            {0L, 190L, 100L, 190L, 100L, 270L};
    // Una penalización o turno totalmente fallido y después el fin de partida.
    private static final long[] ERROR_Y_VICTORIA =
            {0L, 300L, 170L, 240L, 150L, 340L};
    // Si el 180 coincide con la última tirada, anunciar también la victoria.
    private static final long[] CIENTO_OCHENTA_Y_VICTORIA =
            {0L, 190L, 100L, 190L, 100L, 270L, 170L, 240L, 150L, 340L};

    private VibracionHelper() {
        // Solo métodos estáticos.
    }

    /** Bust, penalización en Double Down o turno terminado sin acertar un dardo. */
    public static void vibrarError(Context contexto) {
        vibrar(contexto, VibrationEffect.createOneShot(
                ERROR_MS, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    /** Tres triples al 20 registrados en el mismo turno. */
    public static void vibrar180(Context contexto) {
        vibrar(contexto, VibrationEffect.createWaveform(CIENTO_OCHENTA, -1));
    }

    /** Partida finalizada: dos pulsos largos. */
    public static void vibrarVictoria(Context contexto) {
        vibrar(contexto, VibrationEffect.createWaveform(VICTORIA, -1));
    }

    /** Combina fallo/penalización final con resultado, sin cortar un aviso con otro. */
    public static void vibrarPenalizacionYVictoria(Context contexto) {
        vibrar(contexto, VibrationEffect.createWaveform(ERROR_Y_VICTORIA, -1));
    }

    /** Combina un 180 final y el resultado. */
    public static void vibrar180YVictoria(Context contexto) {
        vibrar(contexto, VibrationEffect.createWaveform(CIENTO_OCHENTA_Y_VICTORIA, -1));
    }

    private static void vibrar(Context contexto, VibrationEffect efecto) {
        if (contexto == null || !contexto.getSharedPreferences(
                AjustesActivity.SP_AJUSTES, Context.MODE_PRIVATE)
                .getBoolean(AjustesActivity.PREF_VIBRACION, true)) {
            return;
        }

        final Vibrator vibrador;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager gestor = (VibratorManager) contexto.getSystemService(
                    Context.VIBRATOR_MANAGER_SERVICE);
            vibrador = gestor == null ? null : gestor.getDefaultVibrator();
        } else {
            vibrador = (Vibrator) contexto.getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (vibrador == null || !vibrador.hasVibrator()) {
            return;
        }

        try {
            vibrador.vibrate(efecto);
        } catch (SecurityException | IllegalStateException ignorada) {
            // No se interrumpe la partida si la vibración no está disponible.
        }
    }
}
