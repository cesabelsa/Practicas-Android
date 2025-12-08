package es.layout.aplicacioncompleta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Fragment contenedor de la pantalla principal (Home) que muestra
 * las pestañas con ViewPager2 y TabLayout. Actúa como destino inicial
 * del grafo de navegación principal (nav_main).
 */
public class HomeTabsFragment extends Fragment {

    public HomeTabsFragment() {
        // Constructor vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_tabs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Toolbar dentro del Fragment
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        TabLayout tabLayout = view.findViewById(R.id.tab_layout_home);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager_home);

        // Reutilizamos el HomePagerAdapter ya existente
        HomePagerAdapter adapter = new HomePagerAdapter(activity);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0, false);

        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        View customView = LayoutInflater.from(requireContext())
                                .inflate(R.layout.tab_custom_icon, null);

                        FrameLayout container = customView.findViewById(R.id.tabContainer);
                        ImageView icon = customView.findViewById(R.id.tabIcon);

                        // Fondo blanco para las pestañas
                        container.setBackgroundColor(0xFFFFFFFF);

                        switch (position) {
                            case 0:
                                icon.setImageResource(R.drawable.camara_recorte);
                                break;
                            case 1:
                                icon.setImageResource(R.drawable.montana_recorte);
                                break;
                            case 2:
                                icon.setImageResource(R.drawable.coche_recorte);
                                break;
                            case 3:
                            default:
                                icon.setImageResource(R.drawable.cara_recorte);
                                break;
                        }

                        tab.setCustomView(customView);
                    }
                }).attach();
    }
}
