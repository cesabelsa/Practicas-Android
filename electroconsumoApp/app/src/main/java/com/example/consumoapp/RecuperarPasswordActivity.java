package com.example.consumoapp;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import com.example.consumoapp.esios.data.AppDatabase;
import com.example.consumoapp.usuarios.data.PasswordUtils;
import com.example.consumoapp.usuarios.data.UsuarioEntity;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Restablecimiento local. No simula el envío de un correo que no existe. */
public class RecuperarPasswordActivity extends AppCompatActivity {
    private EditText edtEmail, edtNuevaPassword;
    private Button btnEnviar;
    private AppDatabase database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_password);
        MaterialToolbar toolbar = findViewById(R.id.toolbarRecuperar);
        setSupportActionBar(toolbar);
        NavigationUtils.configurarToolbarConAtrasBlanco(this, toolbar);
        database = AppDatabase.getInstance(this);
        edtEmail = findViewById(R.id.edtEmail);
        edtNuevaPassword = findViewById(R.id.edtNuevaPassword);
        btnEnviar = findViewById(R.id.btnEnviar);
        TextView volver = findViewById(R.id.txtVolverLogin);
        btnEnviar.setOnClickListener(v -> recuperarPassword());
        volver.setOnClickListener(v -> finish());
    }

    private void recuperarPassword() {
        String email = edtEmail.getText().toString().trim().toLowerCase(Locale.ROOT);
        String nueva = edtNuevaPassword.getText().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { edtEmail.setError("Introduce un correo válido"); return; }
        if (nueva.length() < 8 || !nueva.matches(".*[A-Za-z].*") || !nueva.matches(".*\\d.*")) {
            edtNuevaPassword.setError("Mínimo 8 caracteres, con letra y número"); return;
        }
        btnEnviar.setEnabled(false);
        executor.execute(() -> {
            UsuarioEntity usuario = database.usuarioDao().buscarPorEmail(email);
            if (usuario == null) {
                runOnUiThread(() -> { btnEnviar.setEnabled(true); Toast.makeText(this, "No existe una cuenta con ese correo", Toast.LENGTH_LONG).show(); });
                return;
            }
            PasswordUtils.Credenciales cred = PasswordUtils.crear(nueva);
            database.usuarioDao().actualizarPassword(usuario.getId(), cred.hash, cred.salt);
            runOnUiThread(() -> {
                Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }

    @Override protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
