package com.example.consumoapp.factura.ui;

import com.example.consumoapp.core.factura.engine.ResultadoFactura;
import com.example.consumoapp.databinding.ActivitySimuladorFacturaBinding;

import java.util.Locale;

/**
 * Convierte un ResultadoFactura en textos visibles.
 * Mantiene el formato de presentación fuera de la Activity.
 */
public final class ResultadoFacturaRenderer {

    private final ActivitySimuladorFacturaBinding binding;

    public ResultadoFacturaRenderer(ActivitySimuladorFacturaBinding binding) {
        this.binding = binding;
    }

    public void mostrar(
            ResultadoFactura resultado,
            boolean reguladosSeparados,
            String fuentePrecios
    ) {
        Locale locale = Locale.getDefault();

        binding.txtTotalConsumo.setText(String.format(locale,
                "Consumo electrodomésticos: %.2f kWh", resultado.getConsumoTotalKwh()));
        binding.txtConsumoPeriodos.setText(String.format(locale,
                "P1 punta: %.2f kWh · P2 llano: %.2f kWh · P3 valle: %.2f kWh",
                resultado.getConsumoP1Kwh(), resultado.getConsumoP2Kwh(), resultado.getConsumoP3Kwh()));
        binding.txtCostePeriodos.setText(String.format(locale,
                "Coste P1: %.2f € · P2: %.2f € · P3: %.2f €",
                resultado.getCosteP1(), resultado.getCosteP2(), resultado.getCosteP3()));
        binding.txtTotalEnergia.setText(String.format(locale,
                "Energía consumida: %.2f €", resultado.getCosteEnergia()));
        binding.txtTotalPotencia.setText(String.format(locale,
                "Potencia contratada: %.2f €", resultado.getCostePotencia()));
        binding.txtTotalImpuestos.setText(String.format(locale,
                "Imp. electricidad + IVA: %.2f €",
                resultado.getImpuestoElectricidad() + resultado.getIva()));
        binding.txtTotalFactura.setText(String.format(locale,
                "Total aproximado: %.2f €", resultado.getTotalFactura()));

        String modoRegulado = reguladosSeparados
                ? String.format(locale, "Peajes: %.2f € · Cargos: %.2f €",
                resultado.getPeajesTotal(), resultado.getCargosTotal())
                : "Peajes y cargos incluidos en los precios seleccionados";

        binding.txtResumenDesglose.setText(String.format(locale,
                "Base energía+potencia: %.2f € · Alquiler contador: %.2f € · IEE: %.2f € · IVA: %.2f €\n%s\nFuente de precios: %s",
                resultado.getCosteEnergia() + resultado.getCostePotencia(),
                resultado.getAlquilerContador(),
                resultado.getImpuestoElectricidad(),
                resultado.getIva(),
                modoRegulado,
                fuentePrecios));
    }
}
