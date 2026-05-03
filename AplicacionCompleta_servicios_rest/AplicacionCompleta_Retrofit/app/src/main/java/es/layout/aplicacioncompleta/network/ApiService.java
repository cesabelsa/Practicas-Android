package es.layout.aplicacioncompleta.network;

import es.layout.aplicacioncompleta.model.retrofit.HotelListResponse;
import es.layout.aplicacioncompleta.model.retrofit.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/*
 * Interfaz de Retrofit.
 * Aquí se indican los endpoints que pide la actividad:
 * 1) POST login
 * 2) GET listHotels
 */
public interface ApiService {

    /*
     * Envía usuario y password al servidor para validar el acceso.
     *
     * Importante:
     * Usamos @FormUrlEncoded + @Field porque el enunciado habla de "parámetros"
     * de entrada para el login. Así Retrofit enviará:
     * usuario=...&password=...
     *
     * Esto suele coincidir mejor con los mocks de Postman creados para formularios.
     */
    @FormUrlEncoded
    @POST("login")
    Call<LoginResponse> login(
            @Field("usuario") String usuario,
            @Field("password") String password
    );

    // Obtiene la lista de hoteles. No necesita parámetros.
    @GET("listHotels")
    Call<HotelListResponse> listHotels();
}
