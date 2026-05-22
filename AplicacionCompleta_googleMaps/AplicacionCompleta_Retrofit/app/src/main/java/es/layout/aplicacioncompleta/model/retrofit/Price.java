package es.layout.aplicacioncompleta.model.retrofit;

/* Precio del hotel. */
public class Price {

    private String current;
    private double exactCurrent;

    public Price() {
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public double getExactCurrent() {
        return exactCurrent;
    }

    public void setExactCurrent(double exactCurrent) {
        this.exactCurrent = exactCurrent;
    }
}
