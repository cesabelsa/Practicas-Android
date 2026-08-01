package com.example.consumoapp.core.factura.engine;

/** Calcula energía de mercado, peajes y cargos asociados al consumo. */
final class CalculadoraEnergia {

    Resultado calcular(DatosCalculoFactura datos) {
        DesgloseRegulado regulado = datos.getDesgloseRegulado();

        double mercado = datos.getCosteEnergiaP1()
                + datos.getCosteEnergiaP2()
                + datos.getCosteEnergiaP3();

        double peajes = datos.getConsumoP1Kwh() * regulado.getPeajeEnergiaP1()
                + datos.getConsumoP2Kwh() * regulado.getPeajeEnergiaP2()
                + datos.getConsumoP3Kwh() * regulado.getPeajeEnergiaP3();

        double cargos = datos.getConsumoP1Kwh() * regulado.getCargoEnergiaP1()
                + datos.getConsumoP2Kwh() * regulado.getCargoEnergiaP2()
                + datos.getConsumoP3Kwh() * regulado.getCargoEnergiaP3();

        return new Resultado(mercado, peajes, cargos);
    }

    static final class Resultado {
        final double mercado;
        final double peajes;
        final double cargos;

        Resultado(double mercado, double peajes, double cargos) {
            this.mercado = mercado;
            this.peajes = peajes;
            this.cargos = cargos;
        }

        double total() { return mercado + peajes + cargos; }
    }
}
