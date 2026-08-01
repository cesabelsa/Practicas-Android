package com.example.consumoapp.core.factura.engine;

/**
 * Resultado inmutable y detallado del motor de cálculo.
 *
 * Los getters antiguos se conservan para no romper el simulador, el historial
 * ni el comparador. Los nuevos getters exponen el desglose profesional.
 */
public final class ResultadoFactura {

    private final double consumoTotalKwh;
    private final double consumoP1Kwh;
    private final double consumoP2Kwh;
    private final double consumoP3Kwh;
    private final double costeP1;
    private final double costeP2;
    private final double costeP3;
    private final double costeEnergiaMercado;
    private final double costePotenciaBase;
    private final double peajesEnergia;
    private final double peajesPotencia;
    private final double cargosEnergia;
    private final double cargosPotencia;
    private final double ajustesSistema;
    private final double otrosConceptos;
    private final double costeEnergia;
    private final double costePotencia;
    private final double alquilerContador;
    private final double baseImpuestoElectricidad;
    private final double impuestoElectricidad;
    private final double baseIva;
    private final double iva;
    private final double totalFactura;

    public ResultadoFactura(
            double consumoTotalKwh,
            double consumoP1Kwh,
            double consumoP2Kwh,
            double consumoP3Kwh,
            double costeP1,
            double costeP2,
            double costeP3,
            double costeEnergiaMercado,
            double costePotenciaBase,
            double peajesEnergia,
            double peajesPotencia,
            double cargosEnergia,
            double cargosPotencia,
            double ajustesSistema,
            double otrosConceptos,
            double costeEnergia,
            double costePotencia,
            double alquilerContador,
            double baseImpuestoElectricidad,
            double impuestoElectricidad,
            double baseIva,
            double iva,
            double totalFactura
    ) {
        this.consumoTotalKwh = consumoTotalKwh;
        this.consumoP1Kwh = consumoP1Kwh;
        this.consumoP2Kwh = consumoP2Kwh;
        this.consumoP3Kwh = consumoP3Kwh;
        this.costeP1 = costeP1;
        this.costeP2 = costeP2;
        this.costeP3 = costeP3;
        this.costeEnergiaMercado = costeEnergiaMercado;
        this.costePotenciaBase = costePotenciaBase;
        this.peajesEnergia = peajesEnergia;
        this.peajesPotencia = peajesPotencia;
        this.cargosEnergia = cargosEnergia;
        this.cargosPotencia = cargosPotencia;
        this.ajustesSistema = ajustesSistema;
        this.otrosConceptos = otrosConceptos;
        this.costeEnergia = costeEnergia;
        this.costePotencia = costePotencia;
        this.alquilerContador = alquilerContador;
        this.baseImpuestoElectricidad = baseImpuestoElectricidad;
        this.impuestoElectricidad = impuestoElectricidad;
        this.baseIva = baseIva;
        this.iva = iva;
        this.totalFactura = totalFactura;
    }

    public double getConsumoTotalKwh() { return consumoTotalKwh; }
    public double getConsumoP1Kwh() { return consumoP1Kwh; }
    public double getConsumoP2Kwh() { return consumoP2Kwh; }
    public double getConsumoP3Kwh() { return consumoP3Kwh; }
    public double getCosteP1() { return costeP1; }
    public double getCosteP2() { return costeP2; }
    public double getCosteP3() { return costeP3; }
    public double getCosteEnergiaMercado() { return costeEnergiaMercado; }
    public double getCostePotenciaBase() { return costePotenciaBase; }
    public double getPeajesEnergia() { return peajesEnergia; }
    public double getPeajesPotencia() { return peajesPotencia; }
    public double getPeajesTotal() { return peajesEnergia + peajesPotencia; }
    public double getCargosEnergia() { return cargosEnergia; }
    public double getCargosPotencia() { return cargosPotencia; }
    public double getCargosTotal() { return cargosEnergia + cargosPotencia; }
    public double getAjustesSistema() { return ajustesSistema; }
    public double getOtrosConceptos() { return otrosConceptos; }
    public double getCosteEnergia() { return costeEnergia; }
    public double getCostePotencia() { return costePotencia; }
    public double getAlquilerContador() { return alquilerContador; }
    public double getBaseImpuestoElectricidad() { return baseImpuestoElectricidad; }
    public double getImpuestoElectricidad() { return impuestoElectricidad; }
    public double getBaseIva() { return baseIva; }
    public double getIva() { return iva; }
    public double getTotalFactura() { return totalFactura; }
}
