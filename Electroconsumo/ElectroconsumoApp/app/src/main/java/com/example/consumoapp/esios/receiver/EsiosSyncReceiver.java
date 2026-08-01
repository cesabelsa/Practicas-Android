package com.example.consumoapp.esios.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * BroadcastReceiver de ejemplo para ESIOS.
 *
 * Recibe una acción interna de la app y podría lanzar una sincronización.
 * En una app profesional se combinaría con WorkManager.
 */
public class EsiosSyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String origen = intent.getStringExtra("origen");

        if (origen == null) {
            origen = "Toolbar";
        }

        Toast.makeText(context, "Receiver ESIOS activado desde: " + origen, Toast.LENGTH_SHORT).show();
    }
}
