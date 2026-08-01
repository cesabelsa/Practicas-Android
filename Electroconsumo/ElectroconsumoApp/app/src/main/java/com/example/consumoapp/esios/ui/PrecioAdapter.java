package com.example.consumoapp.esios.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.consumoapp.databinding.ItemPrecioLuzBinding;
import com.example.consumoapp.esios.data.PrecioLuzEntity;
import com.example.consumoapp.esios.settings.EsiosPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter del RecyclerView.
 *
 * Convierte cada PrecioLuzEntity en una tarjeta visual.
 * Ahora muestra:
 * - Hora limpia: 12:00 - 13:00.
 * - Fecha limpia: 22/06/2026.
 * - Precio principal en €/kWh.
 * - Precio secundario en €/MWh.
 * - Periodo PVPC: VALLE, LLANO o PUNTA.
 * - Barra gráfica proporcional al precio más alto del listado.
 */
public class PrecioAdapter extends RecyclerView.Adapter<PrecioAdapter.PrecioViewHolder> {

    private final List<PrecioLuzEntity> precios = new ArrayList<>();
    private OnPrecioClickListener listener;
    private double precioMaximo = 1.0;

    public void setPrecios(List<PrecioLuzEntity> nuevosPrecios) {
        precios.clear();
        precioMaximo = 1.0;

        if (nuevosPrecios != null) {
            precios.addAll(nuevosPrecios);

            for (PrecioLuzEntity precio : nuevosPrecios) {
                if (precio.getPrecio() > precioMaximo) {
                    precioMaximo = precio.getPrecio();
                }
            }
        }

        notifyDataSetChanged();
    }

    public void setOnPrecioClickListener(OnPrecioClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PrecioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ItemPrecioLuzBinding binding = ItemPrecioLuzBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new PrecioViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PrecioViewHolder holder, int position) {
        holder.bind(precios.get(position));
    }

    @Override
    public int getItemCount() {
        return precios.size();
    }

    class PrecioViewHolder extends RecyclerView.ViewHolder {

        private final ItemPrecioLuzBinding binding;

        PrecioViewHolder(ItemPrecioLuzBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final PrecioLuzEntity precio) {

            int hora = obtenerHora(precio.getFechaHora());
            String periodo = obtenerPeriodoPvpc(hora);
            int colorPeriodo = obtenerColorPeriodo(periodo);
            double precioKwh = precio.getPrecio() / 1000.0;
            int progreso = (int) Math.round((precio.getPrecio() * 100.0) / precioMaximo);

            if (progreso < 4) {
                progreso = 4;
            }

            binding.txtHoraPrecio.setText(formatearHora(precio.getFechaHora()));
            binding.txtFechaPrecio.setText(formatearFecha(precio.getFechaHora()));
            boolean mostrarMwh = EsiosPreferences.UNIDAD_MWH.equals(
                    EsiosPreferences.getUnidad(binding.getRoot().getContext()));
            if (mostrarMwh) {
                binding.txtValorPrecio.setText(String.format(Locale.getDefault(), "%.2f €/MWh", precio.getPrecio()));
                binding.txtValorMwhPrecio.setText(String.format(Locale.getDefault(), "%.3f €/kWh", precioKwh));
            } else {
                binding.txtValorPrecio.setText(String.format(Locale.getDefault(), "%.3f €/kWh", precioKwh));
                binding.txtValorMwhPrecio.setText(String.format(Locale.getDefault(), "%.2f €/MWh", precio.getPrecio()));
            }
            binding.txtPeriodoPrecio.setText(periodo);
            binding.txtPeriodoPrecio.setTextColor(colorPeriodo);
            binding.txtZonaPrecio.setText(precio.getZona() == null ? "Zona no indicada" : precio.getZona());
            binding.progresoPrecio.setProgress(progreso);

            aplicarColorBarra(colorPeriodo);
            aplicarFondoPeriodo(colorPeriodo);

            binding.getRoot().setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (listener != null) {
                        listener.onPrecioClick(precio);
                    }
                }
            });
        }

        private void aplicarColorBarra(int color) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.progresoPrecio.setProgressTintList(ColorStateList.valueOf(color));
                binding.progresoPrecio.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EEF3F8")));
            }
        }

        private void aplicarFondoPeriodo(int color) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(18f);
            drawable.setColor(obtenerColorSuave(color));
            drawable.setStroke(1, color);
            binding.txtPeriodoPrecio.setBackground(drawable);
        }
    }

    public interface OnPrecioClickListener {
        void onPrecioClick(PrecioLuzEntity precio);
    }

    public static String formatearHora(String fechaHora) {
        int hora = obtenerHora(fechaHora);
        int horaFin = hora + 1;

        if (horaFin == 24) {
            horaFin = 0;
        }

        return String.format(Locale.getDefault(), "%02d:00 - %02d:00", hora, horaFin);
    }

    public static String formatearFecha(String fechaHora) {
        if (fechaHora == null || fechaHora.length() < 10) {
            return "Fecha no disponible";
        }

        String anio = fechaHora.substring(0, 4);
        String mes = fechaHora.substring(5, 7);
        String dia = fechaHora.substring(8, 10);

        return dia + "/" + mes + "/" + anio;
    }

    public static int obtenerHora(String fechaHora) {
        if (fechaHora == null || fechaHora.length() < 13) {
            return 0;
        }

        try {
            return Integer.parseInt(fechaHora.substring(11, 13));
        } catch (Exception e) {
            return 0;
        }
    }

    public static String obtenerPeriodoPvpc(int hora) {
        if (hora >= 0 && hora < 8) {
            return "VALLE";
        }

        if ((hora >= 10 && hora < 14) || (hora >= 18 && hora < 22)) {
            return "PUNTA";
        }

        return "LLANO";
    }

    private static int obtenerColorPeriodo(String periodo) {
        if ("VALLE".equals(periodo)) {
            return Color.parseColor("#22A447");
        }

        if ("PUNTA".equals(periodo)) {
            return Color.parseColor("#EF3B2D");
        }

        return Color.parseColor("#F59E0B");
    }

    private static int obtenerColorSuave(int color) {
        if (color == Color.parseColor("#22A447")) {
            return Color.parseColor("#EAF8EE");
        }

        if (color == Color.parseColor("#EF3B2D")) {
            return Color.parseColor("#FDEDEC");
        }

        return Color.parseColor("#FFF6E5");
    }
}
