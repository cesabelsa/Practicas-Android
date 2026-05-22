package es.layout.aplicacioncompleta.model.retrofit;

/*
 * Modelo de un hotel.
 * Contiene solo los campos que necesitamos pintar en el RecyclerView.
 */
public class Hotel {

    private int id;
    private String name;
    private int starRating;
    private Address address;
    private GuestReviews guestReviews;
    private RatePlan ratePlan;
    private OptimizedThumbUrls optimizedThumbUrls;

    // Coordenadas GPS del hotel recibidas desde el servicio Retrofit/JSON.
    private Coordinate coordinate;

    public Hotel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStarRating() {
        return starRating;
    }

    public void setStarRating(int starRating) {
        this.starRating = starRating;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public GuestReviews getGuestReviews() {
        return guestReviews;
    }

    public void setGuestReviews(GuestReviews guestReviews) {
        this.guestReviews = guestReviews;
    }

    public RatePlan getRatePlan() {
        return ratePlan;
    }

    public void setRatePlan(RatePlan ratePlan) {
        this.ratePlan = ratePlan;
    }

    public OptimizedThumbUrls getOptimizedThumbUrls() {
        return optimizedThumbUrls;
    }

    public void setOptimizedThumbUrls(OptimizedThumbUrls optimizedThumbUrls) {
        this.optimizedThumbUrls = optimizedThumbUrls;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }
}
