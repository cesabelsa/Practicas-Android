package com.example.consumoapp.factura.ui;

import com.example.consumoapp.core.factura.engine.DatosCalculoFactura;
import com.example.consumoapp.core.factura.engine.MotorCalculoFactura;
import com.example.consumoapp.core.factura.engine.ResultadoFactura;
import com.example.consumoapp.factura.SimulacionFacturaEntity;
import org.junit.Test;
import static org.junit.Assert.*;

public class SimulacionSnapshotFactoryTest {
    @Test public void copiaResultadoYMetadatosSinPerdida() {
        ResultadoFactura r = new MotorCalculoFactura().calcular(new DatosCalculoFactura(
                10,20,30, 1,4,9, 4.6,3.45, 0.1,0.05, 30,0.02,5,21));
        SimulacionFacturaEntity e = SimulacionSnapshotFactory.crearDesdeResultado(
                7,"Casa","Manual","Comercial","Tarifa",r,false,null,
                4.6,3.45,30,4,123456L);
        assertEquals(7, e.getUsuarioId());
        assertEquals(123456L, e.getFechaCreacion());
        assertEquals("Casa", e.getNombre());
        assertEquals(r.getTotalFactura(), e.getTotalFactura(), 1e-9);
        assertEquals(r.getConsumoP3Kwh(), e.getConsumoP3Kwh(), 1e-9);
        assertEquals(4, e.getNumeroElectrodomesticos());
        assertEquals("Incluidos en los precios seleccionados", e.getFuenteConstantes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rechazaResultadoNulo() {
        SimulacionSnapshotFactory.crearDesdeResultado(1,"n","f",null,null,null,false,null,1,1,30,0,1);
    }
}
