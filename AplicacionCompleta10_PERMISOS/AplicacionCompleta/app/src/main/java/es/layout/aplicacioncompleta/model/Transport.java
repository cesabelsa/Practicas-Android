package es.layout.aplicacioncompleta.model;

/**
 * Modelo para representar un tipo de transporte en la lista de la pestaña 1.
 */
public class Transport {

    private final String name;
    private final String description;
    private final int imageResId;

    public Transport(String name, String description, int imageResId) {
        this.name = name;
        this.description = description;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }
}
