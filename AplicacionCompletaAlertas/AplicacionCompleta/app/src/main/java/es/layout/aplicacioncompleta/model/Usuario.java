package es.layout.aplicacioncompleta.model;

public class Usuario {
    private String login;
    private String password;

    public Usuario() {
    }

    public Usuario(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = (login != null) ? login : "";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = (password != null) ? password : "";
    }
}
