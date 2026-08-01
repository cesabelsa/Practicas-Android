package com.example.consumoapp.core.precio.provider;

/**
 * Contrato común para cualquier origen de precios.
 *
 * Lo implementarán ESIOS, las tarifas comerciales y la entrada manual.
 */
public interface FuentePrecio {
    double getPrecioEnergiaP1();
    double getPrecioEnergiaP2();
    double getPrecioEnergiaP3();
    double getPrecioPotenciaP1Dia();
    double getPrecioPotenciaP2Dia();
    String getDescripcionFuente();
}
