package com.example.consumoapp.factura;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos iniciales importados desde el Excel verificado.
 *
 * N/D del Excel se convierte en null para no inventar potencias.
 */
public final class ElectrodomesticoSeed {

    private ElectrodomesticoSeed() {}

    public static List<ElectrodomesticoEntity> crearDatosIniciales() {
        List<ElectrodomesticoEntity> lista = new ArrayList<>();

        lista.add(new ElectrodomesticoEntity(1, "Climatización/ACS", "Aire Acondicionado", 900.0, 2000.0, "N/D", "N/D", "N/D", "Endesa, Iberdrola, Imagina Energía, Papernest", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(2, "Limpieza", "Aspiradora", 1000.0, 1500.0, "1 hora", "1,4", "kWh/semana", "Iberdrola, Papernest", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(3, "Cocina", "Cafetera", null, null, "2 horas", "1,462", "kWh/semana", "Papernest", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(4, "Climatización/ACS", "Calefacción", 1000.0, 3000.0, "N/D", "N/D", "N/D", "Iberdrola, Imagina Energía, Papernest", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(5, "Cocina", "Freidora De Aire", 1000.0, 1000.0, "15 minutos/día", "7,5", "kWh/mes", "Endesa", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(6, "Cocina", "Frigorífico", 200.0, 1000.0, "24 horas/día; 24 horas/día; 720 horas/mes", "144; 150–175; 250–290; 2600; 300–325; 650–700; 700", "kWh/año; kWh/mes", "Endesa, Iberdrola, Imagina Energía, Papernest", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(7, "Cocina", "Horno", 1000.0, 3000.0, "2 horas/día; 60 horas/mes; 3 horas; un par de horas a la semana", "150; 3,52; 47,4", "kWh/año; kWh/mes; kWh/semana", "Endesa, Iberdrola, Imagina Energía, Papernest", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(8, "Iluminación", "Iluminación", null, null, "290 horas", "20,396; 400–500", "kWh/año; kWh/semana", "Endesa, Papernest", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(9, "Lavado y limpieza", "Lavadora", 1430.0, 2650.0, "2 horas/día; 90 horas/mes; 3 horas", "140; 250–300; 29,7; 360; 4,438; 570; 650", "kWh; kWh/año; kWh/mes; kWh/semana", "Endesa, Iberdrola, Imagina Energía, Papernest", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(10, "Cocina", "Lavavajillas", 900.0, 2500.0, "4 horas", "250–300; 5,1", "kWh/año; kWh/semana", "Endesa, Iberdrola, Imagina Energía, Papernest", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(11, "Cocina", "Microondas", 100.0, 1500.0, "1 hora; 1 hora/día; 60 horas/mes", "2; 72", "kWh/mes; kWh/semana", "Iberdrola, Imagina Energía, Papernest", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(12, "Ocio e informática", "Ordenador", null, null, "30 horas; 4 horas/día; 60 horas/mes", "0,06; 2,032; 200–250", "kWh/año; kWh/mes; kWh/semana", "Endesa, Papernest", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(13, "Lavado y limpieza", "Plancha", null, null, "1 hora", "2", "kWh/semana", "Papernest", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(14, "Lavado y limpieza", "Secadora", 300.0, 300.0, "N/D", "N/D", "N/D", "Imagina Energía", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(15, "Consumo fantasma", "Standby", null, null, "N/D", "300–350", "kWh/año", "Endesa", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(16, "Ocio e informática", "Televisión", 150.0, 500.0, "18 horas; 4 horas/día; 120 horas/mes", "150–200; 18,72; 2,1", "kWh/año; kWh/mes; kWh/semana", "Endesa, Iberdrola, Imagina Energía, Papernest", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(17, "Climatización/ACS", "Termo Eléctrico Acs", null, null, "N/D", "N/D", "N/D", "Endesa", "Verificado / consolidado"));
        lista.add(new ElectrodomesticoEntity(18, "Cocina", "Vitrocerámica", 900.0, 2100.0, "5 horas", "2,16", "kWh/semana", "Iberdrola, Imagina Energía, Papernest", "Verificado / consolidado"));

        return lista;
    }
}
