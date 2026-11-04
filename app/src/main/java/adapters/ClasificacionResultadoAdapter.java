package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import modelos.ResultadoJugador;
import com.productos.juegosdedardos.R;

public class ClasificacionResultadoAdapter
        extends RecyclerView.Adapter<ClasificacionResultadoAdapter.ResultadoViewHolder> {

    private final ArrayList<ResultadoJugador> listaJugadores;

    //Constructor --------------------
    public ClasificacionResultadoAdapter(ArrayList<ResultadoJugador> listaJugadores) {
        this.listaJugadores = listaJugadores;
    }

    //Crear fila --------------------
    @NonNull
    @Override
    public ResultadoViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_clasificacion_resultado, parent, false);

        return new ResultadoViewHolder(vista);
    }

    //Rellenar fila --------------------
    @Override
    public void onBindViewHolder(
            @NonNull ResultadoViewHolder holder,
            int position) {

        ResultadoJugador jugador = listaJugadores.get(position);

        int posicionJugador = jugador.getPosicion();

        holder.txtPosicion.setText(holder.itemView.getContext().getString(R.string.lint06_clasificacion_posicion, posicionJugador));
        holder.txtNombre.setText(jugador.getNombre());

        configurarMedalla(holder, posicionJugador);
        configurarPuntuacion(holder, jugador);
    }

    //Configurar medalla --------------------
    private void configurarMedalla(
            @NonNull ResultadoViewHolder holder,
            int posicionJugador) {

        if (posicionJugador == 2) {

            holder.imgMedalla.setVisibility(View.VISIBLE);
            holder.imgMedalla.setImageResource(R.drawable.icono_plata);
            holder.imgMedalla.setContentDescription(holder.itemView.getContext().getString(R.string.lint06_medalla_plata));

        } else if (posicionJugador == 3) {

            holder.imgMedalla.setVisibility(View.VISIBLE);
            holder.imgMedalla.setImageResource(R.drawable.icono_bronce);
            holder.imgMedalla.setContentDescription(holder.itemView.getContext().getString(R.string.lint06_medalla_bronce));

        } else {

            holder.imgMedalla.setVisibility(View.INVISIBLE);
            holder.imgMedalla.setImageDrawable(null);
            holder.imgMedalla.setContentDescription(null);
        }
    }

    //Configurar puntuación --------------------
    private void configurarPuntuacion(
            @NonNull ResultadoViewHolder holder,
            @NonNull ResultadoJugador jugador) {

        if (jugador.isMostrarPuntuacion()) {

            holder.txtPuntos.setVisibility(View.VISIBLE);
            holder.txtPuntos.setText(
                    holder.itemView.getResources().getQuantityString(
                            R.plurals.lint06_clasificacion_puntuacion,
                            jugador.getPuntuacion(),
                            jugador.getPuntuacion()
                    )
            );

        } else {

            holder.txtPuntos.setVisibility(View.GONE);
        }
    }

    //Obtener cantidad --------------------
    @Override
    public int getItemCount() {
        return listaJugadores.size();
    }

    //Actualizar clasificación --------------------
    public void actualizarLista(
            ArrayList<ResultadoJugador> nuevaLista
    ) {

        ArrayList<ResultadoJugador> copiaLista =
                new ArrayList<>(nuevaLista);

        listaJugadores.clear();
        listaJugadores.addAll(copiaLista);

        notifyDataSetChanged();
    }

    //ViewHolder --------------------
    static class ResultadoViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgMedalla;
        private final TextView txtPosicion;
        private final TextView txtNombre;
        private final TextView txtPuntos;

        public ResultadoViewHolder(@NonNull View itemView) {
            super(itemView);

            imgMedalla = itemView.findViewById(
                    R.id.imgMedallaClasificacion
            );

            txtPosicion = itemView.findViewById(
                    R.id.txtPosicionClasificacion
            );

            txtNombre = itemView.findViewById(
                    R.id.txtNombreJugadorClasificacion
            );

            txtPuntos = itemView.findViewById(
                    R.id.txtPuntosJugadorClasificacion
            );
        }
    }
}