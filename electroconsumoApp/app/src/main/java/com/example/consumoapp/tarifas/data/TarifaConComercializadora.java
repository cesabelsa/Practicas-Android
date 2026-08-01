package com.example.consumoapp.tarifas.data;

import androidx.room.Embedded;

/**
 * Resultado preparado para la interfaz: contiene la tarifa y el nombre de su
 * comercializadora en un único objeto.
 */
public class TarifaConComercializadora {

    @Embedded
    public TarifaComercialEntity tarifa;

    public String nombreComercializadora;
}
