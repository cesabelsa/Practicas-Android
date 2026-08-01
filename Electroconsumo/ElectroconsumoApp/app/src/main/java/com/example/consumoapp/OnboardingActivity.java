package com.example.consumoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

/** Pantalla inicial sin Toolbar para mantener una entrada visual limpia. */
public class OnboardingActivity extends AppCompatActivity {

    private MaterialButton btnComenzar;
    private TextView txtIniciaSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Si ya existe una sesión válida, abrimos directamente la pantalla principal.
        if (SessionManager.estaLogueado(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        btnComenzar = findViewById(R.id.btnComenzar);
        txtIniciaSesion = findViewById(R.id.txtIniciaSesion);

        // Ambos controles llevan al inicio de sesión.
        btnComenzar.setOnClickListener(v -> abrirLogin());
        txtIniciaSesion.setOnClickListener(v -> abrirLogin());
    }

    private void abrirLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
