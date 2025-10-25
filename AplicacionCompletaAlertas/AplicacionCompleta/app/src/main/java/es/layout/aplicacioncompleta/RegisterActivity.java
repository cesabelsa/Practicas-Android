package es.layout.aplicacioncompleta;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    // ========= Constantes para devolver datos =========
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_LASTNAME = "extra_lastname";

    // URL "Ver condiciones"
    private static final String TERMS_URL = "https://developers.google.com/ml-kit/terms";

    // ========= Referencias UI =========
    private MaterialToolbar toolbar;
    private ShapeableImageView imgHeader;

    private TextInputLayout tilNombre, tilApellidos, tilEdad;
    private TextInputEditText etNombre, etApellidos;
    private MaterialAutoCompleteTextView actEdad;
    private MaterialButton btnMeApunto;

    // ========= Estado de los "ojos" =========
    private boolean mostrarNombre = true;
    private boolean mostrarApellidos = true;

    // ========= Launchers cámara/permiso =========
    private ActivityResultLauncher<String> requestCameraPermission;
    private ActivityResultLauncher<Intent> takePictureLauncher;

    // ========= Validación: solo letras (Unicode) y espacios =========
    private static final Pattern SOLO_LETRAS_Y_ESPACIOS = Pattern.compile("^[\\p{L} ]+$");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // ---- findViewById ----
        toolbar      = findViewById(R.id.toolbar);
        imgHeader    = findViewById(R.id.imgHeader);

        tilNombre    = findViewById(R.id.tilNombre);
        tilApellidos = findViewById(R.id.tilApellidos);
        tilEdad      = findViewById(R.id.tilEdad);

        etNombre     = findViewById(R.id.etNombre);
        etApellidos  = findViewById(R.id.etApellidos);
        actEdad      = findViewById(R.id.actEdad);

        btnMeApunto  = findViewById(R.id.btnMeApunto);

        // ---- Toolbar ----
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationIconTint(ContextCompat.getColor(this, android.R.color.white));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // ---- Dropdown de edad (desde XML) ----
        final String[] edades = getResources().getStringArray(R.array.rangos_edad);
        actEdad.setSimpleItems(edades);

        // Listener de selección: si edad < 18 → desactivar botón y Toast
        actEdad.setOnItemClickListener((parent, view, position, id) -> {
            String seleccion = getTexto(actEdad);

            if (!esMayorDeEdad(seleccion)) {
                tilEdad.setHelperText("Debes ser mayor de 18 años");
                btnMeApunto.setEnabled(false);
                Toast.makeText(this, "Debes ser mayor de 18 años para registrarte", Toast.LENGTH_SHORT).show();
            } else {
                tilEdad.setHelperText(null);
            }

            validarEdad();
            actualizarEstadoBoton();
        });

        // ---- Cámara: permisos + resultado ----
        configurarLaunchers();

        // ---- Listeners UI ----
        configurarEventosUI();

        // ---- Estado inicial ----
        aplicarEstadoOjoNombre();
        aplicarEstadoOjoApellidos();
        validarNombre();
        validarApellidos();
        validarEdad();
        actualizarEstadoBoton();
    }

    // ===================== Cámara & permisos =====================
    private void configurarLaunchers() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Object bmp = extras.get("data"); // thumbnail
                            if (bmp instanceof Bitmap) {
                                imgHeader.setImageBitmap((Bitmap) bmp);
                            }
                        }
                    }
                });

        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (Boolean.TRUE.equals(isGranted)) {
                        abrirCamara();
                    } else {
                        Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void solicitarPermisoOCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamara() {
        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (camIntent.resolveActivity(getPackageManager()) != null) {
            takePictureLauncher.launch(camIntent);
        } else {
            Toast.makeText(this, "No se encontró una app de cámara en el dispositivo", Toast.LENGTH_LONG).show();
        }
    }

    // ===================== Eventos UI =====================
    private void configurarEventosUI() {
        // Imagen → abre cámara
        imgHeader.setOnClickListener(v -> solicitarPermisoOCamara());

        // Ojo Nombre
        tilNombre.setEndIconOnClickListener(v -> {
            mostrarNombre = !mostrarNombre;
            aplicarEstadoOjoNombre();
        });

        // Ojo Apellidos
        tilApellidos.setEndIconOnClickListener(v -> {
            mostrarApellidos = !mostrarApellidos;
            aplicarEstadoOjoApellidos();
        });

        // "Ver condiciones" → abrir URL
        TextView tvVerCondiciones = findViewById(R.id.tvVerCondiciones);
        tvVerCondiciones.setOnClickListener(v -> {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        android.net.Uri.parse(TERMS_URL));
                startActivity(i);
            } catch (Exception e) {
                Toast.makeText(this, "No se pudo abrir las condiciones", Toast.LENGTH_SHORT).show();
            }
        });

        // Validaciones reactivas
        etNombre.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable s) {
                validarNombre();
                actualizarEstadoBoton();
            }
        });
        etApellidos.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable s) {
                validarApellidos();
                actualizarEstadoBoton();
            }
        });

        // Botón registrar
        btnMeApunto.setOnClickListener(v -> {
            validarNombre();
            validarApellidos();
            validarEdad();
            actualizarEstadoBoton();

            if (!btnMeApunto.isEnabled()) return;

            Intent result = new Intent();
            result.putExtra(EXTRA_NAME, getTexto(etNombre));
            result.putExtra(EXTRA_LASTNAME, getTexto(etApellidos));
            setResult(RESULT_OK, result);
            finish();
        });
    }

    // ===================== OJOS (mostrar/ocultar) =====================
    private void aplicarEstadoOjoNombre() {
        if (mostrarNombre) {
            etNombre.setTransformationMethod(null);
            tilNombre.setEndIconDrawable(R.drawable.baseline_visibility_24);
        } else {
            etNombre.setTransformationMethod(PasswordTransformationMethod.getInstance());
            tilNombre.setEndIconDrawable(R.drawable.baseline_visibility_off_24);
        }
        Editable e = etNombre.getText();
        if (e != null) etNombre.setSelection(e.length());
    }

    private void aplicarEstadoOjoApellidos() {
        if (mostrarApellidos) {
            etApellidos.setTransformationMethod(null);
            tilApellidos.setEndIconDrawable(R.drawable.baseline_visibility_24);
        } else {
            etApellidos.setTransformationMethod(PasswordTransformationMethod.getInstance());
            tilApellidos.setEndIconDrawable(R.drawable.baseline_visibility_off_24);
        }
        Editable e = etApellidos.getText();
        if (e != null) etApellidos.setSelection(e.length());
    }

    // ===================== Validaciones =====================
    private void validarNombre() {
        String nombre = getTexto(etNombre);
        if (nombre.isEmpty()) {
            tilNombre.setError("Campo obligatorio");
        } else if (!esTextoSoloLetras(nombre)) {
            tilNombre.setError("Ups, no creo que sea correcto, revísalo");
        } else {
            tilNombre.setError(null);
        }
    }

    private void validarApellidos() {
        String ap = getTexto(etApellidos);
        if (ap.isEmpty()) {
            tilApellidos.setError("Campo obligatorio");
        } else if (!esTextoSoloLetras(ap)) {
            tilApellidos.setError("Ups, no creo que sea correcto, revísalo");
        } else {
            tilApellidos.setError(null);
        }
    }

    private void validarEdad() {
        String edad = getTexto(actEdad);
        if (edad.isEmpty()) {
            tilEdad.setError("Campo obligatorio");
        } else {
            tilEdad.setError(null);
        }
    }

    private boolean esTextoSoloLetras(String s) {
        if (s == null) return false;
        String t = s.trim();
        if (t.isEmpty()) return false;
        return SOLO_LETRAS_Y_ESPACIOS.matcher(t).matches();
    }

    private boolean contieneCaracteresInvalidos(String s) {
        // Ya no se usa directamente; la lógica está en esTextoSoloLetras()
        // Se deja por compatibilidad si lo llamabas desde otro sitio.
        return s != null && (!esTextoSoloLetras(s));
    }

    // ===================== Lógica botón =====================
    private void actualizarEstadoBoton() {
        boolean nombreOk = tilNombre.getError() == null && !getTexto(etNombre).isEmpty();
        boolean apOk     = tilApellidos.getError() == null && !getTexto(etApellidos).isEmpty();

        String edadStr   = getTexto(actEdad);
        boolean edadOk   = !edadStr.isEmpty() && tilEdad.getError() == null;
        boolean esAdulto = esMayorDeEdad(edadStr);

        btnMeApunto.setEnabled(nombreOk && apOk && edadOk && esAdulto);
    }

    /** Devuelve true si la edad seleccionada es >= 18 */
    private boolean esMayorDeEdad(String edadStr) {
        if (edadStr == null || edadStr.trim().isEmpty()) return false;
        try {
            int edad = Integer.parseInt(edadStr.trim());
            return edad >= 18;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ===================== Utilidades =====================
    private abstract static class SimpleWatcher implements android.text.TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {}
    }

    private String getTexto(View v) {
        if (v instanceof TextInputEditText) {
            Editable e = ((TextInputEditText) v).getText();
            return e == null ? "" : e.toString().trim();
        } else if (v instanceof MaterialAutoCompleteTextView) {
            Editable e = ((MaterialAutoCompleteTextView) v).getText();
            return e == null ? "" : e.toString().trim();
        }
        return "";
    }
}
