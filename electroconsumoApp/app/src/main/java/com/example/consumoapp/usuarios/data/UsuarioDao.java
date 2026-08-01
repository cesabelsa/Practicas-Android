package com.example.consumoapp.usuarios.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertar(UsuarioEntity usuario);

    @Update
    void actualizar(UsuarioEntity usuario);

    @Query("SELECT * FROM usuario WHERE activo = 1 AND "
            + "(usuarioNormalizado = :login OR emailNormalizado = :login) LIMIT 1")
    UsuarioEntity buscarParaLogin(String login);

    @Query("SELECT * FROM usuario WHERE id = :id LIMIT 1")
    UsuarioEntity buscarPorId(long id);

    @Query("SELECT * FROM usuario WHERE emailNormalizado = :email LIMIT 1")
    UsuarioEntity buscarPorEmail(String email);

    @Query("SELECT COUNT(*) FROM usuario WHERE usuarioNormalizado = :usuario")
    int contarUsuario(String usuario);

    @Query("SELECT COUNT(*) FROM usuario WHERE emailNormalizado = :email")
    int contarEmail(String email);

    @Query("SELECT COUNT(*) FROM usuario")
    int contarTodos();

    // Diagnóstico para Database Inspector. No contiene contraseñas en texto plano.
    @Query("SELECT * FROM usuario ORDER BY fechaRegistro DESC")
    List<UsuarioEntity> listarTodos();

    @Query("UPDATE usuario SET passwordHash = :hash, passwordSalt = :salt WHERE id = :usuarioId")
    void actualizarPassword(long usuarioId, String hash, String salt);

    @Query("UPDATE usuario SET activo = 0 WHERE id = :usuarioId")
    void desactivar(long usuarioId);
}
