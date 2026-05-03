package es.layout.aplicacioncompleta.model.retrofit;

import java.util.List;

/*
 * Clase principal del JSON de hoteles.
 * El JSON tiene un totalCount y un array llamado results.
 */
public class HotelListResponse {

    private int totalCount;
    private List<Hotel> results;

    public HotelListResponse() {
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<Hotel> getResults() {
        return results;
    }

    public void setResults(List<Hotel> results) {
        this.results = results;
    }
}
