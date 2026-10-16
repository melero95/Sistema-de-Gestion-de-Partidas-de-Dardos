package adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.productos.juegosdedardos.R;

import java.util.ArrayList;
import java.util.List;

import modelos.Jugador;

/** Adaptador de la lista de jugadores activos e inactivos. */
public class JugadorAdapter extends BaseAdapter {

    public interface AccionesJugador {
        void onEditar(Jugador jugador);
        void onCambiarEstado(Jugador jugador);
    }

    private final List<Jugador> jugadores = new ArrayList<>();
    private final LayoutInflater inflater;
    private final AccionesJugador acciones;

    public JugadorAdapter(Context contexto, AccionesJugador acciones) {
        this.inflater = LayoutInflater.from(contexto);
        this.acciones = acciones;
    }

    public void actualizarJugadores(List<Jugador> nuevos) {
        jugadores.clear();
        jugadores.addAll(nuevos);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return jugadores.size(); }

    @Override
    public Jugador getItem(int position) { return jugadores.get(position); }

    @Override
    public long getItemId(int position) { return getItem(position).getId(); }

    @Override
    public boolean hasStableIds() { return true; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View fila = convertView;
        if (fila == null) {
            fila = inflater.inflate(R.layout.item_jugador_gestion, parent, false);
        }
        Jugador jugador = getItem(position);

        View indicador = fila.findViewById(R.id.indicadorColorJugador);
        TextView txtNombre = fila.findViewById(R.id.txtNombreJugadorGestion);
        TextView txtEstado = fila.findViewById(R.id.txtEstadoJugadorGestion);
        Button btnEditar = fila.findViewById(R.id.btnEditarJugadorGestion);
        Button btnEstado = fila.findViewById(R.id.btnEstadoJugadorGestion);

        GradientDrawable puntoColor = new GradientDrawable();
        puntoColor.setShape(GradientDrawable.OVAL);
        puntoColor.setColor(jugador.getColor());
        puntoColor.setStroke(2, 0xFFFFFFFF);
        indicador.setBackground(puntoColor);

        txtNombre.setText(jugador.getNombre());
        txtNombre.setAlpha(jugador.isActivo() ? 1f : 0.65f);
        txtEstado.setText(jugador.isActivo() ? "ACTIVO" : "INACTIVO");
        txtEstado.setTextColor(jugador.isActivo() ? 0xFFB7FFCC : 0xFFFFD69C);
        btnEstado.setText(jugador.isActivo() ? "Desactivar" : "Reactivar");

        btnEditar.setOnClickListener(v -> acciones.onEditar(jugador));
        btnEstado.setOnClickListener(v -> acciones.onCambiarEstado(jugador));
        return fila;
    }
}
