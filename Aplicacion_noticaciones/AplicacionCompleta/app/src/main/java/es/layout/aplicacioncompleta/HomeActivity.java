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
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

    // Tag para logs (útil para depurar en Logcat)
    private static final String TAG = "WELCOME_NOTIFY";

    // Código (número) para identificar nuestra petición de permisos
    private static final int RC_LOCATION_PERMISSIONS = 1001;

    // ID del canal de notificaciones (Android 8+)
    private static final String NOTIFICATION_CHANNEL_ID = "canal_bienvenida";

    // ID numérico de la notificación (cualquier entero)
    private static final int NOTIFICATION_ID_WELCOME = 1;

    // Lista de permisos que vamos a pedir (en runtime)
    private static final String[] LOCATION_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    // =========================
    // ACTIVIDAD 12 - NOTIFICACIONES
    // =========================
    // Para cumplir el enunciado, SOLO mostramos la notificación cuando:
    // 1) Venimos del login (traemos AuthConstants.EXTRA_USER en el Intent)
    // 2) El usuario ha concedido permisos de notificación (Android 13+)
    // 3) Ya se ha cargado la Home y ya no hay diálogos de permisos encima
    //
    // Guardamos si ya se mostró para evitar duplicados (rotación / recreación tras permisos)
    private boolean notificacionMostrada = false;

    // Nombre del usuario que viene del Login
    private String nombreUsuario = null;

    // Indica si esta HomeActivity fue abierta desde Login (si no, NO mostramos notificación)
    private boolean vieneDeLogin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "HomeActivity.onCreate() API=" + Build.VERSION.SDK_INT);

        // ====== Recuperamos estado (rotación / recreación) ======
        // OJO: antes se hacía "notificacionMostrada = (savedInstanceState != null)",
        // lo cual es INCORRECTO, porque Android puede recrear la Activity durante los
        // diálogos de permisos (notificaciones/localización) y entonces se marcaba
        // como "mostrada" aunque todavía NO se hubiese enviado.
        if (savedInstanceState != null) {
            notificacionMostrada = savedInstanceState.getBoolean("notificacionMostrada", false);
        }

        // ====== Detectamos si venimos del Login ======
        // Si no existe el extra del usuario, NO mostramos notificación.
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(AuthConstants.EXTRA_USER)) {
            vieneDeLogin = true;
            nombreUsuario = intent.getStringExtra(AuthConstants.EXTRA_USER);
        }

        Log.d(TAG, "HomeActivity -> vieneDeLogin=" + vieneDeLogin + " user='" + (nombreUsuario != null ? nombreUsuario : "null") + "'");

        // Mostramos el layout de la Home desde el inicio.
        // Así evitamos situaciones donde se intentan mostrar notificaciones mientras
        // aún hay diálogos del sistema encima.
        setContentView(R.layout.activity_home);

        // ===== ACTIVIDAD 12 - NOTIFICACIONES =====
        // 1) Creamos el canal (Android 8+)
        crearCanalNotificacion();

        // Log del estado global de notificaciones (app)
        Log.d(TAG, "HomeActivity -> areNotificationsEnabled=" + NotificationManagerCompat.from(this).areNotificationsEnabled());

        // ===== ACTIVIDAD 11 - PERMISOS (localización) =====
        // Pedimos permisos de localización si hacen falta.
        // La notificación de bienvenida se lanzará DESPUÉS, cuando la Home esté lista.
        if (!tienePermisosDeLocalizacion()) {
            solicitarPermisosDeLocalizacion();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ✅ ANDROID 15/16 (API 35/36) + EMULADOR:
        // En estas versiones, si lanzamos la notificación mientras el sistema todavía está
        // cerrando diálogos (permisos) o preparando animaciones, es frecuente que parezca
        // que “no sale”.
        //
        // Por eso, el disparo final lo hacemos aquí (cuando la Activity ya está visible)
        // con un pequeño retraso.
        intentarMostrarNotificacionBienvenida();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("notificacionMostrada", notificacionMostrada);
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

        Log.d(TAG, "Permisos localización: COARSE=" + coarseGranted + " FINE=" + fineGranted);

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
        // IMPORTANTE:
        // En este proyecto ya hacemos setContentView(R.layout.activity_home) en onCreate().
        // Si volvemos a llamar a setContentView() después de conceder permisos,
        // podemos provocar estados raros del NavHostFragment (pantalla en blanco,
        // pestañas que no se cargan, etc.) porque se recrea el contenedor.
        //
        // Por eso, aquí NO hacemos nada. Dejamos la Home ya cargada.
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

        boolean granted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

        Log.d(TAG, "Permiso POST_NOTIFICATIONS concedido=" + granted);
        return granted;
    }

    /**
     * Prepara el flujo para mostrar la notificación:
     * - Si no hace falta permiso -> muestra.
     * - Si hace falta y no está concedido -> lo solicita.
     */
    private void prepararYMostrarNotificacionBienvenida() {
        // Método antiguo dejado por compatibilidad.
        // Ahora toda la lógica se centraliza en intentarMostrarNotificacionBienvenida().
        intentarMostrarNotificacionBienvenida();
    }

    /**
     * Intenta mostrar la notificación de bienvenida SOLO si:
     * - Venimos del login (hay usuario en el Intent)
     * - No se ha mostrado ya
     * - Tenemos permisos de notificación (Android 13+)
     * - Tenemos permisos de localización (Actividad 11)
     */
    private void intentarMostrarNotificacionBienvenida() {

        // 1) Si no venimos del Login, NO hacemos nada.
        // Esto evita que se pida permiso de notificaciones antes de pulsar "Login".
        if (!vieneDeLogin) {
            Log.d(TAG, "No vengo del login -> NO muestro notificación");
            return;
        }

        // 2) Evitar duplicados
        if (notificacionMostrada) {
            Log.d(TAG, "Notificación ya mostrada -> NO duplico");
            return;
        }

        // 3) Esperar a tener permisos de localización.
        // Si no están concedidos, el usuario aún está en el diálogo.
        if (!tienePermisosDeLocalizacion()) {
            Log.d(TAG, "Sin permisos de localización aún -> espero");
            return;
        }

        // 4) Android 13+ -> si NO hay permiso de notificaciones, NO podemos mostrar.
        // IMPORTANTÍSIMO: el permiso se pide en LoginFragment tras pulsar Login.
        if (!tienePermisoNotificaciones()) {
            Log.d(TAG, "Sin permiso de notificaciones (Android 13+) -> NO muestro");
            return;
        }

        // 4.1) Si el sistema tiene la app/canal silenciados, Android NO mostrará nada.
        NotificationManagerCompat nmc = NotificationManagerCompat.from(this);
        if (!nmc.areNotificationsEnabled()) {
            Log.d(TAG, "Notificaciones desactivadas a nivel de app -> NO muestro");
            return;
        }

        // 5) MOSTRAR la notificación con un pequeño retraso.
        // En emulador (API 35/36), si la lanzas justo al cerrar un diálogo de permisos,
        // a veces parece que "no sale". 500-800ms suele ser suficiente.
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Mostrando notificación de bienvenida");
                mostrarNotificacionBienvenida();
                notificacionMostrada = true;
            }
        }, 800);
    }

    /**
     * Crea el canal de notificación (obligatorio desde Android 8).
     * Si el canal no existe, Android no mostrará ninguna notificación.
     */
    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Nombre que verá el usuario en Ajustes -> Notificaciones
            CharSequence nombre = "Bienvenida";

            // Importancia:
            // - HIGH hace que en muchos dispositivos aparezca como "heads-up" (banner) si el sistema lo permite.
            // - Además, ayuda a que no quede escondida en canales "silenciosos".
            // IMPORTANTE: una vez creado el canal, su importancia NO se puede cambiar desde código.
            // Para probar cambios del canal, conviene hacer "Wipe Data" o reinstalar la app.
            int importancia = NotificationManager.IMPORTANCE_HIGH;

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

                // Log: comprobar importancia del canal (si el canal ya existía, Android mantiene la anterior)
                NotificationChannel ch = manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID);
                if (ch != null) {
                    Log.d(TAG, "Canal '" + NOTIFICATION_CHANNEL_ID + "' creado/existente. importance=" + ch.getImportance());
                } else {
                    Log.d(TAG, "Canal '" + NOTIFICATION_CHANNEL_ID + "' NO encontrado tras crearlo");
                }
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

        Log.d(TAG, "Bitmap paraiso cargado: " + (imagenGrande != null ? (imagenGrande.getWidth() + "x" + imagenGrande.getHeight()) : "NULL"));

        // 4) Creamos la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                // Icono pequeño (obligatorio).
                // Recomendación: usar un icono simple (vector/monocromo) para que siempre se vea bien.
                .setSmallIcon(R.drawable.baseline_person_24)

                // ✅ Miniatura visible incluso con la notificación plegada
                .setLargeIcon(imagenGrande)

                // Título dinámico (según enunciado del PDF)
                .setContentTitle("Bienvenido " + nombreUsuario)

                // Texto de la notificación (según enunciado del PDF)
                .setContentText("Nos alegra verte en este paraíso.")

                // Para que al tocarla abra la Home
                .setContentIntent(pendingIntent)

                // Al tocarla se cierra
                .setAutoCancel(true)

                // Imagen grande estilo "BigPicture"
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(imagenGrande)

                        .bigLargeIcon((Bitmap) null));

        // Prioridad: HIGH ayuda a que el sistema muestre banner (si no está silenciado)
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        // 5) Mostramos la notificación
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);

        // Logs para saber si el sistema está bloqueando
        Log.d(TAG, "Antes de notify(): areNotificationsEnabled=" + managerCompat.areNotificationsEnabled());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) {
                NotificationChannel ch = nm.getNotificationChannel(NOTIFICATION_CHANNEL_ID);
                if (ch != null) {
                    Log.d(TAG, "Antes de notify(): canal importance=" + ch.getImportance());
                } else {
                    Log.d(TAG, "Antes de notify(): canal=NULL");
                }
            }
        }

        // El permiso POST_NOTIFICATIONS SOLO existe a partir de Android 13 (API 33).
        // En Android 12 o inferior, NO debemos comprobarlo.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Sin permiso -> no mostramos.
                return;
            }
        }

        managerCompat.notify(NOTIFICATION_ID_WELCOME, builder.build());

        Log.d(TAG, "notify() ejecutado con NOTIFICATION_ID_WELCOME=" + NOTIFICATION_ID_WELCOME);
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
                // El usuario aceptó -> la Home ya está cargada (setContentView en onCreate).
                // ✅ No disparamos aquí: onResume() se ejecutará justo después de cerrar el diálogo
                // y es el momento más estable para lanzar la notificación (especialmente en API 36).
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
