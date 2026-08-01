package com.example.consumoapp.factura.ui;

import com.example.consumoapp.core.factura.engine.ResultadoFactura;
import com.example.consumoapp.factura.SimulacionFacturaEntity;
import com.example.consumoapp.regulacion.domain.ConstantesFactura;

/** Convierte el estado calculado en una fotografía persistible para Room. */
public final class SimulacionSnapshotFactory {

    private SimulacionSnapshotFactory() { }

    public static SimulacionFacturaEntity crear(
            long usuarioId,
            String nombre,
            String fuentePrecio,
            String comercializadora,
            String tarifa,
            FacturaCalculationController calculo,
            ConstantesFactura constantes,
            int numeroElectrodomesticos
    ) {
        ResultadoFactura resultado = calculo.getUltimoResultado();
        if (resultado == null) {
            throw new IllegalStateException("No existe un resultado calculado");
        }

        return crearDesdeResultado(
                usuarioId, nombre, fuentePrecio, comercializadora, tarifa,
                resultado, calculo.isReguladosSeparados(), constantes,
                calculo.getPotenciaPuntaKw(), calculo.getPotenciaValleKw(),
                calculo.getDiasFactura(), numeroElectrodomesticos,
                System.currentTimeMillis()
        );
    }

    /** Variante pura y determinista, apta para pruebas unitarias JVM. */
    public static SimulacionFacturaEntity crearDesdeResultado(
            long usuarioId,
            String nombre,
            String fuentePrecio,
            String comercializadora,
            String tarifa,
            ResultadoFactura resultado,
            boolean reguladosSeparados,
            ConstantesFactura constantes,
            double potenciaPuntaKw,
            double potenciaValleKw,
            int diasFactura,
            int numeroElectrodomesticos,
            long fechaCreacion
    ) {
        if (resultado == null) {
            throw new IllegalArgumentException("El resultado no puede ser nulo");
        }
        if (diasFactura <= 0) {
            throw new IllegalArgumentException("Los días de factura deben ser mayores que cero");
        }
        if (numeroElectrodomesticos < 0) {
            throw new IllegalArgumentException("El número de electrodomésticos no puede ser negativo");
        }

        SimulacionFacturaEntity simulacion = new SimulacionFacturaEntity();
        simulacion.setUsuarioId(usuarioId);
        simulacion.setFechaCreacion(fechaCreacion);
        simulacion.setNombre(nombre);
        simulacion.setFuentePrecio(fuentePrecio);
        simulacion.setComercializadora(comercializadora);
        simulacion.setTarifa(tarifa);

        simulacion.setConsumoTotalKwh(resultado.getConsumoTotalKwh());
        simulacion.setConsumoP1Kwh(resultado.getConsumoP1Kwh());
        simulacion.setConsumoP2Kwh(resultado.getConsumoP2Kwh());
        simulacion.setConsumoP3Kwh(resultado.getConsumoP3Kwh());
        simulacion.setCosteP1(resultado.getCosteP1());
        simulacion.setCosteP2(resultado.getCosteP2());
        simulacion.setCosteP3(resultado.getCosteP3());
        simulacion.setCosteEnergia(resultado.getCosteEnergia());
        simulacion.setCostePotencia(resultado.getCostePotencia());
        simulacion.setAlquilerContador(resultado.getAlquilerContador());
        simulacion.setImpuestoElectricidad(resultado.getImpuestoElectricidad());
        simulacion.setIva(resultado.getIva());
        simulacion.setTotalFactura(resultado.getTotalFactura());

        simulacion.setCosteEnergiaMercado(resultado.getCosteEnergiaMercado());
        simulacion.setCostePotenciaBase(resultado.getCostePotenciaBase());
        simulacion.setPeajesEnergia(resultado.getPeajesEnergia());
        simulacion.setPeajesPotencia(resultado.getPeajesPotencia());
        simulacion.setCargosEnergia(resultado.getCargosEnergia());
        simulacion.setCargosPotencia(resultado.getCargosPotencia());
        simulacion.setAjustesSistema(resultado.getAjustesSistema());
        simulacion.setOtrosConceptos(resultado.getOtrosConceptos());
        simulacion.setBaseImpuestoElectricidad(resultado.getBaseImpuestoElectricidad());
        simulacion.setBaseIva(resultado.getBaseIva());

        simulacion.setReguladosSeparados(reguladosSeparados);
        simulacion.setFuenteConstantes(
                reguladosSeparados && constantes != null
                        ? constantes.getFuente()
                        : "Incluidos en los precios seleccionados"
        );
        simulacion.setPotenciaPuntaKw(potenciaPuntaKw);
        simulacion.setPotenciaValleKw(potenciaValleKw);
        simulacion.setDiasFactura(diasFactura);
        simulacion.setNumeroElectrodomesticos(numeroElectrodomesticos);
        return simulacion;
    }
}
