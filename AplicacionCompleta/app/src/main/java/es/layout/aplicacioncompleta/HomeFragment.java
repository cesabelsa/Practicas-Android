package es.layout.aplicacioncompleta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.LifecycleOwner;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Infla el menú con los iconos del castillo y coche
                menuInflater.inflate(R.menu.menu_home, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.action_eurodisney) {
                    Toast.makeText(requireContext(), "Eurodisney pulsado ", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.action_alquilar_coche) {
                    Toast.makeText(requireContext(), "Alquilar coche pulsado ", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        }, (LifecycleOwner) getViewLifecycleOwner());
    }
}
