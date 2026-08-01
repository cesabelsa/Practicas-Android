package com.example.consumoapp.usuarios.data;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Usuario local. La contraseña nunca se guarda en texto plano. */
@Entity(tableName = "usuario", indices = {
        @Index(value = {"usuarioNormalizado"}, unique = true),
        @Index(value = {"emailNormalizado"}, unique = true)
})
public class UsuarioEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String usuario;
    private String usuarioNormalizado;
    private String email;
    private String emailNormalizado;
    private String passwordHash;
    private String passwordSalt;
    private long fechaRegistro;
    private boolean activo;

    public UsuarioEntity(String usuario, String usuarioNormalizado, String email,
                         String emailNormalizado, String passwordHash,
                         String passwordSalt, long fechaRegistro, boolean activo) {
        this.usuario = usuario;
        this.usuarioNormalizado = usuarioNormalizado;
        this.email = email;
        this.emailNormalizado = emailNormalizado;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getUsuario() { return usuario; }
    public String getUsuarioNormalizado() { return usuarioNormalizado; }
    public String getEmail() { return email; }
    public String getEmailNormalizado() { return emailNormalizado; }
    public String getPasswordHash() { return passwordHash; }
    public String getPasswordSalt() { return passwordSalt; }
    public long getFechaRegistro() { return fechaRegistro; }
    public boolean isActivo() { return activo; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
