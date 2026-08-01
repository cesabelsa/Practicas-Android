package com.example.consumoapp.factura.ui;

import android.app.Activity;
import android.content.Intent;

import com.example.consumoapp.core.factura.engine.ResultadoFactura;
import com.example.consumoapp.factura.LineaSimuladorFactura;
import com.example.consumoapp.feature.comparador.ComparadorTarifasActivity;

import java.util.List;

/** Construye y abre el comparador sin exponer sus extras a la Activity. */
public final class ComparadorTarifasNavigator {

    private ComparadorTarifasNavigator() { }

    public static void abrir(
            Activity activity,
            List<LineaSimuladorFactura> lineas,
            FacturaCalculationController calculo,
            String referenciaNombre
    ) {
        ResultadoFactura resultado = calculo.getUltimoResultado();
        if (resultado == null) return;

        Intent intent = new Intent(activity, ComparadorTarifasActivity.class);
        intent.putExtra(ComparadorTarifasActivity.EXTRA_CONSUMO_P1, resultado.getConsumoP1Kwh());
        intent.putExtra(ComparadorTarifasActivity.EXTRA_CONSUMO_P2, resultado.getConsumoP2Kwh());
        intent.putExtra(ComparadorTarifasActivity.EXTRA_CONSUMO_P3, resultado.getConsumoP3Kwh());
        intent.putExtra(ComparadorTarifasActivity.EXTRA_POTENCIA_P1, calculo.getPotenciaPuntaKw());
        intent.putExtra(ComparadorTarifasActivity.EXTRA_POTENCIA_P2, calculo.getPotenciaValleKw());
        intent.putExtra(ComparadorTarifasActivity.EXTRA_DIAS, calculo.getDiasFactura());
        intent.putExtra(ComparadorTarifasActivity.EXTRA_IEE, calculo.getImpuestoElectricidadPct());
        intent.putExtra(ComparadorTarifasActivity.EXTRA_IVA, calculo.getIvaPct());
        intent.putExtra(ComparadorTarifasActivity.EXTRA_TOTAL_ACTUAL, resultado.getTotalFactura());
        intent.putExtra(ComparadorTarifasActivity.EXTRA_NUM_ELECTRODOMESTICOS, lineas.size());
        intent.putExtra(ComparadorTarifasActivity.EXTRA_REFERENCIA_NOMBRE, referenciaNombre);
        activity.startActivity(intent);
    }
}
