package com.example.consumoapp.factura;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.consumoapp.databinding.ItemLineaSimuladorFacturaBinding;

import java.util.List;
import java.util.Locale;

/**
 * Adapter del RecyclerView que actúa como tabla dinámica.
 */
public class LineaSimuladorAdapter extends RecyclerView.Adapter<LineaSimuladorAdapter.LineaViewHolder> {

    private final List<LineaSimuladorFactura> lineas;
    private final OnLineaClickListener listener;

    public interface OnLineaClickListener {
        void onEliminarLinea(int posicion);
    }

    public LineaSimuladorAdapter(List<LineaSimuladorFactura> lineas, OnLineaClickListener listener) {
        this.lineas = lineas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LineaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLineaSimuladorFacturaBinding binding = ItemLineaSimuladorFacturaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new LineaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LineaViewHolder holder, int position) {
        holder.bind(lineas.get(position));
    }

    @Override
    public int getItemCount() {
        return lineas.size();
    }

    class LineaViewHolder extends RecyclerView.ViewHolder {

        private final ItemLineaSimuladorFacturaBinding binding;

        LineaViewHolder(ItemLineaSimuladorFacturaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LineaSimuladorFactura linea) {
            binding.txtLineaNombre.setText(linea.getElectrodomestico());
            binding.txtLineaDetalle.setText(String.format(
                    Locale.getDefault(),
                    "%.0f W · uso %.0f %% · %.2f h/día · %d días · %s",
                    linea.getPotenciaW(),
                    linea.getPorcentajeUso(),
                    linea.getHorasDia(),
                    linea.getDiasMes(),
                    linea.getResumenPeriodos()
            ));
            binding.txtLineaConsumo.setText(String.format(Locale.getDefault(), "%.2f kWh", linea.getConsumoKwh()));
            binding.txtLineaCoste.setText("El coste se calcula en Factura");

            binding.btnEliminarLinea.setOnClickListener(v -> {
                int posicion = getBindingAdapterPosition();
                if (posicion != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEliminarLinea(posicion);
                }
            });
        }
    }
}
