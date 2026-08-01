package com.example.consumoapp.esios.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Preferencias sencillas de ESIOS guardadas en SharedPreferences.
 * No contiene el token ni datos sensibles.
 */
public final class EsiosPreferences {

    private static final String PREFS = "electroconsumo_esios";
    private static final String KEY_GEO_ID = "geo_id";
    private static final String KEY_UNIDAD = "unidad_precio";
    private static final String KEY_ACTUALIZAR_INICIO = "actualizar_inicio";
    private static final String KEY_USAR_CACHE = "usar_cache";

    public static final int GEO_PENINSULA = 8741;
    public static final int GEO_CANARIAS = 8742;
    public static final int GEO_BALEARES = 8743;
    public static final int GEO_CEUTA = 8744;
    public static final int GEO_MELILLA = 8745;

    public static final String UNIDAD_KWH = "kWh";
    public static final String UNIDAD_MWH = "MWh";

    private EsiosPreferences() { }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static int getGeoId(Context context) {
        return prefs(context).getInt(KEY_GEO_ID, GEO_PENINSULA);
    }

    public static void setGeoId(Context context, int geoId) {
        prefs(context).edit().putInt(KEY_GEO_ID, geoId).apply();
    }

    public static String getUnidad(Context context) {
        return prefs(context).getString(KEY_UNIDAD, UNIDAD_KWH);
    }

    public static void setUnidad(Context context, String unidad) {
        prefs(context).edit().putString(KEY_UNIDAD, unidad).apply();
    }

    public static boolean actualizarAlIniciar(Context context) {
        return prefs(context).getBoolean(KEY_ACTUALIZAR_INICIO, true);
    }

    public static void setActualizarAlIniciar(Context context, boolean valor) {
        prefs(context).edit().putBoolean(KEY_ACTUALIZAR_INICIO, valor).apply();
    }

    public static boolean usarCache(Context context) {
        return prefs(context).getBoolean(KEY_USAR_CACHE, true);
    }

    public static void setUsarCache(Context context, boolean valor) {
        prefs(context).edit().putBoolean(KEY_USAR_CACHE, valor).apply();
    }

    public static String nombreZona(int geoId) {
        switch (geoId) {
            case GEO_CANARIAS: return "Canarias";
            case GEO_BALEARES: return "Baleares";
            case GEO_CEUTA: return "Ceuta";
            case GEO_MELILLA: return "Melilla";
            case GEO_PENINSULA:
            default: return "Península";
        }
    }

    public static int geoIdDesdePosicion(int position) {
        switch (position) {
            case 1: return GEO_CANARIAS;
            case 2: return GEO_BALEARES;
            case 3: return GEO_CEUTA;
            case 4: return GEO_MELILLA;
            case 0:
            default: return GEO_PENINSULA;
        }
    }

    public static int posicionDesdeGeoId(int geoId) {
        switch (geoId) {
            case GEO_CANARIAS: return 1;
            case GEO_BALEARES: return 2;
            case GEO_CEUTA: return 3;
            case GEO_MELILLA: return 4;
            case GEO_PENINSULA:
            default: return 0;
        }
    }
}
