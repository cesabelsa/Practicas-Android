package es.layout.aplicacioncompleta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

/**
 * Segundo fragment del onboarding.
 * Permite ir a la página 3 o saltar directamente al Login.
 */
public class OnboardingFragment2 extends Fragment {

    public OnboardingFragment2() {
        // Constructor vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnSkip = view.findViewById(R.id.btnSkip);
        Button btnNext = view.findViewById(R.id.btnNext);

        // Saltar el onboarding -> ir directo al Login
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(OnboardingFragment2.this)
                        .navigate(R.id.action_onboardingFragment2_to_loginFragment);
            }
        });

        // Continuar al tercer fragment del onboarding
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(OnboardingFragment2.this)
                        .navigate(R.id.action_onboardingFragment2_to_onboardingFragment3);
            }
        });
    }
}
