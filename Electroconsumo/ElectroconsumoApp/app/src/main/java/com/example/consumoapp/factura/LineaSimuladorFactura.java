package com.example.consumoapp.factura;

/**
 * Línea de consumo de un electrodoméstico dentro de la factura simulada.
 *
 * Puede representar un uso manual asignado a un único periodo o un uso
 * automático repartido entre varios periodos 2.0TD.
 */
public class LineaSimuladorFactura {

    public static final String PERIODO_AUTO = "Automático por horario";
    public static final String PERIODO_P1 = "P1 - Punta";
    public static final String PERIODO_P2 = "P2 - Llano";
    public static final String PERIODO_P3 = "P3 - Valle";

    private final String electrodomestico;
    private final double potenciaW;
    private final double horasDia;
    private final int diasMes;
    // Porcentaje real de utilización de la potencia nominal (1-100).
    private final double porcentajeUso;
    private final String periodo;
    private final Double horaInicio;

    private final double consumoP1Kwh;
    private final double consumoP2Kwh;
    private final double consumoP3Kwh;
    private final double consumoKwh;

    private double precioP1;
    private double precioP2;
    private double precioP3;
    private double costeEnergia;

    /** Constructor para una asignación manual a un solo periodo. */
    public LineaSimuladorFactura(String electrodomestico, double potenciaW, double horasDia,
                                 int diasMes, double porcentajeUso, String periodo, double precioKwh) {
        this.electrodomestico = electrodomestico;
        this.potenciaW = potenciaW;
        this.horasDia = horasDia;
        this.diasMes = diasMes;
        this.porcentajeUso = porcentajeUso;
        this.periodo = periodo;
        this.horaInicio = null;

        double total = (potenciaW / 1000.0) * (porcentajeUso / 100.0) * horasDia * diasMes;
        this.consumoP1Kwh = PERIODO_P1.equals(periodo) ? total : 0.0;
        this.consumoP2Kwh = PERIODO_P2.equals(periodo) ? total : 0.0;
        this.consumoP3Kwh = PERIODO_P3.equals(periodo) ? total : 0.0;
        this.consumoKwh = total;

        this.precioP1 = precioKwh;
        this.precioP2 = precioKwh;
        this.precioP3 = precioKwh;
        recalcularCoste();
    }

    /** Constructor para un uso automático que puede atravesar varios periodos. */
    public LineaSimuladorFactura(String electrodomestico, double potenciaW, double horasDia,
                                 int diasMes, double porcentajeUso, double horaInicio, boolean diaValleCompleto,
                                 double precioP1, double precioP2, double precioP3) {
        this.electrodomestico = electrodomestico;
        this.potenciaW = potenciaW;
        this.horasDia = horasDia;
        this.diasMes = diasMes;
        this.porcentajeUso = porcentajeUso;
        this.periodo = PERIODO_AUTO;
        this.horaInicio = horaInicio;

        Periodo2TDCalculator.RepartoHoras reparto =
                Periodo2TDCalculator.repartirHoras(horaInicio, horasDia, diaValleCompleto);

        double potenciaKw = (potenciaW / 1000.0) * (porcentajeUso / 100.0);
        this.consumoP1Kwh = potenciaKw * reparto.getHorasP1() * diasMes;
        this.consumoP2Kwh = potenciaKw * reparto.getHorasP2() * diasMes;
        this.consumoP3Kwh = potenciaKw * reparto.getHorasP3() * diasMes;
        this.consumoKwh = consumoP1Kwh + consumoP2Kwh + consumoP3Kwh;

        this.precioP1 = precioP1;
        this.precioP2 = precioP2;
        this.precioP3 = precioP3;
        recalcularCoste();
    }

    public String getElectrodomestico() { return electrodomestico; }
    public double getPotenciaW() { return potenciaW; }
    public double getHorasDia() { return horasDia; }
    public int getDiasMes() { return diasMes; }
    public double getPorcentajeUso() { return porcentajeUso; }
    public String getPeriodo() { return periodo; }
    public Double getHoraInicio() { return horaInicio; }
    public double getConsumoKwh() { return consumoKwh; }
    public double getConsumoP1Kwh() { return consumoP1Kwh; }
    public double getConsumoP2Kwh() { return consumoP2Kwh; }
    public double getConsumoP3Kwh() { return consumoP3Kwh; }
    public double getCosteEnergia() { return costeEnergia; }
    public double getCosteP1() { return consumoP1Kwh * precioP1; }
    public double getCosteP2() { return consumoP2Kwh * precioP2; }
    public double getCosteP3() { return consumoP3Kwh * precioP3; }

    /** Precio representativo para mostrar en líneas de un solo periodo. */
    public double getPrecioKwh() {
        if (consumoP1Kwh > 0 && consumoP2Kwh == 0 && consumoP3Kwh == 0) return precioP1;
        if (consumoP2Kwh > 0 && consumoP1Kwh == 0 && consumoP3Kwh == 0) return precioP2;
        if (consumoP3Kwh > 0 && consumoP1Kwh == 0 && consumoP2Kwh == 0) return precioP3;
        return consumoKwh == 0 ? 0 : costeEnergia / consumoKwh;
    }

    /** Mantiene compatibilidad con ESIOS medio o precio manual único. */
    public void actualizarPrecioKwh(double nuevoPrecioKwh) {
        actualizarPrecios(nuevoPrecioKwh, nuevoPrecioKwh, nuevoPrecioKwh);
    }

    /** Recalcula la línea con precios distintos para P1, P2 y P3. */
    public void actualizarPrecios(double nuevoP1, double nuevoP2, double nuevoP3) {
        this.precioP1 = nuevoP1;
        this.precioP2 = nuevoP2;
        this.precioP3 = nuevoP3;
        recalcularCoste();
    }

    public String getResumenPeriodos() {
        if (!PERIODO_AUTO.equals(periodo)) {
            return periodo;
        }
        return String.format(java.util.Locale.getDefault(),
                "Auto %.2fh · P1 %.2f kWh · P2 %.2f kWh · P3 %.2f kWh",
                horaInicio == null ? 0.0 : horaInicio,
                consumoP1Kwh, consumoP2Kwh, consumoP3Kwh);
    }

    private void recalcularCoste() {
        this.costeEnergia = getCosteP1() + getCosteP2() + getCosteP3();
    }
}
