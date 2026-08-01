package com.example.consumoapp;

import com.example.consumoapp.NavigationUtils;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.consumoapp.databinding.ActivityMiPerfilBinding;

/**
 * Pantalla de consulta de la cuenta activa.
 *
 * En esta fase solo mostramos datos reales guardados en SessionManager.
 * La edición del nombre, correo o contraseña se deja para una fase posterior,
 * porque requiere validaciones y actualización segura en SQLite.
 */
public class MiPerfilActivity extends AppCompatActivity {

    private ActivityMiPerfilBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Una pantalla de perfil solo tiene sentido con una sesión válida.
        if (!SessionManager.estaLogueado(this)) {
            abrirLoginYLimpiarPila();
            return;
        }

        binding = ActivityMiPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarMiPerfil);
        NavigationUtils.configurarToolbarConAtrasBlanco(this, binding.toolbarMiPerfil);

        mostrarDatosUsuario();
        configurarAcciones();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Si la sesión se cerró desde otra pantalla, impedimos mostrar datos antiguos.
        if (!SessionManager.estaLogueado(this)) {
            abrirLoginYLimpiarPila();
            return;
        }

        if (binding != null) {
            mostrarDatosUsuario();
        }
    }

    /** Carga en pantalla los datos no sensibles guardados en la sesión. */
    private void mostrarDatosUsuario() {
        String nombre = SessionManager.obtenerNombre(this);
        String email = SessionManager.obtenerEmail(this);
        String inicial = SessionManager.obtenerInicial(this);

        binding.txtPerfilAvatar.setText(inicial);
        binding.txtPerfilNombre.setText(nombre);
        binding.txtPerfilNombreDetalle.setText(nombre);

        String emailVisible = email == null || email.trim().isEmpty()
                ? getString(R.string.perfil_email_no_disponible)
                : email;

        binding.txtPerfilEmail.setText(emailVisible);
        binding.txtPerfilEmailDetalle.setText(emailVisible);

        binding.txtPerfilAvatar.setContentDescription(
                getString(R.string.perfil_avatar_descripcion, nombre)
        );
    }

    /** Configura las acciones existentes sin crear todavía edición de perfil. */
    private void configurarAcciones() {
        binding.btnPerfilAjustes.setOnClickListener(v -> {
            Intent intent = new Intent(this, AjustesGeneralesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        binding.btnPerfilCerrarSesion.setOnClickListener(v -> confirmarCierreSesion());
    }

    /** Pide confirmación antes de borrar los datos de la sesión local. */
    private void confirmarCierreSesion() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.perfil_confirmar_cierre_titulo)
                .setMessage(R.string.perfil_confirmar_cierre_mensaje)
                .setPositiveButton(R.string.menu_cerrar_sesion, (dialog, which) -> {
                    SessionManager.cerrarSesion(this);
                    abrirLoginYLimpiarPila();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** Abre LoginActivity y elimina las pantallas protegidas de la pila. */
    private void abrirLoginYLimpiarPila() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
