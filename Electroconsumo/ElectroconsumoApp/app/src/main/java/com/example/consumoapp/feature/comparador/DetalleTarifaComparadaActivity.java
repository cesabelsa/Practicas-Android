package com.example.consumoapp.feature.comparador;

import com.example.consumoapp.NavigationUtils;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.consumoapp.R;
import com.example.consumoapp.databinding.ActivityDetalleTarifaComparadaBinding;

import java.util.Locale;

/**
 * Muestra el resultado detallado de una tarifa ya calculada por el comparador.
 *
 * Esta pantalla no vuelve a ejecutar el motor. Recibe los importes calculados
 * para conservar exactamente el resultado mostrado en el ranking.
 */
public class DetalleTarifaComparadaActivity extends AppCompatActivity {

    public static final String EXTRA_COMERCIALIZADORA = "detalle_comercializadora";
    public static final String EXTRA_TARIFA = "detalle_tarifa";
    public static final String EXTRA_DIAS = "detalle_dias";
    public static final String EXTRA_TOTAL = "detalle_total";
    public static final String EXTRA_COSTE_MENSUAL = "detalle_coste_mensual";
    public static final String EXTRA_COSTE_ANUAL = "detalle_coste_anual";
    public static final String EXTRA_AHORRO_PERIODO = "detalle_ahorro_periodo";
    public static final String EXTRA_AHORRO_ANUAL = "detalle_ahorro_anual";
    public static final String EXTRA_ENERGIA = "detalle_energia";
    public static final String EXTRA_POTENCIA = "detalle_potencia";
    public static final String EXTRA_IMPUESTO = "detalle_impuesto";
    public static final String EXTRA_IVA = "detalle_iva";
    public static final String EXTRA_ALQUILER = "detalle_alquiler";
    public static final String EXTRA_PRECIO_P1 = "detalle_precio_p1";
    public static final String EXTRA_PRECIO_P2 = "detalle_precio_p2";
    public static final String EXTRA_PRECIO_P3 = "detalle_precio_p3";
    public static final String EXTRA_PERMANENCIA = "detalle_permanencia";
    public static final String EXTRA_DESCUENTO = "detalle_descuento";
    public static final String EXTRA_SERVICIOS = "detalle_servicios";
    public static final String EXTRA_OBSERVACIONES = "detalle_observaciones";
    public static final String EXTRA_FECHA = "detalle_fecha";
    public static final String EXTRA_FUENTE = "detalle_fuente";
    public static final String EXTRA_REFERENCIA = "detalle_referencia";

    private ActivityDetalleTarifaComparadaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalleTarifaComparadaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarDetalleTarifa);
        NavigationUtils.configurarToolbarConAtrasBlanco(this, binding.toolbarDetalleTarifa);

        mostrarResultado();
    }

    private void mostrarResultado() {
        String comercializadora = texto(EXTRA_COMERCIALIZADORA);
        String tarifa = texto(EXTRA_TARIFA);
        int dias = Math.max(1, getIntent().getIntExtra(EXTRA_DIAS, 30));
        double total = numero(EXTRA_TOTAL);
        double ahorroPeriodo = numero(EXTRA_AHORRO_PERIODO);

        binding.txtDetalleComercializadora.setText(comercializadora);
        binding.txtDetalleNombreTarifa.setText(tarifa);
        binding.txtDetalleTipoTarifa.setText(tipoTarifa());
        binding.txtDetalleTotal.setText(moneda(total));
        binding.txtDetalleProyeccion.setText(String.format(Locale.getDefault(),
                "%d días · %s/mes · %s/año",
                dias, moneda(numero(EXTRA_COSTE_MENSUAL)), moneda(numero(EXTRA_COSTE_ANUAL))));

        if (ahorroPeriodo > 0.005) {
            binding.txtDetalleAhorro.setText(String.format(Locale.getDefault(),
                    "Ahorro frente a %s: %s en el periodo · %s/año",
                    texto(EXTRA_REFERENCIA), moneda(ahorroPeriodo), moneda(numero(EXTRA_AHORRO_ANUAL))));
        } else if (ahorroPeriodo < -0.005) {
            binding.txtDetalleAhorro.setText(String.format(Locale.getDefault(),
                    "Coste superior a %s: %s más en el periodo · %s/año más",
                    texto(EXTRA_REFERENCIA), moneda(Math.abs(ahorroPeriodo)),
                    moneda(Math.abs(numero(EXTRA_AHORRO_ANUAL)))));
        } else {
            binding.txtDetalleAhorro.setText("Coste similar a " + texto(EXTRA_REFERENCIA));
        }

        binding.txtDetalleEnergia.setText(moneda(numero(EXTRA_ENERGIA)));
        binding.txtDetallePotencia.setText(moneda(numero(EXTRA_POTENCIA)));
        binding.txtDetalleImpuesto.setText(moneda(numero(EXTRA_IMPUESTO)));
        binding.txtDetalleIva.setText(moneda(numero(EXTRA_IVA)));
        binding.txtDetalleAlquiler.setText(moneda(numero(EXTRA_ALQUILER)));
        binding.txtDetalleTotalDesglose.setText(moneda(total));

        binding.txtDetallePrecios.setText(String.format(Locale.getDefault(),
                "P1 %.4f €/kWh\nP2 %.4f €/kWh\nP3 %.4f €/kWh",
                numero(EXTRA_PRECIO_P1), numero(EXTRA_PRECIO_P2), numero(EXTRA_PRECIO_P3)));
        binding.txtDetallePermanencia.setText(texto(EXTRA_PERMANENCIA));
        binding.txtDetalleDescuento.setText(texto(EXTRA_DESCUENTO));
        binding.txtDetalleServicios.setText(texto(EXTRA_SERVICIOS));
        binding.txtDetalleActualizacion.setText(texto(EXTRA_FECHA));
        binding.txtDetalleFuente.setText(texto(EXTRA_FUENTE));

        String observaciones = texto(EXTRA_OBSERVACIONES);
        boolean tieneObservaciones = !"No informado".equalsIgnoreCase(observaciones);
        binding.txtDetalleObservacionesTitulo.setVisibility(tieneObservaciones ? View.VISIBLE : View.GONE);
        binding.txtDetalleObservaciones.setVisibility(tieneObservaciones ? View.VISIBLE : View.GONE);
        binding.txtDetalleObservaciones.setText(observaciones);
    }

    private String tipoTarifa() {
        double p1 = numero(EXTRA_PRECIO_P1);
        double p2 = numero(EXTRA_PRECIO_P2);
        double p3 = numero(EXTRA_PRECIO_P3);
        double tolerancia = 0.000001;
        return Math.abs(p1 - p2) < tolerancia && Math.abs(p1 - p3) < tolerancia
                ? "Precio único" : "Tres periodos";
    }

    private String texto(String clave) {
        String valor = getIntent().getStringExtra(clave);
        return valor == null || valor.trim().isEmpty() ? "No informado" : valor.trim();
    }

    private double numero(String clave) {
        return getIntent().getDoubleExtra(clave, 0);
    }

    private String moneda(double valor) {
        return String.format(Locale.getDefault(), "%.2f €", valor);
    }
}
