package es.layout.aplicacioncompleta.model.retrofit;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo que representa las coordenadas GPS de un hotel.
 *
 * En el JSON de Retrofit las coordenadas llegan así:
 * "coordinate": {
 *     "lat": 41.38371,
 *     "lon": 2.17111
 * }
 */
public class Coordinate {

    // Latitud del hotel.
    @SerializedName("lat")
    private double lat;

    // Longitud del hotel.
    @SerializedName("lon")
    private double lon;

    public Coordinate() {
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
