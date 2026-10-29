package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.productos.juegosdedardos.R;
import java.util.ArrayList;
import java.util.List;
import modelos.PartidaResumenDTO;

public class PartidaHistorialAdapter extends RecyclerView.Adapter<PartidaHistorialAdapter.Vista> {
    public interface Eventos {
        void abrir(PartidaResumenDTO partida);
        void menu(View ancla, PartidaResumenDTO partida);
    }
    private final List<PartidaResumenDTO> items = new ArrayList<>();
    private final Eventos eventos;
    public PartidaHistorialAdapter(Eventos eventos) { this.eventos = eventos; }
    public void actualizar(List<PartidaResumenDTO> nuevos) {
        items.clear(); items.addAll(nuevos); notifyDataSetChanged();
    }
    @NonNull @Override public Vista onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Vista(LayoutInflater.from(padre.getContext()).inflate(R.layout.item_partida_historial, padre, false));
    }
    @Override public void onBindViewHolder(@NonNull Vista v, int posicion) {
        PartidaResumenDTO item = items.get(posicion);
        v.modo.setText(item.getPartida().getModo());
        v.ganador.setText("Ganador: " + item.getGanador());
        v.jugadores.setText(item.getPartida().getNumeroJugadores() + " participante(s)");
        v.itemView.setOnClickListener(view -> eventos.abrir(item));
        v.itemView.setOnLongClickListener(view -> { eventos.menu(view, item); return true; });
    }
    @Override public int getItemCount() { return items.size(); }
    static class Vista extends RecyclerView.ViewHolder {
        final TextView modo, ganador, jugadores;
        Vista(View v) { super(v);
            modo = v.findViewById(R.id.txtModoItemHistorial);
            ganador = v.findViewById(R.id.txtGanadorItemHistorial);
            jugadores = v.findViewById(R.id.txtParticipantesItemHistorial);
        }
    }
}
