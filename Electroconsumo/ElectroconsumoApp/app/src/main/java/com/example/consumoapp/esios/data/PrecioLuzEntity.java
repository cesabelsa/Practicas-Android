package com.example.consumoapp.esios.data;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Precio horario descargado desde ESIOS y conservado como histórico local.
 *
 * El valor original se guarda en €/MWh y también convertido a €/kWh para
 * evitar conversiones repetidas en el simulador.
 */
@Entity(
        tableName = "precios_luz",
        indices = {
                @Index(value = {"indicadorId", "fechaHora", "zona"}, unique = true),
                @Index(value = {"fechaHora"})
        }
)
public class PrecioLuzEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int indicadorId;
    private String fechaHora;
    private double precio;
    private double precioKwh;
    private String zona;
    private long fechaDescarga;

    public PrecioLuzEntity(int indicadorId, String fechaHora, double precio,
                           double precioKwh, String zona, long fechaDescarga) {
        this.indicadorId = indicadorId;
        this.fechaHora = fechaHora;
        this.precio = precio;
        this.precioKwh = precioKwh;
        this.zona = zona == null ? "" : zona;
        this.fechaDescarga = fechaDescarga;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIndicadorId() { return indicadorId; }
    public String getFechaHora() { return fechaHora; }
    public double getPrecio() { return precio; }
    public double getPrecioKwh() { return precioKwh; }
    public String getZona() { return zona; }
    public long getFechaDescarga() { return fechaDescarga; }
}
