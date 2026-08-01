package com.example.consumoapp.core.factura.engine;

/** Calcula potencia base, peajes y cargos asociados a la potencia contratada. */
final class CalculadoraPotencia {

    Resultado calcular(DatosCalculoFactura datos) {
        DesgloseRegulado regulado = datos.getDesgloseRegulado();
        int dias = datos.getDiasFactura();

        double base = datos.getPotenciaPuntaKw()
                * datos.getPrecioPotenciaPuntaDia() * dias
                + datos.getPotenciaValleKw()
                * datos.getPrecioPotenciaValleDia() * dias;

        double peajes = datos.getPotenciaPuntaKw()
                * regulado.getPeajePotenciaP1Dia() * dias
                + datos.getPotenciaValleKw()
                * regulado.getPeajePotenciaP2Dia() * dias;

        double cargos = datos.getPotenciaPuntaKw()
                * regulado.getCargoPotenciaP1Dia() * dias
                + datos.getPotenciaValleKw()
                * regulado.getCargoPotenciaP2Dia() * dias;

        return new Resultado(base, peajes, cargos);
    }

    static final class Resultado {
        final double base;
        final double peajes;
        final double cargos;

        Resultado(double base, double peajes, double cargos) {
            this.base = base;
            this.peajes = peajes;
            this.cargos = cargos;
        }

        double total() { return base + peajes + cargos; }
    }
}
