package es.layout.aplicacioncompleta;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import es.layout.aplicacioncompleta.model.Transport;

/**
 * ViewModel que expone una lista "fake" de transportes.
 * La lista se inicializa en memoria a la espera de disponer de un webservice.
 */
public class TransportListViewModel extends ViewModel {

    private final MutableLiveData<List<Transport>> transportList = new MutableLiveData<>();

    public TransportListViewModel() {
        // Carga de datos simulada
        List<Transport> data = new ArrayList<>();
        data.add(new Transport("Classic Car", "Holiday", R.drawable.classic_car));
        data.add(new Transport("Sport Car", "Holiday", R.drawable.sport_cart));
        data.add(new Transport("Flying Car", "Holiday", R.drawable.flying_car));
        data.add(new Transport("Electric Car", "Holiday", R.drawable.electric_car));
        data.add(new Transport("Motorhome", "Holiday", R.drawable.motorhome));
        data.add(new Transport("Pickup", "Holiday", R.drawable.pickup_car));
        data.add(new Transport("Airplane", "Holiday", R.drawable.airplane));
        data.add(new Transport("Bus", "Holiday", R.drawable.bus));

        transportList.setValue(data);
    }

    public LiveData<List<Transport>> getTransportList() {
        return transportList;
    }
}
