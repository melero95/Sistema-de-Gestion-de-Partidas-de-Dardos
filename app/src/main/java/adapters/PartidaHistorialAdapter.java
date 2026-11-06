package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DiffUtil;
import com.productos.juegosdedardos.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        List<PartidaResumenDTO> anteriores = new ArrayList<>(items);
        List<PartidaResumenDTO> siguientes = new ArrayList<>(nuevos);

        DiffUtil.DiffResult cambios = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return anteriores.size(); }
            @Override public int getNewListSize() { return siguientes.size(); }

            @Override public boolean areItemsTheSame(int anterior, int siguiente) {
                return anteriores.get(anterior).getId() == siguientes.get(siguiente).getId();
            }

            @Override public boolean areContentsTheSame(int anterior, int siguiente) {
                PartidaResumenDTO a = anteriores.get(anterior);
                PartidaResumenDTO b = siguientes.get(siguiente);
                modelos.Partida pa = a.getPartida();
                modelos.Partida pb = b.getPartida();
                // Incluye los datos empleados al abrir y repetir la partida.
                return Objects.equals(a.getGanador(), b.getGanador())
                        && Objects.equals(pa.getModo(), pb.getModo())
                        && pa.getNumeroJugadores() == pb.getNumeroJugadores()
                        && pa.getFecha() == pb.getFecha()
                        && pa.getRondasConfiguradas() == pb.getRondasConfiguradas()
                        && pa.getRondasJugadas() == pb.getRondasJugadas()
                        && Objects.equals(pa.getIdGanador(), pb.getIdGanador())
                        && Objects.equals(pa.getMotivoFinalizacion(), pb.getMotivoFinalizacion())
                        && Objects.equals(pa.getConfiguracionJson(), pb.getConfiguracionJson());
            }
        });

        items.clear();
        items.addAll(siguientes);
        cambios.dispatchUpdatesTo(this);
    }
    @NonNull @Override public Vista onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Vista(LayoutInflater.from(padre.getContext()).inflate(R.layout.item_partida_historial, padre, false));
    }
    @Override public void onBindViewHolder(@NonNull Vista v, int posicion) {
        PartidaResumenDTO item = items.get(posicion);
        v.modo.setText(item.getPartida().getModo());
        v.ganador.setText(v.itemView.getContext().getString(R.string.historial_item_ganador, item.getGanador()));
        int participantes = item.getPartida().getNumeroJugadores();
        v.jugadores.setText(v.itemView.getResources().getQuantityString(
                R.plurals.lint06_historial_participantes, participantes, participantes));
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
