package com.example.consumoapp;

import android.content.Context;
import android.content.SharedPreferences;

/** Guarda únicamente datos de sesión no sensibles. */
public final class SessionManager {
    private static final String PREFS_NAME = "sesion_usuario";
    private static final String KEY_LOGUEADO = "logueado";
    private static final String KEY_USUARIO_ID = "usuario_id";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_EMAIL = "email";

    private SessionManager() {}

    public static void guardarSesion(Context context, long usuarioId, String nombre, String email) {
        String nombreSeguro = limpiarTexto(nombre);
        if (nombreSeguro.isEmpty()) nombreSeguro = "Usuario";
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_LOGUEADO, true)
                .putLong(KEY_USUARIO_ID, usuarioId)
                .putString(KEY_NOMBRE, nombreSeguro)
                .putString(KEY_EMAIL, limpiarTexto(email))
                .apply();
    }

    /** Compatibilidad temporal con llamadas antiguas. */
    public static void guardarSesion(Context context, String nombre, String email) {
        guardarSesion(context, 0L, nombre, email);
    }

    public static boolean estaLogueado(Context context) {
        return prefs(context).getBoolean(KEY_LOGUEADO, false)
                && prefs(context).getLong(KEY_USUARIO_ID, 0L) > 0L;
    }

    public static long obtenerUsuarioId(Context context) {
        return prefs(context).getLong(KEY_USUARIO_ID, 0L);
    }

    public static String obtenerNombre(Context context) {
        return prefs(context).getString(KEY_NOMBRE, "Usuario");
    }

    public static String obtenerEmail(Context context) {
        return prefs(context).getString(KEY_EMAIL, "");
    }

    public static String obtenerInicial(Context context) {
        String nombre = obtenerNombre(context);
        return nombre == null || nombre.trim().isEmpty()
                ? "U" : nombre.trim().substring(0, 1).toUpperCase();
    }

    public static void cerrarSesion(Context context) {
        prefs(context).edit().clear().apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static String limpiarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
