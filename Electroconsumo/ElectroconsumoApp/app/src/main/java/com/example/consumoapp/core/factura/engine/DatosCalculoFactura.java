package com.example.consumoapp.core.factura.engine;

/**
 * Datos de entrada necesarios para calcular una factura eléctrica.
 *
 * La clase no depende de Android ni de Room. El constructor corto mantiene
 * compatibilidad con el simulador y el comparador actuales. El constructor
 * ampliado permite separar peajes, cargos, ajustes y otros conceptos.
 */
public final class DatosCalculoFactura {

    private final double consumoP1Kwh;
    private final double consumoP2Kwh;
    private final double consumoP3Kwh;
    private final double costeEnergiaP1;
    private final double costeEnergiaP2;
    private final double costeEnergiaP3;
    private final double potenciaPuntaKw;
    private final double potenciaValleKw;
    private final double precioPotenciaPuntaDia;
    private final double precioPotenciaValleDia;
    private final int diasFactura;
    private final double alquilerContadorDia;
    private final double impuestoElectricidadPorcentaje;
    private final double ivaPorcentaje;
    private final DesgloseRegulado desgloseRegulado;
    private final double ajustesSistema;
    private final double otrosConceptos;

    /**
     * Constructor compatible con las fases anteriores.
     * Los precios recibidos ya pueden contener peajes y cargos; por eso el
     * desglose regulado adicional se inicia a cero y no duplica importes.
     */
    public DatosCalculoFactura(
            double consumoP1Kwh,
            double consumoP2Kwh,
            double consumoP3Kwh,
            double costeEnergiaP1,
            double costeEnergiaP2,
            double costeEnergiaP3,
            double potenciaPuntaKw,
            double potenciaValleKw,
            double precioPotenciaPuntaDia,
            double precioPotenciaValleDia,
            int diasFactura,
            double alquilerContadorDia,
            double impuestoElectricidadPorcentaje,
            double ivaPorcentaje
    ) {
        this(
                consumoP1Kwh, consumoP2Kwh, consumoP3Kwh,
                costeEnergiaP1, costeEnergiaP2, costeEnergiaP3,
                potenciaPuntaKw, potenciaValleKw,
                precioPotenciaPuntaDia, precioPotenciaValleDia,
                diasFactura, alquilerContadorDia,
                impuestoElectricidadPorcentaje, ivaPorcentaje,
                DesgloseRegulado.SIN_DESGLOSE, 0, 0
        );
    }

    /** Constructor profesional con desglose regulado y conceptos adicionales. */
    public DatosCalculoFactura(
            double consumoP1Kwh,
            double consumoP2Kwh,
            double consumoP3Kwh,
            double costeEnergiaP1,
            double costeEnergiaP2,
            double costeEnergiaP3,
            double potenciaPuntaKw,
            double potenciaValleKw,
            double precioPotenciaPuntaDia,
            double precioPotenciaValleDia,
            int diasFactura,
            double alquilerContadorDia,
            double impuestoElectricidadPorcentaje,
            double ivaPorcentaje,
            DesgloseRegulado desgloseRegulado,
            double ajustesSistema,
            double otrosConceptos
    ) {
        this.consumoP1Kwh = consumoP1Kwh;
        this.consumoP2Kwh = consumoP2Kwh;
        this.consumoP3Kwh = consumoP3Kwh;
        this.costeEnergiaP1 = costeEnergiaP1;
        this.costeEnergiaP2 = costeEnergiaP2;
        this.costeEnergiaP3 = costeEnergiaP3;
        this.potenciaPuntaKw = potenciaPuntaKw;
        this.potenciaValleKw = potenciaValleKw;
        this.precioPotenciaPuntaDia = precioPotenciaPuntaDia;
        this.precioPotenciaValleDia = precioPotenciaValleDia;
        this.diasFactura = diasFactura;
        this.alquilerContadorDia = alquilerContadorDia;
        this.impuestoElectricidadPorcentaje = impuestoElectricidadPorcentaje;
        this.ivaPorcentaje = ivaPorcentaje;
        this.desgloseRegulado = desgloseRegulado == null
                ? DesgloseRegulado.SIN_DESGLOSE
                : desgloseRegulado;
        this.ajustesSistema = ajustesSistema;
        this.otrosConceptos = otrosConceptos;
    }

    public double getConsumoP1Kwh() { return consumoP1Kwh; }
    public double getConsumoP2Kwh() { return consumoP2Kwh; }
    public double getConsumoP3Kwh() { return consumoP3Kwh; }
    public double getCosteEnergiaP1() { return costeEnergiaP1; }
    public double getCosteEnergiaP2() { return costeEnergiaP2; }
    public double getCosteEnergiaP3() { return costeEnergiaP3; }
    public double getPotenciaPuntaKw() { return potenciaPuntaKw; }
    public double getPotenciaValleKw() { return potenciaValleKw; }
    public double getPrecioPotenciaPuntaDia() { return precioPotenciaPuntaDia; }
    public double getPrecioPotenciaValleDia() { return precioPotenciaValleDia; }
    public int getDiasFactura() { return diasFactura; }
    public double getAlquilerContadorDia() { return alquilerContadorDia; }
    public double getImpuestoElectricidadPorcentaje() { return impuestoElectricidadPorcentaje; }
    public double getIvaPorcentaje() { return ivaPorcentaje; }
    public DesgloseRegulado getDesgloseRegulado() { return desgloseRegulado; }
    public double getAjustesSistema() { return ajustesSistema; }
    public double getOtrosConceptos() { return otrosConceptos; }
}
