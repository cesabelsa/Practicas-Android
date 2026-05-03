package es.layout.aplicacioncompleta;

/**
 * Navegación de autenticación entre Login, Registro y Home.
 */
public interface AuthNavigator {
    void goToRegister();
    void goToLogin();
    void goToHomeAndFinish();
}
