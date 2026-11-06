package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DiffUtil;
import com.productos.juegosdedardos.R;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import modelos.ResultadoPartida;
import modelos.Jugador;
import sqlite.JugadorDao;

/** Solo clasificación y puntuación final; estadísticas globales se implementarán en fase 8. */
public class ResultadoHistoricoAdapter extends RecyclerView.Adapter<ResultadoHistoricoAdapter.Vista> {
    public static class Fila {
        public final ResultadoPartida resultado;
        public final String nombre;
        public Fila(ResultadoPartida r, String nombre) { this.resultado = r; this.nombre = nombre; }
    }
    private final List<Fila> filas = new ArrayList<>();
    public void actualizar(List<Fila> nuevas) {
        List<Fila> anteriores = new ArrayList<>(filas);
        List<Fila> siguientes = new ArrayList<>(nuevas);

        DiffUtil.DiffResult cambios = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return anteriores.size(); }
            @Override public int getNewListSize() { return siguientes.size(); }

            @Override public boolean areItemsTheSame(int anterior, int siguiente) {
                ResultadoPartida a = anteriores.get(anterior).resultado;
                ResultadoPartida b = siguientes.get(siguiente).resultado;
                // Una partida tiene una única fila de resultado por jugador.
                return a.getIdPartida() == b.getIdPartida()
                        && a.getIdJugador() == b.getIdJugador();
            }

            @Override public boolean areContentsTheSame(int anterior, int siguiente) {
                Fila a = anteriores.get(anterior);
                Fila b = siguientes.get(siguiente);
                return Objects.equals(a.nombre, b.nombre)
                        && a.resultado.getPosicion() == b.resultado.getPosicion()
                        && Objects.equals(a.resultado.getPuntuacionFinal(),
                                b.resultado.getPuntuacionFinal());
            }
        });

        filas.clear();
        filas.addAll(siguientes);
        cambios.dispatchUpdatesTo(this);
    }
    @NonNull @Override public Vista onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vista(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resultado_historico, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Vista holder, int position) {
        Fila fila = filas.get(position);
        holder.posicion.setText(holder.itemView.getContext().getString(R.string.resultado_historico_posicion, fila.resultado.getPosicion()));
        holder.nombre.setText(fila.nombre);
        Integer puntos = fila.resultado.getPuntuacionFinal();
        holder.puntuacion.setText(puntos == null ? "—" : String.valueOf(puntos));
    }
    @Override public int getItemCount() { return filas.size(); }
    static class Vista extends RecyclerView.ViewHolder {
        TextView posicion, nombre, puntuacion;
        Vista(View v) { super(v);
            posicion = v.findViewById(R.id.txtPosicionHistorico);
            nombre = v.findViewById(R.id.txtJugadorHistorico);
            puntuacion = v.findViewById(R.id.txtPuntuacionHistorico);
        }
    }
}
