package es.layout.aplicacioncompleta;

import android.Manifest;
import android.os.Bundle;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;

/**
 * Activity que actúa como host del grafo de navegación principal (nav_main).
 * El contenido real de la Home se gestiona desde HomeTabsFragment.
 *
 * ACTIVIDAD 11 - PERMISOS:
 * - Al entrar en Home, se solicitan permisos de localización (COARSE y FINE).
 * - Si el usuario los concede, se muestra la Home.
 * - Si el usuario los deniega, se informa y se cierra la app.
 */
public class HomeActivity extends AppCompatActivity {

    // Código (número) para identificar nuestra petición de permisos
    private static final int RC_LOCATION_PERMISSIONS = 1001;

    // Lista de permisos que vamos a pedir (en runtime)
    private static final String[] LOCATION_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Antes de mostrar la Home, comprobamos si ya tenemos permisos.
        //    (En Android < 6.0 no existe runtime permissions, se consideran concedidos.)
        if (tienePermisosDeLocalizacion()) {
            // Si ya están concedidos -> mostramos la home
            mostrarHome();
        } else {
            // Si NO están concedidos -> los solicitamos
            solicitarPermisosDeLocalizacion();
        }
    }

    /**
     * Comprueba si la app tiene concedidos los permisos de localización.
     *
     * @return true si están concedidos, false si falta alguno.
     */
    private boolean tienePermisosDeLocalizacion() {
        // En Android 5.1.1 o inferior (API <= 22), los permisos se aceptaban al instalar
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        // En Android 6.0+ comprobamos uno a uno
        boolean coarseGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean fineGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // Consideramos válido si están concedidos ambos (así cumplimos el criterio de la actividad)
        return coarseGranted && fineGranted;
    }

    /**
     * Lanza el cuadro de diálogo del sistema para pedir permisos.
     */
    private void solicitarPermisosDeLocalizacion() {
        // Pedimos los dos permisos en una sola solicitud
        ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, RC_LOCATION_PERMISSIONS);
    }

    /**
     * Muestra la interfaz de Home (nuestro layout con el NavHost).
     */
    private void mostrarHome() {
        setContentView(R.layout.activity_home);
    }

    /**
     * Recibimos aquí la respuesta del usuario a la petición de permisos.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_LOCATION_PERMISSIONS) {

            // grantResults tiene el resultado en el mismo orden que el array permissions
            boolean todosConcedidos = true;

            // Si por algún motivo viene vacío, lo tratamos como denegado
            if (grantResults == null || grantResults.length == 0) {
                todosConcedidos = false;
            } else {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        todosConcedidos = false;
                        break;
                    }
                }
            }

            if (todosConcedidos) {
                // El usuario aceptó -> mostramos la home
                mostrarHome();
            } else {
                // El usuario denegó -> informamos y cerramos la app
                Toast.makeText(this,
                        "No puedes continuar sin conceder los permisos de localización.",
                        Toast.LENGTH_LONG).show();

                // Cerramos todas las activities de la app (cierre "completo")
                finishAffinity();
            }
        }
    }
}
