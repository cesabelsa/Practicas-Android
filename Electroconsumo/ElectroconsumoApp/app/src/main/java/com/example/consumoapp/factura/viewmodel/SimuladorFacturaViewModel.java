package com.example.consumoapp.factura.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.consumoapp.factura.SimulacionFacturaEntity;
import com.example.consumoapp.factura.LineaSimuladorFactura;
import com.example.consumoapp.factura.data.SimuladorFacturaRepository;
import com.example.consumoapp.factura.model.SimuladorFacturaEvent;
import com.example.consumoapp.factura.model.SimuladorFacturaUiState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import com.example.consumoapp.tarifas.data.ComercializadoraEntity;
import java.util.List;
import java.util.ArrayList;

/**
 * Conserva los datos del simulador durante cambios de configuración.
 * Todas las operaciones Room se ejecutan fuera del hilo principal.
 */
public final class SimuladorFacturaViewModel extends AndroidViewModel {
    private final SimuladorFacturaRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // Evita publicar "cargando = false" mientras todavía quedan tareas en cola.
    private final AtomicInteger tareasActivas = new AtomicInteger(0);

    private SimuladorFacturaUiState estadoActual = SimuladorFacturaUiState.inicial();
    private final MutableLiveData<SimuladorFacturaUiState> uiState =
            new MutableLiveData<>(estadoActual);
    private final MutableLiveData<Event<SimuladorFacturaEvent>> eventos = new MutableLiveData<>();

    // Esta lista pertenece al ViewModel, no a la Activity.
    // De este modo no se pierde al girar la pantalla ni al recrearse la interfaz.
    // El adapter y los cálculos trabajan siempre con esta misma instancia.
    private final List<LineaSimuladorFactura> lineasFactura = new ArrayList<>();

    public SimuladorFacturaViewModel(@NonNull Application application) {
        super(application);
        repository = new SimuladorFacturaRepository(application);
    }

    public LiveData<SimuladorFacturaUiState> getUiState() { return uiState; }
    public LiveData<Event<SimuladorFacturaEvent>> getEventos() { return eventos; }

    /**
     * Devuelve la lista compartida de líneas de la factura.
     * No se devuelve una copia porque el RecyclerView debe observar la misma lista.
     */
    public List<LineaSimuladorFactura> getLineasFactura() {
        return lineasFactura;
    }

    public void cargarComercializadoras() {
        ejecutar(() -> {
            List<ComercializadoraEntity> comercializadoras =
                    repository.cargarComercializadoras();

            actualizarEstado(
                    estado -> estado.conComercializadoras(comercializadoras)
            );
        }, "No se pudieron cargar las comercializadoras");
    }
    public void cargarTarifas(long comercializadoraId) {
        ejecutar(() -> actualizarEstado(estado -> estado.conTarifas(repository.cargarTarifas(comercializadoraId))),
                "No se pudieron cargar las tarifas");
    }

    public void cargarConstantes(String fecha, boolean mostrarMensaje) {
        ejecutar(() -> actualizarEstado(estado -> estado.conConstantes(
                        new SimuladorFacturaUiState.ConstantesCargadas(
                                fecha, repository.cargarConstantes(fecha), mostrarMensaje))),
                "No hay constantes vigentes para " + fecha + ". Puedes introducir los valores manualmente.");
    }

    public void cargarCategorias() {
        ejecutar(() -> actualizarEstado(estado -> estado.conCategorias(repository.cargarCategorias())),
                "No se pudo cargar el catálogo de electrodomésticos");
    }

    public void cargarElectrodomesticos(String categoria) {
        ejecutar(() -> actualizarEstado(estado -> estado.conElectrodomesticos(repository.cargarElectrodomesticos(categoria))),
                "No se pudieron cargar los electrodomésticos");
    }

    public void cargarUltimosPreciosEsios() {
        ejecutar(() -> actualizarEstado(estado -> estado.conPreciosEsios(repository.cargarUltimosPreciosEsios())),
                "No se pudieron leer los precios ESIOS guardados");
    }

    public void guardarSimulacion(SimulacionFacturaEntity simulacion) {
        ejecutar(() -> {
            repository.guardarSimulacion(simulacion);
            emitirEvento(new SimuladorFacturaEvent.SimulacionGuardada());
        }, "No se pudo guardar la simulación");
    }

    private void ejecutar(Tarea tarea, String mensajeError) {
        // Incrementamos antes de enviar la tarea para cubrir también el tiempo en cola.
        tareasActivas.incrementAndGet();
        actualizarEstado(estado -> estado.conCargando(true));

        try {
            executor.execute(() -> {
                try {
                    tarea.ejecutar();
                } catch (InterruptedException error) {
                    // Conservamos la marca de interrupción para cancelar correctamente.
                    Thread.currentThread().interrupt();
                } catch (Exception error) {
                    emitirEvento(new SimuladorFacturaEvent.MostrarError(mensajeError));
                } finally {
                    finalizarTarea();
                }
            });
        } catch (RejectedExecutionException error) {
            // Puede ocurrir si la pantalla se cierra justo al solicitar una operación.
            finalizarTarea();
        }
    }

    private void emitirEvento(SimuladorFacturaEvent evento) {
        eventos.postValue(new Event<>(evento));
    }

    /** Publica el fin de carga únicamente cuando no queda ninguna tarea pendiente. */
    private void finalizarTarea() {
        int pendientes = tareasActivas.decrementAndGet();
        if (pendientes <= 0) {
            tareasActivas.set(0);
            actualizarEstado(estado -> estado.conCargando(false));
        }
    }


    private synchronized void actualizarEstado(ActualizadorEstado actualizador) {
        estadoActual = actualizador.actualizar(estadoActual);
        uiState.postValue(estadoActual);
    }

    @Override
    protected void onCleared() {
        // Cancela tareas pendientes cuando el ViewModel deja de utilizarse definitivamente.
        executor.shutdownNow();
    }

    private interface ActualizadorEstado {
        SimuladorFacturaUiState actualizar(SimuladorFacturaUiState estado);
    }

    private interface Tarea {
        void ejecutar() throws Exception;
    }

}
