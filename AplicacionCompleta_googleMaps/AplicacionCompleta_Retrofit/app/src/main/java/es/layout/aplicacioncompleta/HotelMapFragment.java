package es.layout.aplicacioncompleta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Fragment que muestra el hotel seleccionado en Google Maps.
 *
 * Cumple los criterios del PDF:
 * - Se abre al seleccionar un hotel de la lista.
 * - Muestra un mapa de Google dentro de la propia aplicación.
 * - Coloca un marker en la posición del hotel.
 * - El marker tiene como descripción el nombre del hotel.
 */
public class HotelMapFragment extends Fragment implements OnMapReadyCallback {

    // Claves usadas para recibir datos mediante Bundle.
    public static final String ARG_HOTEL_NAME = "hotel_name";
    public static final String ARG_HOTEL_LAT = "hotel_lat";
    public static final String ARG_HOTEL_LON = "hotel_lon";

    // Datos del hotel seleccionado.
    private String hotelName;
    private double hotelLat;
    private double hotelLon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hotel_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recuperamos los argumentos enviados desde HotelListFragment.
        Bundle args = getArguments();
        if (args != null) {
            hotelName = args.getString(ARG_HOTEL_NAME, "Hotel seleccionado");
            hotelLat = args.getDouble(ARG_HOTEL_LAT, 0.0);
            hotelLon = args.getDouble(ARG_HOTEL_LON, 0.0);
        } else {
            hotelName = "Hotel seleccionado";
        }

        // Mostramos el nombre del hotel en la cabecera.
        TextView tvTituloMapa = view.findViewById(R.id.tvTituloMapa);
        tvTituloMapa.setText(hotelName);

        // Botón para volver a la lista.
        view.findViewById(R.id.btnVolverMapa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(HotelMapFragment.this).popBackStack();
            }
        });

        // Buscamos el SupportMapFragment interno y pedimos el mapa de forma asíncrona.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.fragmentGoogleMap);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Creamos la posición del hotel con latitud y longitud.
        LatLng posicionHotel = new LatLng(hotelLat, hotelLon);

        // Añadimos el marker pedido en el PDF.
        googleMap.addMarker(new MarkerOptions()
                .position(posicionHotel)
                .title(hotelName)
                .snippet("Hotel seleccionado"));

        // Movemos la cámara para que el hotel quede centrado y con zoom suficiente.
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionHotel, 16f));

        // Activamos controles básicos del mapa.
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
    }
}
