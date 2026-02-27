package es.layout.aplicacioncompleta;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public final class IntentHelper {

    private IntentHelper() {}

    /**
     * Abre una URL usando un intent implícito y un chooser.
     * Si no hay ninguna aplicación capaz de gestionarlo, muestra un Toast.
     */
    public static void openUrlSafe(Context ctx, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            Intent chooser = Intent.createChooser(intent, "Abrir con...");
            ctx.startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ctx, "No hay app para abrir enlaces", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Versión "en app" eliminada para no depender de WebActivity.
     * Delegamos en openUrlSafe para mantener la compatibilidad.
     */
    public static void openUrlInApp(Context ctx, String url) {
        openUrlSafe(ctx, url);
    }
}
