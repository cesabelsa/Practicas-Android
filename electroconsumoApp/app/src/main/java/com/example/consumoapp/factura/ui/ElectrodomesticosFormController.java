package com.example.consumoapp.factura.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.consumoapp.databinding.ActivitySimuladorFacturaBinding;
import com.example.consumoapp.factura.ElectrodomesticoEntity;
import com.example.consumoapp.factura.LineaSimuladorFactura;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controla exclusivamente el formulario de uso de electrodomésticos.
 *
 * La Activity ya no necesita conocer cómo se configuran los Spinner,
 * cómo se validan los campos ni cómo se construye una línea de consumo.
 */
public final class ElectrodomesticosFormController {

    public interface CategoriaListener {
        void onCategoriaSeleccionada(String categoria);
    }

    private final Context context;
    private final ActivitySimuladorFacturaBinding binding;
    private final CategoriaListener categoriaListener;
    private final Runnable cambioPeriodoListener;

    private final List<ElectrodomesticoEntity> electrodomesticos = new ArrayList<>();
    private final List<String> periodos = new ArrayList<>();
    private ElectrodomesticoEntity seleccionado;

    public ElectrodomesticosFormController(
            Context context,
            ActivitySimuladorFacturaBinding binding,
            CategoriaListener categoriaListener,
            Runnable cambioPeriodoListener
    ) {
        this.context = context;
        this.binding = binding;
        this.categoriaListener = categoriaListener;
        this.cambioPeriodoListener = cambioPeriodoListener;
    }

