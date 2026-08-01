package com.example.consumoapp.tarifas.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Tarifa eléctrica del mercado libre.
 *
 * Los precios de energía y potencia se guardan como números para que el motor
 * de cálculo pueda utilizarlos directamente.
 */
@Entity(
        tableName = "tarifa_comercial",
        foreignKeys = @ForeignKey(
                entity = ComercializadoraEntity.class,
                parentColumns = "id",
                childColumns = "comercializadoraId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("comercializadoraId"),
                @Index(value = {"comercializadoraId", "nombre"}, unique = true)
        }
)
public class TarifaComercialEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long comercializadoraId;

    @NonNull
    private String nombre;

    @Nullable
    private String fechaActualizacion;

    @Nullable
    private String fuente;

    @Nullable
    private Double precioP1;

    @Nullable
    private Double precioP2;

    @Nullable
    private Double precioP3;

    @Nullable
    private Double potenciaP1;

    @Nullable
    private Double potenciaP2;

    @Nullable
    private Double alquiler;

    @Nullable
    private String permanencia;

    @Nullable
    private String descuento;

    @Nullable
    private String servicios;

    @Nullable
    private String observaciones;

    private boolean activa;

    public TarifaComercialEntity(long comercializadoraId, @NonNull String nombre) {
        this.comercializadoraId = comercializadoraId;
        this.nombre = nombre;
        this.activa = true;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getComercializadoraId() { return comercializadoraId; }
    public void setComercializadoraId(long comercializadoraId) { this.comercializadoraId = comercializadoraId; }
    @NonNull public String getNombre() { return nombre; }
    public void setNombre(@NonNull String nombre) { this.nombre = nombre; }
    @Nullable public String getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(@Nullable String fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    @Nullable public String getFuente() { return fuente; }
    public void setFuente(@Nullable String fuente) { this.fuente = fuente; }
    @Nullable public Double getPrecioP1() { return precioP1; }
    public void setPrecioP1(@Nullable Double precioP1) { this.precioP1 = precioP1; }
    @Nullable public Double getPrecioP2() { return precioP2; }
    public void setPrecioP2(@Nullable Double precioP2) { this.precioP2 = precioP2; }
    @Nullable public Double getPrecioP3() { return precioP3; }
    public void setPrecioP3(@Nullable Double precioP3) { this.precioP3 = precioP3; }
    @Nullable public Double getPotenciaP1() { return potenciaP1; }
    public void setPotenciaP1(@Nullable Double potenciaP1) { this.potenciaP1 = potenciaP1; }
    @Nullable public Double getPotenciaP2() { return potenciaP2; }
    public void setPotenciaP2(@Nullable Double potenciaP2) { this.potenciaP2 = potenciaP2; }
    @Nullable public Double getAlquiler() { return alquiler; }
    public void setAlquiler(@Nullable Double alquiler) { this.alquiler = alquiler; }
    @Nullable public String getPermanencia() { return permanencia; }
    public void setPermanencia(@Nullable String permanencia) { this.permanencia = permanencia; }
    @Nullable public String getDescuento() { return descuento; }
    public void setDescuento(@Nullable String descuento) { this.descuento = descuento; }
    @Nullable public String getServicios() { return servicios; }
    public void setServicios(@Nullable String servicios) { this.servicios = servicios; }
    @Nullable public String getObservaciones() { return observaciones; }
    public void setObservaciones(@Nullable String observaciones) { this.observaciones = observaciones; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
