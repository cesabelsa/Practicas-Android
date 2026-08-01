package com.example.consumoapp.factura.ui;

import androidx.appcompat.app.AppCompatActivity;

import com.example.consumoapp.NavigationUtils;
import com.example.consumoapp.databinding.ActivitySimuladorFacturaBinding;

/** Configura la toolbar del simulador sin acoplarla a la Activity concreta. */
public final class SimuladorToolbarController {

    private final AppCompatActivity activity;
    private final ActivitySimuladorFacturaBinding binding;

    public SimuladorToolbarController(
            AppCompatActivity activity,
            ActivitySimuladorFacturaBinding binding
    ) {
        this.activity = activity;
        this.binding = binding;
    }

    public void configurar() {
        activity.setSupportActionBar(binding.toolbarSimulador);
        NavigationUtils.configurarToolbarConAtrasBlanco(
                activity,
                binding.toolbarSimulador
        );
    }
}
