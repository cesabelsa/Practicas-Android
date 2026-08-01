package com.example.consumoapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.consumoapp.databinding.ActivityAjustesGeneralesBinding;
import com.example.consumoapp.esios.data.AppDatabase;
import com.example.consumoapp.esios.data.PrecioLuzEntity;
import com.example.consumoapp.esios.network.EsiosRepository;
import com.example.consumoapp.esios.settings.EsiosPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Ajustes ESIOS accesibles desde el menú principal de Electroconsumo. */
public class AjustesGeneralesActivity extends AppCompatActivity {

    private ActivityAjustesGeneralesBinding binding;
    private EsiosRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean cargandoControles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAjustesGeneralesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarAjustesGenerales);
        NavigationUtils.configurarToolbarConAtrasBlanco(this, binding.toolbarAjustesGenerales);

        repository = new EsiosRepository(this);
        configurarZona();
        cargarPreferencias();
        configurarAcciones();
        actualizarInformacion();
    }

    private void configurarZona() {
        String[] zonas = {"Península", "Canarias", "Baleares", "Ceuta", "Melilla"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, zonas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerZonaEsios.setAdapter(adapter);
    }

    private void cargarPreferencias() {
        cargandoControles = true;
        binding.spinnerZonaEsios.setSelection(EsiosPreferences.posicionDesdeGeoId(
                EsiosPreferences.getGeoId(this)));
        boolean kwh = EsiosPreferences.UNIDAD_KWH.equals(EsiosPreferences.getUnidad(this));
        binding.radioKwh.setChecked(kwh);
        binding.radioMwh.setChecked(!kwh);
        binding.switchActualizarInicio.setChecked(EsiosPreferences.actualizarAlIniciar(this));
        binding.switchUsarCache.setChecked(EsiosPreferences.usarCache(this));
        cargandoControles = false;
    }

    private void configurarAcciones() {
        binding.spinnerZonaEsios.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (!cargandoControles) {
                    EsiosPreferences.setGeoId(AjustesGeneralesActivity.this,
                            EsiosPreferences.geoIdDesdePosicion(position));
                    Toast.makeText(AjustesGeneralesActivity.this,
                            "Zona guardada. Actualiza los precios para aplicar el cambio.",
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        binding.radioGroupUnidad.setOnCheckedChangeListener((group, checkedId) -> {
            if (cargandoControles) return;
            EsiosPreferences.setUnidad(this,
                    checkedId == R.id.radioMwh ? EsiosPreferences.UNIDAD_MWH : EsiosPreferences.UNIDAD_KWH);
        });

        binding.switchActualizarInicio.setOnCheckedChangeListener((button, checked) -> {
            if (!cargandoControles) EsiosPreferences.setActualizarAlIniciar(this, checked);
        });
        binding.switchUsarCache.setOnCheckedChangeListener((button, checked) -> {
            if (!cargandoControles) EsiosPreferences.setUsarCache(this, checked);
        });

        binding.btnActualizarAhora.setOnClickListener(v -> actualizarAhora());
        binding.btnBorrarPrecios.setOnClickListener(v -> confirmarBorrado());
    }

    private void actualizarAhora() {
        binding.btnActualizarAhora.setEnabled(false);
        binding.txtEstadoConexion.setText("Actualizando…");
        repository.descargarPreciosHoy(new EsiosRepository.EsiosRepositoryCallback() {
            @Override public void onSuccess(List<PrecioLuzEntity> precios) {
                runOnUiThread(() -> {
                    binding.btnActualizarAhora.setEnabled(true);
                    binding.txtEstadoConexion.setText("Conectado");
                    Toast.makeText(AjustesGeneralesActivity.this,
                            "Precios actualizados: " + precios.size() + " horas",
                            Toast.LENGTH_SHORT).show();
                    actualizarInformacion();
                });
            }
            @Override public void onCache(List<PrecioLuzEntity> precios, String aviso) {
                runOnUiThread(() -> {
                    binding.btnActualizarAhora.setEnabled(true);
                    binding.txtEstadoConexion.setText("Usando datos guardados");
                    Toast.makeText(AjustesGeneralesActivity.this, aviso, Toast.LENGTH_LONG).show();
                    actualizarInformacion();
                });
            }
            @Override public void onError(String mensaje) {
                runOnUiThread(() -> {
                    binding.btnActualizarAhora.setEnabled(true);
                    binding.txtEstadoConexion.setText("Error ESIOS");
                    Toast.makeText(AjustesGeneralesActivity.this, mensaje, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void confirmarBorrado() {
        new AlertDialog.Builder(this)
                .setTitle("Borrar precios guardados")
                .setMessage("Se eliminará únicamente la caché de precios ESIOS. No se borrarán usuarios, electrodomésticos, tarifas ni simulaciones.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Borrar", (dialog, which) -> repository.borrarTodoLocal(() ->
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Precios ESIOS borrados", Toast.LENGTH_SHORT).show();
                            actualizarInformacion();
                        })))
                .show();
    }

    private void actualizarInformacion() {
        final boolean conectado = hayConexion();
        executor.execute(() -> {
            Long ultima = AppDatabase.getInstance(getApplicationContext())
                    .precioLuzDao().obtenerUltimaFechaDescarga();
            runOnUiThread(() -> {
                if (ultima == null || ultima <= 0L) {
                    binding.txtUltimaActualizacion.setText("Sin datos guardados");
                    binding.txtEstadoConexion.setText(conectado ? "Conectado · sin datos" : "Sin conexión · sin datos");
                } else {
                    String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(new Date(ultima));
                    binding.txtUltimaActualizacion.setText(fecha);
                    binding.txtEstadoConexion.setText(conectado ? "Conectado" : "Usando datos guardados");
                }
            });
        });
    }

    private boolean hayConexion() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager == null ? null : manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @Override protected void onResume() {
        super.onResume();
        actualizarInformacion();
    }
}
