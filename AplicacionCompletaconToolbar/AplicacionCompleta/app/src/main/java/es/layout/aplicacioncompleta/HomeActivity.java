package es.layout.aplicacioncompleta;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Listener adicional por si la Toolbar no delega correctamente al ActionBar
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_eurodisney) {
            openUrlInApp("https://www.disneylandparis.com/es-es/");
            return true;
        }
        else if (id == R.id.action_alquilar_coche) {
            // 👉 En lugar de abrir un enlace, abrimos el fragment lila
            openAlquilaCocheFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Abre el fragment "AlquilaCocheFragment" dentro del contenedor overlay
     */
    private void openAlquilaCocheFragment() {
        // Mostrar el contenedor overlay
        View overlay = findViewById(R.id.overlay_fragment_container);
        if (overlay != null && overlay.getVisibility() != View.VISIBLE) {
            overlay.setVisibility(View.VISIBLE);
        }

        // Crear e insertar el fragment
        Fragment fragment = AlquilaCocheFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
        transaction.replace(R.id.overlay_fragment_container, fragment);
        transaction.addToBackStack("AlquilaCoche");
        transaction.commit();
    }

    /**
     * Cierra el fragment overlay y vuelve al contenido principal
     */
    public void closeOverlayFragment() {
        getSupportFragmentManager().popBackStack();
        View overlay = findViewById(R.id.overlay_fragment_container);
        if (overlay != null) {
            overlay.setVisibility(View.GONE);
        }
    }

    /**
     * Método auxiliar para abrir una URL en el navegador externo
     */
    private void openUrlSafe(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            Intent chooser = Intent.createChooser(intent, "Abrir con...");
            startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No hay app para abrir enlaces", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Método para abrir una URL dentro de la app (WebActivity)
     */
    private void openUrlInApp(String url) {
        try {
            Intent intent = new Intent(this, WebActivity.class);
            intent.putExtra(WebActivity.EXTRA_URL, url);
            startActivity(intent);
        } catch (Exception e) {
            // Si algo falla, degradar a navegador externo
            openUrlSafe(url);
        }
    }
}
