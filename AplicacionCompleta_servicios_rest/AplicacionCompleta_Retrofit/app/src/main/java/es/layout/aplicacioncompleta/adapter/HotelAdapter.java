package es.layout.aplicacioncompleta.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import es.layout.aplicacioncompleta.databinding.ItemHotelBinding;
import es.layout.aplicacioncompleta.model.retrofit.Address;
import es.layout.aplicacioncompleta.model.retrofit.GuestReviews;
import es.layout.aplicacioncompleta.model.retrofit.Hotel;
import es.layout.aplicacioncompleta.model.retrofit.OptimizedThumbUrls;
import es.layout.aplicacioncompleta.model.retrofit.Price;
import es.layout.aplicacioncompleta.model.retrofit.RatePlan;

import java.util.ArrayList;
import java.util.List;

/*
 * Adaptador del RecyclerView.
 * Su trabajo es convertir cada objeto Hotel en una tarjeta visual de la lista.
 */
public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    // Lista de hoteles que se mostrarán en pantalla.
    private final List<Hotel> hoteles;

    public HotelAdapter() {
        this.hoteles = new ArrayList<>();
    }

    // Método para actualizar la lista cuando llegue la respuesta de Retrofit.
    public void setHoteles(List<Hotel> nuevosHoteles) {
        hoteles.clear();

        if (nuevosHoteles != null) {
            hoteles.addAll(nuevosHoteles);
        }

        // Avisa al RecyclerView de que debe volver a dibujar la lista.
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Inflamos el diseño XML item_hotel.xml usando ViewBinding.
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemHotelBinding binding = ItemHotelBinding.inflate(inflater, parent, false);
        return new HotelViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {

        // Recuperamos el hotel de esta posición.
        Hotel hotel = hoteles.get(position);

        // Pedimos al ViewHolder que pinte los datos del hotel.
        holder.bind(hotel);
    }

    @Override
    public int getItemCount() {
        return hoteles.size();
    }

    /*
     * ViewHolder: representa una fila/tarjeta del RecyclerView.
     */
    static class HotelViewHolder extends RecyclerView.ViewHolder {

        private final ItemHotelBinding binding;

        public HotelViewHolder(ItemHotelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Hotel hotel) {

            // Nombre del hotel.
            binding.tvNombreHotel.setText(textoSeguro(hotel.getName()));

            // Dirección y municipio.
            Address address = hotel.getAddress();
            if (address != null) {
                String direccion = textoSeguro(address.getStreetAddress()) + " - " + textoSeguro(address.getLocality());
                binding.tvDireccionHotel.setText(direccion);
            } else {
                binding.tvDireccionHotel.setText("Dirección no disponible");
            }

            // Precio.
            RatePlan ratePlan = hotel.getRatePlan();
            Price price = ratePlan != null ? ratePlan.getPrice() : null;
            if (price != null) {
                binding.tvPrecioHotel.setText("Precio: " + textoSeguro(price.getCurrent()));
            } else {
                binding.tvPrecioHotel.setText("Precio no disponible");
            }

            // Rating.
            GuestReviews reviews = hotel.getGuestReviews();
            if (reviews != null) {
                binding.tvRatingHotel.setText("Rating: " + textoSeguro(reviews.getRating()) + " / 5 - " + textoSeguro(reviews.getBadgeText()));
            } else {
                binding.tvRatingHotel.setText("Rating no disponible");
            }

            // Imagen del hotel.
            OptimizedThumbUrls urls = hotel.getOptimizedThumbUrls();
            String imageUrl = urls != null ? urls.getSrpDesktop() : null;

            Glide.with(binding.ivHotel.getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .into(binding.ivHotel);
        }

        private String textoSeguro(String texto) {
            return texto == null ? "" : texto;
        }
    }
}
