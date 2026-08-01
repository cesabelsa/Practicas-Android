package com.example.consumoapp.navigation;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.MenuItemCompat;

import com.example.consumoapp.R;
import com.example.consumoapp.SessionManager;

/** Construye y presenta el menú de navegación desplegable. */
public final class DrawerManager {

    private DrawerManager() {
    }

    public static void mostrarMenuNavegacion(
            AppCompatActivity activity,
            View anchor
    ) {
        PopupMenu popupMenu = new PopupMenu(activity, anchor);
        popupMenu.getMenuInflater().inflate(
                R.menu.menu_app_navigation,
                popupMenu.getMenu()
        );
        popupMenu.setForceShowIcon(true);

        Menu menu = popupMenu.getMenu();
        HeaderManager.configurarOpcionesSesion(activity, menu);
        HeaderManager.configurarTituloCuenta(activity, menu);
        configurarOpcionActiva(activity, menu);

        popupMenu.setOnMenuItemClickListener(
                item -> MenuNavigator.handleNavigation(activity, item.getItemId())
        );
        popupMenu.show();
    }

    private static void configurarOpcionActiva(
            AppCompatActivity activity,
            Menu menu
    ) {
        int itemId = MenuNavigator.resolverOpcionActual(activity);
        if (itemId == Menu.NONE) {
            return;
        }

        MenuItem actual = menu.findItem(itemId);
        if (actual == null || !actual.isVisible()) {
            return;
        }

        actual.setChecked(true);
        MenuItemCompat.setContentDescription(
                actual,
                activity.getString(
                        R.string.descripcion_opcion_actual,
                        actual.getTitle()
                )
        );
    }
}
