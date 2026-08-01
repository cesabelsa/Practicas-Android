package com.example.consumoapp.core.factura.engine;

/** Calcula el impuesto eléctrico y el IVA a partir de sus bases. */
final class CalculadoraImpuestos {

    Resultado calcular(
            double baseImpuestoElectricidad,
            double alquiler,
            double otrosConceptos,
            double porcentajeIee,
            double porcentajeIva
    ) {
        double iee = baseImpuestoElectricidad * (porcentajeIee / 100.0);
        double baseIva = baseImpuestoElectricidad + iee + alquiler + otrosConceptos;
        double iva = baseIva * (porcentajeIva / 100.0);
        return new Resultado(iee, baseIva, iva);
    }

    static final class Resultado {
        final double impuestoElectricidad;
        final double baseIva;
        final double iva;

        Resultado(double impuestoElectricidad, double baseIva, double iva) {
            this.impuestoElectricidad = impuestoElectricidad;
            this.baseIva = baseIva;
            this.iva = iva;
        }
    }
}
