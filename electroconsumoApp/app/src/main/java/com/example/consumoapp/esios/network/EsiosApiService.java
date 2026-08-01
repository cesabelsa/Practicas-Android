package com.example.consumoapp.esios.network;

import com.example.consumoapp.esios.model.EsiosResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/** Contrato Retrofit de la API de ESIOS. */
public interface EsiosApiService {

    /**
     * Obtiene el indicador publicado actualmente.
     *
     * ESIOS devuelve HTTP 403 cuando se añaden filtros temporales al indicador
     * 1001 con este acceso. Sin filtros devuelve las 24 horas y las cinco zonas.
     */
    @GET("indicators/{indicatorId}")
    Call<EsiosResponse> getIndicadorActual(
            @Path("indicatorId") int indicatorId
    );
}
