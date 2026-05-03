package es.layout.aplicacioncompleta;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

/**
 * Activity que actúa como host del grafo de navegación de Login:
 * incluye onboarding, login y registro en un único NavHostFragment.
 */
public class LoginActivity extends AppCompatActivity implements ToolbarController, AuthNavigator {

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            // No mostramos título de texto, se gestiona desde el Layout
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    // ===== ToolbarController =====

    @Override
    public void setToolbarVisible(boolean visible) {
        if (getSupportActionBar() != null) {
            if (visible) {
                getSupportActionBar().show();
            } else {
                getSupportActionBar().hide();
            }
        }
    }

    // ===== Utilidad privada para obtener el NavController =====

    private NavController getNavController() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_login);
        if (f instanceof NavHostFragment) {
            return ((NavHostFragment) f).getNavController();
        }
        return null;
    }

    // ===== AuthNavigator =====

    @Override
    public void goToRegister() {
        NavController nav = getNavController();
        if (nav != null) {
            nav.navigate(R.id.action_loginFragment_to_registerFragment);
        }
    }

    @Override
    public void goToLogin() {
        NavController nav = getNavController();
        if (nav != null) {
            nav.navigate(R.id.action_registerFragment_to_loginFragment);
        }
    }

    @Override
    public void goToHomeAndFinish() {
        // Actualmente el propio LoginFragment se encarga de lanzar HomeActivity
        // mediante Intent. Si quieres centralizar ese flujo, puedes mover aquí
        // la lógica de navegación hacia HomeActivity.
    }
}
