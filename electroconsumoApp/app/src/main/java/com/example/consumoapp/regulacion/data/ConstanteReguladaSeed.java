package com.example.consumoapp.regulacion.data;

import com.example.consumoapp.esios.data.AppDatabase;

import java.util.ArrayList;
import java.util.List;

/** Carga inicial de valores 2.0TD 2026 documentados para el proyecto. */
public final class ConstanteReguladaSeed {

    public static final String GENERAL = "GENERAL";
    public static final String P1 = "P1";
    public static final String P2 = "P2";
    public static final String P3 = "P3";

    public static final String PEAJE_POTENCIA = "PEAJE_POTENCIA";
    public static final String CARGO_POTENCIA = "CARGO_POTENCIA";
    public static final String PEAJE_ENERGIA = "PEAJE_ENERGIA";
    public static final String CARGO_ENERGIA = "CARGO_ENERGIA";
    public static final String IVA_ELECTRICIDAD = "IVA_ELECTRICIDAD";
    public static final String IMPUESTO_ELECTRICIDAD = "IMPUESTO_ELECTRICIDAD";
    public static final String ALQUILER_CONTADOR_DIA = "ALQUILER_CONTADOR_DIA";

    private ConstanteReguladaSeed() { }

    /**
     * Debe llamarse desde un hilo de trabajo. Es síncrono para impedir que el
     * simulador consulte antes de terminar la carga inicial.
     */
    public static void cargarSiEstaVacio(AppDatabase database) {
        ConstanteReguladaDao dao = database.constanteReguladaDao();
        if (dao.contar() > 0) return;

        List<ConstanteReguladaEntity> datos = new ArrayList<>();
        String inicio2026 = "2026-01-01";
        String fiscal2026 = "2026-06-01";

        // Potencia regulada, expresada originalmente en €/kW/año.
        datos.add(valor(PEAJE_POTENCIA, P1, 23.324952, "EUR_KW_ANIO", inicio2026, "CNMC/BOE 2026"));
        datos.add(valor(PEAJE_POTENCIA, P2, 0.443770, "EUR_KW_ANIO", inicio2026, "CNMC/BOE 2026"));
        datos.add(valor(CARGO_POTENCIA, P1, 4.379461, "EUR_KW_ANIO", inicio2026, "MITECO/BOE 2026"));
        datos.add(valor(CARGO_POTENCIA, P2, 0.281653, "EUR_KW_ANIO", inicio2026, "MITECO/BOE 2026"));

        // Energía regulada. Se almacena para cálculos indexados futuros.
        datos.add(valor(PEAJE_ENERGIA, P1, 0.033261, "EUR_KWH", inicio2026, "CNMC/BOE 2026"));
        datos.add(valor(PEAJE_ENERGIA, P2, 0.016409, "EUR_KWH", inicio2026, "CNMC/BOE 2026"));
        datos.add(valor(PEAJE_ENERGIA, P3, 0.000077, "EUR_KWH", inicio2026, "CNMC/BOE 2026"));
        datos.add(valor(CARGO_ENERGIA, P1, 0.064292, "EUR_KWH", inicio2026, "MITECO/BOE 2026"));
        datos.add(valor(CARGO_ENERGIA, P2, 0.012858, "EUR_KWH", inicio2026, "MITECO/BOE 2026"));
        datos.add(valor(CARGO_ENERGIA, P3, 0.003215, "EUR_KWH", inicio2026, "MITECO/BOE 2026"));

        // Tipos fiscales vigentes desde el 1 de junio de 2026 según el documento del proyecto.
        datos.add(valor(IVA_ELECTRICIDAD, GENERAL, 21.0, "PORCENTAJE", fiscal2026, "Normativa fiscal vigente"));
        datos.add(valor(IMPUESTO_ELECTRICIDAD, GENERAL, 5.11269632, "PORCENTAJE", fiscal2026, "Normativa fiscal vigente"));

        // Valor inicial editable; se mantiene separado porque depende del equipo/contrato.
        datos.add(valor(ALQUILER_CONTADOR_DIA, GENERAL, 0.026, "EUR_DIA", inicio2026, "Valor de trabajo del simulador"));

        dao.insertarTodos(datos);
    }

    private static ConstanteReguladaEntity valor(
            String codigo, String periodo, double valor, String unidad,
            String desde, String fuente
    ) {
        return new ConstanteReguladaEntity(
                codigo, periodo, valor, unidad, desde, null, fuente, true
        );
    }
}
