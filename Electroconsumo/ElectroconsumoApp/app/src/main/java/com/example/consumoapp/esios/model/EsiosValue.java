package com.example.consumoapp.esios.model;

import com.google.gson.annotations.SerializedName;

/** Representa una fila horaria del precio recibido desde ESIOS. */
public class EsiosValue {

    @SerializedName("datetime")
    private String datetime;

    @SerializedName("datetime_utc")
    private String datetimeUtc;

    // El indicador 1001 devuelve el valor en €/MWh.
    @SerializedName("value")
    private double value;

    @SerializedName("geo_id")
    private int geoId;

    @SerializedName("geo_name")
    private String geoName;

    public String getDatetime() { return datetime; }
    public String getDatetimeUtc() { return datetimeUtc; }
    public double getValue() { return value; }
    public int getGeoId() { return geoId; }
    public String getGeoName() { return geoName; }
}
