package activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.productos.juegosdedardos.R;

public class AcercaDeActivity extends AppCompatActivity {

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
    }
}