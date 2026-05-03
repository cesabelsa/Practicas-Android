package es.layout.login;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import es.layout.login.databinding.ActivityMainBinding;
import es.layout.login.model.Usuario;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final ArrayList<Usuario> usuarios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Modelo inicial enlazado al layout (@={user.login}, @={user.password})
        binding.setUser(new Usuario("demo", "demo"));

        // "Repositorio" en memoria para probar el login
        usuarios.add(new Usuario("juan",  "1111"));
        usuarios.add(new Usuario("pedro", "abcd"));
        usuarios.add(new Usuario("maria", "qwerty"));
        usuarios.add(new Usuario("ana",   "secreto"));
        usuarios.add(new Usuario("demo",  "demo"));

        // Acción del botón Login
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Usuario actual = binding.getUser(); // gracias a DataBinding bidireccional

                String login = actual != null && actual.getLogin() != null
                        ? actual.getLogin().trim() : "";
                String pass = actual != null && actual.getPassword() != null
                        ? actual.getPassword() : "";

                // Limpiar errores previos
                clearError(binding.tilUser);
                clearError(binding.tilPass);

                // Validaciones básicas
                boolean hayError = false;
                if (login.isEmpty()) {
                    setError(binding.tilUser, "El usuario es obligatorio");
                    hayError = true;
                }
                if (pass.isEmpty()) {
                    setError(binding.tilPass, "La contraseña es obligatoria");
                    hayError = true;
                }
                if (hayError) return;

                if (autenticar(login, pass)) {
                    Snackbar.make(view, "Bienvenido, " + login, Snackbar.LENGTH_SHORT).show();
                    // Opcional: limpiar la contraseña tras éxito
                    binding.getUser().setPassword("");
                    binding.edtPass.setText(""); // asegura limpiar campo visible
                } else {
                    setError(binding.tilPass, "Credenciales incorrectas");
                    Snackbar.make(view, "Usuario o contraseña no válidos", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean autenticar(String login, String pass) {
        for (Usuario u : usuarios) {
            if (u.getLogin() != null && u.getLogin().equals(login)
                    && u.getPassword() != null && u.getPassword().equals(pass)) {
                return true;
            }
        }
        return false;
    }

    private void setError(TextInputLayout til, String msg) {
        if (til != null) til.setError(msg);
    }

    private void clearError(TextInputLayout til) {
        if (til != null) til.setError(null);
    }
}

