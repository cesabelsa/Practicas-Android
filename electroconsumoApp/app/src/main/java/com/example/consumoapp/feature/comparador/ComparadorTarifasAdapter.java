package com.example.consumoapp.feature.comparador;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.consumoapp.databinding.ItemResultadoComparacionBinding;

import java.util.List;
import java.util.Locale;

/** Muestra el ranking de tarifas calculadas con exactamente el mismo consumo. */
public class ComparadorTarifasAdapter extends RecyclerView.Adapter<ComparadorTarifasAdapter.Holder> {
    private final List<ResultadoComparacion> resultados;
    private final OnDetalleTarifaClickListener listener;

    public interface OnDetalleTarifaClickListener {
        void onVerDetalle(ResultadoComparacion resultado);
    }

    public ComparadorTarifasAdapter(List<ResultadoComparacion> resultados,
                                    OnDetalleTarifaClickListener listener) {
        this.resultados = resultados;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemResultadoComparacionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(resultados.get(position), position, listener);
    }

    @Override
    public int getItemCount() { return resultados.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        private final ItemResultadoComparacionBinding binding;

        Holder(ItemResultadoComparacionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ResultadoComparacion resultado, int position, OnDetalleTarifaClickListener listener) {
            binding.txtPuesto.setText(position == 0 ? "1º · Mejor resultado" : (position + 1) + "º");
            binding.txtComercializadora.setText(resultado.getComercializadora());
            binding.txtTarifa.setText(resultado.getTarifa());
            binding.txtTotalComparacion.setText(String.format(Locale.getDefault(),
                    "%.2f €", resultado.getFactura().getTotalFactura()));
            binding.txtPeriodoComparacion.setText(String.format(Locale.getDefault(),
                    "Coste para %d días · %.2f €/mes · %.2f €/año",
                    resultado.getDiasPeriodo(), resultado.getCosteMensualEstimado(),
                    resultado.getCosteAnualEstimado()));
            binding.txtDetalleComparacion.setText(String.format(Locale.getDefault(),
                    "Energía %.2f € · Potencia %.2f € · Impuestos %.2f €",
                    resultado.getFactura().getCosteEnergia(),
                    resultado.getFactura().getCostePotencia(),
                    resultado.getFactura().getImpuestoElectricidad() + resultado.getFactura().getIva()));
            binding.txtPreciosTarifa.setText(String.format(Locale.getDefault(),
                    "Energía: P1 %.4f · P2 %.4f · P3 %.4f €/kWh",
                    resultado.getPrecioP1(), resultado.getPrecioP2(), resultado.getPrecioP3()));
            binding.txtCondicionesTarifa.setText(
                    "Permanencia: " + resultado.getPermanencia()
                            + "\nDescuento: " + resultado.getDescuento()
                            + "\nServicios: " + resultado.getServicios());
            binding.txtActualizacionTarifa.setText(
                    "Actualización: " + resultado.getFechaActualizacion()
                            + " · Fuente: " + resultado.getFuente());
            binding.txtObservacionesTarifa.setText(resultado.getObservaciones());
            binding.txtObservacionesTarifa.setVisibility(
                    "No informado".equals(resultado.getObservaciones()) ? View.GONE : View.VISIBLE);

            binding.btnVerDetalleTarifa.setOnClickListener(v -> {
                if (listener != null) listener.onVerDetalle(resultado);
            });

            if (resultado.getAhorroPeriodo() > 0.005) {
                binding.txtAhorro.setText(String.format(Locale.getDefault(),
                        "Ahorro estimado: %.2f € en el periodo · %.2f €/año",
                        resultado.getAhorroPeriodo(), resultado.getAhorroAnual()));
            } else if (resultado.getAhorroPeriodo() < -0.005) {
                binding.txtAhorro.setText(String.format(Locale.getDefault(),
                        "Costaría %.2f € más en el periodo · %.2f €/año más",
                        Math.abs(resultado.getAhorroPeriodo()), Math.abs(resultado.getAhorroAnual())));
            } else {
                binding.txtAhorro.setText("Coste similar a la tarifa de referencia");
            }
        }
    }
}
