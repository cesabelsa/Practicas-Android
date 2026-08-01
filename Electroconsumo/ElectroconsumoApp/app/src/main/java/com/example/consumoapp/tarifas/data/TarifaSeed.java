package com.example.consumoapp.tarifas.data;

import android.util.Log;

import android.content.Context;

import com.example.consumoapp.esios.data.AppDatabase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Importa una sola vez el archivo assets/comercializadoras.csv a Room.
 */
public final class TarifaSeed {

    private static final String TAG = "TarifaSeed";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private TarifaSeed() {
        // Evita crear objetos de esta clase de utilidad.
    }

    public static void cargarSiEstaVacio(Context context, AppDatabase database) {
        EXECUTOR.execute(() -> {
            TarifaDao dao = database.tarifaDao();

            // Si ya hay datos, no volvemos a importarlos.
            if (dao.contarTarifas() > 0) {
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            context.getAssets().open("comercializadoras.csv"),
                            StandardCharsets.UTF_8
                    )
            )) {
                // La primera línea contiene los nombres de las columnas.
                reader.readLine();

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    List<String> columns = parseCsvLine(line);
                    if (columns.size() < 14) {
                        continue;
                    }

                    String nombreTarifa = columns.get(0).trim();
                    String nombreComercializadora = columns.get(1).trim();

                    Long comercializadoraId = dao.buscarIdComercializadora(nombreComercializadora);
                    if (comercializadoraId == null) {
                        long nuevoId = dao.insertarComercializadora(
                                new ComercializadoraEntity(nombreComercializadora)
                        );

                        comercializadoraId = nuevoId > 0
                                ? nuevoId
                                : dao.buscarIdComercializadora(nombreComercializadora);
                    }

                    if (comercializadoraId == null) {
                        continue;
                    }

                    TarifaComercialEntity tarifa = new TarifaComercialEntity(
                            comercializadoraId,
                            nombreTarifa
                    );

                    tarifa.setFechaActualizacion(emptyToNull(columns.get(2)));
                    tarifa.setFuente(emptyToNull(columns.get(3)));
                    tarifa.setPrecioP1(parseNullableDouble(columns.get(4)));
                    tarifa.setPrecioP2(parseNullableDouble(columns.get(5)));
                    tarifa.setPrecioP3(parseNullableDouble(columns.get(6)));
                    tarifa.setPotenciaP1(parseNullableDouble(columns.get(7)));
                    tarifa.setPotenciaP2(parseNullableDouble(columns.get(8)));
                    tarifa.setAlquiler(parseNullableDouble(columns.get(9)));
                    tarifa.setPermanencia(emptyToNull(columns.get(10)));
                    tarifa.setDescuento(emptyToNull(columns.get(11)));
                    tarifa.setServicios(emptyToNull(columns.get(12)));
                    tarifa.setObservaciones(emptyToNull(columns.get(13)));
                    tarifa.setActiva(true);

                    dao.insertarTarifa(tarifa);
                }
            } catch (Exception exception) {
                // Registramos el error en Logcat sin interrumpir el arranque.
                Log.e(TAG, "No se pudieron cargar las tarifas iniciales", exception);
            }
        });
    }

    /**
     * Lee una línea CSV respetando textos entre comillas y comillas escapadas.
     */
    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);

            if (character == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (character == ',' && !insideQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        result.add(current.toString());
        return result;
    }

    private static Double parseNullableDouble(String value) {
        String normalized = value == null ? "" : value.trim().replace(',', '.');
        if (normalized.isEmpty()) {
            return null;
        }

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
