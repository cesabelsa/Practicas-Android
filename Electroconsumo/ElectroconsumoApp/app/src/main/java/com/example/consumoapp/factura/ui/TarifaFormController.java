package com.example.consumoapp.factura.ui;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.consumoapp.R;
import com.example.consumoapp.databinding.ActivitySimuladorFacturaBinding;
import com.example.consumoapp.factura.LineaSimuladorAdapter;
import com.example.consumoapp.factura.LineaSimuladorFactura;
import com.example.consumoapp.tarifas.data.ComercializadoraEntity;
import com.example.consumoapp.tarifas.data.TarifaComercialEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.LongConsumer;

/**
 * Gestiona la selección de la fuente del precio, comercializadoras y tarifas.
 *
 * La Activity solo coordina acciones generales. Este controlador se encarga de:
 * - configurar los selectores visuales;
 * - cargar la tarifa seleccionada en los campos del contrato;
 * - aplicar P1, P2 y P3 a las líneas de consumo existentes;
 * - mantener la descripción de la fuente utilizada.
 */
public final class TarifaFormController {

    private final android.content.Context context;
    private final ActivitySimuladorFacturaBinding binding;
    private final List<LineaSimuladorFactura> lineas;
    private final LineaSimuladorAdapter lineasAdapter;
    private final LongConsumer solicitarTarifas;
    private final Runnable recalcularTotales;
    private final BooleanSupplier constantesDisponibles;

    private final List<ComercializadoraEntity> comercializadoras = new ArrayList<>();
    private final List<TarifaComercialEntity> tarifas = new ArrayList<>();

    private TarifaComercialEntity tarifaSeleccionada;
    private String fuenteSeleccionada = "PVPC / ESIOS";

    public TarifaFormController(
            android.content.Context context,
            ActivitySimuladorFacturaBinding binding,
            List<LineaSimuladorFactura> lineas,
            LineaSimuladorAdapter lineasAdapter,
            LongConsumer solicitarTarifas,
            Runnable recalcularTotales,
            BooleanSupplier constantesDisponibles
    ) {
        this.context = context;
        this.binding = binding;
        this.lineas = lineas;
        this.lineasAdapter = lineasAdapter;
        this.solicitarTarifas = solicitarTarifas;
        this.recalcularTotales = recalcularTotales;
        this.constantesDisponibles = constantesDisponibles;
    }

    /** Configura las tres fuentes posibles del precio de energía. */
    public void configurarSelectorFuentePrecio() {
        binding.radioGrupoFuentePrecio.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioComercializadora) {
                mostrarModoComercializadora();
                aplicarTarifaSeleccionada();
            } else if (checkedId == R.id.radioManual) {
                mostrarModoManual();
            } else {
                mostrarModoEsios();
            }

