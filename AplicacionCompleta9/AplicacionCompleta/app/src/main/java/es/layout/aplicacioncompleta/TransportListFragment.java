package es.layout.aplicacioncompleta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import es.layout.aplicacioncompleta.databinding.FragmentTransportListBinding;
import es.layout.aplicacioncompleta.model.Transport;

/**
 * Fragment que se mostrará en la pestaña 1.
 * Contiene un RecyclerView con una lista de transportes construida mediante ViewBinding
 * y observando los datos expuestos por un ViewModel.
 */
public class TransportListFragment extends Fragment {

    private FragmentTransportListBinding binding;
    private TransportListViewModel viewModel;

    public TransportListFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentTransportListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configuramos el RecyclerView
        binding.recyclerTransport.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Obtenemos el ViewModel y observamos la lista de transportes
        viewModel = new ViewModelProvider(this).get(TransportListViewModel.class);
        viewModel.getTransportList().observe(getViewLifecycleOwner(), new Observer<List<Transport>>() {
            @Override
            public void onChanged(List<Transport> transports) {
                // Creamos el adapter cada vez que cambie la lista
                TransportAdapter adapter = new TransportAdapter(transports);
                binding.recyclerTransport.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
