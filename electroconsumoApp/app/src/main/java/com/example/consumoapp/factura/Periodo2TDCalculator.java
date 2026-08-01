package com.example.consumoapp.factura;

/**
 * Utilidades para clasificar y repartir un intervalo de uso entre los
 * periodos de energía 2.0TD.
 */
public final class Periodo2TDCalculator {

    private Periodo2TDCalculator() {
        // Clase de utilidades: no necesita instancias.
    }

    /** Resultado del reparto diario de horas entre P1, P2 y P3. */
    public static final class RepartoHoras {
        private double horasP1;
        private double horasP2;
        private double horasP3;

        public double getHorasP1() { return horasP1; }
        public double getHorasP2() { return horasP2; }
        public double getHorasP3() { return horasP3; }

        private void sumar(String periodo, double horas) {
            if (LineaSimuladorFactura.PERIODO_P1.equals(periodo)) {
                horasP1 += horas;
            } else if (LineaSimuladorFactura.PERIODO_P2.equals(periodo)) {
                horasP2 += horas;
            } else {
                horasP3 += horas;
            }
        }
    }

    /** Clasifica una hora concreta. */
    public static String obtenerPeriodo(double hora, boolean diaValleCompleto) {
        if (hora < 0.0 || hora >= 24.0) {
            throw new IllegalArgumentException("La hora debe estar entre 0 y menos de 24");
        }

        if (diaValleCompleto) {
            return LineaSimuladorFactura.PERIODO_P3;
        }

        if ((hora >= 10.0 && hora < 14.0) || (hora >= 18.0 && hora < 22.0)) {
            return LineaSimuladorFactura.PERIODO_P1;
        }

        if ((hora >= 8.0 && hora < 10.0)
                || (hora >= 14.0 && hora < 18.0)
                || hora >= 22.0) {
            return LineaSimuladorFactura.PERIODO_P2;
        }

        return LineaSimuladorFactura.PERIODO_P3;
    }

    /**
     * Reparte una duración continua entre P1, P2 y P3.
     *
     * Se avanza hasta el siguiente cambio de periodo o hasta medianoche. Si el
     * uso supera las 24 horas, los días completos siguientes mantienen el mismo
     * tipo de día elegido por el usuario.
     */
    public static RepartoHoras repartirHoras(double horaInicio, double duracionHoras,
                                              boolean diaValleCompleto) {
        if (horaInicio < 0.0 || horaInicio >= 24.0) {
            throw new IllegalArgumentException("La hora de inicio debe estar entre 0 y menos de 24");
        }
        if (duracionHoras <= 0.0) {
            throw new IllegalArgumentException("La duración debe ser mayor que cero");
        }

        RepartoHoras reparto = new RepartoHoras();
        double horaActual = horaInicio;
        double restante = duracionHoras;

        while (restante > 0.000001) {
            String periodo = obtenerPeriodo(horaActual, diaValleCompleto);
            double siguienteCambio = obtenerSiguienteCambio(horaActual, diaValleCompleto);
            double tramoDisponible = siguienteCambio - horaActual;

            // Protección frente a errores de redondeo justo en medianoche.
            if (tramoDisponible <= 0.000001) {
                tramoDisponible = 24.0 - horaActual;
            }

            double tramo = Math.min(restante, tramoDisponible);
            reparto.sumar(periodo, tramo);
            restante -= tramo;
            horaActual += tramo;

            if (horaActual >= 24.0 - 0.000001) {
                horaActual = 0.0;
            }
        }

        return reparto;
    }

    /** Devuelve la siguiente frontera horaria del periodo actual. */
    private static double obtenerSiguienteCambio(double hora, boolean diaValleCompleto) {
        if (diaValleCompleto) {
            return 24.0;
        }

        double[] fronteras = {8.0, 10.0, 14.0, 18.0, 22.0, 24.0};
        for (double frontera : fronteras) {
            if (frontera > hora + 0.000001) {
                return frontera;
            }
        }
        return 24.0;
    }
}
