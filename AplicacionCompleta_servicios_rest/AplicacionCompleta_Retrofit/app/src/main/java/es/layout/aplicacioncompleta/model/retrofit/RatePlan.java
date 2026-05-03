package es.layout.aplicacioncompleta.model.retrofit;

/* Bloque del JSON donde está el precio del hotel. */
public class RatePlan {

    private Price price;

    public RatePlan() {
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }
}
