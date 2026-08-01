package com.example.consumoapp.factura.viewmodel;

/**
 * Contenedor para acciones que deben consumirse una sola vez, como mensajes Toast.
 * Evita repetirlas cuando Android recrea la Activity tras una rotación.
 */
public final class Event<T> {
    private final T contenido;
    private boolean consumido;

    public Event(T contenido) {
        this.contenido = contenido;
    }

    /** Devuelve el contenido una sola vez. */
    public synchronized T consumir() {
        if (consumido) {
            return null;
        }
        consumido = true;
        return contenido;
    }
}
