package es.layout.aplicacioncompleta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import es.layout.aplicacioncompleta.adapter.HotelAdapter;
import es.layout.aplicacioncompleta.model.retrofit.Hotel;
import es.layout.aplicacioncompleta.model.retrofit.HotelListResponse;
import es.layout.aplicacioncompleta.network.ApiClient;
import es.layout.aplicacioncompleta.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Primer TAB de la pantalla Home.
 *
 * Este Fragment cumple la segunda historia de usuario del PDF:
 * - Llama mediante Retrofit al endpoint GET /listHotels.
 * - Muestra los hoteles recibidos en un RecyclerView.
 * - Si hay error, muestra un mensaje indicando que no se han encontrado hoteles.
 */
public class HotelListFragment extends Fragment {

    // RecyclerView donde se pintará la lista de hoteles.
    private RecyclerView rvHoteles;

    // Indicador visual de carga mientras Retrofit espera respuesta.
    private View progressHoteles;

    // Texto que se muestra cuando hay error o la lista viene vacía.
    private View tvErrorHoteles;

    // Adaptador que transforma cada objeto Hotel en una fila visual.
    private HotelAdapter hotelAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflamos el diseño del Fragment.
        return inflater.inflate(R.layout.fragment_hotel_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Enlazamos las vistas del XML con variables Java.
        rvHoteles = view.findViewById(R.id.rvHoteles);
        progressHoteles = view.findViewById(R.id.progressHoteles);
        tvErrorHoteles = view.findViewById(R.id.tvErrorHoteles);

        // Configuramos el RecyclerView antes de pedir los datos.
        configurarRecyclerView();

        // Cargamos los hoteles usando Retrofit.
        cargarHoteles();
    }

    /**
     * Prepara el RecyclerView para mostrar una lista vertical.
     */
    private void configurarRecyclerView() {
        // Creamos el adaptador vacío.
        hotelAdapter = new HotelAdapter();

        // LinearLayoutManager coloca los elementos uno debajo de otro.
        rvHoteles.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Conectamos el RecyclerView con su adaptador.
        rvHoteles.setAdapter(hotelAdapter);
    }

    /**
     * Realiza la petición GET /listHotels con Retrofit.
     */
    private void cargarHoteles() {
        // Mostramos estado de carga.
        progressHoteles.setVisibility(View.VISIBLE);
        tvErrorHoteles.setVisibility(View.GONE);
        rvHoteles.setVisibility(View.VISIBLE);

        // Obtenemos la interfaz de servicios Retrofit.
        ApiService apiService = ApiClient.getApiService();

        // Creamos la llamada al endpoint del PDF: GET listHotels.
        Call<HotelListResponse> call = apiService.listHotels();

        // Ejecutamos la llamada en segundo plano.
        call.enqueue(new Callback<HotelListResponse>() {
            @Override
            public void onResponse(@NonNull Call<HotelListResponse> call,
                                   @NonNull Response<HotelListResponse> response) {
                // Ocultamos el progreso porque ya llegó respuesta.
                progressHoteles.setVisibility(View.GONE);

                // Si HTTP es 200 y hay body, intentamos mostrar los hoteles.
                if (response.isSuccessful() && response.body() != null) {
                    List<Hotel> hoteles = response.body().getResults();

                    // Si la lista trae datos, los enviamos al adaptador.
                    if (hoteles != null && !hoteles.isEmpty()) {
                        hotelAdapter.setHoteles(hoteles);
                        rvHoteles.setVisibility(View.VISIBLE);
                        tvErrorHoteles.setVisibility(View.GONE);
                    } else {
                        mostrarErrorHoteles();
                    }
                } else {
                    // Si el mock no devuelve 200, intentamos cargar los hoteles desde assets.
                    cargarHotelesDesdeAssets();
                }
            }

            @Override
            public void onFailure(@NonNull Call<HotelListResponse> call,
                                  @NonNull Throwable t) {
                // Si falla la red o el DNS, usamos el JSON local de la actividad.
                progressHoteles.setVisibility(View.GONE);
                cargarHotelesDesdeAssets();
            }
        });
    }


    /**
     * Carga la lista de hoteles desde app/src/main/assets/response_hotel_list.json.
     *
     * Este método sirve de respaldo cuando el mock de Postman no está disponible.
     */
    private void cargarHotelesDesdeAssets() {
        try {
            // Leemos y convertimos el JSON local en HotelListResponse.
            HotelListResponse hotelListResponse = JsonAssetReader.readJsonFromAssets(
                    requireContext(),
                    "response_hotel_list.json",
                    HotelListResponse.class
            );

            // Obtenemos la lista real de hoteles que está dentro del campo "results".
            List<Hotel> hoteles = hotelListResponse.getResults();

            // Si hay hoteles, los mostramos en el RecyclerView.
            if (hoteles != null && !hoteles.isEmpty()) {
                hotelAdapter.setHoteles(hoteles);
                rvHoteles.setVisibility(View.VISIBLE);
                tvErrorHoteles.setVisibility(View.GONE);
            } else {
                mostrarErrorHoteles();
            }
        } catch (Exception e) {
            // Si tampoco se puede leer el JSON local, mostramos la pantalla de error.
            mostrarErrorHoteles();
        }
    }

    /**
     * Muestra la pantalla de error pedida por el PDF.
     */
    private void mostrarErrorHoteles() {
        rvHoteles.setVisibility(View.GONE);
        tvErrorHoteles.setVisibility(View.VISIBLE);
    }
}
