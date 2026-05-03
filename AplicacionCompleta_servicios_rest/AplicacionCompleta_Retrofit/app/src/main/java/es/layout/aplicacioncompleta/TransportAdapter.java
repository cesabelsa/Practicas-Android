package es.layout.aplicacioncompleta;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.layout.aplicacioncompleta.databinding.ItemTransportBinding;
import es.layout.aplicacioncompleta.model.Transport;

/**
 * Adapter del RecyclerView que muestra la lista de transportes.
 */
public class TransportAdapter extends RecyclerView.Adapter<TransportAdapter.TransportViewHolder> {

    private final List<Transport> items;

    public TransportAdapter(List<Transport> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public TransportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemTransportBinding binding = ItemTransportBinding.inflate(inflater, parent, false);
        return new TransportViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransportViewHolder holder, int position) {
        final Transport transport = items.get(position);

        holder.binding.imageTransport.setImageResource(transport.getImageResId());
        holder.binding.textName.setText(transport.getName());
        holder.binding.textDescription.setText(transport.getDescription());

        // Mostramos un Toast con el nombre del transporte cuando el usuario pulsa sobre la card
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        v.getContext(),
                        "Transporte seleccionado: " + transport.getName(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class TransportViewHolder extends RecyclerView.ViewHolder {

        final ItemTransportBinding binding;

        TransportViewHolder(ItemTransportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
