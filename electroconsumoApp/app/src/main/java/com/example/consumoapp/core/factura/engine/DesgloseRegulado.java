package com.example.consumoapp.core.factura.engine;

/**
 * Precios unitarios regulados utilizados por el motor profesional.
 *
 * Todos los importes de energía se expresan en EUR/kWh y los de potencia
 * en EUR/kW/día. El objeto es inmutable para evitar cambios accidentales
 * durante el cálculo de una factura.
 */
public final class DesgloseRegulado {

    public static final DesgloseRegulado SIN_DESGLOSE = new DesgloseRegulado(
            0, 0, 0, 0, 0, 0,
            0, 0, 0, 0
    );

    private final double peajeEnergiaP1;
    private final double peajeEnergiaP2;
    private final double peajeEnergiaP3;
    private final double cargoEnergiaP1;
    private final double cargoEnergiaP2;
    private final double cargoEnergiaP3;
    private final double peajePotenciaP1Dia;
    private final double peajePotenciaP2Dia;
    private final double cargoPotenciaP1Dia;
    private final double cargoPotenciaP2Dia;

    public DesgloseRegulado(
            double peajeEnergiaP1,
            double peajeEnergiaP2,
            double peajeEnergiaP3,
            double cargoEnergiaP1,
            double cargoEnergiaP2,
            double cargoEnergiaP3,
            double peajePotenciaP1Dia,
            double peajePotenciaP2Dia,
            double cargoPotenciaP1Dia,
            double cargoPotenciaP2Dia
    ) {
        this.peajeEnergiaP1 = peajeEnergiaP1;
        this.peajeEnergiaP2 = peajeEnergiaP2;
        this.peajeEnergiaP3 = peajeEnergiaP3;
        this.cargoEnergiaP1 = cargoEnergiaP1;
        this.cargoEnergiaP2 = cargoEnergiaP2;
        this.cargoEnergiaP3 = cargoEnergiaP3;
        this.peajePotenciaP1Dia = peajePotenciaP1Dia;
        this.peajePotenciaP2Dia = peajePotenciaP2Dia;
        this.cargoPotenciaP1Dia = cargoPotenciaP1Dia;
        this.cargoPotenciaP2Dia = cargoPotenciaP2Dia;
    }

    public double getPeajeEnergiaP1() { return peajeEnergiaP1; }
    public double getPeajeEnergiaP2() { return peajeEnergiaP2; }
    public double getPeajeEnergiaP3() { return peajeEnergiaP3; }
    public double getCargoEnergiaP1() { return cargoEnergiaP1; }
    public double getCargoEnergiaP2() { return cargoEnergiaP2; }
    public double getCargoEnergiaP3() { return cargoEnergiaP3; }
    public double getPeajePotenciaP1Dia() { return peajePotenciaP1Dia; }
    public double getPeajePotenciaP2Dia() { return peajePotenciaP2Dia; }
    public double getCargoPotenciaP1Dia() { return cargoPotenciaP1Dia; }
    public double getCargoPotenciaP2Dia() { return cargoPotenciaP2Dia; }
}
