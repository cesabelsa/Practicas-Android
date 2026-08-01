package com.example.consumoapp.navigation;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.consumoapp.AjustesGeneralesActivity;
import com.example.consumoapp.LoginActivity;
import com.example.consumoapp.MainActivity;
import com.example.consumoapp.MiPerfilActivity;
import com.example.consumoapp.OnboardingActivity;
import com.example.consumoapp.R;
import com.example.consumoapp.RegistroActivity;
import com.example.consumoapp.SessionManager;
import com.example.consumoapp.factura.SimuladorFacturaActivity;

/** Resuelve las acciones del menú y la navegación entre pantallas. */
public final class MenuNavigator {

    public static final String EXTRA_MAIN_TAB = "extra_main_tab";

    private MenuNavigator() {
    }

    public static int resolverOpcionActual(AppCompatActivity activity) {
        if (activity instanceof MainActivity) {
            int tab = activity.getIntent().getIntExtra(EXTRA_MAIN_TAB, 0);
            return tab == 1 ? R.id.action_nav_esios : R.id.action_nav_main;
        }
        if (activity instanceof SimuladorFacturaActivity) {
            int tab = activity.getIntent().getIntExtra(
                    SimuladorFacturaActivity.EXTRA_TAB_INICIAL,
                    0
            );
            return tab == 1 ? R.id.action_nav_tarifas : R.id.action_nav_factura;
        }
        if (activity instanceof MiPerfilActivity) {
            return R.id.action_nav_mi_perfil;
        }
        if (activity instanceof AjustesGeneralesActivity) {
            return R.id.action_nav_ajustes_app;
        }
        if (activity instanceof OnboardingActivity) {
            return R.id.action_nav_onboarding;
        }
        if (activity instanceof LoginActivity) {
            return R.id.action_nav_login;
        }
        if (activity instanceof RegistroActivity) {
            return R.id.action_nav_registro;
        }
        return android.view.Menu.NONE;
    }

    public static boolean handleNavigation(AppCompatActivity activity, int itemId) {
        if (itemId == R.id.action_usuario_conectado) {
            mostrarDialogoUsuario(activity);
            return true;
        }
        if (itemId == R.id.action_nav_main) {
            abrirMainEnTab(activity, 0);
            return true;
        }
        if (itemId == R.id.action_nav_esios) {
            abrirMainEnTab(activity, 1);
            return true;
        }
        if (itemId == R.id.action_nav_mi_perfil) {
            abrirPantalla(activity, MiPerfilActivity.class, false);
            return true;
        }
        if (itemId == R.id.action_nav_ajustes_app) {
            abrirPantalla(activity, AjustesGeneralesActivity.class, false);
            return true;
        }
        if (itemId == R.id.action_nav_factura) {
            abrirSimuladorEnTab(activity, 0);
            return true;
        }
        if (itemId == R.id.action_nav_tarifas) {
            abrirSimuladorEnTab(activity, 1);
            return true;
        }
        if (itemId == R.id.action_nav_onboarding) {
            abrirPantalla(activity, OnboardingActivity.class, false);
            return true;
        }
        if (itemId == R.id.action_nav_login) {
            abrirPantalla(activity, LoginActivity.class, false);
            return true;
        }
        if (itemId == R.id.action_nav_registro) {
            abrirPantalla(activity, RegistroActivity.class, false);
            return true;
        }
        if (itemId == R.id.action_logout) {
            cerrarSesion(activity);
            return true;
        }
        return false;
    }

    public static void mostrarDialogoUsuario(AppCompatActivity activity) {
        if (!SessionManager.estaLogueado(activity)) {
            abrirPantalla(activity, LoginActivity.class, false);
            return;
        }

        View contenido = activity.getLayoutInflater().inflate(
                R.layout.dialog_usuario_conectado,
                null
        );

        TextView txtAvatar = contenido.findViewById(R.id.txtDialogAvatar);
        TextView txtNombre = contenido.findViewById(R.id.txtDialogNombre);
        TextView txtEmail = contenido.findViewById(R.id.txtDialogEmail);

        txtAvatar.setText(SessionManager.obtenerInicial(activity));
        txtNombre.setText(SessionManager.obtenerNombre(activity));

        String email = SessionManager.obtenerEmail(activity);
        if (email == null || email.trim().isEmpty()) {
            txtEmail.setVisibility(View.GONE);
        } else {
            txtEmail.setText(email);
        }

        new AlertDialog.Builder(activity)
                .setTitle("Mi cuenta")
                .setView(contenido)
                .setPositiveButton(
                        R.string.titulo_mi_perfil,
                        (dialog, which) -> abrirPantalla(
                                activity,
                                MiPerfilActivity.class,
                                false
                        )
                )
                .setNegativeButton(
                        "Cerrar sesión",
                        (dialog, which) -> cerrarSesion(activity)
                )
                .setNeutralButton("Cerrar", null)
                .show();
    }

    private static void abrirMainEnTab(AppCompatActivity activity, int tab) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra(EXTRA_MAIN_TAB, tab);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }

    private static void abrirSimuladorEnTab(AppCompatActivity activity, int tab) {
        Intent intent = new Intent(activity, SimuladorFacturaActivity.class);
        intent.putExtra(SimuladorFacturaActivity.EXTRA_TAB_INICIAL, tab);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }

    private static void cerrarSesion(AppCompatActivity activity) {
        SessionManager.cerrarSesion(activity);
        abrirPantalla(activity, LoginActivity.class, true);
    }

    private static void abrirPantalla(
            AppCompatActivity activity,
            Class<?> pantallaDestino,
            boolean limpiarPila
    ) {
        if (!limpiarPila && activity.getClass().equals(pantallaDestino)) {
            return;
        }

        Intent intent = new Intent(activity, pantallaDestino);
        if (limpiarPila) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        activity.startActivity(intent);
        if (limpiarPila) {
            activity.finish();
        }
    }
}
