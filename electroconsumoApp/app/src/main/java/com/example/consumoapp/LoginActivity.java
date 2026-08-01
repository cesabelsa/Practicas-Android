package com.example.consumoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.consumoapp.esios.data.AppDatabase;
import com.example.consumoapp.usuarios.data.PasswordUtils;
import com.example.consumoapp.usuarios.data.UsuarioEntity;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Login real contra Room: admite nombre de usuario o correo electrónico. */
public class LoginActivity extends AppCompatActivity {
    private EditText edtUsuario;
    private EditText edtPassword;
    private Button btnEntrar;
    private TextView txtCrearCuenta;
    private TextView txtOlvidastePassword;
    private AppDatabase database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        database = AppDatabase.getInstance(this);

        edtUsuario = findViewById(R.id.edtUsuario);
        edtPassword = findViewById(R.id.edtPassword);
        btnEntrar = findViewById(R.id.btnEntrar);
        txtCrearCuenta = findViewById(R.id.txtCrearCuenta);
        txtOlvidastePassword = findViewById(R.id.txtOlvidastePassword);

        btnEntrar.setOnClickListener(v -> validarLogin());
        txtCrearCuenta.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
        txtOlvidastePassword.setOnClickListener(v -> startActivity(new Intent(this, RecuperarPasswordActivity.class)));
    }

    private void validarLogin() {
        String login = texto(edtUsuario).toLowerCase(Locale.ROOT);
        String password = texto(edtPassword);
        if (login.isEmpty()) { edtUsuario.setError("Introduce tu usuario o email"); return; }
        if (password.isEmpty()) { edtPassword.setError("Introduce tu contraseña"); return; }

        btnEntrar.setEnabled(false);
        executor.execute(() -> {
            try {
                UsuarioEntity usuario = database.usuarioDao().buscarParaLogin(login);
                boolean valido = usuario != null && PasswordUtils.verificar(
                        password, usuario.getPasswordHash(), usuario.getPasswordSalt());
                runOnUiThread(() -> {
                    btnEntrar.setEnabled(true);
                    if (!valido) {
                        Toast.makeText(this, "Usuario, email o contraseña incorrectos", Toast.LENGTH_LONG).show();
                        return;
                    }
                    SessionManager.guardarSesion(this, usuario.getId(), usuario.getUsuario(), usuario.getEmail());
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnEntrar.setEnabled(true);
                    Toast.makeText(this,
                            "No se pudo consultar SQLite: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private String texto(EditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
