package com.example.consumoapp;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class TerminosCondicionesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminos_condiciones);

        MaterialToolbar toolbar =
                findViewById(R.id.toolbarTerminos);
        setSupportActionBar(toolbar);
        NavigationUtils.configurarToolbarConAtrasBlanco(this, toolbar);
        TextView txtTerminos =
                findViewById(R.id.txtTerminos);

        txtTerminos.setText(
                getString(R.string.terminos_condiciones)
        );
    }
}