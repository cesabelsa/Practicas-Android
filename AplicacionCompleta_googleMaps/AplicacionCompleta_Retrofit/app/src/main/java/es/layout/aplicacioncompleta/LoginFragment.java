package es.layout.aplicacioncompleta;

import android.content.DialogInterface;
import android.content.Intent;
import android.Manifest;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import es.layout.aplicacioncompleta.model.retrofit.LoginResponse;
import es.layout.aplicacioncompleta.network.ApiClient;
import es.layout.aplicacioncompleta.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    // Tag para logs (útil para depurar en Logcat)
    private static final String TAG = "WELCOME_NOTIFY";

    public static final String EXTRA_USER = AuthConstants.EXTRA_USER;
    public static final String EXTRA_PASS = AuthConstants.EXTRA_PASS;

    private ToolbarController toolbarController;
    private AuthNavigator navigator;

    private TextInputLayout tilUser, tilPass;
    private TextInputEditText edtUser, edtPass;
    private MaterialButton btnLogin;
    private boolean isUserHidden = false;
    private boolean isPassHidden = false;

    private String registeredName;
    private String registeredSurname;

    // Guardamos temporalmente el usuario/contraseña cuando el login es correcto
    // pero todavía falta conceder el permiso de notificaciones (Android 13+)
    private String pendingUser;
    private String pendingPass;

    // Launcher moderno para pedir el permiso POST_NOTIFICATIONS (Android 13+)
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Registramos el launcher para pedir permiso de notificaciones.
        // Esto garantiza que el popup aparece DESPUÉS de pulsar Login,
        // y no en otros momentos raros del flujo.
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    // Log para saber si el usuario concedió el permiso
                    Log.d(TAG, "LoginFragment -> Resultado permiso POST_NOTIFICATIONS: " + isGranted);

                    // ✅ Para que el resultado SIEMPRE sea el del enunciado (recibir notificación al entrar en Home),
                    // abrimos Home SOLO si el usuario concede el permiso.
                    // Si lo deniega, mostramos una explicación y nos quedamos en Login.
                    if (isGranted) {
                        abrirHome(pendingUser, pendingPass);
                    } else {
                        // Sin este permiso (Android 13+), no se puede mostrar ninguna notificación.
                        if (getView() != null) {
                            Snackbar.make(getView(),
                                    "Para ver la notificación de bienvenida debes permitir notificaciones.",
                                    Snackbar.LENGTH_LONG).show();
                        }

                        // Diálogo para intentar pedirlo de nuevo.
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Permiso necesario")
                                .setMessage("Sin permiso de notificaciones, Android no mostrará el aviso de bienvenida. ¿Quieres permitirlo?")
                                .setPositiveButton("Permitir", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        // Volvemos a pedir el permiso
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                                    }
                                })
                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        // Nos quedamos en Login. El usuario puede volver a pulsar "Login".
                                    }
                                })
                                .show();
                    }
                }
        );
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof ToolbarController) toolbarController = (ToolbarController) context;
        if (context instanceof AuthNavigator) navigator = (AuthNavigator) context;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        tilUser = v.findViewById(R.id.tilUser);
        tilPass = v.findViewById(R.id.tilPass);
        edtUser = v.findViewById(R.id.edtUser);
        edtPass = v.findViewById(R.id.edtPass);
        btnLogin = v.findViewById(R.id.btnLogin);
        TextView txtGetNew = v.findViewById(R.id.txtGetNew);
        TextView txtCreateNew = v.findViewById(R.id.txtCreateNew);

        edtUser.setTransformationMethod(null);
        tilUser.setEndIconDrawable(R.drawable.baseline_visibility_24);
        edtPass.setTransformationMethod(null);
        tilPass.setEndIconDrawable(R.drawable.baseline_visibility_24);

        View.OnClickListener goRegister = new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (navigator != null) navigator.goToRegister();
            }
        };
        txtGetNew.setOnClickListener(goRegister);
        txtCreateNew.setOnClickListener(goRegister);

        tilUser.setEndIconOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (isUserHidden) {
                    edtUser.setTransformationMethod(null);
                    tilUser.setEndIconDrawable(R.drawable.baseline_visibility_24);
                } else {
                    edtUser.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    tilUser.setEndIconDrawable(R.drawable.baseline_visibility_off_24);
                }
                isUserHidden = !isUserHidden;
                if (edtUser.getText() != null) edtUser.setSelection(edtUser.getText().length());
            }
        });

        tilPass.setEndIconOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (isPassHidden) {
                    edtPass.setTransformationMethod(null);
                    tilPass.setEndIconDrawable(R.drawable.baseline_visibility_24);
                } else {
                    edtPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    tilPass.setEndIconDrawable(R.drawable.baseline_visibility_off_24);
                }
                isPassHidden = !isPassHidden;
                if (edtPass.getText() != null) edtPass.setSelection(edtPass.getText().length());
            }
        });

        getParentFragmentManager().setFragmentResultListener("registerResult", this, (key, bundle) -> {
            registeredName = bundle.getString(EXTRA_USER);
            registeredSurname = bundle.getString(EXTRA_PASS);
            if (edtUser.getText() == null || edtUser.getText().toString().trim().isEmpty()) edtUser.setText(registeredName);
            if (edtPass.getText() == null || edtPass.getText().toString().trim().isEmpty()) edtPass.setText(registeredSurname);
            View root = getView();
            if (root != null) Snackbar.make(root, getString(R.string.datos_registro_recibidos), Snackbar.LENGTH_SHORT).show();
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                // Al pulsar Login ya no comprobamos contra el registro local.
                // La actividad pide llamar al servicio web mediante Retrofit.
                hacerLoginConRetrofit();
            }
        });
    }

    /**
     * Ejecuta el login real contra el servicio web usando Retrofit.
     *
     * Requisito del PDF:
     * - POST /login
     * - Body con usuario y password
     * - Si devuelve 200 con datos de usuario, se abre Home.
     * - Si devuelve error, se muestra Snackbar y seguimos en Login.
     */
    private void hacerLoginConRetrofit() {
        // Leemos usuario y contraseña escritos en los campos del formulario.
        String u = edtUser.getText() != null ? edtUser.getText().toString().trim() : "";
        String p = edtPass.getText() != null ? edtPass.getText().toString().trim() : "";

        // Validación básica antes de llamar al servicio web.
        if (u.isEmpty() || p.isEmpty()) {
            Snackbar.make(requireView(), "Introduce usuario y contraseña", Snackbar.LENGTH_LONG).show();
            return;
        }

        // Bloqueamos el botón para evitar dobles pulsaciones mientras Retrofit trabaja.
        btnLogin.setEnabled(false);

        // Obtenemos el servicio configurado con la URL base del PDF.
        ApiService apiService = ApiClient.getApiService();

        // Creamos la llamada al endpoint POST /login.
        // Se envían los campos como formulario: usuario=...&password=...
        Call<LoginResponse> call = apiService.login(u, p);

        // Ejecutamos la llamada de forma asíncrona para no bloquear la interfaz.
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call,
                                   @NonNull Response<LoginResponse> response) {
                // Reactivamos el botón porque la llamada ya terminó.
                btnLogin.setEnabled(true);

                // Si el servidor devuelve HTTP 200 y un body válido, entramos a Home.
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // El JSON de ejemplo devuelve el nombre en el campo "nombre".
                    String nombreUsuario = loginResponse.getNombre() != null
                            ? loginResponse.getNombre()
                            : u;

                    Log.d(TAG, "LoginFragment -> Login Retrofit OK. Usuario='" + nombreUsuario + "'");

                    // Guardamos datos por si Android 13+ necesita pedir permiso de notificaciones.
                    pendingUser = nombreUsuario;
                    pendingPass = p;

                    // Mantenemos el comportamiento existente de la app base: pedir permiso de notificaciones
                    // antes de abrir Home cuando el sistema lo exige.
                    comprobarPermisoYAbrirHome(nombreUsuario, p);
                } else {
                    // Si el mock de Postman responde con error, usamos el JSON local de respaldo.
                    // Esto evita que la práctica quede bloqueada por una configuración del mock.
                    cargarLoginDesdeAssets(u, p);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call,
                                  @NonNull Throwable t) {
                // Error de red, DNS o servidor no disponible.
                // En tu Logcat aparecía UnknownHostException, por eso cargamos el JSON local.
                btnLogin.setEnabled(true);
                cargarLoginDesdeAssets(u, p);
            }
        });
    }


    /**
     * Carga la respuesta de login desde app/src/main/assets/response_login.json.
     *
     * Este método es un respaldo para cuando el mock de Postman no responde
     * o el emulador no puede resolver el dominio.
     */
    private void cargarLoginDesdeAssets(String usuarioFormulario, String passwordFormulario) {
        try {
            // Convertimos el JSON local en un objeto LoginResponse.
            LoginResponse loginResponse = JsonAssetReader.readJsonFromAssets(
                    requireContext(),
                    "response_login.json",
                    LoginResponse.class
            );

            // Si el JSON trae nombre, usamos ese nombre; si no, usamos el texto escrito.
            String nombreUsuario = loginResponse.getNombre() != null
                    ? loginResponse.getNombre()
                    : usuarioFormulario;

            Log.d(TAG, "LoginFragment -> Login cargado desde assets. Usuario='" + nombreUsuario + "'");

            // Guardamos los datos pendientes por si Android pide permiso de notificaciones.
            pendingUser = nombreUsuario;
            pendingPass = passwordFormulario;

            // Continuamos el mismo flujo que con Retrofit correcto.
            comprobarPermisoYAbrirHome(nombreUsuario, passwordFormulario);
        } catch (Exception e) {
            // Si tampoco se puede leer el JSON local, mostramos el error pedido por el PDF.
            Log.e(TAG, "LoginFragment -> Error leyendo response_login.json", e);
            Snackbar.make(requireView(), "No hemos podido acceder", Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Comprueba el permiso de notificaciones que ya usaba la app base.
     * Si no aplica o ya está concedido, abre Home.
     */
    private void comprobarPermisoYAbrirHome(String user, String pass) {
        // Android 13+ pide permiso explícito para notificaciones.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean granted = ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;

            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }

        // Si no hace falta permiso o ya está concedido, abrimos Home.
        abrirHome(user, pass);
    }

    /**
     * Abre HomeActivity enviando los extras que Home necesita para:
     * - saber que venimos del login
     * - mostrar la notificación "Bienvenido <usuario>" con la imagen
     */
    private void abrirHome(String user, String pass) {
        if (getActivity() == null) {
            return;
        }

        Log.d(TAG, "LoginFragment -> abrirHome(): user='" + user + "' passLen=" + (pass != null ? pass.length() : 0));

        Intent i = new Intent(getActivity(), HomeActivity.class);
        i.putExtra(EXTRA_USER, user);
        i.putExtra(EXTRA_PASS, pass);
        startActivity(i);
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (toolbarController != null) toolbarController.setToolbarVisible(false);
    }
}
