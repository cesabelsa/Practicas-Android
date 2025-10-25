package es.layout.aplicacioncompleta;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_REGISTER = 1001;

    public static final String EXTRA_USER = "extra_user";
    public static final String EXTRA_PASS = "extra_pass";

    private String registeredName = null;
    private String registeredSurname = null;

    private TextInputLayout tilUser, tilPass;
    private TextInputEditText edtUser, edtPass;
    private MaterialButton btnLogin;

    // Comienzan VISIBLES y con el ojo ABIERTO
    private boolean isUserHidden = false;
    private boolean isPassHidden = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tilUser = findViewById(R.id.tilUser);
        tilPass = findViewById(R.id.tilPass);
        edtUser = findViewById(R.id.edtUser);
        edtPass = findViewById(R.id.edtPass);
        btnLogin = findViewById(R.id.btnLogin);

        TextView txtForgot    = findViewById(R.id.txtForgot);
        TextView txtGetNew    = findViewById(R.id.txtGetNew);
        TextView txtCreateNew = findViewById(R.id.txtCreateNew);

        // Estado inicial: visibles + ojo abierto
        edtUser.setTransformationMethod(null);
        tilUser.setEndIconDrawable(R.drawable.baseline_visibility_24);

        edtPass.setTransformationMethod(null); // visible de inicio
        tilPass.setEndIconDrawable(R.drawable.baseline_visibility_24);

        // Solo "Get new" y "Create new" abren RegisterActivity
        View.OnClickListener goRegister = new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivityForResult(
                        new Intent(LoginActivity.this, RegisterActivity.class),
                        RC_REGISTER
                );
            }
        };
        txtGetNew.setOnClickListener(goRegister);
        txtCreateNew.setOnClickListener(goRegister);

        // Habilitar Login si ambos campos tienen texto
        btnLogin.setEnabled(false);
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String u = edtUser.getText() != null ? edtUser.getText().toString().trim() : "";
                String p = edtPass.getText() != null ? edtPass.getText().toString().trim() : "";
                btnLogin.setEnabled(!u.isEmpty() && !p.isEmpty());
            }
        };
        edtUser.addTextChangedListener(watcher);
        edtPass.addTextChangedListener(watcher);

        // Toggle USUARIO
        tilUser.setEndIconOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (isUserHidden) {
                    // Mostrar
                    edtUser.setTransformationMethod(null);
                    tilUser.setEndIconDrawable(R.drawable.baseline_visibility_24);
                } else {
                    // Ocultar
                    edtUser.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    tilUser.setEndIconDrawable(R.drawable.baseline_visibility_off_24);
                }
                isUserHidden = !isUserHidden;
                if (edtUser.getText() != null) {
                    edtUser.setSelection(edtUser.getText().length());
                }
            }
        });

        // Toggle CONTRASEÑA
        tilPass.setEndIconOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (isPassHidden) {
                    // Mostrar
                    edtPass.setTransformationMethod(null);
                    tilPass.setEndIconDrawable(R.drawable.baseline_visibility_24);
                } else {
                    // Ocultar (password)
                    edtPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    tilPass.setEndIconDrawable(R.drawable.baseline_visibility_off_24);
                }
                isPassHidden = !isPassHidden;
                if (edtPass.getText() != null) {
                    edtPass.setSelection(edtPass.getText().length());
                }
            }
        });

        // Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String u = edtUser.getText() != null ? edtUser.getText().toString().trim() : "";
                String p = edtPass.getText() != null ? edtPass.getText().toString().trim() : "";

                if (registeredName == null || registeredSurname == null) {
                    edtUser.setError("Primero ve a Register now y regístrate");
                    return;
                }
                // En este flujo, comparamos: user = nombre, pass = apellidos (como ejemplo didáctico)
                if (u.equals(registeredName) && p.equals(registeredSurname)) {
                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                    i.putExtra(EXTRA_USER, u);
                    i.putExtra(EXTRA_PASS, p);
                    // (ELIMINADO) i.putExtra(EXTRA_SURNAME, s);  // 's' no existe y EXTRA_SURNAME no está definido
                    startActivity(i);
                } else {
    // Mostrar alerta modal y NO navegar a Home
    if (edtPass != null) edtPass.setError(null);
    new AlertDialog.Builder(LoginActivity.this)
        .setTitle("Datos incorrectos")
        .setMessage("Usuario o contraseña no válidos. Verifique los datos.")
        .setPositiveButton("ENTENDIDO", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
        })
        .setCancelable(true)
        .show();
}

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_REGISTER && resultCode == RESULT_OK && data != null) {
            // OJO: Usar las constantes reales de RegisterActivity
            registeredName = data.getStringExtra(RegisterActivity.EXTRA_NAME);
            registeredSurname = data.getStringExtra(RegisterActivity.EXTRA_LASTNAME);

            if (edtUser.getText() == null || edtUser.getText().toString().trim().isEmpty()) {
                edtUser.setText(registeredName);
            }
            if (edtPass.getText() == null || edtPass.getText().toString().trim().isEmpty()) {
                edtPass.setText(registeredSurname);
            }
        }
    }
}

