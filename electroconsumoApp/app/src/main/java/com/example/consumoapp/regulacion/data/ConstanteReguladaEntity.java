package com.example.consumoapp.regulacion.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Valor regulado con periodo de vigencia.
 *
 * Los importes regulados no se dejan escritos permanentemente en la Activity:
 * se consultan por fecha para poder conservar históricos cuando cambie la norma.
 */
@Entity(
        tableName = "constante_regulada",
        indices = {
                @Index(value = {"codigo", "periodo", "fechaDesde"}, unique = true),
                @Index(value = {"fechaDesde", "fechaHasta"})
        }
)
public class ConstanteReguladaEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String codigo;

    @NonNull
    private String periodo;

    private double valor;

    @NonNull
    private String unidad;

    @NonNull
    private String fechaDesde;

    private String fechaHasta;

    @NonNull
    private String fuente;

    private boolean activa;

    public ConstanteReguladaEntity(
            @NonNull String codigo,
            @NonNull String periodo,
            double valor,
            @NonNull String unidad,
            @NonNull String fechaDesde,
            String fechaHasta,
            @NonNull String fuente,
            boolean activa
    ) {
        this.codigo = codigo;
        this.periodo = periodo;
        this.valor = valor;
        this.unidad = unidad;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.fuente = fuente;
        this.activa = activa;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    @NonNull public String getCodigo() { return codigo; }
    public void setCodigo(@NonNull String codigo) { this.codigo = codigo; }
    @NonNull public String getPeriodo() { return periodo; }
    public void setPeriodo(@NonNull String periodo) { this.periodo = periodo; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    @NonNull public String getUnidad() { return unidad; }
    public void setUnidad(@NonNull String unidad) { this.unidad = unidad; }
    @NonNull public String getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(@NonNull String fechaDesde) { this.fechaDesde = fechaDesde; }
    public String getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(String fechaHasta) { this.fechaHasta = fechaHasta; }
    @NonNull public String getFuente() { return fuente; }
    public void setFuente(@NonNull String fuente) { this.fuente = fuente; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
