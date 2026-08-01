package com.example.consumoapp;

import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;

import com.example.consumoapp.navigation.HeaderManager;
import com.example.consumoapp.navigation.MenuNavigator;
import com.example.consumoapp.navigation.ToolbarManager;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Fachada compatible para la navegación histórica de la aplicación.
 *
 * La implementación está dividida en ToolbarManager, HeaderManager,
 * DrawerManager y MenuNavigator. Mantener esta fachada permite migrar las
 * Activities de forma gradual sin romper sus llamadas actuales.
 */
public final class NavigationUtils {

    public static final String EXTRA_MAIN_TAB = MenuNavigator.EXTRA_MAIN_TAB;

    private NavigationUtils() {
    }

    public static void configurarToolbarConMenu(
            AppCompatActivity activity,
            MaterialToolbar toolbar
    ) {
        ToolbarManager.configurarConMenu(activity, toolbar);
    }

    public static void configurarToolbarConAtrasBlanco(
            AppCompatActivity activity,
            MaterialToolbar toolbar
    ) {
        ToolbarManager.configurarConAtras(activity, toolbar);
    }

    public static void prepararMenuUsuario(AppCompatActivity activity, Menu menu) {
        HeaderManager.prepararMenuUsuario(activity, menu);
    }

    public static void configurarOpcionesSesion(AppCompatActivity activity, Menu menu) {
        HeaderManager.configurarOpcionesSesion(activity, menu);
    }

    public static boolean handleNavigation(AppCompatActivity activity, int itemId) {
        return MenuNavigator.handleNavigation(activity, itemId);
    }
}
