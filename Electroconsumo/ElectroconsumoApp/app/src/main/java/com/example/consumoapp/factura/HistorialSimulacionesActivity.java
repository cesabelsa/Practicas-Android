package com.example.consumoapp.factura;

import com.example.consumoapp.NavigationUtils;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.consumoapp.R;
import com.example.consumoapp.SessionManager;
import com.example.consumoapp.databinding.ActivityHistorialSimulacionesBinding;
import com.example.consumoapp.esios.data.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Pantalla que consulta y administra las simulaciones guardadas en Room. */
public class HistorialSimulacionesActivity extends AppCompatActivity {

    private ActivityHistorialSimulacionesBinding binding;
    private AppDatabase database;
    private HistorialSimulacionesAdapter adapter;
    private long usuarioId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistorialSimulacionesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getInstance(this);
        usuarioId = SessionManager.obtenerUsuarioId(this);
        if (usuarioId <= 0) { finish(); return; }
        setSupportActionBar(binding.toolbarHistorial);
        NavigationUtils.configurarToolbarConAtrasBlanco(this, binding.toolbarHistorial);

        adapter = new HistorialSimulacionesAdapter(new HistorialSimulacionesAdapter.Listener() {
            @Override
            public void onVerFactura(SimulacionFacturaEntity simulacion) {
                Intent intent = new Intent(HistorialSimulacionesActivity.this, FacturaVisualActivity.class);
                intent.putExtra(FacturaVisualActivity.EXTRA_SIMULACION_ID, simulacion.getId());
                startActivity(intent);
            }

            @Override
            public void onEliminar(SimulacionFacturaEntity simulacion) {
                confirmarEliminar(simulacion);
            }
        });
        binding.recyclerHistorial.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerHistorial.setAdapter(adapter);

        binding.btnEliminarHistorial.setOnClickListener(v -> confirmarEliminarTodo());
        cargarHistorial();
    }

    private void cargarHistorial() {
        executor.execute(() -> {
            List<SimulacionFacturaEntity> datos = database.simulacionFacturaDao().listarTodas(usuarioId);
            runOnUiThread(() -> {
                adapter.actualizar(datos);
                boolean vacio = datos.isEmpty();
                binding.txtHistorialVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
                binding.recyclerHistorial.setVisibility(vacio ? View.GONE : View.VISIBLE);
                binding.btnEliminarHistorial.setEnabled(!vacio);
            });
        });
    }

    private void confirmarEliminar(SimulacionFacturaEntity simulacion) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar simulación")
                .setMessage("¿Quieres eliminar esta simulación del historial?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> executor.execute(() -> {
                    database.simulacionFacturaDao().eliminar(simulacion);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Simulación eliminada", Toast.LENGTH_SHORT).show();
                        cargarHistorial();
                    });
                }))
                .show();
    }

    private void confirmarEliminarTodo() {
        new AlertDialog.Builder(this)
                .setTitle("Vaciar historial")
                .setMessage("Se eliminarán todas las simulaciones guardadas.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar todo", (dialog, which) -> executor.execute(() -> {
                    database.simulacionFacturaDao().eliminarTodas(usuarioId);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Historial eliminado", Toast.LENGTH_SHORT).show();
                        cargarHistorial();
                    });
                }))
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
