package com.example.consumoapp.esios.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Representa el indicador recibido desde ESIOS.
 *
 * Ejemplo: PVPC, demanda eléctrica, precio mercado diario, etc.
 */
public class EsiosIndicator {

    // Nombre del indicador.
    @SerializedName("name")
    private String name;

    // Lista de valores horarios devueltos por la API.
    @SerializedName("values")
    private List<EsiosValue> values;

    public String getName() {
        return name;
    }

    public List<EsiosValue> getValues() {
        return values;
    }
}