    /** Configura los periodos 2.0TD y el panel horario automático. */
    public void configurarPeriodos() {
        periodos.clear();
        periodos.add(LineaSimuladorFactura.PERIODO_AUTO);
        periodos.add(LineaSimuladorFactura.PERIODO_P1);
        periodos.add(LineaSimuladorFactura.PERIODO_P2);
        periodos.add(LineaSimuladorFactura.PERIODO_P3);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                periodos
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPeriodoUso.setAdapter(adapter);
        binding.spinnerPeriodoUso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarPanelHorarioAutomatico();
                cambioPeriodoListener.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Conservamos la selección inicial.
            }
        });
    }

    /** Configura el tipo de día usado para el reparto horario automático. */
    public void configurarTiposDia() {
        List<String> tiposDia = new ArrayList<>();
        tiposDia.add("Laborable");
        tiposDia.add("Sábado, domingo o festivo nacional");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                tiposDia
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTipoDia.setAdapter(adapter);
        binding.edtHoraInicio.setText("8");
        actualizarPanelHorarioAutomatico();
    }

    /** Muestra las categorías y solicita al ViewModel los aparatos de la seleccionada. */
    public void mostrarCategorias(List<String> categorias) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                categorias
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategoria.setAdapter(adapter);
        binding.spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < categorias.size()) {
                    categoriaListener.onCategoriaSeleccionada(categorias.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                seleccionado = null;
            }
        });
    }

    /** Actualiza el catálogo y la ficha del electrodoméstico seleccionado. */
    public void mostrarElectrodomesticos(List<ElectrodomesticoEntity> datos) {
        electrodomesticos.clear();
        electrodomesticos.addAll(datos);

        ArrayAdapter<ElectrodomesticoEntity> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                electrodomesticos
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerElectrodomestico.setAdapter(adapter);
        binding.spinnerElectrodomestico.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < electrodomesticos.size()) {
                    seleccionado = electrodomesticos.get(position);
                    mostrarDatos(seleccionado);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                seleccionado = null;
            }
        });
    }

    /** Carga en el formulario un aparato personalizado procedente de otra pantalla. */
    public void aplicarPersonalizado(
            String nombre,
            double potencia,
            double porcentaje,
            double horas,
            int dias,
            String periodo,
            double horaInicio
    ) {
        seleccionado = new ElectrodomesticoEntity(
                -1,
                "Mi hogar",
                nombre == null ? "Electrodoméstico" : nombre,
                potencia,
                potencia,
                null,
                null,
                null,
                "Usuario",
                "Personalizado"
        );

        binding.edtPotenciaManual.setText(String.valueOf(potencia));
        binding.edtPorcentajeUso.setText(String.valueOf(porcentaje));
        binding.edtHorasDia.setText(String.valueOf(horas));
        binding.edtDiasMes.setText(String.valueOf(dias));
        binding.edtHoraInicio.setText(String.valueOf(horaInicio));

        if (periodo != null) {
            int posicion = periodos.indexOf(periodo);
            if (posicion >= 0) {
                binding.spinnerPeriodoUso.setSelection(posicion);
            }
        }
    }

    /** Valida el formulario y crea la línea; devuelve null cuando hay algún error. */
    public LineaSimuladorFactura crearLinea() {
        if (seleccionado == null) {
            Toast.makeText(context, "Selecciona un electrodoméstico", Toast.LENGTH_SHORT).show();
            return null;
        }

        String potenciaTexto = texto(binding.edtPotenciaManual);
        String horasTexto = texto(binding.edtHorasDia);
        String diasTexto = texto(binding.edtDiasMes);
        String porcentajeTexto = texto(binding.edtPorcentajeUso);

        if (TextUtils.isEmpty(potenciaTexto)) {
            binding.edtPotenciaManual.setError("Introduce potencia en W");
            return null;
        }
        if (TextUtils.isEmpty(horasTexto)) {
            binding.edtHorasDia.setError("Introduce horas al día");
            return null;
        }
        if (TextUtils.isEmpty(diasTexto)) {
            binding.edtDiasMes.setError("Introduce días al mes");
            return null;
        }

        Double potenciaW = leerDoubleSeguro(potenciaTexto);
        Double horasDia = leerDoubleSeguro(horasTexto);
        Integer diasMes = leerEnteroSeguro(diasTexto);
        Double porcentajeUso = TextUtils.isEmpty(porcentajeTexto)
                ? 100.0
                : leerDoubleSeguro(porcentajeTexto);

        if (potenciaW == null) {
            binding.edtPotenciaManual.setError("La potencia no es un número válido");
            return null;
        }
        if (horasDia == null) {
            binding.edtHorasDia.setError("Las horas no son un número válido");
            return null;
        }
        if (diasMes == null) {
            binding.edtDiasMes.setError("Los días deben ser un número entero");
            return null;
        }
        if (porcentajeUso == null) {
            binding.edtPorcentajeUso.setError("El porcentaje no es válido");
            return null;
        }

        if (potenciaW <= 0) {
            binding.edtPotenciaManual.setError("La potencia debe ser mayor que cero");
            return null;
        }
        if (horasDia <= 0 || horasDia > 24) {
            binding.edtHorasDia.setError("Introduce entre 0 y 24 horas");
            return null;
        }
        if (diasMes <= 0 || diasMes > 31) {
            binding.edtDiasMes.setError("Introduce entre 1 y 31 días");
            return null;
        }
        if (porcentajeUso <= 0 || porcentajeUso > 100) {
            binding.edtPorcentajeUso.setError("Introduce un porcentaje entre 1 y 100");
            return null;
        }

        String periodo = obtenerPeriodoSeleccionado();
        if (periodo == null) {
            return null;
        }

        if (LineaSimuladorFactura.PERIODO_AUTO.equals(periodo)) {
            Double horaInicio = leerDoubleSeguro(texto(binding.edtHoraInicio));
            if (horaInicio == null || horaInicio < 0 || horaInicio >= 24) {
                binding.edtHoraInicio.setError("Introduce una hora entre 0 y 23,99");
                return null;
            }

            boolean diaValleCompleto = binding.spinnerTipoDia.getSelectedItemPosition() == 1;
            return new LineaSimuladorFactura(
                    seleccionado.getNombre(), potenciaW, horasDia, diasMes,
                    porcentajeUso, horaInicio, diaValleCompleto, 0.0, 0.0, 0.0
            );
        }

        return new LineaSimuladorFactura(
                seleccionado.getNombre(), potenciaW, horasDia, diasMes,
                porcentajeUso, periodo, 0.0
        );
    }

    /** Limpia solo los campos variables después de añadir una línea. */
    public void limpiarUso() {
        binding.edtHorasDia.setText("");
        binding.edtDiasMes.setText("");
        binding.edtPorcentajeUso.setText("100");
    }

    public String obtenerPeriodoSeleccionado() {
        int posicion = binding.spinnerPeriodoUso.getSelectedItemPosition();
        if (posicion < 0 || posicion >= periodos.size()) {
            return null;
        }
        return periodos.get(posicion);
    }

    private void actualizarPanelHorarioAutomatico() {
        String periodo = obtenerPeriodoSeleccionado();
        boolean automatico = LineaSimuladorFactura.PERIODO_AUTO.equals(periodo);
        binding.panelHorarioAutomatico.setVisibility(automatico ? View.VISIBLE : View.GONE);
    }

    private void mostrarDatos(ElectrodomesticoEntity e) {
        String potenciaMin = e.getPotenciaMinW() == null
                ? "N/D"
                : String.format(Locale.getDefault(), "%.0f W", e.getPotenciaMinW());
        String potenciaMax = e.getPotenciaMaxW() == null
                ? "N/D"
                : String.format(Locale.getDefault(), "%.0f W", e.getPotenciaMaxW());
        String potenciaMedia = e.getPotenciaMediaW() == null
                ? "Introduce potencia manual"
                : String.format(Locale.getDefault(), "%.0f W", e.getPotenciaMediaW());

        binding.txtDatosElectrodomestico.setText(
                "Categoría: " + e.getCategoria()
                        + "\nPotencia mínima publicada: " + potenciaMin
                        + "\nPotencia máxima publicada: " + potenciaMax
                        + "\nPotencia media usada: " + potenciaMedia
                        + "\nUso publicado: " + valorONoDisponible(e.getUsoPublicado())
                        + "\nConsumo publicado: " + valorONoDisponible(e.getConsumoPublicado())
                        + " " + valorONoDisponible(e.getUnidadConsumo())
                        + "\nFuentes Excel: " + valorONoDisponible(e.getFuentes())
        );

        if (e.getPotenciaMediaW() == null) {
            binding.edtPotenciaManual.setText("");
        } else {
            binding.edtPotenciaManual.setText(
                    String.format(Locale.US, "%.0f", e.getPotenciaMediaW())
            );
        }
    }

    private static String valorONoDisponible(String valor) {
        return valor == null || valor.trim().isEmpty() ? "N/D" : valor;
    }

    private static String texto(android.widget.EditText campo) {
        return campo.getText().toString().trim();
    }

    private static Double leerDoubleSeguro(String texto) {
        try {
            return Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException error) {
            return null;
        }
    }

    private static Integer leerEnteroSeguro(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException error) {
            return null;
        }
    }
}
