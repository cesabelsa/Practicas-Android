package com.example.consumoapp.core.factura.engine;

import org.junit.Test;
import static org.junit.Assert.*;

public class MotorCalculoFacturaValidacionTest {
    private DatosCalculoFactura datos(double consumo, int dias, double iva) {
        return new DatosCalculoFactura(consumo,0,0, 1,0,0, 4,4, 0.1,0.1, dias,0.02,5,iva);
    }

    @Test(expected = IllegalArgumentException.class) public void rechazaDatosNulos() {
        new MotorCalculoFactura().calcular(null);
    }
    @Test(expected = IllegalArgumentException.class) public void rechazaDiasCero() {
        new MotorCalculoFactura().calcular(datos(1,0,21));
    }
    @Test(expected = IllegalArgumentException.class) public void rechazaIvaNegativo() {
        new MotorCalculoFactura().calcular(datos(1,30,-1));
    }
    @Test(expected = IllegalArgumentException.class) public void rechazaConsumoInfinito() {
        new MotorCalculoFactura().calcular(datos(Double.POSITIVE_INFINITY,30,21));
    }
    @Test public void facturaCeroConsumoMantieneTerminoPotencia() {
        ResultadoFactura r = new MotorCalculoFactura().calcular(datos(0,30,21));
        assertTrue(r.getCostePotencia() > 0);
        assertTrue(r.getTotalFactura() > 0);
    }
}
