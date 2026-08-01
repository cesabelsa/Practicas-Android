package com.example.consumoapp.esios.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter de ViewPager2.
 *
 * Crea las pantallas principales de ESIOS por pestañas.
 */
public class EsiosPagerAdapter extends FragmentStateAdapter {

    public EsiosPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        if (position == 0) {
            return new ResumenFragment();
        }

        if (position == 1) {
            return new PreciosFragment();
        }

        if (position == 2) {
            return new GraficosFragment();
        }

        return new GraficosFragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
