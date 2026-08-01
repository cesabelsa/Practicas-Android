package com.example.consumoapp.esios.network;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Cliente Retrofit para la API de ESIOS. */
public final class EsiosApiClient {

    private static final String TAG_RED = "ESIOS_RED";
    private static volatile Retrofit retrofit;

    private EsiosApiClient() {}

    public static EsiosApiService getService() {
        if (retrofit == null) {
            synchronized (EsiosApiClient.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
                    logging.redactHeader("x-api-key");

                    EventListener listener = new EventListener() {
                        @Override public void connectionAcquired(Call call, Connection connection) {
                            Log.d(TAG_RED, "Protocolo: " + connection.protocol());
                        }

                        @Override public void callFailed(Call call, IOException ioe) {
                            Log.e(TAG_RED, "Fallo: " + ioe.getClass().getSimpleName()
                                    + " - " + ioe.getMessage(), ioe);
                        }
                    };

                    OkHttpClient client = new OkHttpClient.Builder()
                            // OkHttp negocia HTTP/2 o HTTP/1.1 automáticamente.
                            .retryOnConnectionFailure(true)
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(90, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .callTimeout(120, TimeUnit.SECONDS)
                            .eventListener(listener)
                            .addInterceptor(chain -> {
                                Request original = chain.request();
                                Request request = original.newBuilder()
                                        .header("Accept", EsiosConfig.ACCEPT_HEADER)
                                        // Cabecera indicada en la documentación oficial de ESIOS.
                                        .header("Content-Type", "application/json")
                                        .header("User-Agent", "Electroconsumo-Android/1.0")
                                        .header("x-api-key", EsiosConfig.API_KEY)
                                        .method(original.method(), original.body())
                                        .build();
                                return chain.proceed(request);
                            })
                            .addInterceptor(logging)
                            .build();

                    Log.d(TAG_RED, "Token configurado: " + !EsiosConfig.API_KEY.isEmpty()
                            + " | formato válido: " + EsiosConfig.tieneFormatoTokenValido()
                            + " | longitud: " + EsiosConfig.API_KEY.length());

                    retrofit = new Retrofit.Builder()
                            .baseUrl(EsiosConfig.BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit.create(EsiosApiService.class);
    }
}
