package es.upm.miw.bantumi.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.datos.Resultado;

public class ResultadosAdapter extends RecyclerView.Adapter<ResultadosAdapter.ViewHolder> {

    private List<Resultado> listaResultados = new ArrayList<>();

    public void setResultados(List<Resultado> resultados) {
        this.listaResultados = resultados;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resultado, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Resultado res = listaResultados.get(position);
        holder.tvPosicion.setText("#" + (position + 1));
        holder.tvNombreJugador.setText(res.getNombreJugador());
        holder.tvFecha.setText(res.getFecha());
        holder.tvPuntuacion.setText(res.getSemillasJ1() + " - " + res.getSemillasJ2());
    }

    @Override
    public int getItemCount() {
        return listaResultados != null ? listaResultados.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosicion, tvNombreJugador, tvFecha, tvPuntuacion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosicion = itemView.findViewById(R.id.tvPosicion);
            tvNombreJugador = itemView.findViewById(R.id.tvNombreJugador);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvPuntuacion = itemView.findViewById(R.id.tvPuntuacion);
        }
    }
}