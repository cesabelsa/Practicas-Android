package com.example.consumoapp.navigation;

import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.consumoapp.R;
import com.example.consumoapp.SessionManager;

/** Gestiona la información de sesión visible en menús y cabeceras. */
public final class HeaderManager {

    private HeaderManager() {
    }

    public static void prepararMenuUsuario(AppCompatActivity activity, Menu menu) {
        MenuItem itemUsuario = menu.findItem(R.id.action_usuario_conectado);
        if (itemUsuario == null) {
            return;
        }

        if (!SessionManager.estaLogueado(activity)) {
            itemUsuario.setVisible(false);
            return;
        }

        String nombre = SessionManager.obtenerNombre(activity);
        String inicial = SessionManager.obtenerInicial(activity);

        View actionView = activity.getLayoutInflater().inflate(
                R.layout.action_view_usuario,
                null
        );

        TextView txtAvatar = actionView.findViewById(R.id.txtToolbarAvatar);
        txtAvatar.setText(inicial);

        String descripcion = activity.getString(
                R.string.descripcion_abrir_cuenta_usuario,
                nombre
        );
        actionView.setContentDescription(descripcion);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            actionView.setTooltipText(
                    activity.getString(R.string.tooltip_cuenta_usuario, nombre)
            );
        }

        actionView.setOnClickListener(
                view -> MenuNavigator.mostrarDialogoUsuario(activity)
        );

        itemUsuario.setTitle(R.string.accion_perfil_cuenta);
        itemUsuario.setActionView(actionView);
        itemUsuario.setVisible(true);
    }

    public static void configurarTituloCuenta(AppCompatActivity activity, Menu menu) {
        MenuItem grupoCuenta = menu.findItem(R.id.group_nav_cuenta);
        if (grupoCuenta == null) {
            return;
        }

        if (!SessionManager.estaLogueado(activity)) {
            grupoCuenta.setTitle(R.string.menu_grupo_cuenta_configuracion);
            return;
        }

        String nombre = SessionManager.obtenerNombre(activity);
        if (nombre == null || nombre.trim().isEmpty()) {
            grupoCuenta.setTitle(R.string.menu_grupo_cuenta_configuracion);
        } else {
            grupoCuenta.setTitle(
                    activity.getString(R.string.menu_grupo_cuenta_usuario, nombre.trim())
            );
        }
    }

    public static void configurarOpcionesSesion(AppCompatActivity activity, Menu menu) {
        boolean sesionActiva = SessionManager.estaLogueado(activity);

        cambiarVisibilidad(menu, R.id.action_nav_login, !sesionActiva);
        cambiarVisibilidad(menu, R.id.action_nav_registro, !sesionActiva);
        cambiarVisibilidad(menu, R.id.action_nav_mi_perfil, sesionActiva);
        cambiarVisibilidad(menu, R.id.action_logout, sesionActiva);
    }

    private static void cambiarVisibilidad(Menu menu, int itemId, boolean visible) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) {
            item.setVisible(visible);
        }
    }
}
