package es.layout.aplicacioncompleta;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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

    // Código para identificar la petición del permiso de notificaciones (Android 13+)
    private static final int RC_NOTIFICATION_PERMISSION = 2001;

    // ID del canal de notificaciones (Android 8+)
    private static final String NOTIFICATION_CHANNEL_ID = "canal_bienvenida";

    // ID numérico de la notificación (cualquier entero)
    private static final int NOTIFICATION_ID_WELCOME = 1;

    // Lista de permisos que vamos a pedir (en runtime)
    private static final String[] LOCATION_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    // Para evitar mostrar la notificación varias veces (por ejemplo, al rotar pantalla)
    private boolean notificacionMostrada = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si es la primera vez que se crea la Activity (no es una recreación por rotación)
        // permitimos mostrar la notificación. Si no, evitamos duplicados.
        notificacionMostrada = (savedInstanceState != null);

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

        // ===== ACTIVIDAD 12 - NOTIFICACIONES =====
        // 1) Creamos el canal (si el móvil es Android 8+)
        crearCanalNotificacion();

        // 2) Pedimos permiso de notificaciones si hace falta (Android 13+)
        //    Si no hace falta, mostramos la notificación directamente.
        prepararYMostrarNotificacionBienvenida();
    }

    /**
     * Comprueba si tenemos permiso para mostrar notificaciones.
     * Solo es obligatorio a partir de Android 13 (API 33).
     */
    private boolean tienePermisoNotificaciones() {
        // En Android 12L o inferior no existe este permiso
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Pide el permiso de notificaciones en runtime (solo Android 13+).
     */
    private void solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    RC_NOTIFICATION_PERMISSION
            );
        }
    }

    /**
     * Prepara el flujo para mostrar la notificación:
     * - Si no hace falta permiso -> muestra.
     * - Si hace falta y no está concedido -> lo solicita.
     */
    private void prepararYMostrarNotificacionBienvenida() {

        // Si ya se mostró (por ejemplo, rotación), no hacemos nada
        if (notificacionMostrada) {
            return;
        }

        // Si tenemos permiso, mostramos directamente
        if (tienePermisoNotificaciones()) {
            mostrarNotificacionBienvenida();
            notificacionMostrada = true;
            return;
        }

        // Si no tenemos permiso (Android 13+), lo pedimos
        solicitarPermisoNotificaciones();
    }

    /**
     * Crea el canal de notificación (obligatorio desde Android 8).
     * Si el canal no existe, Android no mostrará ninguna notificación.
     */
    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Nombre que verá el usuario en Ajustes -> Notificaciones
            CharSequence nombre = "Bienvenida";

            // Importancia: DEFAULT = suena y aparece, pero no es intrusiva
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;

            // Creamos el canal
            NotificationChannel canal = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    nombre,
                    importancia
            );

            // (Opcional) descripción del canal
            canal.setDescription("Notificaciones de bienvenida al entrar en la app");

            // Registramos el canal en el sistema
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }
    }

    /**
     * Muestra la notificación cumpliendo los criterios de la actividad:
     * - Imagen
     * - Título: "Bienvenido <nombre-usuario>"
     * - Descripción: "Nos alegra verte en este paraíso."
     */
    private void mostrarNotificacionBienvenida() {

        // 1) Recuperamos el nombre de usuario que vino desde el Login
        String nombreUsuario = getIntent().getStringExtra(AuthConstants.EXTRA_USER);
        if (nombreUsuario == null) {
            nombreUsuario = "";
        }

        // 2) Intent para abrir la Home si el usuario toca la notificación
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // 3) Cargamos la imagen grande (la isla / paraíso) desde drawable
        Bitmap imagenGrande = BitmapFactory.decodeResource(getResources(), R.drawable.paraiso);

        // 4) Creamos la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                // Icono pequeño (obligatorio)
                .setSmallIcon(R.mipmap.ic_launcher)

                // Título dinámico
                .setContentTitle("Bienvenido " + nombreUsuario)

                // Texto de la notificación
                .setContentText("Nos alegra verte en este paraíso.")

                // Para que al tocarla abra la Home
                .setContentIntent(pendingIntent)

                // Al tocarla se cierra
                .setAutoCancel(true)

                // Imagen grande estilo "BigPicture"
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(imagenGrande)
                        .bigLargeIcon(null));

        // 5) Mostramos la notificación
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(NOTIFICATION_ID_WELCOME, builder.build());
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

        // ===== ACTIVIDAD 12 - NOTIFICACIONES =====
        // Si el usuario responde a la petición del permiso de notificaciones
        if (requestCode == RC_NOTIFICATION_PERMISSION) {

            boolean concedido = (grantResults != null
                    && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED);

            if (concedido) {
                // Si concede -> mostramos la notificación
                mostrarNotificacionBienvenida();
                notificacionMostrada = true;
            } else {
                // Si no concede -> simplemente no mostramos notificación
                // (la app sigue funcionando)
                Toast.makeText(this,
                        "No se mostrarán notificaciones porque no se concedió el permiso.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
