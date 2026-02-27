package es.layout.aplicacioncompleta;

import android.os.Bundle;
import android.text.Editable;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterFragment extends Fragment {
    private ToolbarController toolbarController;
    private AuthNavigator navigator;

    private TextInputLayout tilNombre, tilApellidos, tilEdad;
    private TextInputEditText etNombre, etApellidos;
    private MaterialAutoCompleteTextView actEdad;
    private MaterialButton btnMeApunto;
    private boolean mostrarNombre = true;
    private boolean mostrarApellidos = true;

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof ToolbarController) toolbarController = (ToolbarController) context;
        if (context instanceof AuthNavigator) navigator = (AuthNavigator) context;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        tilNombre = v.findViewById(R.id.tilNombre);
        tilApellidos = v.findViewById(R.id.tilApellidos);
        tilEdad = v.findViewById(R.id.tilEdad);
        etNombre = v.findViewById(R.id.etNombre);
        etApellidos = v.findViewById(R.id.etApellidos);
        actEdad = v.findViewById(R.id.actEdad);
        btnMeApunto = v.findViewById(R.id.btnMeApunto);
        TextView tvPrivacidad = v.findViewById(R.id.tvPrivacidad);
        TextView tvVerCondiciones = v.findViewById(R.id.tvVerCondiciones);

        String[] edades = getResources().getStringArray(R.array.rangos_edad);
        actEdad.setSimpleItems(edades);
        actEdad.setOnItemClickListener((parent, view, position, id) -> {
            String seleccion = safeText(actEdad);
            if (!esMayorDeEdad(seleccion)) {
                tilEdad.setHelperText(getString(R.string.debes_ser_mayor));
                btnMeApunto.setEnabled(false);
                Toast.makeText(requireContext(), R.string.debes_ser_mayor, Toast.LENGTH_SHORT).show();
            } else {
                tilEdad.setHelperText(null);
                btnMeApunto.setEnabled(true);
            }
            tilEdad.setError(null);
        });

        tilNombre.setEndIconOnClickListener(v1 -> {
            mostrarNombre = !mostrarNombre;
            if (mostrarNombre) {
                etNombre.setTransformationMethod(null);
                tilNombre.setEndIconDrawable(R.drawable.baseline_visibility_24);
            } else {
                etNombre.setTransformationMethod(PasswordTransformationMethod.getInstance());
                tilNombre.setEndIconDrawable(R.drawable.baseline_visibility_off_24);
            }
            if (etNombre.getText() != null) etNombre.setSelection(etNombre.getText().length());
        });
        tilApellidos.setEndIconOnClickListener(v12 -> {
            mostrarApellidos = !mostrarApellidos;
            if (mostrarApellidos) {
                etApellidos.setTransformationMethod(null);
                tilApellidos.setEndIconDrawable(R.drawable.baseline_visibility_24);
            } else {
                etApellidos.setTransformationMethod(PasswordTransformationMethod.getInstance());
                tilApellidos.setEndIconDrawable(R.drawable.baseline_visibility_off_24);
            }
            if (etApellidos.getText() != null) etApellidos.setSelection(etApellidos.getText().length());
        });

        etNombre.addTextChangedListener(new SimpleWatcher() { @Override public void afterTextChanged(Editable s) { validarNombre(); actualizarEstadoBoton(); } });
        etApellidos.addTextChangedListener(new SimpleWatcher() { @Override public void afterTextChanged(Editable s) { validarApellidos(); actualizarEstadoBoton(); } });

        tvPrivacidad.setOnClickListener(v13 -> Toast.makeText(requireContext(), R.string.politica_privacidad, Toast.LENGTH_SHORT).show());
        tvVerCondiciones.setOnClickListener(v14 -> IntentHelper.openUrlInApp(requireContext(), "https://developers.google.com/ml-kit/terms"));

        btnMeApunto.setOnClickListener(view -> {
            validarNombre(); validarApellidos(); validarEdad(); actualizarEstadoBoton();
            if (!btnMeApunto.isEnabled()) return;

            Bundle result = new Bundle();
            result.putString(AuthConstants.EXTRA_USER, safeText(etNombre));
            result.putString(AuthConstants.EXTRA_PASS, safeText(etApellidos));
            getParentFragmentManager().setFragmentResult("registerResult", result);
            if (navigator != null) navigator.goToLogin();
        });

        actualizarEstadoBoton();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (toolbarController != null) toolbarController.setToolbarVisible(true);
    }

    private void validarNombre() {
        String n = safeText(etNombre);
        if (n.isEmpty()) tilNombre.setError(getString(R.string.obligatorio));
        else if (n.length() < 2) tilNombre.setError(getString(R.string.min_dos));
        else tilNombre.setError(null);
    }

    private void validarApellidos() {
        String a = safeText(etApellidos);
        if (a.isEmpty()) tilApellidos.setError(getString(R.string.obligatorio));
        else if (a.length() < 2) tilApellidos.setError(getString(R.string.min_dos));
        else tilApellidos.setError(null);
    }

    private void validarEdad() {
        String e = safeText(actEdad);
        if (e.isEmpty()) tilEdad.setError(getString(R.string.obligatorio));
        else if (!esMayorDeEdad(e)) tilEdad.setError(getString(R.string.debes_ser_mayor));
        else tilEdad.setError(null);
    }

    private void actualizarEstadoBoton() {
        boolean nombreOk = tilNombre.getError() == null && !safeText(etNombre).isEmpty();
        boolean apOk = tilApellidos.getError() == null && !safeText(etApellidos).isEmpty();
        String edadStr = safeText(actEdad);
        boolean edadOk = !edadStr.isEmpty() && tilEdad.getError() == null && esMayorDeEdad(edadStr);
        btnMeApunto.setEnabled(nombreOk && apOk && edadOk);
    }

    private static abstract class SimpleWatcher implements android.text.TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {}
    }

    private String safeText(View v) {
        if (v instanceof TextInputEditText) {
            Editable e = ((TextInputEditText) v).getText();
            return e == null ? "" : e.toString().trim();
        } else if (v instanceof MaterialAutoCompleteTextView) {
            Editable e = ((MaterialAutoCompleteTextView) v).getText();
            return e == null ? "" : e.toString().trim();
        }
        return "";
    }

    private boolean esMayorDeEdad(String edadStr) {
        try { return Integer.parseInt(edadStr.trim()) >= 18; }
        catch (Exception e) { return false; }
    }
}
