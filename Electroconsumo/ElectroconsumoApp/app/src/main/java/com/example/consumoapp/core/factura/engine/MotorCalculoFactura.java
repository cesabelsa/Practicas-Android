package com.example.consumoapp.core.factura.engine;

/**
 * Motor único y desacoplado para calcular una factura eléctrica.
 *
 * La Activity reúne datos; este motor aplica las fórmulas. Así puede usarse
 * también desde el comparador, pruebas unitarias y una futura factura visual.
 */
public final class MotorCalculoFactura {

    private final CalculadoraEnergia calculadoraEnergia = new CalculadoraEnergia();
    private final CalculadoraPotencia calculadoraPotencia = new CalculadoraPotencia();
    private final CalculadoraImpuestos calculadoraImpuestos = new CalculadoraImpuestos();

    public ResultadoFactura calcular(DatosCalculoFactura datos) {
        validar(datos);

        double consumoTotal = datos.getConsumoP1Kwh()
                + datos.getConsumoP2Kwh()
                + datos.getConsumoP3Kwh();

        CalculadoraEnergia.Resultado energia = calculadoraEnergia.calcular(datos);
        CalculadoraPotencia.Resultado potencia = calculadoraPotencia.calcular(datos);

        double costeEnergia = energia.total();
        double costePotencia = potencia.total();
        double alquiler = datos.getAlquilerContadorDia() * datos.getDiasFactura();

        // Los ajustes del sistema forman parte de la base energética del contrato.
        double baseIee = costeEnergia + costePotencia + datos.getAjustesSistema();
        CalculadoraImpuestos.Resultado impuestos = calculadoraImpuestos.calcular(
                baseIee,
                alquiler,
                datos.getOtrosConceptos(),
                datos.getImpuestoElectricidadPorcentaje(),
                datos.getIvaPorcentaje()
        );

        double total = impuestos.baseIva + impuestos.iva;

        return new ResultadoFactura(
                consumoTotal,
                datos.getConsumoP1Kwh(),
                datos.getConsumoP2Kwh(),
                datos.getConsumoP3Kwh(),
                datos.getCosteEnergiaP1(),
                datos.getCosteEnergiaP2(),
                datos.getCosteEnergiaP3(),
                energia.mercado,
                potencia.base,
                energia.peajes,
                potencia.peajes,
                energia.cargos,
                potencia.cargos,
                datos.getAjustesSistema(),
                datos.getOtrosConceptos(),
                costeEnergia,
                costePotencia,
                alquiler,
                baseIee,
                impuestos.impuestoElectricidad,
                impuestos.baseIva,
                impuestos.iva,
                total
        );
    }

    private void validar(DatosCalculoFactura datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos de cálculo no pueden ser nulos");
        }
        if (datos.getDiasFactura() <= 0) {
            throw new IllegalArgumentException("Los días de factura deben ser mayores que cero");
        }
        if (datos.getIvaPorcentaje() < 0 || datos.getImpuestoElectricidadPorcentaje() < 0) {
            throw new IllegalArgumentException("Los porcentajes no pueden ser negativos");
        }
        validarNoNegativo(datos.getConsumoP1Kwh(), "El consumo P1");
        validarNoNegativo(datos.getConsumoP2Kwh(), "El consumo P2");
        validarNoNegativo(datos.getConsumoP3Kwh(), "El consumo P3");
        validarNoNegativo(datos.getPotenciaPuntaKw(), "La potencia P1");
        validarNoNegativo(datos.getPotenciaValleKw(), "La potencia P2");
        validarNoNegativo(datos.getAlquilerContadorDia(), "El alquiler del contador");
    }

    private void validarNoNegativo(double valor, String nombre) {
        if (!Double.isFinite(valor) || valor < 0) {
            throw new IllegalArgumentException(nombre + " debe ser un número no negativo");
        }
    }
}
