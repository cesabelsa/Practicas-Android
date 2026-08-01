package com.example.consumoapp.esios.network;

import com.example.consumoapp.BuildConfig;

/**
 * Configuración centralizada de la API de ESIOS.
 *
 * Para trabajar en local:
 * 1. Abre el archivo local.properties de la raíz del proyecto.
 * 2. Añade: ESIOS_API_KEY=tu_token_personal
 * 3. Sincroniza Gradle y vuelve a ejecutar la app.
 *
 * Nota importante:
 * - No subas el token a GitHub.
 * - Si el token se compartió en un ZIP, chat o repositorio, puede quedar comprometido.
 * - Si ESIOS devuelve 403 incluso con cabeceras correctas, solicita un token nuevo.
 */
public final class EsiosConfig {

    // URL base oficial de la API e·sios.
    public static final String BASE_URL = "https://api.esios.ree.es/";

    // Cabecera indicada por la documentación oficial para la API v1.
    public static final String ACCEPT_HEADER = "application/json; application/vnd.esios-api-v1+json";

    // Token leído desde local.properties mediante BuildConfig.
    // Se normaliza para evitar un error típico: pegar el token con comillas,
    // con "Token token=..." o con espacios/saltos de línea.
    public static final String API_KEY = normalizarToken(BuildConfig.ESIOS_API_KEY);

    /**
     * Comprueba el formato habitual del token de ESIOS.
     *
     * El token personal suele tener 64 caracteres hexadecimales. Esta validación
     * no garantiza que el servidor lo considere activo, pero detecta tokens
     * vacíos, incompletos o pegados con un formato incorrecto.
     */
    public static boolean tieneFormatoTokenValido() {
        return API_KEY.matches("^[0-9a-fA-F]{64}$");
    }

    // Indicador PVPC 2.0TD usado para precios regulados por horas.
    public static final int INDICADOR_PVPC_20TD = 1001;

    // Geo ID usado por ESIOS para Península.
    public static final int GEO_ID_PENINSULA = 8741;

    private EsiosConfig() {
        // Constructor privado para evitar instanciar esta clase de constantes.
    }

    /**
     * Limpia el token antes de enviarlo a ESIOS.
     *
     * Ejemplos que corrige:
     * - "abc123..."  -> abc123...
     * - 'abc123...'  -> abc123...
     * - Token token="abc123..." -> abc123...
     * - Bearer abc123... -> abc123...
     */
    private static String normalizarToken(String tokenOriginal) {
        if (tokenOriginal == null) {
            return "";
        }

        String token = tokenOriginal.trim();

        // Si el usuario pegó una cabecera completa antigua, nos quedamos solo con el valor.
        token = token.replace("Authorization:", "").trim();
        token = token.replace("Token token=", "").trim();
        token = token.replace("Bearer", "").trim();

        // Quitamos comillas exteriores si existen.
        while ((token.startsWith("\"") && token.endsWith("\""))
                || (token.startsWith("'") && token.endsWith("'"))) {
            token = token.substring(1, token.length() - 1).trim();
        }

        // Quitamos comillas que hayan quedado dentro al pegar token="...".
        token = token.replace("\"", "").replace("'", "").trim();

        return token;
    }
}
