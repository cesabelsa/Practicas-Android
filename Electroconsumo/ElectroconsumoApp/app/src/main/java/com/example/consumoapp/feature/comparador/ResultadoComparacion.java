package com.example.consumoapp.feature.comparador;

import com.example.consumoapp.core.factura.engine.ResultadoFactura;

/**
 * Resultado de aplicar una tarifa comercial al mismo perfil de consumo.
 *
 * El ahorro se conserva para el periodo real de la simulación. Las cifras
 * mensuales y anuales son proyecciones proporcionales, no nuevos cálculos de
 * consumo.
 */
public class ResultadoComparacion {
    private final String comercializadora;
    private final String tarifa;
    private final ResultadoFactura factura;
    private final double ahorroPeriodo;
    private final int diasPeriodo;
    private final String fechaActualizacion;
    private final String fuente;
    private final String permanencia;
    private final String descuento;
    private final String servicios;
    private final String observaciones;
    private final double precioP1;
    private final double precioP2;
    private final double precioP3;

    public ResultadoComparacion(
            String comercializadora,
            String tarifa,
            ResultadoFactura factura,
            double ahorroPeriodo,
            int diasPeriodo,
            String fechaActualizacion,
            String fuente,
            String permanencia,
            String descuento,
            String servicios,
            String observaciones,
            double precioP1,
            double precioP2,
            double precioP3) {
        this.comercializadora = valorSeguro(comercializadora);
        this.tarifa = valorSeguro(tarifa);
        this.factura = factura;
        this.ahorroPeriodo = ahorroPeriodo;
        this.diasPeriodo = Math.max(1, diasPeriodo);
        this.fechaActualizacion = valorSeguro(fechaActualizacion);
        this.fuente = valorSeguro(fuente);
        this.permanencia = valorSeguro(permanencia);
        this.descuento = valorSeguro(descuento);
        this.servicios = valorSeguro(servicios);
        this.observaciones = valorSeguro(observaciones);
        this.precioP1 = precioP1;
        this.precioP2 = precioP2;
        this.precioP3 = precioP3;
    }

    private static String valorSeguro(String valor) {
        return valor == null || valor.trim().isEmpty() ? "No informado" : valor.trim();
    }

    public String getComercializadora() { return comercializadora; }
    public String getTarifa() { return tarifa; }
    public ResultadoFactura getFactura() { return factura; }
    public double getAhorroPeriodo() { return ahorroPeriodo; }
    public int getDiasPeriodo() { return diasPeriodo; }
    public double getCosteMensualEstimado() { return factura.getTotalFactura() * 30.0 / diasPeriodo; }
    public double getCosteAnualEstimado() { return factura.getTotalFactura() * 365.0 / diasPeriodo; }
    public double getAhorroMensual() { return ahorroPeriodo * 30.0 / diasPeriodo; }
    public double getAhorroAnual() { return ahorroPeriodo * 365.0 / diasPeriodo; }
    public String getFechaActualizacion() { return fechaActualizacion; }
    public String getFuente() { return fuente; }
    public String getPermanencia() { return permanencia; }
    public String getDescuento() { return descuento; }
    public String getServicios() { return servicios; }
    public String getObservaciones() { return observaciones; }
    public double getPrecioP1() { return precioP1; }
    public double getPrecioP2() { return precioP2; }
    public double getPrecioP3() { return precioP3; }

    /** Devuelve true cuando P1, P2 y P3 representan un precio único. */
    public boolean esPrecioUnico() {
        final double tolerancia = 0.000001;
        return Math.abs(precioP1 - precioP2) < tolerancia
                && Math.abs(precioP1 - precioP3) < tolerancia;
    }

    /** Interpreta de forma conservadora el texto de permanencia guardado. */
    public boolean esSinPermanencia() {
        String valor = permanencia.toLowerCase(java.util.Locale.ROOT);
        return valor.contains("sin permanencia")
                || valor.equals("no")
                || valor.startsWith("no ")
                || valor.contains("ninguna");
    }

    /** Indica si la oferta contiene una descripción real de descuento. */
    public boolean tieneDescuento() {
        String valor = descuento.toLowerCase(java.util.Locale.ROOT);
        return !"no informado".equals(valor)
                && !valor.equals("no")
                && !valor.contains("sin descuento")
                && !valor.contains("ninguno");
    }
}

