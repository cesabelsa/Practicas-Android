package com.example.consumoapp.esios.model;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo principal de la respuesta JSON que devuelve ESIOS.
 *
 * La API responde con un objeto llamado "indicator".
 * Dentro de ese objeto vienen el nombre del indicador y sus valores horarios.
 */
public class EsiosResponse {

    // Campo JSON: "indicator".
    @SerializedName("indicator")
    private EsiosIndicator indicator;

    // Devuelve el indicador completo recibido desde ESIOS.
    public EsiosIndicator getIndicator() {
        return indicator;
    }

    // Permite asignar el indicador si fuese necesario.
    public void setIndicator(EsiosIndicator indicator) {
        this.indicator = indicator;
    }
}
