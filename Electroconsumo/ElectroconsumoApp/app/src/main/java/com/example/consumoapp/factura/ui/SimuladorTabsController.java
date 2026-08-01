package com.example.consumoapp.factura.ui;

import android.view.View;

import com.example.consumoapp.R;
import com.example.consumoapp.databinding.ActivitySimuladorFacturaBinding;
import com.google.android.material.tabs.TabLayout;

/**
 * Controla exclusivamente la navegación visual entre las tres secciones
 * del simulador. No accede a Room, no realiza cálculos y no conserva datos.
 */
public final class SimuladorTabsController {

    public static final int TAB_HOGAR = 0;
    public static final int TAB_USO = 1;
    public static final int TAB_FACTURA = 2;

    private final ActivitySimuladorFacturaBinding binding;
    private final Runnable alMostrarFactura;

    public SimuladorTabsController(
            ActivitySimuladorFacturaBinding binding,
            Runnable alMostrarFactura
    ) {
        this.binding = binding;
        this.alMostrarFactura = alMostrarFactura;
    }

    /** Configura las pestañas y muestra inicialmente la sección Hogar. */
    public void configurar() {
        binding.tabLayoutFactura.removeAllTabs();
        binding.tabLayoutFactura.addTab(binding.tabLayoutFactura.newTab().setText(R.string.simulador_tab_hogar));
        binding.tabLayoutFactura.addTab(binding.tabLayoutFactura.newTab().setText(R.string.simulador_tab_uso));
        binding.tabLayoutFactura.addTab(binding.tabLayoutFactura.newTab().setText(R.string.simulador_tab_factura));

        binding.tabLayoutFactura.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mostrar(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // No hay trabajo adicional al abandonar una pestaña.
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mostrar(tab.getPosition());
            }
        });

        mostrar(TAB_HOGAR);
    }

    /** Selecciona una pestaña validando siempre sus límites. */
    public void seleccionar(int posicion) {
        int posicionSegura = Math.max(TAB_HOGAR, Math.min(TAB_FACTURA, posicion));
        TabLayout.Tab tab = binding.tabLayoutFactura.getTabAt(posicionSegura);
        if (tab != null) {
            tab.select();
        } else {
            mostrar(posicionSegura);
        }
    }

    /** Muestra solamente el panel correspondiente a la pestaña solicitada. */
    public void mostrar(int posicion) {
        binding.tabHogar.setVisibility(posicion == TAB_HOGAR ? View.VISIBLE : View.GONE);
        binding.tabUso.setVisibility(posicion == TAB_USO ? View.VISIBLE : View.GONE);
        binding.tabFactura.setVisibility(posicion == TAB_FACTURA ? View.VISIBLE : View.GONE);

        if (posicion == TAB_FACTURA && alMostrarFactura != null) {
            alMostrarFactura.run();
        }
    }
}
