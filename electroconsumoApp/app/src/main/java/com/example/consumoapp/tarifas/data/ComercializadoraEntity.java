package com.example.consumoapp.tarifas.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Empresa comercializadora de electricidad.
 *
 * Solo guardamos su nombre porque el proyecto no necesita logos ni páginas web
 * para calcular una factura.
 */
@Entity(
        tableName = "comercializadora",
        indices = {@Index(value = {"nombre"}, unique = true)}
)
public class ComercializadoraEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String nombre;

    public ComercializadoraEntity(@NonNull String nombre) {
        this.nombre = nombre;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getNombre() {
        return nombre;
    }

    public void setNombre(@NonNull String nombre) {
        this.nombre = nombre;
    }
}
