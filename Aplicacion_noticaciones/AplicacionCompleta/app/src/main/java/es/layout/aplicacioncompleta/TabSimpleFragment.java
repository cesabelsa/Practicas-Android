package es.layout.aplicacioncompleta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment sencillo para las pestañas 1, 2 y 3.
 * Muestra un texto indicando la posición de la pestaña.
 */
public class TabSimpleFragment extends Fragment {

    private static final String ARG_POSITION = "arg_position";

    public static TabSimpleFragment newInstance(int position) {
        TabSimpleFragment fragment = new TabSimpleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tab_simple, container, false);

        TextView tv = view.findViewById(R.id.tvTabPosition);
        int position = 0;
        Bundle args = getArguments();
        if (args != null) {
            position = args.getInt(ARG_POSITION, 0);
        }
        // Mostramos la posición de la pestaña
        tv.setText("Contenido de la pestaña " + position);

        return view;
    }
}
