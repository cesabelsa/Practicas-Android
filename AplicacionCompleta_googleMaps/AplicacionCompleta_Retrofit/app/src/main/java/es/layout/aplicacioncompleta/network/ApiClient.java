package es.layout.aplicacioncompleta.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/*
 * Clase encargada de crear y configurar Retrofit.
 * Así evitamos repetir la configuración en cada Activity.
 */
public class ApiClient {

    // Host indicado en el PDF de la actividad.
    public static final String BASE_URL = "https://01394d44-8918-4a1d-8059-629c50c25e87.mock.pstmn.io/";

    // Objeto Retrofit único para toda la aplicación.
    private static Retrofit retrofit;

    // Constructor privado para impedir crear objetos de esta clase.
    private ApiClient() {
    }

    public static ApiService getApiService() {

        // Si Retrofit todavía no existe, lo creamos.
        if (retrofit == null) {

            // Interceptor para ver en Logcat las peticiones y respuestas HTTP.
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Cliente HTTP usado internamente por Retrofit.
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            // Construcción final de Retrofit.
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        // Retrofit crea una implementación real de nuestra interfaz ApiService.
        return retrofit.create(ApiService.class);
    }
}
