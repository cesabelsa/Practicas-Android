package com.example.consumoapp.factura.data;

import android.content.Context;

import com.example.consumoapp.esios.data.AppDatabase;
import com.example.consumoapp.esios.data.PrecioLuzEntity;
import com.example.consumoapp.esios.settings.EsiosPreferences;
import com.example.consumoapp.factura.ElectrodomesticoEntity;
import com.example.consumoapp.factura.ElectrodomesticoSeed;
import com.example.consumoapp.factura.SimulacionFacturaEntity;
import com.example.consumoapp.regulacion.domain.ConstantesFactura;
import com.example.consumoapp.regulacion.domain.ConstantesReguladasRepository;
import com.example.consumoapp.tarifas.data.ComercializadoraEntity;
import com.example.consumoapp.tarifas.data.TarifaComercialEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Único punto de acceso a los datos usados por el simulador.
 * Esta clase no conoce vistas ni Activities, por lo que puede probarse aisladamente.
 */
public final class SimuladorFacturaRepository {
    private final Context appContext;
    private final AppDatabase database;
    private final ConstantesReguladasRepository constantesRepository;

    public SimuladorFacturaRepository(Context context) {
        appContext = context.getApplicationContext();
        database = AppDatabase.getInstance(appContext);
        constantesRepository = new ConstantesReguladasRepository(database);
    }

    public List<ComercializadoraEntity> cargarComercializadoras() throws InterruptedException {
        List<ComercializadoraEntity> datos = database.tarifaDao().obtenerComercializadoras();

        // En el primer arranque, la importación del CSV puede seguir en curso.
        for (int intento = 0; datos.isEmpty() && intento < 10; intento++) {
            Thread.sleep(150);
            datos = database.tarifaDao().obtenerComercializadoras();
        }
        return datos;
    }

    public List<TarifaComercialEntity> cargarTarifas(long comercializadoraId) {
        return database.tarifaDao().obtenerTarifasDeComercializadora(comercializadoraId);
    }

    public ConstantesFactura cargarConstantes(String fecha) {
        return constantesRepository.obtenerParaFactura(fecha);
    }

    public List<String> cargarCategorias() {
        if (database.electrodomesticoDao().contar() == 0) {
            database.electrodomesticoDao().insertarTodos(ElectrodomesticoSeed.crearDatosIniciales());
        }

        List<String> categorias = new ArrayList<>(database.electrodomesticoDao().listarCategorias());
        categorias.add(0, "Todos");
        return categorias;
    }

    public List<ElectrodomesticoEntity> cargarElectrodomesticos(String categoria) {
        if ("Todos".equals(categoria)) {
            return database.electrodomesticoDao().listarTodos();
        }
        return database.electrodomesticoDao().listarPorCategoria(categoria);
    }

    public PreciosEsios cargarUltimosPreciosEsios() {
        String fecha = database.precioLuzDao().obtenerFechaMasReciente();
        List<PrecioLuzEntity> precios = fecha == null
                ? new ArrayList<>()
                : database.precioLuzDao().listarPorFechaYZona(
                        fecha,
                        EsiosPreferences.nombreZona(EsiosPreferences.getGeoId(appContext))
                );
        return new PreciosEsios(fecha, precios);
    }

    public void guardarSimulacion(SimulacionFacturaEntity simulacion) {
        database.simulacionFacturaDao().insertar(simulacion);
    }

    /** Resultado conjunto para no publicar fecha y precios por separado. */
    public static final class PreciosEsios {
        public final String fecha;
        public final List<PrecioLuzEntity> precios;

        public PreciosEsios(String fecha, List<PrecioLuzEntity> precios) {
            this.fecha = fecha;
            this.precios = precios;
        }
    }
}
