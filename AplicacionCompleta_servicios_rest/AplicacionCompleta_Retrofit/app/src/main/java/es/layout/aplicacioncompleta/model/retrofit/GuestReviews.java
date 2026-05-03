package es.layout.aplicacioncompleta.model.retrofit;

/* Valoraciones de los clientes dentro del JSON. */
public class GuestReviews {

    private double unformattedRating;
    private String rating;
    private int total;
    private String badgeText;

    public GuestReviews() {
    }

    public double getUnformattedRating() {
        return unformattedRating;
    }

    public void setUnformattedRating(double unformattedRating) {
        this.unformattedRating = unformattedRating;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public void setBadgeText(String badgeText) {
        this.badgeText = badgeText;
    }
}
