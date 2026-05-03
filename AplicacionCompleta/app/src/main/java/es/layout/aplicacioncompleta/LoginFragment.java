package es.layout.aplicacioncompleta;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {
    public static final String EXTRA_USER = AuthConstants.EXTRA_USER;
    public static final String EXTRA_PASS = AuthConstants.EXTRA_PASS;

    private ToolbarController toolbarController;
    private AuthNavigator navigator;

    private TextInputLayout tilUser, tilPass;
    private TextInputEditText edtUser, edtPass;
    private MaterialButton btnLogin;
    private boolean isUserHidden = false;
    private boolean isPassHidden = false;

    private String registeredName;
    private String registeredSurname;

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof ToolbarController) toolbarController = (ToolbarController) context;
        if (context instanceof AuthNavigator) navigator = (AuthNavigator) context;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        tilUser = v.findViewById(R.id.tilUser);
        tilPass = v.findViewById(R.id.tilPass);
        edtUser = v.findViewById(R.id.edtUser);
        edtPass = v.findViewById(R.id.edtPass);
        btnLogin = v.findViewById(R.id.btnLogin);
        TextView txtGetNew = v.findViewById(R.id.txtGetNew);
        TextView txtCreateNew = v.findViewById(R.id.txtCreateNew);

        edtUser.setTransformationMethod(null);
        tilUser.setEndIconDrawable(R.drawable.baseline_visibility_24);
        edtPass.setTransformationMethod(null);
        tilPass.setEndIconDrawable(R.drawable.baseline_visibility_24);

        View.OnClickListener goRegister = new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (navigator != null) navigator.goToRegister();
            }
        };
        txtGetNew.setOnClickListener(goRegister);
        txtCreateNew.setOnClickListener(goRegister);

        tilUser.setEndIconOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (isUserHidden) {
                    edtUser.setTransformationMethod(null);
                    tilUser.setEndIconDrawable(R.drawable.baseline_visibility_24);
                } else {
                    edtUser.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    tilUser.setEndIconDrawable(R.drawable.baseline_visibility_off_24);
                }
                isUserHidden = !isUserHidden;
                if (edtUser.getText() != null) edtUser.setSelection(edtUser.getText().length());
            }
        });

        tilPass.setEndIconOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (isPassHidden) {
                    edtPass.setTransformationMethod(null);
                    tilPass.setEndIconDrawable(R.drawable.baseline_visibility_24);
                } else {
                    edtPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    tilPass.setEndIconDrawable(R.drawable.baseline_visibility_off_24);
                }
                isPassHidden = !isPassHidden;
                if (edtPass.getText() != null) edtPass.setSelection(edtPass.getText().length());
            }
        });

        getParentFragmentManager().setFragmentResultListener("registerResult", this, (key, bundle) -> {
            registeredName = bundle.getString(EXTRA_USER);
            registeredSurname = bundle.getString(EXTRA_PASS);
            if (edtUser.getText() == null || edtUser.getText().toString().trim().isEmpty()) edtUser.setText(registeredName);
            if (edtPass.getText() == null || edtPass.getText().toString().trim().isEmpty()) edtPass.setText(registeredSurname);
            View root = getView();
            if (root != null) Snackbar.make(root, getString(R.string.datos_registro_recibidos), Snackbar.LENGTH_SHORT).show();
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                String u = edtUser.getText() != null ? edtUser.getText().toString().trim() : "";
                String p = edtPass.getText() != null ? edtPass.getText().toString().trim() : "";

                if (registeredName == null || registeredSurname == null) {
                    edtUser.setError(getString(R.string.primero_registrate));
                    return;
                }

                if (u.equals(registeredName) && p.equals(registeredSurname)) {
                    if (getActivity() != null) {
                        Intent i = new Intent(getActivity(), HomeActivity.class);
                        i.putExtra(EXTRA_USER, u);
                        i.putExtra(EXTRA_PASS, p);
                        startActivity(i);
                        getActivity().finish();
                    }
                } else {
                    new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.login_incorrecto)
                        .setMessage(R.string.credenciales_no_coinciden)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
                        })
                        .show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (toolbarController != null) toolbarController.setToolbarVisible(false);
    }
}
