package com.example.consumoapp.navigation;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.consumoapp.R;
import com.google.android.material.appbar.MaterialToolbar;

/** Configura las variantes comunes de Toolbar de la aplicación. */
public final class ToolbarManager {

    private ToolbarManager() {
    }

    public static void configurarConMenu(
            AppCompatActivity activity,
            MaterialToolbar toolbar
    ) {
        toolbar.setNavigationIcon(R.drawable.ic_menu_24);
        toolbar.setNavigationContentDescription(R.string.descripcion_menu_navegacion);
        toolbar.setNavigationOnClickListener(
                view -> DrawerManager.mostrarMenuNavegacion(activity, toolbar)
        );
    }

    public static void configurarConAtras(
            AppCompatActivity activity,
            MaterialToolbar toolbar
    ) {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationIconTint(
                ContextCompat.getColor(activity, R.color.texto_blanco)
        );
        toolbar.setNavigationContentDescription(R.string.nav_volver_atras);
        toolbar.setNavigationOnClickListener(
                view -> activity.getOnBackPressedDispatcher().onBackPressed()
        );

        HeaderManager.configurarOpcionesSesion(activity, toolbar.getMenu());
        toolbar.setOnMenuItemClickListener(
                item -> MenuNavigator.handleNavigation(activity, item.getItemId())
        );
    }
}
