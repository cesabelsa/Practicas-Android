package es.layout.login.model;


public class Usuario {
    private String login;   // antes: nombre
    private String password;  // antes: edad

    public Usuario() { }

    public Usuario(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String usuario) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Compara la contraseña de forma directa.
     * (Para producción, ver versión segura más abajo.)
     */
    public boolean validarPassword(String intento) {
        return password != null && password.equals(intento);
    }

    @Override
    public String toString() {
        // Nunca incluir la contraseña en toString()
        return login;
    }
}

