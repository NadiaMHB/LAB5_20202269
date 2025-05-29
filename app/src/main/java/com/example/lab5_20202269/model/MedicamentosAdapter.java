package com.example.lab5_20202269.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Sí enseñado en Clase 03.2
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab5_20202269.R;
import com.example.lab5_20202269.model.Medicamento;

import java.util.List;

public class MedicamentosAdapter extends RecyclerView.Adapter<MedicamentosAdapter.ViewHolder> {

    private List<Medicamento> lista;
    private OnEliminarClickListener eliminarClickListener;

    public interface OnEliminarClickListener {
        void onEliminarClick(int position);
    }

    public MedicamentosAdapter(List<Medicamento> lista, OnEliminarClickListener listener) {
        this.lista = lista;
        this.eliminarClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicamento, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicamento m = lista.get(position);
        holder.tvNombre.setText(m.getNombre());
        holder.tvDetalle.setText(m.getTipo() + " - " + m.getDosis() + " unidades - Cada " + m.getFrecuenciaHoras() + " horas");
        holder.tvFecha.setText("Desde: " + m.getFechaHoraInicio());
        holder.btnEliminar.setOnClickListener(v -> eliminarClickListener.onEliminarClick(position));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDetalle, tvFecha;
        ImageButton btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDetalle = itemView.findViewById(R.id.tvDetalle);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}
