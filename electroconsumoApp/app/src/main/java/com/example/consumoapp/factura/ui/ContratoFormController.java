package com.example.consumoapp.factura.ui;

import com.example.consumoapp.R;
import com.example.consumoapp.databinding.ActivitySimuladorFacturaBinding;
import com.example.consumoapp.regulacion.domain.ConstantesFactura;

import java.util.Locale;

/**
 * Gestiona exclusivamente los campos del contrato eléctrico mostrados en el simulador.
 * No ejecuta cálculos ni accede a Room.
 */
public final class ContratoFormController {

    private final ActivitySimuladorFacturaBinding binding;
    private final Runnable alCambiarContrato;

    public ContratoFormController(
            ActivitySimuladorFacturaBinding binding,
            Runnable alCambiarContrato
    ) {
        this.binding = binding;
        this.alCambiarContrato = alCambiarContrato;
    }

    /** Valores editables del hogar usados como punto de partida. */
    public void cargarValoresPorDefecto() {
        binding.edtPotenciaPuntaKw.setText("3.45");
        binding.edtPotenciaValleKw.setText("3.45");
        binding.edtDiasFactura.setText("30");
    }

    /** Muestra en el formulario las constantes reguladas recuperadas por el ViewModel. */
    public void mostrarConstantes(ConstantesFactura constantes) {
        if (constantes == null) {
            return;
        }

        binding.edtPrecioPotenciaPunta.setText(String.format(
                Locale.US, "%.6f", constantes.getPotenciaP1Dia()));
        binding.edtPrecioPotenciaValle.setText(String.format(
                Locale.US, "%.6f", constantes.getPotenciaP2Dia()));
        binding.edtAlquilerContadorDia.setText(String.format(
                Locale.US, "%.6f", constantes.getAlquilerContadorDia()));
        binding.edtImpuestoElectricidad.setText(String.format(
                Locale.US, "%.8f", constantes.getImpuestoElectricidadPct()));
        binding.edtIva.setText(String.format(
                Locale.US, "%.2f", constantes.getIvaPct()));

        if (alCambiarContrato != null) {
            alCambiarContrato.run();
        }
    }

    /** Evita iniciar cargas duplicadas mientras existe una operación pendiente. */
    public void mostrarCargando(boolean cargando) {
        binding.btnCargarConstantes.setEnabled(!cargando);
    }

    /** Lee y valida el precio manual permitiendo coma decimal. */
    public Double leerPrecioManual() {
        String texto = binding.edtPrecioKwh.getText().toString();
        if (texto == null || texto.trim().isEmpty()) {
            binding.edtPrecioKwh.setError(binding.getRoot().getContext().getString(R.string.simulador_error_precio_manual));
            return null;
        }

        try {
            double precio = Double.parseDouble(texto.trim().replace(',', '.'));
            if (!Double.isFinite(precio) || precio < 0.0) {
                binding.edtPrecioKwh.setError(binding.getRoot().getContext().getString(R.string.simulador_error_precio_manual));
                return null;
            }
            binding.edtPrecioKwh.setError(null);
            return precio;
        } catch (NumberFormatException error) {
            binding.edtPrecioKwh.setError(binding.getRoot().getContext().getString(R.string.simulador_error_precio_manual));
            return null;
        }
    }
}
