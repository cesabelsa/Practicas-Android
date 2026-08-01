package com.example.consumoapp.factura;

import org.junit.Test;
import static org.junit.Assert.*;

public class LineaSimuladorFacturaTest {
    private static final double D = 1e-9;

    @Test public void calculaConsumoManualConPorcentajeUso() {
        LineaSimuladorFactura l = new LineaSimuladorFactura("Horno", 2000, 1.5, 10, 50,
                LineaSimuladorFactura.PERIODO_P1, 0.20);
        assertEquals(15.0, l.getConsumoKwh(), D);
        assertEquals(15.0, l.getConsumoP1Kwh(), D);
        assertEquals(3.0, l.getCosteEnergia(), D);
    }

    @Test public void actualizarPreciosRecalculaCosteSinCambiarConsumo() {
        LineaSimuladorFactura l = new LineaSimuladorFactura("Lavadora", 1000, 4, 1, 100,
                9.0, false, 0.1, 0.2, 0.3);
        double consumo = l.getConsumoKwh();
        l.actualizarPrecios(1.0, 2.0, 3.0);
        assertEquals(consumo, l.getConsumoKwh(), D);
        assertEquals(l.getCosteP1()+l.getCosteP2()+l.getCosteP3(), l.getCosteEnergia(), D);
    }
}
