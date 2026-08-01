package com.example.consumoapp.factura;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.consumoapp.databinding.ItemSimulacionFacturaBinding;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Adaptador del historial de simulaciones guardadas. */
public class HistorialSimulacionesAdapter extends RecyclerView.Adapter<HistorialSimulacionesAdapter.ViewHolder> {

    public interface Listener {
        void onVerFactura(SimulacionFacturaEntity simulacion);
        void onEliminar(SimulacionFacturaEntity simulacion);
    }

    private final List<SimulacionFacturaEntity> datos = new ArrayList<>();
    private final Listener listener;

    public HistorialSimulacionesAdapter(Listener listener) {
        this.listener = listener;
    }

    public void actualizar(List<SimulacionFacturaEntity> nuevas) {
        datos.clear();
        datos.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSimulacionFacturaBinding binding = ItemSimulacionFacturaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.vincular(datos.get(position));
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSimulacionFacturaBinding binding;

        ViewHolder(ItemSimulacionFacturaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void vincular(SimulacionFacturaEntity item) {
            String fecha = DateFormat.getDateTimeInstance(
                    DateFormat.SHORT,
                    DateFormat.SHORT
            ).format(new Date(item.getFechaCreacion()));

            binding.txtNombreSimulacion.setText(item.getNombre());
            binding.txtFechaSimulacion.setText(fecha);
            binding.txtFuenteSimulacion.setText(item.getFuentePrecio());
            binding.txtTotalSimulacion.setText(String.format(
                    Locale.getDefault(), "%.2f €", item.getTotalFactura()
            ));
            binding.txtDetalleSimulacion.setText(String.format(
                    Locale.getDefault(),
                    "Consumo: %.2f kWh · P1 %.2f · P2 %.2f · P3 %.2f\n" +
                            "Energía: %.2f € · Potencia: %.2f € · Peajes: %.2f € · Cargos: %.2f €\n" +
                            "Impuestos: %.2f € · %d días · %d electrodomésticos",
                    item.getConsumoTotalKwh(), item.getConsumoP1Kwh(), item.getConsumoP2Kwh(),
                    item.getConsumoP3Kwh(), item.getCosteEnergia(), item.getCostePotencia(),
                    item.getPeajesTotal(), item.getCargosTotal(),
                    item.getImpuestoElectricidad() + item.getIva(), item.getDiasFactura(),
                    item.getNumeroElectrodomesticos()
            ));
            binding.btnVerFactura.setOnClickListener(v -> listener.onVerFactura(item));
            binding.getRoot().setOnClickListener(v -> listener.onVerFactura(item));
            binding.btnEliminarSimulacion.setOnClickListener(v -> listener.onEliminar(item));
        }
    }
}