            actualizarFuenteResultado();
            recalcularTotales.run();
        });

        // Estado inicial: ESIOS seleccionado y precio no editable.
        mostrarModoEsios();
        actualizarFuenteResultado();
    }

    private void mostrarModoComercializadora() {
        binding.panelComercializadora.setVisibility(View.VISIBLE);
        binding.edtPrecioKwh.setEnabled(false);
        binding.checkPrecioSoloMercado.setChecked(false);
        binding.checkPrecioSoloMercado.setEnabled(false);
        fuenteSeleccionada = "Tarifa de comercializadora";
        binding.txtAvisoFuentePrecio.setText(
                "Selecciona una comercializadora y una tarifa. Sus precios se aplicarán automáticamente."
        );
    }

    private void mostrarModoManual() {
        binding.panelComercializadora.setVisibility(View.GONE);
        binding.edtPrecioKwh.setEnabled(true);
        binding.checkPrecioSoloMercado.setEnabled(constantesDisponibles.getAsBoolean());
        fuenteSeleccionada = "Precios introducidos manualmente";
        binding.txtAvisoFuentePrecio.setText(
                "Introduce aquí el precio de energía y usa en Contrato los precios de potencia de tu factura."
        );
    }

    private void mostrarModoEsios() {
        binding.panelComercializadora.setVisibility(View.GONE);
        binding.edtPrecioKwh.setEnabled(false);
        binding.checkPrecioSoloMercado.setChecked(false);
        binding.checkPrecioSoloMercado.setEnabled(false);
        fuenteSeleccionada = "PVPC / ESIOS";
        binding.txtAvisoFuentePrecio.setText(
                "ESIOS se usa para PVPC o indexadas. Pulsa «Cargar precios ESIOS guardados» y después calcula la factura."
        );
    }

    /** Actualiza el estado del control manual cuando ya existen constantes. */
    public void actualizarDisponibilidadConstantes() {
        if (esManual()) {
            binding.checkPrecioSoloMercado.setEnabled(constantesDisponibles.getAsBoolean());
        }
    }

    public void mostrarComercializadoras(List<ComercializadoraEntity> datos) {
        comercializadoras.clear();
        if (datos != null) {
            comercializadoras.addAll(datos);
        }

        List<String> nombres = new ArrayList<>();
        for (ComercializadoraEntity comercializadora : comercializadoras) {
            nombres.add(comercializadora.getNombre());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                nombres
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerComercializadora.setAdapter(adapter);

        binding.spinnerComercializadora.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < comercializadoras.size()) {
                    solicitarTarifas.accept(comercializadoras.get(position).getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tarifaSeleccionada = null;
            }
        });
    }

    public void mostrarTarifas(List<TarifaComercialEntity> datos) {
        tarifas.clear();
        if (datos != null) {
            tarifas.addAll(datos);
        }

        List<String> nombres = new ArrayList<>();
        for (TarifaComercialEntity tarifa : tarifas) {
            nombres.add(tarifa.getNombre());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                nombres
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTarifa.setAdapter(adapter);

        binding.spinnerTarifa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < tarifas.size()) {
                    tarifaSeleccionada = tarifas.get(position);
                    mostrarResumenTarifa(tarifaSeleccionada);
                    if (esComercializadora()) {
                        aplicarTarifaSeleccionada();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tarifaSeleccionada = null;
            }
        });
    }

    /** Muestra todos los datos útiles de la tarifa seleccionada. */
    private void mostrarResumenTarifa(TarifaComercialEntity tarifa) {
        String resumen =
                "Energía P1: " + formatoPrecio(tarifa.getPrecioP1(), "€/kWh") +
                " · P2: " + formatoPrecio(tarifa.getPrecioP2(), "€/kWh") +
                " · P3: " + formatoPrecio(tarifa.getPrecioP3(), "€/kWh") +
                "\nPotencia P1: " + formatoPrecio(tarifa.getPotenciaP1(), "€/kW día") +
                " · P2: " + formatoPrecio(tarifa.getPotenciaP2(), "€/kW día") +
                "\nAlquiler: " + formatoPrecio(tarifa.getAlquiler(), "€/día") +
                "\nPermanencia: " + textoONoDisponible(tarifa.getPermanencia()) +
                "\nDescuento: " + textoONoDisponible(tarifa.getDescuento()) +
                "\nServicios: " + textoONoDisponible(tarifa.getServicios()) +
                "\nActualización: " + textoONoDisponible(tarifa.getFechaActualizacion());

        binding.txtResumenTarifaSeleccionada.setText(resumen);
    }

    /** Aplica la tarifa elegida a campos y líneas del simulador. */
    public boolean aplicarTarifaSeleccionada() {
        if (tarifaSeleccionada == null || !esComercializadora()) {
            return false;
        }

        actualizarPrecioCampoSegunPeriodo();
        actualizarPrecioLineasPorPeriodo();

        if (tarifaSeleccionada.getPotenciaP1() != null) {
            binding.edtPrecioPotenciaPunta.setText(
                    String.format(Locale.US, "%.6f", tarifaSeleccionada.getPotenciaP1())
            );
        }
        if (tarifaSeleccionada.getPotenciaP2() != null) {
            binding.edtPrecioPotenciaValle.setText(
                    String.format(Locale.US, "%.6f", tarifaSeleccionada.getPotenciaP2())
            );
        }
        if (tarifaSeleccionada.getAlquiler() != null) {
            binding.edtAlquilerContadorDia.setText(
                    String.format(Locale.US, "%.6f", tarifaSeleccionada.getAlquiler())
            );
        }

        String nombreComercializadora = "";
        int posicion = binding.spinnerComercializadora.getSelectedItemPosition();
        if (posicion >= 0 && posicion < comercializadoras.size()) {
            nombreComercializadora = comercializadoras.get(posicion).getNombre();
        }

        fuenteSeleccionada = nombreComercializadora + " · " + tarifaSeleccionada.getNombre();
        actualizarFuenteResultado();
        recalcularTotales.run();
        return true;
    }

    /** Actualiza el precio visible al cambiar el periodo del electrodoméstico. */
    public void actualizarPrecioCampoSegunPeriodo() {
        String periodo = obtenerPeriodoSeleccionado();
        if (periodo == null || !esComercializadora() || tarifaSeleccionada == null) {
            return;
        }

        double precio = LineaSimuladorFactura.PERIODO_AUTO.equals(periodo)
                ? calcularMediaPreciosEnergia(tarifaSeleccionada)
                : obtenerPrecioPeriodo(periodo);
        binding.edtPrecioKwh.setText(String.format(Locale.US, "%.6f", precio));
    }

    private String obtenerPeriodoSeleccionado() {
        Object seleccionado = binding.spinnerPeriodoUso.getSelectedItem();
        if (seleccionado == null) {
            return null;
        }
        String texto = seleccionado.toString();
        if (texto.startsWith("P1")) return LineaSimuladorFactura.PERIODO_P1;
        if (texto.startsWith("P2")) return LineaSimuladorFactura.PERIODO_P2;
        if (texto.startsWith("P3")) return LineaSimuladorFactura.PERIODO_P3;
        return LineaSimuladorFactura.PERIODO_AUTO;
    }

    /** Recalcula las líneas existentes respetando el periodo de cada una. */
    public void actualizarPrecioLineasPorPeriodo() {
        double precioP1 = obtenerPrecioPeriodo(LineaSimuladorFactura.PERIODO_P1);
        double precioP2 = obtenerPrecioPeriodo(LineaSimuladorFactura.PERIODO_P2);
        double precioP3 = obtenerPrecioPeriodo(LineaSimuladorFactura.PERIODO_P3);

        for (LineaSimuladorFactura linea : lineas) {
            linea.actualizarPrecios(precioP1, precioP2, precioP3);
        }
        lineasAdapter.notifyDataSetChanged();
    }

    /** Devuelve el precio aplicable al periodo solicitado. */
    public double obtenerPrecioPeriodo(String periodo) {
        if (esComercializadora() && tarifaSeleccionada != null) {
            Double precio = null;
            if (LineaSimuladorFactura.PERIODO_P1.equals(periodo)) {
                precio = tarifaSeleccionada.getPrecioP1();
            } else if (LineaSimuladorFactura.PERIODO_P2.equals(periodo)) {
                precio = tarifaSeleccionada.getPrecioP2();
            } else if (LineaSimuladorFactura.PERIODO_P3.equals(periodo)) {
                precio = tarifaSeleccionada.getPrecioP3();
            }

            // Algunas tarifas tienen un único precio; P1 actúa como respaldo.
            if (precio == null) {
                precio = tarifaSeleccionada.getPrecioP1();
            }
            return precio == null ? 0.0 : precio;
        }

        return leerDouble(binding.edtPrecioKwh.getText().toString(), 0.0);
    }

    public boolean esComercializadora() {
        return binding.radioComercializadora.isChecked();
    }

    public boolean esManual() {
        return binding.radioManual.isChecked();
    }

    public boolean tieneTarifaSeleccionada() {
        return tarifaSeleccionada != null;
    }


    /** Nombre de la comercializadora actualmente seleccionada. */
    public String getNombreComercializadoraSeleccionada() {
        int posicion = binding.spinnerComercializadora.getSelectedItemPosition();
        if (posicion < 0 || posicion >= comercializadoras.size()) {
            return null;
        }
        return comercializadoras.get(posicion).getNombre();
    }

    /** Nombre de la tarifa actualmente seleccionada. */
    public String getNombreTarifaSeleccionada() {
        return tarifaSeleccionada == null ? null : tarifaSeleccionada.getNombre();
    }

    public String getFuenteSeleccionada() {
        return fuenteSeleccionada;
    }

    /** Registra una fuente ESIOS concreta después de cargar sus precios. */
    public void establecerFuenteEsios(String descripcion) {
        fuenteSeleccionada = descripcion == null || descripcion.trim().isEmpty()
                ? "PVPC / ESIOS"
                : descripcion;
        actualizarFuenteResultado();
    }

    /** Registra explícitamente que se usarán precios manuales. */
    public void establecerFuenteManual() {
        fuenteSeleccionada = "Precios introducidos manualmente";
        actualizarFuenteResultado();
    }

    private void actualizarFuenteResultado() {
        binding.txtFuenteResultado.setText("Fuente: " + fuenteSeleccionada);
    }

    private double calcularMediaPreciosEnergia(TarifaComercialEntity tarifa) {
        double suma = 0.0;
        int cantidad = 0;
        Double[] precios = {tarifa.getPrecioP1(), tarifa.getPrecioP2(), tarifa.getPrecioP3()};
        for (Double precio : precios) {
            if (precio != null) {
                suma += precio;
                cantidad++;
            }
        }
        return cantidad == 0 ? 0.0 : suma / cantidad;
    }

    private String formatoPrecio(Double valor, String unidad) {
        return valor == null
                ? "N/D"
                : String.format(Locale.getDefault(), "%.6f %s", valor, unidad);
    }

    private String textoONoDisponible(String texto) {
        return texto == null || texto.trim().isEmpty() ? "N/D" : texto;
    }

    private double leerDouble(String texto, double valorPorDefecto) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (Exception error) {
            return valorPorDefecto;
        }
    }
}
