package com.example.consumoapp.esios.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.consumoapp.databinding.FragmentGraficosBinding;
import com.example.consumoapp.esios.data.PrecioLuzEntity;
import com.example.consumoapp.esios.network.EsiosRepository;

import java.util.List;
import java.util.Locale;

/**
 * Pantalla de gráficos.
 *
 * Para no añadir librerías externas, el gráfico se representa con barras horizontales
 * hechas con TextView. Más adelante se puede sustituir por MPAndroidChart.
 */
public class GraficosFragment extends Fragment {

    private FragmentGraficosBinding binding;
    private EsiosRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentGraficosBinding.inflate(inflater, container, false);
        repository = new EsiosRepository(requireContext());

        cargarDatosLocales();

        return binding.getRoot();
    }

    private void cargarDatosLocales() {

        repository.cargarPreciosLocales(new EsiosRepository.EsiosRepositoryCallback() {
            @Override
            public void onSuccess(final List<PrecioLuzEntity> precios) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mostrarGrafico(precios);
                    }
                });
            }

            @Override
            public void onError(String mensaje) {
                // Si no hay datos locales, dejamos el estado inicial.
            }
        });
    }

    private void mostrarGrafico(List<PrecioLuzEntity> precios) {

        if (precios == null || precios.isEmpty()) {
            binding.txtEstadoGrafico.setText("Sin datos guardados. Actualiza desde ESIOS.");
            binding.txtResumenGrafico.setText("Precio medio: Sin datos");
            return;
        }

        double suma = 0;
        double maximo = 0;

        for (PrecioLuzEntity precio : precios) {
            suma = suma + precio.getPrecio();
            if (precio.getPrecio() > maximo) {
                maximo = precio.getPrecio();
            }
        }

        double media = suma / precios.size();

        binding.txtEstadoGrafico.setText("Gráfico generado con precios locales");
        binding.txtResumenGrafico.setText(
                String.format(Locale.getDefault(), "Precio medio del día: %.2f €/MWh", media)
        );

        // Mostramos 6 barras de ejemplo usando las primeras horas disponibles.
        pintarBarra(binding.barra1, binding.txtBarra1, precios, 0, maximo);
        pintarBarra(binding.barra2, binding.txtBarra2, precios, 1, maximo);
        pintarBarra(binding.barra3, binding.txtBarra3, precios, 2, maximo);
        pintarBarra(binding.barra4, binding.txtBarra4, precios, 3, maximo);
        pintarBarra(binding.barra5, binding.txtBarra5, precios, 4, maximo);
        pintarBarra(binding.barra6, binding.txtBarra6, precios, 5, maximo);
    }

    private void pintarBarra(TextView barra,
                             TextView texto,
                             List<PrecioLuzEntity> precios,
                             int posicion,
                             double maximo) {

        if (posicion >= precios.size() || maximo <= 0) {
            barra.setText("");
            texto.setText("--:--  Sin datos");
            return;
        }

        PrecioLuzEntity precio = precios.get(posicion);
        int longitud = (int) ((precio.getPrecio() / maximo) * 18);

        if (longitud < 3) {
            longitud = 3;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < longitud; i++) {
            builder.append("█");
        }

        barra.setText(builder.toString());
        texto.setText(
                String.format(Locale.getDefault(), "Hora %02d:00  %.2f €/MWh", posicion, precio.getPrecio())
        );
    }
}
