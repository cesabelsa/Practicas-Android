package com.example.consumoapp.esios.provider;

import com.example.consumoapp.core.precio.provider.FuentePrecio;
import com.example.consumoapp.esios.data.PrecioLuzEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Convierte los precios horarios guardados de ESIOS en precios medios P1/P2/P3.
 * Los electrodomésticos y su consumo no se modifican: solo cambia la fuente
 * utilizada para valorar sus kWh.
 */
public class FuentePrecioEsiosLocal implements FuentePrecio {

    private final double p1;
    private final double p2;
    private final double p3;
    private final String fecha;

    public FuentePrecioEsiosLocal(List<PrecioLuzEntity> precios, String fecha) {
        this.fecha = fecha == null ? "" : fecha;
        double sumaP1 = 0, sumaP2 = 0, sumaP3 = 0;
        int nP1 = 0, nP2 = 0, nP3 = 0;

        if (precios != null) {
            for (PrecioLuzEntity precio : precios) {
                int periodo = obtenerPeriodo(precio.getFechaHora());
                double valor = precio.getPrecioKwh() > 0
                        ? precio.getPrecioKwh()
                        : precio.getPrecio() / 1000.0;
                if (periodo == 1) { sumaP1 += valor; nP1++; }
                else if (periodo == 2) { sumaP2 += valor; nP2++; }
                else { sumaP3 += valor; nP3++; }
            }
        }

        double mediaGlobal = mediaGlobal(precios);
        p1 = nP1 == 0 ? mediaGlobal : sumaP1 / nP1;
        p2 = nP2 == 0 ? mediaGlobal : sumaP2 / nP2;
        p3 = nP3 == 0 ? mediaGlobal : sumaP3 / nP3;
    }

    private double mediaGlobal(List<PrecioLuzEntity> precios) {
        if (precios == null || precios.isEmpty()) return 0.0;
        double suma = 0;
        for (PrecioLuzEntity p : precios) {
            suma += p.getPrecioKwh() > 0 ? p.getPrecioKwh() : p.getPrecio() / 1000.0;
        }
        return suma / precios.size();
    }

    /** Devuelve 1=P1, 2=P2 o 3=P3 usando el calendario 2.0TD. */
    private int obtenerPeriodo(String fechaHora) {
        if (fechaHora == null) return 3;
        Calendar c = Calendar.getInstance();
        boolean parsed = false;
        String[] formatos = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss"
        };
        for (String patron : formatos) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(patron, Locale.US);
                c.setTime(sdf.parse(fechaHora));
                parsed = true;
                break;
            } catch (ParseException ignored) { }
        }
        if (!parsed) return 3;

        int dia = c.get(Calendar.DAY_OF_WEEK);
        if (dia == Calendar.SATURDAY || dia == Calendar.SUNDAY) return 3;

        int hora = c.get(Calendar.HOUR_OF_DAY);
        if ((hora >= 10 && hora < 14) || (hora >= 18 && hora < 22)) return 1;
        if ((hora >= 8 && hora < 10) || (hora >= 14 && hora < 18) || hora >= 22) return 2;
        return 3;
    }

    @Override public double getPrecioEnergiaP1() { return p1; }
    @Override public double getPrecioEnergiaP2() { return p2; }
    @Override public double getPrecioEnergiaP3() { return p3; }
    @Override public double getPrecioPotenciaP1Dia() { return 0; }
    @Override public double getPrecioPotenciaP2Dia() { return 0; }
    @Override public String getDescripcionFuente() {
        return "PVPC / ESIOS histórico local" + (fecha.isEmpty() ? "" : " · " + fecha);
    }
}
