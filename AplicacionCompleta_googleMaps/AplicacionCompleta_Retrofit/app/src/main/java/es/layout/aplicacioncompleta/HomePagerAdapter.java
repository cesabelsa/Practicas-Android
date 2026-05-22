package es.layout.aplicacioncompleta;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter para gestionar los Fragments de las pestañas de la Home.
 *
 * Pestaña 0 -> HomeFragment (contenido principal de la Home).
 * Pestaña 1 -> TransportListFragment con la lista de transportes.
 * Pestañas 2 y 3 -> TabSimpleFragment indicando su posición.
 */
public class HomePagerAdapter extends FragmentStateAdapter {

    public HomePagerAdapter(@NonNull Fragment fragment) {
        // IMPORTANTE:
        // Al usar ViewPager2 dentro de HomeTabsFragment, el adapter debe recibir
        // el Fragment padre y no la Activity.
        // Así los fragments de las pestañas quedan como hijos de HomeTabsFragment
        // y la navegación hacia Google Maps no provoca cierre de la app.
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // Primer TAB: lista de hoteles cargada con Retrofit (GET /listHotels)
                return new HotelListFragment();
            case 1:
                // Lista de transportes (Actividad 9)
                return new TransportListFragment();
            case 2:
            case 3:
            default:
                // Fragments sencillos que muestran la posición de la pestaña
                return TabSimpleFragment.newInstance(position);
        }
    }

    @Override
    public int getItemCount() {
        // Número total de pestañas
        return 4;
    }
}
