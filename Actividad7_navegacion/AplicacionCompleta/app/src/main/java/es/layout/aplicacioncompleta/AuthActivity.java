package es.layout.aplicacioncompleta;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class AuthActivity extends AppCompatActivity implements ToolbarController, AuthNavigator {

    private Toolbar toolbar; // UNA sola referencia, sin duplicados

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Configura la Toolbar de la Activity
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Fuerza que la Toolbar NO muestre título nunca
        toolbar.setTitle("");
        toolbar.setSubtitle(null);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (savedInstanceState == null) {
            replaceFragment(new LoginFragment(), false);
        }
    }

    @Override
    public void setToolbarVisible(boolean visible) {
        if (getSupportActionBar() != null) {
            if (visible) getSupportActionBar().show();
            else getSupportActionBar().hide();
        }
    }

    @Override
    public void goToRegister() {
        replaceFragment(new RegisterFragment(), true);
    }

    @Override
    public void goToLogin() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void goToHomeAndFinish() {
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
        finish();
    }

    private void replaceFragment(Fragment fragment, boolean addToBackstack) {
        if (addToBackstack) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}

interface ToolbarController {
    void setToolbarVisible(boolean visible);
}

interface AuthNavigator {
    void goToRegister();
    void goToLogin();
    void goToHomeAndFinish();
}
