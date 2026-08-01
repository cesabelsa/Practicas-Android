package com.example.consumoapp.regulacion.domain;

import com.example.consumoapp.core.factura.engine.DesgloseRegulado;
import com.example.consumoapp.esios.data.AppDatabase;
import com.example.consumoapp.regulacion.data.ConstanteReguladaDao;
import com.example.consumoapp.regulacion.data.ConstanteReguladaEntity;
import com.example.consumoapp.regulacion.data.ConstanteReguladaSeed;

/** Obtiene de Room las constantes vigentes para una fecha concreta. */
public class ConstantesReguladasRepository {

    private final AppDatabase database;

    public ConstantesReguladasRepository(AppDatabase database) {
        this.database = database;
    }

    public ConstantesFactura obtenerParaFactura(String fechaIso) {
        ConstanteReguladaSeed.cargarSiEstaVacio(database);
        ConstanteReguladaDao dao = database.constanteReguladaDao();

        ConstanteReguladaEntity peajeP1 = exigir(dao, ConstanteReguladaSeed.PEAJE_POTENCIA, ConstanteReguladaSeed.P1, fechaIso);
        ConstanteReguladaEntity cargoP1 = exigir(dao, ConstanteReguladaSeed.CARGO_POTENCIA, ConstanteReguladaSeed.P1, fechaIso);
        ConstanteReguladaEntity peajeP2 = exigir(dao, ConstanteReguladaSeed.PEAJE_POTENCIA, ConstanteReguladaSeed.P2, fechaIso);
        ConstanteReguladaEntity cargoP2 = exigir(dao, ConstanteReguladaSeed.CARGO_POTENCIA, ConstanteReguladaSeed.P2, fechaIso);
        ConstanteReguladaEntity peajeEnergiaP1 = exigir(dao, ConstanteReguladaSeed.PEAJE_ENERGIA, ConstanteReguladaSeed.P1, fechaIso);
        ConstanteReguladaEntity peajeEnergiaP2 = exigir(dao, ConstanteReguladaSeed.PEAJE_ENERGIA, ConstanteReguladaSeed.P2, fechaIso);
        ConstanteReguladaEntity peajeEnergiaP3 = exigir(dao, ConstanteReguladaSeed.PEAJE_ENERGIA, ConstanteReguladaSeed.P3, fechaIso);
        ConstanteReguladaEntity cargoEnergiaP1 = exigir(dao, ConstanteReguladaSeed.CARGO_ENERGIA, ConstanteReguladaSeed.P1, fechaIso);
        ConstanteReguladaEntity cargoEnergiaP2 = exigir(dao, ConstanteReguladaSeed.CARGO_ENERGIA, ConstanteReguladaSeed.P2, fechaIso);
        ConstanteReguladaEntity cargoEnergiaP3 = exigir(dao, ConstanteReguladaSeed.CARGO_ENERGIA, ConstanteReguladaSeed.P3, fechaIso);
        ConstanteReguladaEntity alquiler = exigir(dao, ConstanteReguladaSeed.ALQUILER_CONTADOR_DIA, ConstanteReguladaSeed.GENERAL, fechaIso);
        ConstanteReguladaEntity iee = exigir(dao, ConstanteReguladaSeed.IMPUESTO_ELECTRICIDAD, ConstanteReguladaSeed.GENERAL, fechaIso);
        ConstanteReguladaEntity iva = exigir(dao, ConstanteReguladaSeed.IVA_ELECTRICIDAD, ConstanteReguladaSeed.GENERAL, fechaIso);

        // Room guarda potencia en €/kW/año; el motor trabaja en €/kW/día.
        double peajeP1Dia = peajeP1.getValor() / 365.0;
        double peajeP2Dia = peajeP2.getValor() / 365.0;
        double cargoP1Dia = cargoP1.getValor() / 365.0;
        double cargoP2Dia = cargoP2.getValor() / 365.0;

        double p1Dia = peajeP1Dia + cargoP1Dia;
        double p2Dia = peajeP2Dia + cargoP2Dia;

        DesgloseRegulado desglose = new DesgloseRegulado(
                peajeEnergiaP1.getValor(),
                peajeEnergiaP2.getValor(),
                peajeEnergiaP3.getValor(),
                cargoEnergiaP1.getValor(),
                cargoEnergiaP2.getValor(),
                cargoEnergiaP3.getValor(),
                peajeP1Dia,
                peajeP2Dia,
                cargoP1Dia,
                cargoP2Dia
        );

        return new ConstantesFactura(
                p1Dia,
                p2Dia,
                alquiler.getValor(),
                iee.getValor(),
                iva.getValor(),
                desglose,
                "SQLite · valores vigentes a " + fechaIso
        );
    }

    private ConstanteReguladaEntity exigir(
            ConstanteReguladaDao dao, String codigo, String periodo, String fecha
    ) {
        ConstanteReguladaEntity valor = dao.buscarVigente(codigo, periodo, fecha);
        if (valor == null) {
            throw new IllegalStateException(
                    "No existe una constante vigente: " + codigo + " / " + periodo + " / " + fecha
            );
        }
        return valor;
    }
}
