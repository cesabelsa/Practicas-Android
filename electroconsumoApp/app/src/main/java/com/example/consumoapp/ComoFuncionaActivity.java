package com.example.consumoapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.consumoapp.databinding.ActivityComoFuncionaBinding;

/**
 * Pantalla de ayuda con una explicación sencilla de la aplicación
 * y un glosario de conceptos eléctricos básicos.
 */
public class ComoFuncionaActivity extends AppCompatActivity {

    // View Binding permite acceder a las vistas sin usar findViewById().
    private ActivityComoFuncionaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Infla el archivo activity_como_funciona.xml y lo muestra en pantalla.
        binding = ActivityComoFuncionaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configura la barra superior y su flecha para volver atrás.
        setSupportActionBar(binding.toolbarComoFunciona);
        NavigationUtils.configurarToolbarConAtrasBlanco(
                this,
                binding.toolbarComoFunciona
        );
    }
}
