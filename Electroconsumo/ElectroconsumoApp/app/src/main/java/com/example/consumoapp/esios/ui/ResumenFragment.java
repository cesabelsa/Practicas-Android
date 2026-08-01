package com.example.consumoapp.esios.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.consumoapp.databinding.FragmentResumenBinding;
import com.example.consumoapp.esios.data.PrecioLuzEntity;
import com.example.consumoapp.esios.network.EsiosRepository;

import java.util.List;
import java.util.Locale;

/**
 * Fragment de resumen.
 *
 * Usa DataBinding para mostrar los textos de resumen en el XML.
 */
public class ResumenFragment extends Fragment {

    private FragmentResumenBinding binding;
    private EsiosRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflamos el layout con DataBinding.
        binding = FragmentResumenBinding.inflate(inflater, container, false);

        // Estado inicial para evitar valores nulos en el XML.
        binding.setResumen(new ResumenPrecio("Sin datos", "Sin datos", "Sin datos", "Sin datos"));

        // Creamos el repositorio que usa Retrofit y Room.
        repository = new EsiosRepository(requireContext());

        // Evento del botón actualizar.
        binding.btnActualizarResumen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                descargarDatos();
            }
        });

        // Al abrir la pantalla intentamos cargar datos guardados.
        cargarDatosLocales();

        return binding.getRoot();
    }

    private void descargarDatos() {

        binding.btnActualizarResumen.setEnabled(false);
        binding.btnActualizarResumen.setText("Actualizando...");

        repository.descargarPreciosHoy(new EsiosRepository.EsiosRepositoryCallback() {
            @Override
            public void onSuccess(final List<PrecioLuzEntity> precios) {

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mostrarResumen(precios);
                        binding.btnActualizarResumen.setEnabled(true);
                        binding.btnActualizarResumen.setText("Actualizar desde ESIOS");
                        Toast.makeText(requireContext(), "Datos ESIOS actualizados", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(final String mensaje) {

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.btnActualizarResumen.setEnabled(true);
                        binding.btnActualizarResumen.setText("Actualizar desde ESIOS");
                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void cargarDatosLocales() {

        repository.cargarPreciosLocales(new EsiosRepository.EsiosRepositoryCallback() {
            @Override
            public void onSuccess(final List<PrecioLuzEntity> precios) {

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mostrarResumen(precios);
                    }
                });
            }

            @Override
            public void onError(String mensaje) {
                // La carga local no muestra error porque puede no haber datos aún.
            }
        });
    }

    private void mostrarResumen(List<PrecioLuzEntity> precios) {

        if (precios == null || precios.isEmpty()) {
            binding.setResumen(new ResumenPrecio("Sin datos", "Sin datos", "Sin datos", "Sin datos"));
            return;
        }

        double suma = 0;
        double minimo = Double.MAX_VALUE;
        double maximo = Double.MIN_VALUE;
        double actual = precios.get(0).getPrecio();

        for (PrecioLuzEntity precio : precios) {
            suma = suma + precio.getPrecio();
            minimo = Math.min(minimo, precio.getPrecio());
            maximo = Math.max(maximo, precio.getPrecio());
        }

        double media = suma / precios.size();

        binding.setResumen(new ResumenPrecio(
                formatear(actual),
                formatear(media),
                formatear(minimo),
                formatear(maximo)
        ));
    }

    private String formatear(double valor) {
        return String.format(Locale.getDefault(), "%.2f €/MWh", valor);
    }
}
