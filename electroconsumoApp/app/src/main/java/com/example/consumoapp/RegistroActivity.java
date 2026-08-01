package com.example.consumoapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.consumoapp.esios.data.AppDatabase;
import com.example.consumoapp.usuarios.data.PasswordUtils;
import com.example.consumoapp.usuarios.data.UsuarioEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Registro local real con comprobación de duplicados y contraseña protegida. */
public class RegistroActivity extends AppCompatActivity {
    private TextInputEditText edtEmail, edtUsuario, edtPassword;
    private MaterialCheckBox chkAceptacionLegal;
    private MaterialButton btnRegistrar;
    private TextView txtVolverLogin;
    private AppDatabase database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        database = AppDatabase.getInstance(this);

        edtEmail = findViewById(R.id.edtEmail);
        edtUsuario = findViewById(R.id.edtUsuario);
        edtPassword = findViewById(R.id.edtPassword);
        chkAceptacionLegal = findViewById(R.id.chkAceptacionLegal);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        txtVolverLogin = findViewById(R.id.txtVolverLogin);

        configurarEnlacesLegales();
        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { actualizarEstadoBotonRegistro(); }
            public void afterTextChanged(Editable s) {}
        };
        edtEmail.addTextChangedListener(watcher);
        edtUsuario.addTextChangedListener(watcher);
        edtPassword.addTextChangedListener(watcher);
        chkAceptacionLegal.setOnCheckedChangeListener((buttonView, isChecked) -> actualizarEstadoBotonRegistro());
        btnRegistrar.setOnClickListener(v -> registrarUsuario());
        txtVolverLogin.setOnClickListener(v -> volverAlLogin());
        actualizarEstadoBotonRegistro();
    }

    private void configurarEnlacesLegales() {
        String texto = "He leído y acepto los Términos y Condiciones y la Política de Privacidad";
        SpannableString span = new SpannableString(texto);
        String terminos = "Términos y Condiciones";
        String privacidad = "Política de Privacidad";
        span.setSpan(new ClickableSpan() {
            @Override public void onClick(View widget) { startActivity(new Intent(RegistroActivity.this, TerminosCondicionesActivity.class)); }
        }, texto.indexOf(terminos), texto.indexOf(terminos) + terminos.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ClickableSpan() {
            @Override public void onClick(View widget) { startActivity(new Intent(RegistroActivity.this, PoliticaPrivacidadActivity.class)); }
        }, texto.indexOf(privacidad), texto.indexOf(privacidad) + privacidad.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        chkAceptacionLegal.setText(span);
        chkAceptacionLegal.setMovementMethod(LinkMovementMethod.getInstance());
        chkAceptacionLegal.setHighlightColor(ContextCompat.getColor(this, android.R.color.transparent));
    }

    private void actualizarEstadoBotonRegistro() {
        boolean valido = !texto(edtUsuario).isEmpty() && !texto(edtEmail).isEmpty()
                && !texto(edtPassword).isEmpty() && chkAceptacionLegal.isChecked();
        btnRegistrar.setEnabled(valido);
        btnRegistrar.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                this, valido ? R.color.boton_primario : R.color.azul_claro)));
    }

    private void registrarUsuario() {
        String usuario = texto(edtUsuario);
        String email = texto(edtEmail);
        String password = texto(edtPassword);
        if (usuario.length() < 3) { edtUsuario.setError("Usa al menos 3 caracteres"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { edtEmail.setError("Introduce un email válido"); return; }
        if (!passwordSegura(password)) {
            edtPassword.setError("Mínimo 8 caracteres, con letra y número");
            return;
        }
        if (!chkAceptacionLegal.isChecked()) {
            Toast.makeText(this, "Debes aceptar las condiciones legales", Toast.LENGTH_LONG).show();
            return;
        }

        String usuarioNorm = usuario.toLowerCase(Locale.ROOT);
        String emailNorm = email.toLowerCase(Locale.ROOT);
        btnRegistrar.setEnabled(false);
        executor.execute(() -> {
            if (database.usuarioDao().contarUsuario(usuarioNorm) > 0) {
                runOnUiThread(() -> { btnRegistrar.setEnabled(true); edtUsuario.setError("Ese usuario ya existe"); });
                return;
            }
            if (database.usuarioDao().contarEmail(emailNorm) > 0) {
                runOnUiThread(() -> { btnRegistrar.setEnabled(true); edtEmail.setError("Ese email ya está registrado"); });
                return;
            }
            try {
                // La contraseña no se guarda en texto plano. Se almacenan un hash PBKDF2
                // y un salt aleatorio, que son los campos passwordHash/passwordSalt.
                PasswordUtils.Credenciales cred = PasswordUtils.crear(password);
                UsuarioEntity entity = new UsuarioEntity(usuario, usuarioNorm, email, emailNorm,
                        cred.hash, cred.salt, System.currentTimeMillis(), true);

                long id = database.usuarioDao().insertar(entity);
                UsuarioEntity guardado = database.usuarioDao().buscarPorId(id);

                if (id <= 0 || guardado == null) {
                    throw new IllegalStateException("Room no confirmó el usuario insertado");
                }

                runOnUiThread(() -> {
                    SessionManager.guardarSesion(this, id, guardado.getUsuario(), guardado.getEmail());
                    Toast.makeText(this,
                            "Cuenta guardada en SQLite. Usuario ID: " + id,
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnRegistrar.setEnabled(true);
                    Toast.makeText(this,
                            "No se pudo guardar la cuenta en SQLite: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private boolean passwordSegura(String p) {
        return p.length() >= 8 && p.matches(".*[A-Za-zÁÉÍÓÚáéíóúÑñ].*") && p.matches(".*\\d.*");
    }

    private void volverAlLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
