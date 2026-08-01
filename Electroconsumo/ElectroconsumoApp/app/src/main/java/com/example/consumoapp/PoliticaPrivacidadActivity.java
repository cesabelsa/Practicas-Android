package com.example.consumoapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class PoliticaPrivacidadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_politica_privacidad);

        // Obtenemos la Toolbar del XML
        MaterialToolbar toolbar =
                findViewById(R.id.toolbarPrivacidad);
        setSupportActionBar(toolbar);
        NavigationUtils.configurarToolbarConAtrasBlanco(this, toolbar);

        // Obtenemos el TextView donde mostraremos el texto
        TextView txtPrivacidad =
                findViewById(R.id.txtPrivacidad);

        // Cargamos el texto de la política de privacidad desde strings.xml
        txtPrivacidad.setText(
                getString(R.string.politica_privacidad)
        );

        // Al pulsar la flecha de volver se cierra la Activity
    }
}