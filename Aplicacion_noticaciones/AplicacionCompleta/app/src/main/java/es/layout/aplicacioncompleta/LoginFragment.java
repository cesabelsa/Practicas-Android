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
                String u = edtUser.getText() != null ? edtUser.getText().toString().trim() : "";
                String p = edtPass.getText() != null ? edtPass.getText().toString().trim() : "";

                if (registeredName == null || registeredSurname == null) {
                    edtUser.setError(getString(R.string.primero_registrate));
                    return;
                }

                if (u.equals(registeredName) && p.equals(registeredSurname)) {
                    Log.d(TAG, "LoginFragment -> Login OK. User='" + u + "'");

                    // Guardamos el usuario/contraseña por si necesitamos pedir permiso
                    pendingUser = u;
                    pendingPass = p;

                    // ✅ Android 13+ -> pedir permiso de notificaciones SOLO cuando el login es correcto
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        boolean granted = ContextCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED;

                        Log.d(TAG, "LoginFragment -> Android " + Build.VERSION.SDK_INT + " (API). Permiso POST_NOTIFICATIONS concedido=" + granted);

                        if (!granted) {
                            Log.d(TAG, "LoginFragment -> Solicito permiso POST_NOTIFICATIONS (popup)");
                            // Pedimos el permiso (aparece el popup justo aquí)
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                            return;
                        }
                    }

                    // Si no hace falta permiso (Android < 13) o ya está concedido, abrimos Home
                    Log.d(TAG, "LoginFragment -> Abro HomeActivity (ya tengo permiso o no aplica)");
                    abrirHome(u, p);
                } else {
                    new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.login_incorrecto)
                        .setMessage(R.string.credenciales_no_coinciden)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
                        })
                        .show();
                }
            }
        });
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
