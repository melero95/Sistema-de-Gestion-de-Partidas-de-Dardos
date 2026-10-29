package activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.productos.juegosdedardos.R;
import java.util.ArrayList;
import java.util.List;
import java.text.DateFormat;
import java.util.Date;
import adapters.ResultadoHistoricoAdapter;
import modelos.Jugador;
import modelos.Partida;
import modelos.ResultadoPartida;
import sqlite.JugadorDao;
import sqlite.PartidaDao;
import sqlite.ResultadoPartidaDao;
import utilidades.AccionesPartidaHistorica;

public class DetallePartidaActivity extends AppCompatActivity {
    public static final String EXTRA_ID_PARTIDA = "idPartida";
    private Partida partida;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.blue));
        setContentView(R.layout.activity_detalle_partida);
        long id = getIntent().getLongExtra(EXTRA_ID_PARTIDA, -1L);
        if (id <= 0) { finish(); return; }
        try (PartidaDao dao = new PartidaDao(this)) { partida = dao.obtenerPorId(id); }
        if (partida == null) { Toast.makeText(this, "Partida no encontrada", Toast.LENGTH_SHORT).show(); finish(); return; }
        ((TextView) findViewById(R.id.txtModoDetalle)).setText(partida.getModo());
        ((TextView) findViewById(R.id.txtResumenDetalle)).setText(
                partida.getNumeroJugadores() + " participantes · " + partida.getRondasJugadas() +
                " de " + partida.getRondasConfiguradas() + " rondas");
        ((TextView) findViewById(R.id.txtFechaDetalle)).setText(DateFormat.getDateTimeInstance().format(new Date(partida.getFecha())));
        ((TextView) findViewById(R.id.txtMotivoDetalle)).setText(
                "Finalización: " + partida.getMotivoFinalizacion());
        RecyclerView recycler = findViewById(R.id.recyclerResultadosHistoricos);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        ResultadoHistoricoAdapter adapter = new ResultadoHistoricoAdapter();
        recycler.setAdapter(adapter);
        List<ResultadoHistoricoAdapter.Fila> filas = new ArrayList<>();
        try (ResultadoPartidaDao resultados = new ResultadoPartidaDao(this);
             JugadorDao jugadores = new JugadorDao(this)) {
            for (ResultadoPartida r : resultados.listarPorPartida(id)) {
                Jugador j = jugadores.obtenerPorId(r.getIdJugador());
                filas.add(new ResultadoHistoricoAdapter.Fila(r, j == null ? "Jugador desconocido" : j.getNombre()));
            }
        }
        adapter.actualizar(filas);
        findViewById(R.id.btnRepetirDetalle).setOnClickListener(v -> AccionesPartidaHistorica.repetir(this, partida));
        findViewById(R.id.btnEditarDetalle).setOnClickListener(v -> AccionesPartidaHistorica.editar(this, partida));
        findViewById(R.id.btnVolverDetalle).setOnClickListener(v -> finish());
    }
}
