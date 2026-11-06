package activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.productos.juegosdedardos.R;

public class AcercaDeActivity extends AppCompatActivity {

    private static final String URL_POLITICA_PRIVACIDAD =
            "https://melero95.github.io/Sistema-de-Gestion-de-Partidas-de-Dardos/";

    private Button btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acerca_de);

        EdgeToEdge.enable(this);

        // Enlazar componentes
        btnVolver = findViewById(R.id.btnVolverMenuAcercaDe);

        // Volver al menú principal
        btnVolver.setOnClickListener(v -> finish());

        // Mostrar la política de privacidad publicada en GitHub Pages.
        findViewById(R.id.btnPoliticaPrivacidad).setOnClickListener(v -> {
            Intent navegador = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_POLITICA_PRIVACIDAD));
            try {
                startActivity(navegador);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(this, R.string.privacidad_sin_navegador, Toast.LENGTH_LONG).show();
            }
        });
    }
}
