package es.layout.aplicacioncompleta;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Clase auxiliar para leer archivos JSON guardados en la carpeta assets.
 *
 * ¿Por qué la usamos?
 * La URL del mock de Postman puede fallar desde el emulador o desde algunos móviles.
 * Si Retrofit no puede conectarse, usamos estos JSON locales como respaldo para que
 * la práctica siga funcionando y se pueda ver el Login y el RecyclerView de hoteles.
 */
public class JsonAssetReader {

    /**
     * Lee un archivo JSON desde assets y lo convierte en un objeto Java.
     *
     * @param context contexto de Android para acceder a assets
     * @param fileName nombre del archivo dentro de app/src/main/assets
     * @param clazz clase Java a la que queremos convertir el JSON
     * @param <T> tipo genérico del objeto que se devuelve
     * @return objeto Java creado a partir del JSON
     * @throws IOException si el archivo no existe o no se puede leer
     */
    public static <T> T readJsonFromAssets(Context context, String fileName, Class<T> clazz) throws IOException {
        // Abrimos el archivo que está dentro de la carpeta assets.
        InputStream inputStream = context.getAssets().open(fileName);

        // StringBuilder nos permite construir el texto completo del JSON línea a línea.
        StringBuilder jsonBuilder = new StringBuilder();

        // BufferedReader lee el archivo de forma cómoda línea por línea.
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;

        try {
            // Mientras existan líneas, las añadimos al StringBuilder.
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        } finally {
            // Cerramos recursos para evitar fugas de memoria.
            reader.close();
            inputStream.close();
        }

        // Gson transforma el texto JSON en un objeto Java del tipo indicado.
        return new Gson().fromJson(jsonBuilder.toString(), clazz);
    }

    // Constructor privado porque esta clase solo tiene métodos estáticos.
    private JsonAssetReader() {
    }
}
