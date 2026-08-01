package com.example.consumoapp.esios.ui;

/**
 * Modelo sencillo para DataBinding.
 *
 * El XML de resumen lee estos textos directamente con @{resumen.precioMedio}.
 */
public class ResumenPrecio {

    private final String precioActual;
    private final String precioMedio;
    private final String precioMinimo;
    private final String precioMaximo;

    public ResumenPrecio(String precioActual, String precioMedio, String precioMinimo, String precioMaximo) {
        this.precioActual = precioActual;
        this.precioMedio = precioMedio;
        this.precioMinimo = precioMinimo;
        this.precioMaximo = precioMaximo;
    }

    public String getPrecioActual() {
        return precioActual;
    }

    public String getPrecioMedio() {
        return precioMedio;
    }

    public String getPrecioMinimo() {
        return precioMinimo;
    }

    public String getPrecioMaximo() {
        return precioMaximo;
    }
}
