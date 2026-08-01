package com.example.consumoapp.factura;

import com.example.consumoapp.NavigationUtils;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.consumoapp.R;
import com.example.consumoapp.SessionManager;
import com.example.consumoapp.databinding.ActivityFacturaVisualBinding;
import com.example.consumoapp.esios.data.AppDatabase;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Muestra una factura visual a partir de una simulación guardada.
 *
 * Esta pantalla no recalcula importes: utiliza la fotografía persistida en Room
 * para que el desglose coincida con el resultado obtenido originalmente.
 */
public class FacturaVisualActivity extends AppCompatActivity {

    public static final String EXTRA_SIMULACION_ID = "simulacion_id";

    private ActivityFacturaVisualBinding binding;
    private AppDatabase database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFacturaVisualBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarFacturaVisual);
        NavigationUtils.configurarToolbarConAtrasBlanco(this, binding.toolbarFacturaVisual);

        database = AppDatabase.getInstance(this);
        long simulacionId = getIntent().getLongExtra(EXTRA_SIMULACION_ID, -1L);
        long usuarioId = SessionManager.obtenerUsuarioId(this);

        if (simulacionId <= 0 || usuarioId <= 0) {
            mostrarError();
            return;
        }

        cargarFactura(simulacionId, usuarioId);
    }

    private void cargarFactura(long simulacionId, long usuarioId) {
        binding.progressFacturaVisual.setVisibility(View.VISIBLE);
        binding.contenidoFacturaVisual.setVisibility(View.GONE);

        executor.execute(() -> {
            SimulacionFacturaEntity factura = database.simulacionFacturaDao()
                    .obtenerPorIdYUsuario(simulacionId, usuarioId);

            runOnUiThread(() -> {
                binding.progressFacturaVisual.setVisibility(View.GONE);
                if (factura == null) {
                    mostrarError();
                } else {
                    mostrarFactura(factura);
                }
            });
        });
    }

    private void mostrarFactura(SimulacionFacturaEntity f) {
        binding.contenidoFacturaVisual.setVisibility(View.VISIBLE);
        binding.txtFacturaError.setVisibility(View.GONE);

        String fecha = DateFormat.getDateInstance(DateFormat.MEDIUM)
                .format(new Date(f.getFechaCreacion()));
        String nombre = SessionManager.obtenerNombre(this);
        String email = SessionManager.obtenerEmail(this);

        binding.txtNumeroFactura.setText(String.format(Locale.getDefault(), "SIM-%06d", f.getId()));
        binding.txtFechaEmision.setText(fecha);
        binding.txtTitularFactura.setText(valorSeguro(nombre, getString(R.string.factura_dato_no_disponible)));
        binding.txtEmailFactura.setText(valorSeguro(email, getString(R.string.factura_dato_no_disponible)));
        binding.txtComercializadoraFactura.setText(valorSeguro(f.getComercializadora(), getString(R.string.factura_no_aplica)));
        binding.txtTarifaFactura.setText(valorSeguro(f.getTarifa(), valorSeguro(f.getFuentePrecio(), getString(R.string.factura_no_aplica))));
        binding.txtFuenteFactura.setText(valorSeguro(f.getFuentePrecio(), getString(R.string.factura_dato_no_disponible)));
        binding.txtPeriodoFactura.setText(construirPeriodo(f.getFechaCreacion(), f.getDiasFactura()));
        binding.txtDiasFacturadosFactura.setText(getString(R.string.factura_periodo_dias, f.getDiasFactura()));
        binding.txtDireccionFactura.setText(R.string.factura_no_configurado);
        binding.txtCupsFactura.setText(R.string.factura_no_configurado);
        binding.txtLecturasFactura.setText(R.string.factura_no_configurado);
        binding.txtNumeroElectrodomesticosFactura.setText(String.valueOf(f.getNumeroElectrodomesticos()));

        ((TextView) findViewById(R.id.txtConsumoTotalFactura)).setText(formatoKwh(f.getConsumoTotalKwh()));
        ((TextView) findViewById(R.id.txtConsumoP1Factura)).setText(formatoKwh(f.getConsumoP1Kwh()));
        ((TextView) findViewById(R.id.txtConsumoP2Factura)).setText(formatoKwh(f.getConsumoP2Kwh()));
        ((TextView) findViewById(R.id.txtConsumoP3Factura)).setText(formatoKwh(f.getConsumoP3Kwh()));
        ((TextView) findViewById(R.id.txtPotenciaP1Factura)).setText(formatoKw(f.getPotenciaPuntaKw()));
        ((TextView) findViewById(R.id.txtPotenciaP2Factura)).setText(formatoKw(f.getPotenciaValleKw()));

        ((TextView) findViewById(R.id.txtEnergiaFactura)).setText(formatoEuros(f.getCosteEnergia()));
        ((TextView) findViewById(R.id.txtPotenciaFactura)).setText(formatoEuros(f.getCostePotencia()));
        ((TextView) findViewById(R.id.txtPeajesFactura)).setText(formatoEuros(f.getPeajesTotal()));
        ((TextView) findViewById(R.id.txtCargosFactura)).setText(formatoEuros(f.getCargosTotal()));
        ((TextView) findViewById(R.id.txtAjustesFactura)).setText(formatoEuros(f.getAjustesSistema() + f.getOtrosConceptos()));
        ((TextView) findViewById(R.id.txtAlquilerFactura)).setText(formatoEuros(f.getAlquilerContador()));
        ((TextView) findViewById(R.id.txtImpuestoFactura)).setText(formatoEuros(f.getImpuestoElectricidad()));
        ((TextView) findViewById(R.id.txtIvaFactura)).setText(formatoEuros(f.getIva()));
        binding.txtTotalFacturaVisual.setText(formatoEuros(f.getTotalFactura()));

        double energiaGrafico = f.getCosteEnergia() + f.getPeajesEnergia() + f.getCargosEnergia();
        double potenciaGrafico = f.getCostePotencia() + f.getPeajesPotencia() + f.getCargosPotencia();
        double impuestosGrafico = f.getImpuestoElectricidad() + f.getIva();
        double otrosGrafico = f.getAjustesSistema() + f.getOtrosConceptos() + f.getAlquilerContador();
        binding.graficoDistribucionCoste.establecerImportes(
                energiaGrafico, potenciaGrafico, impuestosGrafico, otrosGrafico);
        ((TextView) findViewById(R.id.txtGraficoEnergia)).setText(formatoEuros(energiaGrafico));
        ((TextView) findViewById(R.id.txtGraficoPotencia)).setText(formatoEuros(potenciaGrafico));
        ((TextView) findViewById(R.id.txtGraficoImpuestos)).setText(formatoEuros(impuestosGrafico));
        ((TextView) findViewById(R.id.txtGraficoOtros)).setText(formatoEuros(otrosGrafico));

        String fuenteConstantes = valorSeguro(f.getFuenteConstantes(), getString(R.string.factura_constantes_incluidas));
        binding.txtFuenteConstantesFactura.setText(fuenteConstantes);
    }

    /**
     * Construye un periodo aproximado a partir de la fecha de creación y los días
     * facturados guardados. No inventa lecturas ni fechas externas.
     */
    private String construirPeriodo(long fechaFinMillis, int diasFactura) {
        Calendar fin = Calendar.getInstance();
        fin.setTimeInMillis(fechaFinMillis);

        Calendar inicio = (Calendar) fin.clone();
        inicio.add(Calendar.DAY_OF_MONTH, -Math.max(0, diasFactura - 1));

        DateFormat formato = DateFormat.getDateInstance(DateFormat.SHORT);
        return getString(R.string.factura_periodo_fechas,
                formato.format(inicio.getTime()),
                formato.format(fin.getTime()));
    }

    private void mostrarError() {
        binding.progressFacturaVisual.setVisibility(View.GONE);
        binding.contenidoFacturaVisual.setVisibility(View.GONE);
        binding.txtFacturaError.setVisibility(View.VISIBLE);
    }

    private String formatoEuros(double valor) {
        return String.format(Locale.getDefault(), "%.2f €", valor);
    }

    private String formatoKwh(double valor) {
        return String.format(Locale.getDefault(), "%.2f kWh", valor);
    }

    private String formatoKw(double valor) {
        return String.format(Locale.getDefault(), "%.2f kW", valor);
    }

    private String valorSeguro(String valor, String alternativa) {
        return valor == null || valor.trim().isEmpty() ? alternativa : valor.trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
