package com.example.consumoapp.esios.network;

import android.content.Context;

import com.example.consumoapp.esios.data.AppDatabase;
import com.example.consumoapp.esios.data.PrecioLuzEntity;
import com.example.consumoapp.esios.model.EsiosResponse;
import com.example.consumoapp.esios.model.EsiosValue;
import com.example.consumoapp.esios.settings.EsiosPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Descarga el PVPC actual y conserva en Room los días obtenidos. */
public class EsiosRepository {

    private final EsiosApiService apiService;
    private final AppDatabase database;
    private final ExecutorService executorService;
    private final Context appContext;

    public EsiosRepository(Context context) {
        appContext = context.getApplicationContext();
        apiService = EsiosApiClient.getService();
        database = AppDatabase.getInstance(appContext);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void descargarPreciosHoy(EsiosRepositoryCallback callback) {
        descargarIndicadorActual(formatearFecha(new Date()), callback);
    }

    /**
     * ESIOS solo permite en este momento obtener el indicador 1001 sin filtros.
     * Para una fecha pasada se usa la copia que ya exista en SQLite.
     */
    public void descargarPreciosFecha(String fecha, EsiosRepositoryCallback callback) {
        if (!esFechaValida(fecha)) {
            callback.onError("La fecha debe tener formato yyyy-MM-dd.");
            return;
        }
        String hoy = formatearFecha(new Date());
        if (!hoy.equals(fecha)) {
            cargarFechaComoFallback(fecha, callback,
                    "ESIOS no admite actualmente filtros temporales para el indicador 1001. "
                            + "Solo se descarga el día publicado actualmente.");
            return;
        }
        descargarIndicadorActual(fecha, callback);
    }

    private void descargarIndicadorActual(String fechaEsperada, EsiosRepositoryCallback callback) {
        if (EsiosConfig.API_KEY.isEmpty()) {
            cargarFechaComoFallback(fechaEsperada, callback,
                    "Falta ESIOS_API_KEY en local.properties.");
            return;
        }

        if (!EsiosConfig.tieneFormatoTokenValido()) {
            cargarFechaComoFallback(fechaEsperada, callback,
                    "El token ESIOS no tiene el formato esperado de 64 caracteres hexadecimales.");
            return;
        }

        apiService.getIndicadorActual(EsiosConfig.INDICADOR_PVPC_20TD)
                .enqueue(new Callback<EsiosResponse>() {
                    @Override public void onResponse(Call<EsiosResponse> call,
                                                     Response<EsiosResponse> response) {
                        if (!response.isSuccessful()) {
                            String mensaje;
                            if (response.code() == 401 || response.code() == 403) {
                                mensaje = "HTTP " + response.code()
                                        + " al consultar ESIOS. La petición lleva x-api-key, "
                                        + "pero el servidor ha rechazado el token. Comprueba que "
                                        + "sigue activo o solicita uno nuevo.";
                            } else {
                                mensaje = "HTTP " + response.code() + " al consultar ESIOS.";
                            }
                            cargarFechaComoFallback(fechaEsperada, callback, mensaje);
                            return;
                        }
                        if (response.body() == null) {
                            cargarFechaComoFallback(fechaEsperada, callback,
                                    "ESIOS respondió sin cuerpo de datos.");
                            return;
                        }

                        List<PrecioLuzEntity> precios = convertirRespuestaEnEntidades(response.body());
                        if (precios.isEmpty()) {
                            cargarFechaComoFallback(fechaEsperada, callback,
                                    "ESIOS no devolvió precios de Península.");
                            return;
                        }

                        // La fecha real se toma de la respuesta, no del reloj del dispositivo.
                        String fechaReal = precios.get(0).getFechaHora().substring(0, 10);
                        guardarPreciosEnLocal(precios, fechaReal, callback);
                    }

                    @Override public void onFailure(Call<EsiosResponse> call, Throwable t) {
                        cargarFechaComoFallback(fechaEsperada, callback,
                                "No se pudo conectar con ESIOS: " + t.getMessage());
                    }
                });
    }

    /**
     * El histórico remoto por rango no está disponible con esta API. Se revisa
     * qué días del rango ya están guardados localmente.
     */
    public void descargarRango(String fechaInicio, String fechaFin,
                               EsiosRangeCallback callback) {
        if (!esFechaValida(fechaInicio) || !esFechaValida(fechaFin)) {
            callback.onError("Las fechas deben tener formato yyyy-MM-dd.");
            return;
        }
        executorService.execute(() -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                sdf.setLenient(false);
                Date inicio = sdf.parse(fechaInicio);
                Date fin = sdf.parse(fechaFin);
                if (inicio == null || fin == null || inicio.after(fin)) {
                    callback.onError("La fecha inicial no puede ser posterior a la final.");
                    return;
                }
                long dias = ((fin.getTime() - inicio.getTime()) / 86400000L) + 1L;
                if (dias > 31L) {
                    callback.onError("El rango máximo es de 31 días.");
                    return;
                }
                int disponibles = 0;
                int total = 0;
                List<String> avisos = new ArrayList<>();
                Calendar cal = Calendar.getInstance();
                cal.setTime(inicio);
                while (!cal.getTime().after(fin)) {
                    String fecha = sdf.format(cal.getTime());
                    total++;
                    int cantidad = database.precioLuzDao().contarPorFecha(fecha);
                    if (cantidad > 0) disponibles++;
                    else avisos.add(fecha + ": no está guardado en SQLite");
                    callback.onProgress(total, (int) dias, fecha);
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                }
                callback.onComplete(disponibles, total, avisos);
            } catch (ParseException e) {
                callback.onError("No se pudieron interpretar las fechas.");
            }
        });
    }

    public void cargarResumenLocal(EsiosLocalSummaryCallback callback) {
        executorService.execute(() -> callback.onLoaded(
                database.precioLuzDao().listarFechasDisponibles(),
                database.precioLuzDao().obtenerFechaMasAntigua(),
                database.precioLuzDao().obtenerFechaMasReciente(),
                database.precioLuzDao().contarTodos()));
    }

    public void borrarFechaLocal(String fecha, Runnable onComplete) {
        executorService.execute(() -> {
            database.precioLuzDao().borrarFecha(fecha);
            onComplete.run();
        });
    }

    public void borrarTodoLocal(Runnable onComplete) {
        executorService.execute(() -> {
            database.precioLuzDao().borrarTodos();
            onComplete.run();
        });
    }

    public void cargarPreciosLocales(EsiosRepositoryCallback callback) {
        executorService.execute(() -> {
            String fecha = database.precioLuzDao().obtenerFechaMasReciente();
            List<PrecioLuzEntity> precios = fecha == null
                    ? new ArrayList<>() : database.precioLuzDao().listarPorFechaYZona(fecha, EsiosPreferences.nombreZona(EsiosPreferences.getGeoId(appContext)));
            callback.onSuccess(precios);
        });
    }

    public void cargarPreciosFecha(String fecha, EsiosRepositoryCallback callback) {
        executorService.execute(() -> callback.onSuccess(
                database.precioLuzDao().listarPorFechaYZona(fecha, EsiosPreferences.nombreZona(EsiosPreferences.getGeoId(appContext)))));
    }

    private void cargarFechaComoFallback(String fecha, EsiosRepositoryCallback callback,
                                         String errorRed) {
        if (!EsiosPreferences.usarCache(appContext)) {
            callback.onError(errorRed);
            return;
        }
        executorService.execute(() -> {
            List<PrecioLuzEntity> locales = database.precioLuzDao().listarPorFechaYZona(fecha, EsiosPreferences.nombreZona(EsiosPreferences.getGeoId(appContext)));
            if (!locales.isEmpty()) callback.onCache(locales, errorRed);
            else callback.onError(errorRed + " No existe una copia local para " + fecha + ".");
        });
    }

    private void guardarPreciosEnLocal(List<PrecioLuzEntity> precios, String fecha,
                                       EsiosRepositoryCallback callback) {
        executorService.execute(() -> {
            database.runInTransaction(() -> {
                database.precioLuzDao().insertarTodos(precios);
                database.precioLuzDao().borrarAnterioresA(fechaLimiteHistorico(400));
            });
            callback.onSuccess(database.precioLuzDao().listarPorFechaYZona(fecha, EsiosPreferences.nombreZona(EsiosPreferences.getGeoId(appContext))));
        });
    }

    /** Convierte 120 filas (24 h x 5 zonas) en las 24 horas de Península. */
    private List<PrecioLuzEntity> convertirRespuestaEnEntidades(EsiosResponse response) {
        List<PrecioLuzEntity> lista = new ArrayList<>();
        if (response.getIndicator() == null || response.getIndicator().getValues() == null) {
            return lista;
        }
        long descarga = System.currentTimeMillis();
        for (EsiosValue value : response.getIndicator().getValues()) {
            if (value == null || value.getDatetime() == null) continue;
            if (value.getGeoId() != EsiosPreferences.getGeoId(appContext)) continue;
            double precioMwh = value.getValue();
            lista.add(new PrecioLuzEntity(
                    EsiosConfig.INDICADOR_PVPC_20TD,
                    value.getDatetime(),
                    precioMwh,
                    precioMwh / 1000.0,
                    value.getGeoName(),
                    descarga));
        }
        return lista;
    }

    private String fechaLimiteHistorico(int dias) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
        c.add(Calendar.DAY_OF_YEAR, -dias);
        return formatearFecha(c.getTime()) + "T00:00:00";
    }

    private String formatearFecha(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        return sdf.format(date);
    }

    private boolean esFechaValida(String fecha) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setLenient(false);
            return sdf.parse(fecha) != null;
        } catch (ParseException e) {
            return false;
        }
    }

    public interface EsiosRangeCallback {
        void onProgress(int actual, int total, String fecha);
        void onComplete(int diasDisponibles, int totalSolicitados, List<String> avisos);
        void onError(String mensaje);
    }

    public interface EsiosLocalSummaryCallback {
        void onLoaded(List<String> fechas, String fechaAntigua,
                      String fechaReciente, int totalRegistros);
    }

    public interface EsiosRepositoryCallback {
        void onSuccess(List<PrecioLuzEntity> precios);
        void onError(String mensaje);
        default void onCache(List<PrecioLuzEntity> precios, String aviso) { onSuccess(precios); }
    }
}
