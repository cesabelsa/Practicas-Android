package com.example.consumoapp;

import com.example.consumoapp.core.factura.engine.DatosCalculoFactura;
import com.example.consumoapp.core.factura.engine.DesgloseRegulado;
import com.example.consumoapp.core.factura.engine.MotorCalculoFactura;
import com.example.consumoapp.core.factura.engine.ResultadoFactura;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Pruebas locales del motor. No necesitan emulador ni Android. */
public class MotorCalculoFacturaTest {

    private static final double DELTA = 0.000001;

    @Test
    public void constructorCompatibleMantieneElCalculoAnterior() {
        DatosCalculoFactura datos = new DatosCalculoFactura(
                100, 50, 50,
                20, 8, 4,
                4.6, 4.6,
                0.08, 0.01,
                30,
                0.026,
                5,
                21
        );

        ResultadoFactura resultado = new MotorCalculoFactura().calcular(datos);

        double potencia = 4.6 * 0.08 * 30 + 4.6 * 0.01 * 30;
        double alquiler = 0.026 * 30;
        double iee = (32 + potencia) * 0.05;
        double baseIva = 32 + potencia + iee + alquiler;
        double total = baseIva * 1.21;

        assertEquals(200, resultado.getConsumoTotalKwh(), DELTA);
        assertEquals(32, resultado.getCosteEnergia(), DELTA);
        assertEquals(potencia, resultado.getCostePotencia(), DELTA);
        assertEquals(0, resultado.getPeajesTotal(), DELTA);
        assertEquals(0, resultado.getCargosTotal(), DELTA);
        assertEquals(total, resultado.getTotalFactura(), DELTA);
    }

    @Test
    public void calculaPeajesCargosAjustesEImpuestosPorSeparado() {
        DesgloseRegulado regulado = new DesgloseRegulado(
                0.03, 0.02, 0.01,
                0.04, 0.02, 0.01,
                0.05, 0.01,
                0.02, 0.005
        );

        DatosCalculoFactura datos = new DatosCalculoFactura(
                100, 50, 50,
                10, 5, 2,
                4, 4,
                0, 0,
                30,
                0.02,
                5,
                21,
                regulado,
                1.50,
                2.00
        );

        ResultadoFactura resultado = new MotorCalculoFactura().calcular(datos);

        double peajesEnergia = 100 * 0.03 + 50 * 0.02 + 50 * 0.01;
        double cargosEnergia = 100 * 0.04 + 50 * 0.02 + 50 * 0.01;
        double peajesPotencia = 4 * 0.05 * 30 + 4 * 0.01 * 30;
        double cargosPotencia = 4 * 0.02 * 30 + 4 * 0.005 * 30;
        double baseIee = 17 + peajesEnergia + cargosEnergia
                + peajesPotencia + cargosPotencia + 1.50;
        double iee = baseIee * 0.05;
        double baseIva = baseIee + iee + 0.60 + 2.00;
        double total = baseIva * 1.21;

        assertEquals(peajesEnergia, resultado.getPeajesEnergia(), DELTA);
        assertEquals(peajesPotencia, resultado.getPeajesPotencia(), DELTA);
        assertEquals(cargosEnergia, resultado.getCargosEnergia(), DELTA);
        assertEquals(cargosPotencia, resultado.getCargosPotencia(), DELTA);
        assertEquals(1.50, resultado.getAjustesSistema(), DELTA);
        assertEquals(total, resultado.getTotalFactura(), DELTA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rechazaConsumosNegativos() {
        DatosCalculoFactura datos = new DatosCalculoFactura(
                -1, 0, 0,
                0, 0, 0,
                4, 4,
                0, 0,
                30,
                0,
                0,
                21
        );
        new MotorCalculoFactura().calcular(datos);
    }
}
