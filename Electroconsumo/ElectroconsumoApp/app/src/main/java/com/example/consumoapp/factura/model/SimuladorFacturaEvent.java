package com.example.consumoapp.factura.model;

import androidx.annotation.NonNull;

/**
 * Acciones puntuales emitidas por el simulador.
 *
 * A diferencia de {@link SimuladorFacturaUiState}, estos valores no representan
 * estado reproducible: deben procesarse una sola vez por la interfaz.
 */
public interface SimuladorFacturaEvent {

    /** Mensaje informativo para el usuario. */
    final class MostrarMensaje implements SimuladorFacturaEvent {
        @NonNull
        public final String mensaje;

        public MostrarMensaje(@NonNull String mensaje) {
            this.mensaje = mensaje;
        }
    }

    /** Error de una operación asíncrona. */
    final class MostrarError implements SimuladorFacturaEvent {
        @NonNull
        public final String mensaje;

        public MostrarError(@NonNull String mensaje) {
            this.mensaje = mensaje;
        }
    }

    /** Confirma que la simulación terminó de guardarse en Room. */
    final class SimulacionGuardada implements SimuladorFacturaEvent {
        @NonNull
        public final String mensaje;

        public SimulacionGuardada() {
            this("Simulación guardada en el historial");
        }

        public SimulacionGuardada(@NonNull String mensaje) {
            this.mensaje = mensaje;
        }
    }
}
