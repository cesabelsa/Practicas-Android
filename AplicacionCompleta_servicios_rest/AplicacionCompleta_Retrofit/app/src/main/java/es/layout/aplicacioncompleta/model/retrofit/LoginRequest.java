package es.layout.aplicacioncompleta.model.retrofit;

/*
 * Clase que representa los datos que enviamos al servicio de login.
 * El PDF indica que el endpoint login recibe usuario y password.
 */
public class LoginRequest {

    // Nombre de usuario escrito en la pantalla de login.
    private String usuario;

    // Contraseña escrita en la pantalla de login.
    private String password;

    // Constructor vacío necesario para que Gson pueda trabajar con la clase.
    public LoginRequest() {
    }

    // Constructor con datos para crear rápidamente el objeto antes de enviarlo.
    public LoginRequest(String usuario, String password) {
        this.usuario = usuario;
        this.password = password;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
