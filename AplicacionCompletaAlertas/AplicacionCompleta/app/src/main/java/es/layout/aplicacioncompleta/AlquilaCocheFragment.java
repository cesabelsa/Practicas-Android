package es.layout.aplicacioncompleta;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AlquilaCocheFragment extends Fragment {

    public static AlquilaCocheFragment newInstance() {
        return new AlquilaCocheFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alquila_coche, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Manejo del botón “atrás” en la toolbar del fragment
        View toolbar = view.findViewById(R.id.alquila_toolbar);
        if (toolbar instanceof com.google.android.material.appbar.MaterialToolbar) {
            ((com.google.android.material.appbar.MaterialToolbar) toolbar)
                    .setNavigationOnClickListener(v -> {
                        // Cierra el fragment y oculta el overlay
                        if (getActivity() instanceof HomeActivity) {
                            ((HomeActivity) getActivity()).closeOverlayFragment();
                        } else {
                            requireActivity().getSupportFragmentManager().popBackStack();
                        }
                    });
        }
    }
}

