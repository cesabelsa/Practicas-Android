package es.layout.registro;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

// 👇 IMPORTS NECESARIOS PARA STATUS BAR
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import es.layout.registro.databinding.ActivityMainBinding;
import es.layout.registro.R;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // ==== CÁMARA ====
    private ActivityResultLauncher<String> requestCameraPermission;
    private ActivityResultLauncher<Intent> takePictureLauncher;

    private boolean mostrarNombre = true;
    private boolean mostrarApellidos = true;

    private final TextWatcher watcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
        @Override public void afterTextChanged(Editable s) {
            validarNombre();
            validarApellidos();
            actualizarEstadoBoton();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 👇 OCULTAR LA STATUS BAR (iconos de batería, wifi, cobertura…)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.statusBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

        configurarToolbar();
        configurarEdadDropdown();

        // ==== CÁMARA ====
        configurarLaunchersCamara();
        configurarEventosUI();

        aplicarEstadoOjoNombre();
        aplicarEstadoOjoApellidos();

        validarNombre();
        validarApellidos();
        validarEdad();
        actualizarEstadoBoton();
    }

    // ===== Toolbar / Up navigation =====
    private void configurarToolbar() {
        MaterialToolbar tb = binding.toolbar;
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.registro);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // o finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ===== Dropdown Edad =====
    private void configurarEdadDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.rangos_edad)
        );
        binding.actEdad.setAdapter(adapter);
        binding.actEdad.setOnItemClickListener((parent, view, position, id) -> {
            validarEdad();
            actualizarEstadoBoton();
        });
    }

    // ===== Cámara / permisos =====
    private void configurarLaunchersCamara() {
        // 1) Permiso de cámara
        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean granted) {
                        if (granted != null && granted) {
                            abrirCamara();
                        } else {
                            Toast.makeText(MainActivity.this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // 2) Resultado de la cámara real (ACTION_IMAGE_CAPTURE)
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bitmap bmp = null;
                            if (result.getData().getExtras() != null) {
                                Object data = result.getData().getExtras().get("data");
                                if (data instanceof Bitmap) {
                                    bmp = (Bitmap) data; // thumbnail
                                }
                            }
                            if (bmp != null) {
                                binding.imgAvatar.setImageBitmap(bmp);
                            } else {
                                Toast.makeText(MainActivity.this, "No se recibió miniatura de la cámara", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    // ===== UI handlers =====
    private void configurarEventosUI() {
        binding.etNombre.addTextChangedListener(watcher);
        binding.etApellidos.addTextChangedListener(watcher);

        // Abrir condiciones (TextView inferior)
        binding.tvVerCondiciones.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://developers.google.com/ml-kit/terms"));
            startActivity(i);
        });

        // Asegurar que el avatar es clicable
        binding.imgAvatar.setClickable(true);
        binding.imgAvatar.setFocusable(true);

        // Pulsar el avatar -> pedir permiso (si procede) y abrir cámara real
        binding.imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                solicitarPermisoOCamara();
            }
        });

        binding.btnMeApunto.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(MainActivity.this, "¡Registro completado!", Toast.LENGTH_SHORT).show();
            }
        });

        //  Ojo Nombre (tilNombre debe tener endIconMode="custom")
        binding.tilNombre.setEndIconOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mostrarNombre = !mostrarNombre;
                aplicarEstadoOjoNombre();
            }
        });

        // Ojo Apellidos (tilApellidos debe tener endIconMode="custom")
        binding.tilApellidos.setEndIconOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mostrarApellidos = !mostrarApellidos;
                aplicarEstadoOjoApellidos();
            }
        });
    }

    private void aplicarEstadoOjoNombre() {
        if (mostrarNombre) {
            binding.etNombre.setTransformationMethod(null); // mostrar texto
            binding.tilNombre.setEndIconDrawable(R.drawable.baseline_visibility_24);
        } else {
            binding.etNombre.setTransformationMethod(new PasswordTransformationMethod()); // ocultar
            binding.tilNombre.setEndIconDrawable(R.drawable.baseline_visibility_off_24);
        }
        Editable e = binding.etNombre.getText();
        if (e != null) binding.etNombre.setSelection(e.length());
    }

    private void aplicarEstadoOjoApellidos() {
        if (mostrarApellidos) {
            binding.etApellidos.setTransformationMethod(null);
            binding.tilApellidos.setEndIconDrawable(R.drawable.baseline_visibility_24);
        } else {
            binding.etApellidos.setTransformationMethod(new PasswordTransformationMethod());
            binding.tilApellidos.setEndIconDrawable(R.drawable.baseline_visibility_off_24);
        }
        Editable e = binding.etApellidos.getText();
        if (e != null) binding.etApellidos.setSelection(e.length());
    }

    // ===== Permisos cámara =====
    private void solicitarPermisoOCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    // Abre la cámara real del sistema (devolverá un thumbnail)
    private void abrirCamara() {
        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (camIntent.resolveActivity(getPackageManager()) != null) {
            takePictureLauncher.launch(camIntent);
        } else {
            Toast.makeText(this, "No se encontró una app de cámara en el dispositivo", Toast.LENGTH_LONG).show();
        }
    }

    // ===== Validaciones =====
    private void validarNombre() {
        String nombre = getTexto(binding.etNombre);
        if (nombre.isEmpty()) {
            binding.tilNombre.setError("Campo obligatorio");
        } else if (contieneCaracteresInvalidos(nombre)) {
            binding.tilNombre.setError("Ups, no creo que sea correcto, revísalo");
        } else {
            binding.tilNombre.setError(null);
        }
    }

    private void validarApellidos() {
        String ap = getTexto(binding.etApellidos);
        if (ap.isEmpty()) {
            binding.tilApellidos.setError("Campo obligatorio");
        } else if (contieneCaracteresInvalidos(ap)) {
            binding.tilApellidos.setError("Ups, no creo que sea correcto, revísalo");
        } else {
            binding.tilApellidos.setError(null);
        }
    }

    private void validarEdad() {
        String sel = getTexto(binding.actEdad);
        if (sel.matches("0-5|6-11|12-17")) {
            binding.tilEdad.setError("Esta app no es para ti");
        } else {
            binding.tilEdad.setError(null);
        }
    }

    private void actualizarEstadoBoton() {
        boolean nombreOk = binding.tilNombre.getError() == null && !getTexto(binding.etNombre).isEmpty();
        boolean apOk     = binding.tilApellidos.getError() == null && !getTexto(binding.etApellidos).isEmpty();
        boolean edadOk   = binding.tilEdad.getError() == null && !getTexto(binding.actEdad).isEmpty();
        binding.btnMeApunto.setEnabled(nombreOk && apOk && edadOk);
    }

    private boolean contieneCaracteresInvalidos(String s) {
        return s.contains("@") || s.contains("!");
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
