package com.example.consumoapp.regulacion.domain;

import com.example.consumoapp.core.factura.engine.DesgloseRegulado;

/**
 * Valores vigentes que necesita el motor para una factura doméstica.
 *
 * Incluye tanto los precios combinados usados por la interfaz antigua como el
 * desglose separado de peajes y cargos requerido por el motor profesional.
 */
public class ConstantesFactura {
    private final double potenciaP1Dia;
    private final double potenciaP2Dia;
    private final double alquilerContadorDia;
    private final double impuestoElectricidadPct;
    private final double ivaPct;
    private final DesgloseRegulado desgloseRegulado;
    private final String fuente;

    public ConstantesFactura(
            double potenciaP1Dia,
            double potenciaP2Dia,
            double alquilerContadorDia,
            double impuestoElectricidadPct,
            double ivaPct,
            DesgloseRegulado desgloseRegulado,
            String fuente
    ) {
        this.potenciaP1Dia = potenciaP1Dia;
        this.potenciaP2Dia = potenciaP2Dia;
        this.alquilerContadorDia = alquilerContadorDia;
        this.impuestoElectricidadPct = impuestoElectricidadPct;
        this.ivaPct = ivaPct;
        this.desgloseRegulado = desgloseRegulado == null
                ? DesgloseRegulado.SIN_DESGLOSE
                : desgloseRegulado;
        this.fuente = fuente;
    }

    public double getPotenciaP1Dia() { return potenciaP1Dia; }
    public double getPotenciaP2Dia() { return potenciaP2Dia; }
    public double getAlquilerContadorDia() { return alquilerContadorDia; }
    public double getImpuestoElectricidadPct() { return impuestoElectricidadPct; }
    public double getIvaPct() { return ivaPct; }
    public DesgloseRegulado getDesgloseRegulado() { return desgloseRegulado; }
    public String getFuente() { return fuente; }
}
