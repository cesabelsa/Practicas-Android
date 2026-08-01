package com.example.consumoapp.factura;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Electrodoméstico guardado en SQLite mediante Room.
 *
 * Los datos salen de la hoja 02_Datos_SQLite del Excel verificado.
 * No se inventan potencias: cuando una fuente no publica potencia,
 * potenciaMinW y potenciaMaxW se guardan como null.
 */
@Entity(tableName = "electrodomesticos")
public class ElectrodomesticoEntity {

    @PrimaryKey
    private int id;

    private String categoria;
    private String nombre;
    private Double potenciaMinW;
    private Double potenciaMaxW;
    private String usoPublicado;
    private String consumoPublicado;
    private String unidadConsumo;
    private String fuentes;
    private String estado;

    public ElectrodomesticoEntity(int id, String categoria, String nombre, Double potenciaMinW,
                                  Double potenciaMaxW, String usoPublicado, String consumoPublicado,
                                  String unidadConsumo, String fuentes, String estado) {
        this.id = id;
        this.categoria = categoria;
        this.nombre = nombre;
        this.potenciaMinW = potenciaMinW;
        this.potenciaMaxW = potenciaMaxW;
        this.usoPublicado = usoPublicado;
        this.consumoPublicado = consumoPublicado;
        this.unidadConsumo = unidadConsumo;
        this.fuentes = fuentes;
        this.estado = estado;
    }

    public int getId() { return id; }
    public String getCategoria() { return categoria; }
    public String getNombre() { return nombre; }
    public Double getPotenciaMinW() { return potenciaMinW; }
    public Double getPotenciaMaxW() { return potenciaMaxW; }
    public String getUsoPublicado() { return usoPublicado; }
    public String getConsumoPublicado() { return consumoPublicado; }
    public String getUnidadConsumo() { return unidadConsumo; }
    public String getFuentes() { return fuentes; }
    public String getEstado() { return estado; }

    /**
     * Devuelve la potencia media si existen mínimo y máximo publicados.
     */
    public Double getPotenciaMediaW() {
        if (potenciaMinW == null || potenciaMaxW == null) {
            return null;
        }
        return (potenciaMinW + potenciaMaxW) / 2.0;
    }

    /**
     * El Spinner mostrará directamente el nombre del electrodoméstico.
     */
    @NonNull
    @Override
    public String toString() {
        return nombre;
    }
}
