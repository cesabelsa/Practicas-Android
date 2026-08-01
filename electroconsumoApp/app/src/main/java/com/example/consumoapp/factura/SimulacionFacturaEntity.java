package com.example.consumoapp.factura;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Guarda una fotografía completa del resultado de una simulación.
 *
 * No depende de que la tarifa siga existiendo en el CSV: conserva el nombre,
 * la fuente y todos los importes utilizados en el momento de guardarla.
 */
@Entity(tableName = "simulacion_factura", indices = @Index("usuarioId"))
public class SimulacionFacturaEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long usuarioId;

    private long fechaCreacion;
    private String nombre;
    private String fuentePrecio;
    private String comercializadora;
    private String tarifa;

    private double consumoTotalKwh;
    private double consumoP1Kwh;
    private double consumoP2Kwh;
    private double consumoP3Kwh;

    private double costeP1;
    private double costeP2;
    private double costeP3;
    private double costeEnergia;
    private double costePotencia;
    private double alquilerContador;
    private double impuestoElectricidad;
    private double iva;
    private double totalFactura;

    // Desglose profesional persistido por la Fase 15.1.4.
    private double costeEnergiaMercado;
    private double costePotenciaBase;
    private double peajesEnergia;
    private double peajesPotencia;
    private double cargosEnergia;
    private double cargosPotencia;
    private double ajustesSistema;
    private double otrosConceptos;
    private double baseImpuestoElectricidad;
    private double baseIva;
    private boolean reguladosSeparados;
    private String fuenteConstantes;

    private double potenciaPuntaKw;
    private double potenciaValleKw;
    private int diasFactura;
    private int numeroElectrodomesticos;

    public long getId() { return id; }
    public long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(long usuarioId) { this.usuarioId = usuarioId; }
    public void setId(long id) { this.id = id; }
    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getFuentePrecio() { return fuentePrecio; }
    public void setFuentePrecio(String fuentePrecio) { this.fuentePrecio = fuentePrecio; }
    public String getComercializadora() { return comercializadora; }
    public void setComercializadora(String comercializadora) { this.comercializadora = comercializadora; }
    public String getTarifa() { return tarifa; }
    public void setTarifa(String tarifa) { this.tarifa = tarifa; }
    public double getConsumoTotalKwh() { return consumoTotalKwh; }
    public void setConsumoTotalKwh(double consumoTotalKwh) { this.consumoTotalKwh = consumoTotalKwh; }
    public double getConsumoP1Kwh() { return consumoP1Kwh; }
    public void setConsumoP1Kwh(double consumoP1Kwh) { this.consumoP1Kwh = consumoP1Kwh; }
    public double getConsumoP2Kwh() { return consumoP2Kwh; }
    public void setConsumoP2Kwh(double consumoP2Kwh) { this.consumoP2Kwh = consumoP2Kwh; }
    public double getConsumoP3Kwh() { return consumoP3Kwh; }
    public void setConsumoP3Kwh(double consumoP3Kwh) { this.consumoP3Kwh = consumoP3Kwh; }
    public double getCosteP1() { return costeP1; }
    public void setCosteP1(double costeP1) { this.costeP1 = costeP1; }
    public double getCosteP2() { return costeP2; }
    public void setCosteP2(double costeP2) { this.costeP2 = costeP2; }
    public double getCosteP3() { return costeP3; }
    public void setCosteP3(double costeP3) { this.costeP3 = costeP3; }
    public double getCosteEnergia() { return costeEnergia; }
    public void setCosteEnergia(double costeEnergia) { this.costeEnergia = costeEnergia; }
    public double getCostePotencia() { return costePotencia; }
    public void setCostePotencia(double costePotencia) { this.costePotencia = costePotencia; }
    public double getAlquilerContador() { return alquilerContador; }
    public void setAlquilerContador(double alquilerContador) { this.alquilerContador = alquilerContador; }
    public double getImpuestoElectricidad() { return impuestoElectricidad; }
    public void setImpuestoElectricidad(double impuestoElectricidad) { this.impuestoElectricidad = impuestoElectricidad; }
    public double getIva() { return iva; }
    public void setIva(double iva) { this.iva = iva; }
    public double getTotalFactura() { return totalFactura; }
    public void setTotalFactura(double totalFactura) { this.totalFactura = totalFactura; }

    public double getCosteEnergiaMercado() { return costeEnergiaMercado; }
    public void setCosteEnergiaMercado(double costeEnergiaMercado) { this.costeEnergiaMercado = costeEnergiaMercado; }
    public double getCostePotenciaBase() { return costePotenciaBase; }
    public void setCostePotenciaBase(double costePotenciaBase) { this.costePotenciaBase = costePotenciaBase; }
    public double getPeajesEnergia() { return peajesEnergia; }
    public void setPeajesEnergia(double peajesEnergia) { this.peajesEnergia = peajesEnergia; }
    public double getPeajesPotencia() { return peajesPotencia; }
    public void setPeajesPotencia(double peajesPotencia) { this.peajesPotencia = peajesPotencia; }
    public double getPeajesTotal() { return peajesEnergia + peajesPotencia; }
    public double getCargosEnergia() { return cargosEnergia; }
    public void setCargosEnergia(double cargosEnergia) { this.cargosEnergia = cargosEnergia; }
    public double getCargosPotencia() { return cargosPotencia; }
    public void setCargosPotencia(double cargosPotencia) { this.cargosPotencia = cargosPotencia; }
    public double getCargosTotal() { return cargosEnergia + cargosPotencia; }
    public double getAjustesSistema() { return ajustesSistema; }
    public void setAjustesSistema(double ajustesSistema) { this.ajustesSistema = ajustesSistema; }
    public double getOtrosConceptos() { return otrosConceptos; }
    public void setOtrosConceptos(double otrosConceptos) { this.otrosConceptos = otrosConceptos; }
    public double getBaseImpuestoElectricidad() { return baseImpuestoElectricidad; }
    public void setBaseImpuestoElectricidad(double baseImpuestoElectricidad) { this.baseImpuestoElectricidad = baseImpuestoElectricidad; }
    public double getBaseIva() { return baseIva; }
    public void setBaseIva(double baseIva) { this.baseIva = baseIva; }
    public boolean isReguladosSeparados() { return reguladosSeparados; }
    public void setReguladosSeparados(boolean reguladosSeparados) { this.reguladosSeparados = reguladosSeparados; }
    public String getFuenteConstantes() { return fuenteConstantes; }
    public void setFuenteConstantes(String fuenteConstantes) { this.fuenteConstantes = fuenteConstantes; }
    public double getPotenciaPuntaKw() { return potenciaPuntaKw; }
    public void setPotenciaPuntaKw(double potenciaPuntaKw) { this.potenciaPuntaKw = potenciaPuntaKw; }
    public double getPotenciaValleKw() { return potenciaValleKw; }
    public void setPotenciaValleKw(double potenciaValleKw) { this.potenciaValleKw = potenciaValleKw; }
    public int getDiasFactura() { return diasFactura; }
    public void setDiasFactura(int diasFactura) { this.diasFactura = diasFactura; }
    public int getNumeroElectrodomesticos() { return numeroElectrodomesticos; }
    public void setNumeroElectrodomesticos(int numeroElectrodomesticos) { this.numeroElectrodomesticos = numeroElectrodomesticos; }
}
