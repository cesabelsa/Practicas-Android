package es.layout.aplicacioncompleta.model.retrofit;

/*
 * Clase que representa la respuesta JSON del login.
 * Ejemplo del archivo response_login.json:
 * nombre, edad, genero, userToken e idBdReference.
 */
public class LoginResponse {

    private String nombre;
    private int edad;
    private int genero;
    private String userToken;
    private int idBdReference;

    public LoginResponse() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public int getGenero() {
        return genero;
    }

    public void setGenero(int genero) {
        this.genero = genero;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public int getIdBdReference() {
        return idBdReference;
    }

    public void setIdBdReference(int idBdReference) {
        this.idBdReference = idBdReference;
    }
}
