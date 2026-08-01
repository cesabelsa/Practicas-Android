package com.example.consumoapp.factura.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.consumoapp.factura.ElectrodomesticoEntity;
import com.example.consumoapp.factura.data.SimuladorFacturaRepository;
import com.example.consumoapp.regulacion.domain.ConstantesFactura;
import com.example.consumoapp.tarifas.data.ComercializadoraEntity;
import com.example.consumoapp.tarifas.data.TarifaComercialEntity;

import java.util.Collections;
import java.util.List;

/**
 * Estado inmutable y reproducible de la pantalla del simulador.
 *
 * Los mensajes de una sola ejecución no forman parte de este objeto: se mantienen
 * como eventos independientes para evitar que reaparezcan tras una rotación.
 */
public final class SimuladorFacturaUiState {
    @NonNull public final List<ComercializadoraEntity> comercializadoras;
    @NonNull public final List<TarifaComercialEntity> tarifas;
    @NonNull public final List<String> categorias;
    @NonNull public final List<ElectrodomesticoEntity> electrodomesticos;
    @Nullable public final ConstantesCargadas constantes;
    @Nullable public final SimuladorFacturaRepository.PreciosEsios preciosEsios;
    public final boolean cargando;

    private SimuladorFacturaUiState(
            @NonNull List<ComercializadoraEntity> comercializadoras,
            @NonNull List<TarifaComercialEntity> tarifas,
            @NonNull List<String> categorias,
            @NonNull List<ElectrodomesticoEntity> electrodomesticos,
            @Nullable ConstantesCargadas constantes,
            @Nullable SimuladorFacturaRepository.PreciosEsios preciosEsios,
            boolean cargando
    ) {
        this.comercializadoras = comercializadoras;
        this.tarifas = tarifas;
        this.categorias = categorias;
        this.electrodomesticos = electrodomesticos;
        this.constantes = constantes;
        this.preciosEsios = preciosEsios;
        this.cargando = cargando;
    }

    @NonNull
    public static SimuladorFacturaUiState inicial() {
        return new SimuladorFacturaUiState(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                false
        );
    }

    @NonNull
    public SimuladorFacturaUiState conComercializadoras(@Nullable List<ComercializadoraEntity> valor) {
        return copiar(listaSegura(valor), tarifas, categorias, electrodomesticos,
                constantes, preciosEsios, cargando);
    }

    @NonNull
    public SimuladorFacturaUiState conTarifas(@Nullable List<TarifaComercialEntity> valor) {
        return copiar(comercializadoras, listaSegura(valor), categorias, electrodomesticos,
                constantes, preciosEsios, cargando);
    }

    @NonNull
    public SimuladorFacturaUiState conCategorias(@Nullable List<String> valor) {
        return copiar(comercializadoras, tarifas, listaSegura(valor), electrodomesticos,
                constantes, preciosEsios, cargando);
    }

    @NonNull
    public SimuladorFacturaUiState conElectrodomesticos(@Nullable List<ElectrodomesticoEntity> valor) {
        return copiar(comercializadoras, tarifas, categorias, listaSegura(valor),
                constantes, preciosEsios, cargando);
    }

    @NonNull
    public SimuladorFacturaUiState conConstantes(
            @Nullable ConstantesCargadas valor
    ) {
        return copiar(comercializadoras, tarifas, categorias, electrodomesticos,
                valor, preciosEsios, cargando);
    }

    @NonNull
    public SimuladorFacturaUiState conPreciosEsios(
            @Nullable SimuladorFacturaRepository.PreciosEsios valor
    ) {
        return copiar(comercializadoras, tarifas, categorias, electrodomesticos,
                constantes, valor, cargando);
    }

    @NonNull
    public SimuladorFacturaUiState conCargando(boolean valor) {
        return copiar(comercializadoras, tarifas, categorias, electrodomesticos,
                constantes, preciosEsios, valor);
    }

    private SimuladorFacturaUiState copiar(
            List<ComercializadoraEntity> nuevasComercializadoras,
            List<TarifaComercialEntity> nuevasTarifas,
            List<String> nuevasCategorias,
            List<ElectrodomesticoEntity> nuevosElectrodomesticos,
            ConstantesCargadas nuevasConstantes,
            SimuladorFacturaRepository.PreciosEsios nuevosPrecios,
            boolean nuevoCargando
    ) {
        return new SimuladorFacturaUiState(
                nuevasComercializadoras,
                nuevasTarifas,
                nuevasCategorias,
                nuevosElectrodomesticos,
                nuevasConstantes,
                nuevosPrecios,
                nuevoCargando
        );
    }

    private static <T> List<T> listaSegura(@Nullable List<T> valor) {
        return valor == null ? Collections.emptyList() : Collections.unmodifiableList(valor);
    }

    /** Resultado de cargar las constantes junto con el contexto solicitado por la UI. */
    public static final class ConstantesCargadas {
        public final String fecha;
        public final ConstantesFactura constantes;
        public final boolean mostrarMensaje;

        public ConstantesCargadas(
                String fecha,
                ConstantesFactura constantes,
                boolean mostrarMensaje
        ) {
            this.fecha = fecha;
            this.constantes = constantes;
            this.mostrarMensaje = mostrarMensaje;
        }
    }

}
