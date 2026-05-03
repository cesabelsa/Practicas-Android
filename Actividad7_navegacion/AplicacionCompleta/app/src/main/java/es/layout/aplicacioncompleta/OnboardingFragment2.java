package es.layout.aplicacioncompleta;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class OnboardingFragment2 extends Fragment {

    public OnboardingFragment2() {
        // Required empty public constructor
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

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ir directamente a la pantalla de login (AuthActivity)
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), AuthActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(OnboardingFragment2.this)
                        .navigate(R.id.action_onboardingFragment2_to_onboardingFragment3);
            }
        });
    }
}
