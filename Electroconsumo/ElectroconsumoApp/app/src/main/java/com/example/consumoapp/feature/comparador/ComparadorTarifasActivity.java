package com.example.consumoapp.feature.comparador;

import com.example.consumoapp.NavigationUtils;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.consumoapp.R;
import com.example.consumoapp.SessionManager;
import com.example.consumoapp.LoginActivity;
import com.example.consumoapp.core.factura.engine.DatosCalculoFactura;
import com.example.consumoapp.core.factura.engine.MotorCalculoFactura;
import com.example.consumoapp.core.factura.engine.ResultadoFactura;
import com.example.consumoapp.databinding.ActivityComparadorTarifasBinding;
import com.example.consumoapp.esios.data.AppDatabase;
import com.example.consumoapp.tarifas.data.TarifaComercialEntity;
import com.example.consumoapp.tarifas.data.TarifaConComercializadora;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Compara todas las tarifas activas usando exactamente los consumos P1, P2 y P3
 * obtenidos de los electrodomésticos añadidos a la factura.
 */
public class ComparadorTarifasActivity extends AppCompatActivity {
    public static final String EXTRA_CONSUMO_P1 = "consumo_p1";
    public static final String EXTRA_CONSUMO_P2 = "consumo_p2";
    public static final String EXTRA_CONSUMO_P3 = "consumo_p3";
    public static final String EXTRA_POTENCIA_P1 = "potencia_p1";
    public static final String EXTRA_POTENCIA_P2 = "potencia_p2";
    public static final String EXTRA_DIAS = "dias";
    public static final String EXTRA_IEE = "iee";
    public static final String EXTRA_IVA = "iva";
    public static final String EXTRA_TOTAL_ACTUAL = "total_actual";
    public static final String EXTRA_NUM_ELECTRODOMESTICOS = "numero_electrodomesticos";
    public static final String EXTRA_REFERENCIA_NOMBRE = "referencia_nombre";

    private ActivityComparadorTarifasBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MotorCalculoFactura motor = new MotorCalculoFactura();
    private final List<ResultadoComparacion> resultados = new ArrayList<>();
    private final List<ResultadoComparacion> resultadosCompletos = new ArrayList<>();
    private ComparadorTarifasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.estaLogueado(this)) {
            startActivity(new android.content.Intent(this, LoginActivity.class));
            finish();
            return;
        }
        binding = ActivityComparadorTarifasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarComparador);
        NavigationUtils.configurarToolbarConAtrasBlanco(this, binding.toolbarComparador);

        adapter = new ComparadorTarifasAdapter(resultados, this::abrirDetalleTarifa);
        binding.recyclerComparador.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerComparador.setAdapter(adapter);

        configurarFiltrosYOrden();
        mostrarResumenEntrada();
        compararTarifas();
    }


    /** Configura filtros y orden sin volver a consultar Room. */
    private void configurarFiltrosYOrden() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> aplicarFiltrosYOrden());
        binding.spinnerOrdenComparador.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltrosYOrden();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // El Spinner siempre tiene una opción seleccionada.
            }
        });
    }

    /**
     * Filtra la lista ya calculada y la ordena según la opción elegida.
     * Ningún filtro modifica los datos guardados ni vuelve a calcular importes.
     */
    private void aplicarFiltrosYOrden() {
        if (binding == null) return;

        List<ResultadoComparacion> filtrados = new ArrayList<>();
        int filtroId = binding.chipGroupFiltros.getCheckedChipId();
        for (ResultadoComparacion resultado : resultadosCompletos) {
            boolean incluir;
            if (filtroId == R.id.chipSinPermanencia) {
                incluir = resultado.esSinPermanencia();
            } else if (filtroId == R.id.chipPrecioUnico) {
                incluir = resultado.esPrecioUnico();
            } else if (filtroId == R.id.chipTresPeriodos) {
                incluir = !resultado.esPrecioUnico();
            } else if (filtroId == R.id.chipConDescuento) {
                incluir = resultado.tieneDescuento();
            } else {
                incluir = true;
            }
            if (incluir) filtrados.add(resultado);
        }

        int orden = binding.spinnerOrdenComparador.getSelectedItemPosition();
        if (orden == 1) {
            filtrados.sort(Comparator.comparingDouble(ResultadoComparacion::getAhorroAnual).reversed());
        } else if (orden == 2) {
            filtrados.sort(Comparator.comparingDouble(ResultadoComparacion::getCosteAnualEstimado));
        } else if (orden == 3) {
            filtrados.sort(Comparator
                    .comparing(ResultadoComparacion::getComercializadora, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(ResultadoComparacion::getTarifa, String.CASE_INSENSITIVE_ORDER));
        } else {
            filtrados.sort(Comparator.comparingDouble(r -> r.getFactura().getTotalFactura()));
        }

        resultados.clear();
        resultados.addAll(filtrados);
        adapter.notifyDataSetChanged();
        actualizarResumenResultados();
    }

    /** Actualiza el contador y la mejor opción de la lista actualmente visible. */
    private void actualizarResumenResultados() {
        binding.txtNumeroTarifas.setText(resultados.size() + " de "
                + resultadosCompletos.size() + " tarifas mostradas");
        if (!resultados.isEmpty()) {
            ResultadoComparacion mejor = resultados.stream()
                    .min(Comparator.comparingDouble(r -> r.getFactura().getTotalFactura()))
                    .orElse(resultados.get(0));
            binding.txtMejorOpcion.setVisibility(View.VISIBLE);
            binding.txtMejorOpcion.setText(String.format(Locale.getDefault(),
                    "Mejor coste entre las opciones visibles: %s · %s\n%.2f € en el periodo · %.2f €/año estimados",
                    mejor.getComercializadora(), mejor.getTarifa(),
                    mejor.getFactura().getTotalFactura(), mejor.getCosteAnualEstimado()));
        } else {
            binding.txtMejorOpcion.setVisibility(View.GONE);
        }
    }

    private void mostrarResumenEntrada() {
        double p1 = getIntent().getDoubleExtra(EXTRA_CONSUMO_P1, 0);
        double p2 = getIntent().getDoubleExtra(EXTRA_CONSUMO_P2, 0);
        double p3 = getIntent().getDoubleExtra(EXTRA_CONSUMO_P3, 0);
        int aparatos = getIntent().getIntExtra(EXTRA_NUM_ELECTRODOMESTICOS, 0);
        int dias = Math.max(1, getIntent().getIntExtra(EXTRA_DIAS, 30));
        double totalActual = getIntent().getDoubleExtra(EXTRA_TOTAL_ACTUAL, 0);
        double anualActual = totalActual * 365.0 / dias;
        String referencia = getIntent().getStringExtra(EXTRA_REFERENCIA_NOMBRE);
        if (referencia == null || referencia.trim().isEmpty()) {
            referencia = "Tarifa usada en la simulación";
        }
        binding.txtResumenComparador.setText(String.format(Locale.getDefault(),
                "%d electrodomésticos · %.2f kWh · %d días\nP1 %.2f kWh · P2 %.2f kWh · P3 %.2f kWh\nReferencia: %s\n%.2f € en el periodo · %.2f €/año estimados",
                aparatos, p1 + p2 + p3, dias, p1, p2, p3, referencia, totalActual, anualActual));
    }

    private void compararTarifas() {
        binding.progressComparador.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            List<TarifaConComercializadora> tarifas = AppDatabase.getInstance(this)
                    .tarifaDao().obtenerTodasLasTarifas();
            List<ResultadoComparacion> calculados = new ArrayList<>();
            double totalActual = getIntent().getDoubleExtra(EXTRA_TOTAL_ACTUAL, 0);
            int diasPeriodo = Math.max(1, getIntent().getIntExtra(EXTRA_DIAS, 30));

            for (TarifaConComercializadora relacion : tarifas) {
                TarifaComercialEntity tarifa = relacion.tarifa;
                if (tarifa == null || tarifa.getPrecioP1() == null) continue;

                double precioP1 = tarifa.getPrecioP1();
                double precioP2 = tarifa.getPrecioP2() != null ? tarifa.getPrecioP2() : precioP1;
                double precioP3 = tarifa.getPrecioP3() != null ? tarifa.getPrecioP3() : precioP1;
                double consumoP1 = getIntent().getDoubleExtra(EXTRA_CONSUMO_P1, 0);
                double consumoP2 = getIntent().getDoubleExtra(EXTRA_CONSUMO_P2, 0);
                double consumoP3 = getIntent().getDoubleExtra(EXTRA_CONSUMO_P3, 0);
                double potenciaP1 = tarifa.getPotenciaP1() != null ? tarifa.getPotenciaP1() : 0;
                double potenciaP2 = tarifa.getPotenciaP2() != null ? tarifa.getPotenciaP2() : potenciaP1;
                double alquiler = tarifa.getAlquiler() != null ? tarifa.getAlquiler() : 0;

                DatosCalculoFactura datos = new DatosCalculoFactura(
                        consumoP1, consumoP2, consumoP3,
                        consumoP1 * precioP1,
                        consumoP2 * precioP2,
                        consumoP3 * precioP3,
                        getIntent().getDoubleExtra(EXTRA_POTENCIA_P1, 0),
                        getIntent().getDoubleExtra(EXTRA_POTENCIA_P2, 0),
                        potenciaP1, potenciaP2,
                        Math.max(1, getIntent().getIntExtra(EXTRA_DIAS, 30)),
                        alquiler,
                        getIntent().getDoubleExtra(EXTRA_IEE, 0),
                        getIntent().getDoubleExtra(EXTRA_IVA, 0)
                );

                try {
                    ResultadoFactura factura = motor.calcular(datos);
                    calculados.add(new ResultadoComparacion(
                            relacion.nombreComercializadora,
                            tarifa.getNombre(),
                            factura,
                            totalActual - factura.getTotalFactura(),
                            diasPeriodo,
                            tarifa.getFechaActualizacion(),
                            tarifa.getFuente(),
                            tarifa.getPermanencia(),
                            tarifa.getDescuento(),
                            tarifa.getServicios(),
                            tarifa.getObservaciones(),
                            precioP1, precioP2, precioP3));
                } catch (IllegalArgumentException ignored) {
                    // Una tarifa incompleta no debe bloquear el resto del ranking.
                }
            }

            calculados.sort(Comparator.comparingDouble(r -> r.getFactura().getTotalFactura()));
            runOnUiThread(() -> {
                binding.progressComparador.setVisibility(View.GONE);
                resultadosCompletos.clear();
                resultadosCompletos.addAll(calculados);
                aplicarFiltrosYOrden();
                if (calculados.isEmpty()) {
                    Toast.makeText(this, "No hay tarifas completas para comparar", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    /** Abre una ficha detallada utilizando el resultado ya calculado. */
    private void abrirDetalleTarifa(ResultadoComparacion resultado) {
        ResultadoFactura factura = resultado.getFactura();
        String referencia = getIntent().getStringExtra(EXTRA_REFERENCIA_NOMBRE);
        if (referencia == null || referencia.trim().isEmpty()) {
            referencia = "Tarifa usada en la simulación";
        }

        Intent intent = new Intent(this, DetalleTarifaComparadaActivity.class);
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_COMERCIALIZADORA, resultado.getComercializadora());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_TARIFA, resultado.getTarifa());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_DIAS, resultado.getDiasPeriodo());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_TOTAL, factura.getTotalFactura());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_COSTE_MENSUAL, resultado.getCosteMensualEstimado());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_COSTE_ANUAL, resultado.getCosteAnualEstimado());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_AHORRO_PERIODO, resultado.getAhorroPeriodo());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_AHORRO_ANUAL, resultado.getAhorroAnual());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_ENERGIA, factura.getCosteEnergia());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_POTENCIA, factura.getCostePotencia());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_IMPUESTO, factura.getImpuestoElectricidad());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_IVA, factura.getIva());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_ALQUILER, factura.getAlquilerContador());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_PRECIO_P1, resultado.getPrecioP1());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_PRECIO_P2, resultado.getPrecioP2());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_PRECIO_P3, resultado.getPrecioP3());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_PERMANENCIA, resultado.getPermanencia());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_DESCUENTO, resultado.getDescuento());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_SERVICIOS, resultado.getServicios());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_OBSERVACIONES, resultado.getObservaciones());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_FECHA, resultado.getFechaActualizacion());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_FUENTE, resultado.getFuente());
        intent.putExtra(DetalleTarifaComparadaActivity.EXTRA_REFERENCIA, referencia);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
