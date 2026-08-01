package com.example.consumoapp.esios.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.consumoapp.databinding.FragmentPreciosBinding;
import com.example.consumoapp.esios.data.PrecioLuzEntity;
import com.example.consumoapp.esios.network.EsiosRepository;
import com.example.consumoapp.esios.settings.EsiosPreferences;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment con RecyclerView.
 *
 * Muestra los precios horarios guardados en Room.
 * Ahora permite filtrar por zona:
 * - Península
 * - Canarias
 * - Baleares
 * - Ceuta
 * - Melilla
 *
 * Además, muestra las horas y fechas en formato legible.
 */
public class PreciosFragment extends Fragment {

    private FragmentPreciosBinding binding;
    private PrecioAdapter adapter;
    private EsiosRepository repository;
    private final List<PrecioLuzEntity> todosLosPrecios = new ArrayList<>();
    private String zonaSeleccionada = "Península";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentPreciosBinding.inflate(inflater, container, false);

        repository = new EsiosRepository(requireContext());
        adapter = new PrecioAdapter();

        configurarRecyclerView();
        configurarSelectorZona();
        configurarEventos();
        cargarDatosLocales();

        return binding.getRoot();
    }

    private void configurarRecyclerView() {
        binding.recyclerPrecios.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPrecios.setAdapter(adapter);
    }

    private void configurarSelectorZona() {

        String[] zonas = new String[]{
                "Península",
                "Canarias",
                "Baleares",
                "Ceuta",
                "Melilla",
                "Todas"
        };

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                zonas
        );

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerZona.setAdapter(spinnerAdapter);
        int posicionGuardada = EsiosPreferences.posicionDesdeGeoId(EsiosPreferences.getGeoId(requireContext()));
        binding.spinnerZona.setSelection(posicionGuardada);

        binding.spinnerZona.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                zonaSeleccionada = parent.getItemAtPosition(position).toString();
                aplicarFiltroZona();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacemos nada si el usuario no selecciona ninguna zona.
            }
        });
    }

    private void configurarEventos() {

        adapter.setOnPrecioClickListener(new PrecioAdapter.OnPrecioClickListener() {
            @Override
            public void onPrecioClick(PrecioLuzEntity precio) {
                Toast.makeText(
                        requireContext(),
                        PrecioAdapter.formatearHora(precio.getFechaHora()) + " - "
                                + String.format(Locale.getDefault(), "%.3f €/kWh", precio.getPrecio() / 1000.0),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        binding.swipePrecios.setOnRefreshListener(new androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                descargarDatos();
            }
        });
    }

    private void descargarDatos() {

        binding.swipePrecios.setRefreshing(true);

        repository.descargarPreciosHoy(new EsiosRepository.EsiosRepositoryCallback() {
            @Override
            public void onSuccess(final List<PrecioLuzEntity> precios) {

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        todosLosPrecios.clear();

                        if (precios != null) {
                            todosLosPrecios.addAll(precios);
                        }

                        aplicarFiltroZona();
                        binding.swipePrecios.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onCache(final List<PrecioLuzEntity> precios, final String aviso) {
                requireActivity().runOnUiThread(() -> {
                    todosLosPrecios.clear();
                    if (precios != null) todosLosPrecios.addAll(precios);
                    aplicarFiltroZona();
                    binding.swipePrecios.setRefreshing(false);
                    Toast.makeText(requireContext(),
                            aviso + " Se muestran datos guardados.", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(final String mensaje) {

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.swipePrecios.setRefreshing(false);
                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void cargarDatosLocales() {

        binding.swipePrecios.setRefreshing(true);

        repository.cargarPreciosLocales(new EsiosRepository.EsiosRepositoryCallback() {
            @Override
            public void onSuccess(final List<PrecioLuzEntity> precios) {

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        todosLosPrecios.clear();

                        if (precios != null) {
                            todosLosPrecios.addAll(precios);
                        }

                        aplicarFiltroZona();
                        binding.swipePrecios.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onError(final String mensaje) {

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.swipePrecios.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void aplicarFiltroZona() {

        List<PrecioLuzEntity> preciosFiltrados = new ArrayList<>();

        for (PrecioLuzEntity precio : todosLosPrecios) {
            if (debeMostrarPrecio(precio)) {
                preciosFiltrados.add(precio);
            }
        }

        adapter.setPrecios(preciosFiltrados);
        actualizarResumenZona(preciosFiltrados);
    }

    private boolean debeMostrarPrecio(PrecioLuzEntity precio) {

        if (precio == null) {
            return false;
        }

        if ("Todas".equals(zonaSeleccionada)) {
            return true;
        }

        String zonaPrecio = normalizar(precio.getZona());
        String zonaFiltro = normalizar(zonaSeleccionada);

        return zonaPrecio.contains(zonaFiltro);
    }

    private void actualizarResumenZona(List<PrecioLuzEntity> preciosFiltrados) {

        binding.txtResumenZonaTitulo.setText(zonaSeleccionada);

        if (preciosFiltrados == null || preciosFiltrados.isEmpty()) {
            binding.txtResumenZonaFecha.setText("--/--/----");
            binding.txtResumenZonaDatos.setText("Sin datos para esta zona");
            return;
        }

        double minimo = Double.MAX_VALUE;
        double maximo = 0.0;
        double suma = 0.0;

        for (PrecioLuzEntity precio : preciosFiltrados) {
            double valor = precio.getPrecio() / 1000.0;
            suma = suma + valor;

            if (valor < minimo) {
                minimo = valor;
            }

            if (valor > maximo) {
                maximo = valor;
            }
        }

        double media = suma / preciosFiltrados.size();

        binding.txtResumenZonaFecha.setText(PrecioAdapter.formatearFecha(preciosFiltrados.get(0).getFechaHora()));
        boolean mostrarMwh = EsiosPreferences.UNIDAD_MWH.equals(
                EsiosPreferences.getUnidad(requireContext()));
        if (mostrarMwh) {
            binding.txtResumenZonaDatos.setText(String.format(Locale.getDefault(),
                    "%d horas · Media %.2f €/MWh · Mín %.2f · Máx %.2f",
                    preciosFiltrados.size(), media * 1000.0, minimo * 1000.0, maximo * 1000.0));
        } else {
            binding.txtResumenZonaDatos.setText(String.format(Locale.getDefault(),
                    "%d horas · Media %.3f €/kWh · Mín %.3f · Máx %.3f",
                    preciosFiltrados.size(), media, minimo, maximo));
        }
    }

    private String normalizar(String texto) {

        if (texto == null) {
            return "";
        }

        String textoSinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        return textoSinAcentos.toLowerCase(Locale.ROOT).trim();
    }
}
