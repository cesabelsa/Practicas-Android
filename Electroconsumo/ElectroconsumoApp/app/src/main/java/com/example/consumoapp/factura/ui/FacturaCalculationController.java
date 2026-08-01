package com.example.consumoapp.factura.ui;

import android.content.Context;
import android.widget.Toast;

import com.example.consumoapp.core.factura.engine.DatosCalculoFactura;
import com.example.consumoapp.core.factura.engine.DesgloseRegulado;
import com.example.consumoapp.core.factura.engine.MotorCalculoFactura;
import com.example.consumoapp.core.factura.engine.ResultadoFactura;
import com.example.consumoapp.databinding.ActivitySimuladorFacturaBinding;
import com.example.consumoapp.factura.LineaSimuladorAdapter;
import com.example.consumoapp.factura.LineaSimuladorFactura;
import com.example.consumoapp.regulacion.domain.ConstantesFactura;

import java.util.List;
import java.util.function.Supplier;

/**
 * Construye y valida los datos del contrato, ejecuta el motor de factura y
 * conserva el último resultado. No realiza navegación ni acceso a Room.
 */
public final class FacturaCalculationController {

    private final Context context;
    private final ActivitySimuladorFacturaBinding binding;
    private final List<LineaSimuladorFactura> lineas;
    private final LineaSimuladorAdapter adapter;
    private final ResultadoFacturaRenderer renderer;
    private final Supplier<String> fuenteSupplier;
    private final MotorCalculoFactura motor = new MotorCalculoFactura();

    private ConstantesFactura constantes;
    private ResultadoFactura ultimoResultado;
    private boolean reguladosSeparados;

    public FacturaCalculationController(
            Context context,
            ActivitySimuladorFacturaBinding binding,
            List<LineaSimuladorFactura> lineas,
            LineaSimuladorAdapter adapter,
            ResultadoFacturaRenderer renderer,
            Supplier<String> fuenteSupplier
    ) {
        this.context = context;
        this.binding = binding;
        this.lineas = lineas;
        this.adapter = adapter;
        this.renderer = renderer;
        this.fuenteSupplier = fuenteSupplier;
    }

    public void setConstantes(ConstantesFactura constantes) {
        this.constantes = constantes;
    }

    public ResultadoFactura getUltimoResultado() {
        return ultimoResultado;
    }

    public boolean isReguladosSeparados() {
        return reguladosSeparados;
    }

    public int getDiasFactura() {
        return Math.max(1, leerEntero(binding.edtDiasFactura.getText().toString(), 30));
    }

    public double getPotenciaPuntaKw() {
        return leerDouble(binding.edtPotenciaPuntaKw.getText().toString(), 0.0);
    }

    public double getPotenciaValleKw() {
        return leerDouble(binding.edtPotenciaValleKw.getText().toString(), 0.0);
    }

    public double getImpuestoElectricidadPct() {
        return leerDouble(binding.edtImpuestoElectricidad.getText().toString(), 0.0);
    }

    public double getIvaPct() {
        return leerDouble(binding.edtIva.getText().toString(), 0.0);
    }

    /** Suma las líneas, valida el contrato y muestra el resultado. */
    public boolean recalcular() {
        Totales total = sumarLineas();
        reguladosSeparados = binding.checkPrecioSoloMercado.isChecked() && constantes != null;

        Double potenciaP1 = leerCampoNoNegativo(binding.edtPotenciaPuntaKw, "Potencia punta no válida");
        Double potenciaP2 = leerCampoNoNegativo(binding.edtPotenciaValleKw, "Potencia valle no válida");
        Integer dias = leerDiasValidos();
        Double alquiler = leerCampoNoNegativo(binding.edtAlquilerContadorDia, "Alquiler no válido");
        Double impuesto = leerCampoNoNegativo(binding.edtImpuestoElectricidad, "Impuesto eléctrico no válido");
        Double iva = leerCampoNoNegativo(binding.edtIva, "IVA no válido");
        if (potenciaP1 == null || potenciaP2 == null || dias == null
                || alquiler == null || impuesto == null || iva == null) {
            return false;
        }

        double precioPotenciaP1 = 0.0;
        double precioPotenciaP2 = 0.0;
        if (!reguladosSeparados) {
            Double precioP1 = leerCampoNoNegativo(binding.edtPrecioPotenciaPunta,
                    "Precio de potencia punta no válido");
            Double precioP2 = leerCampoNoNegativo(binding.edtPrecioPotenciaValle,
                    "Precio de potencia valle no válido");
            if (precioP1 == null || precioP2 == null) return false;
            precioPotenciaP1 = precioP1;
            precioPotenciaP2 = precioP2;
        }

        DesgloseRegulado desglose = reguladosSeparados
                ? constantes.getDesgloseRegulado()
                : DesgloseRegulado.SIN_DESGLOSE;

        DatosCalculoFactura datos = new DatosCalculoFactura(
                total.consumoP1, total.consumoP2, total.consumoP3,
                total.costeP1, total.costeP2, total.costeP3,
                potenciaP1, potenciaP2,
                precioPotenciaP1, precioPotenciaP2,
                dias, alquiler, impuesto, iva,
                desglose, 0.0, 0.0
        );

        try {
            ultimoResultado = motor.calcular(datos);
            renderer.mostrar(ultimoResultado, reguladosSeparados, fuenteSupplier.get());
            return true;
        } catch (IllegalArgumentException error) {
            Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void actualizarPrecios(double p1, double p2, double p3) {
        for (LineaSimuladorFactura linea : lineas) {
            linea.actualizarPrecios(p1, p2, p3);
        }
        adapter.notifyDataSetChanged();
        recalcular();
    }

    public void actualizarPrecioUnico(double precioKwh) {
        for (LineaSimuladorFactura linea : lineas) {
            linea.actualizarPrecioKwh(precioKwh);
        }
        adapter.notifyDataSetChanged();
    }

    private Totales sumarLineas() {
        Totales total = new Totales();
        for (LineaSimuladorFactura linea : lineas) {
            total.consumoP1 += linea.getConsumoP1Kwh();
            total.consumoP2 += linea.getConsumoP2Kwh();
            total.consumoP3 += linea.getConsumoP3Kwh();
            total.costeP1 += linea.getCosteP1();
            total.costeP2 += linea.getCosteP2();
            total.costeP3 += linea.getCosteP3();
        }
        return total;
    }

    private Double leerCampoNoNegativo(android.widget.EditText campo, String mensaje) {
        double valor = leerDouble(campo.getText().toString(), Double.NaN);
        if (Double.isNaN(valor) || Double.isInfinite(valor) || valor < 0.0) {
            campo.setError(mensaje);
            return null;
        }
        return valor;
    }

    private Integer leerDiasValidos() {
        int dias = leerEntero(binding.edtDiasFactura.getText().toString(), -1);
        if (dias < 1) {
            binding.edtDiasFactura.setError("Introduce un número de días válido");
            return null;
        }
        return dias;
    }

    private int leerEntero(String texto, int defecto) {
        try {
            return Integer.parseInt(texto == null ? "" : texto.trim());
        } catch (NumberFormatException error) {
            return defecto;
        }
    }

    private double leerDouble(String texto, double defecto) {
        if (texto == null || texto.trim().isEmpty()) return defecto;
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException error) {
            return defecto;
        }
    }

    private static final class Totales {
        double consumoP1;
        double consumoP2;
        double consumoP3;
        double costeP1;
        double costeP2;
        double costeP3;
    }
}
