package es.layout.aplicacioncompleta;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public final class IntentHelper {
    private IntentHelper(){}
    public static void openUrlSafe(Context ctx, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            Intent chooser = Intent.createChooser(intent, "Abrir con...");
            ctx.startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ctx, "No hay app para abrir enlaces", Toast.LENGTH_SHORT).show();
        }
    }
    public static void openUrlInApp(Context ctx, String url) {
        try {
            Intent intent = new Intent(ctx, WebActivity.class);
            intent.putExtra(WebActivity.EXTRA_URL, url);
            ctx.startActivity(intent);
        } catch (Exception e) {
            openUrlSafe(ctx, url);
        }
    }
}
