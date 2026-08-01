package com.example.consumoapp.factura;

import org.junit.Test;
import static org.junit.Assert.*;

public class Periodo2TDCalculatorTest {
    private static final double D = 1e-9;

    @Test public void clasificaFronterasLaborables() {
        assertEquals(LineaSimuladorFactura.PERIODO_P3, Periodo2TDCalculator.obtenerPeriodo(7.999, false));
        assertEquals(LineaSimuladorFactura.PERIODO_P2, Periodo2TDCalculator.obtenerPeriodo(8.0, false));
        assertEquals(LineaSimuladorFactura.PERIODO_P1, Periodo2TDCalculator.obtenerPeriodo(10.0, false));
        assertEquals(LineaSimuladorFactura.PERIODO_P2, Periodo2TDCalculator.obtenerPeriodo(14.0, false));
        assertEquals(LineaSimuladorFactura.PERIODO_P1, Periodo2TDCalculator.obtenerPeriodo(18.0, false));
        assertEquals(LineaSimuladorFactura.PERIODO_P2, Periodo2TDCalculator.obtenerPeriodo(22.0, false));
    }

    @Test public void diaValleCompletoSiempreEsP3() {
        assertEquals(LineaSimuladorFactura.PERIODO_P3, Periodo2TDCalculator.obtenerPeriodo(12.0, true));
    }

    @Test public void reparteIntervaloQueCruzaTresPeriodos() {
        Periodo2TDCalculator.RepartoHoras r = Periodo2TDCalculator.repartirHoras(7.0, 8.0, false);
        assertEquals(4.0, r.getHorasP1(), D);
        assertEquals(3.0, r.getHorasP2(), D);
        assertEquals(1.0, r.getHorasP3(), D);
        assertEquals(8.0, r.getHorasP1()+r.getHorasP2()+r.getHorasP3(), D);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rechazaHora24() { Periodo2TDCalculator.obtenerPeriodo(24.0, false); }
}
