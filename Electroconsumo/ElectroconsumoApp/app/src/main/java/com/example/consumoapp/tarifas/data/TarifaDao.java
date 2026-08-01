package com.example.consumoapp.tarifas.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Operaciones de SQLite relacionadas con comercializadoras y tarifas.
 */
@Dao
public interface TarifaDao {

    @Query("SELECT COUNT(*) FROM tarifa_comercial")
    int contarTarifas();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertarComercializadora(ComercializadoraEntity comercializadora);

    @Query("SELECT id FROM comercializadora WHERE nombre = :nombre LIMIT 1")
    Long buscarIdComercializadora(String nombre);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertarTarifa(TarifaComercialEntity tarifa);

    @Query("SELECT * FROM comercializadora ORDER BY nombre")
    List<ComercializadoraEntity> obtenerComercializadoras();

    @Query("SELECT * FROM tarifa_comercial WHERE comercializadoraId = :comercializadoraId AND activa = 1 ORDER BY nombre")
    List<TarifaComercialEntity> obtenerTarifasDeComercializadora(long comercializadoraId);

    @Query("SELECT t.*, c.nombre AS nombreComercializadora " +
            "FROM tarifa_comercial t " +
            "INNER JOIN comercializadora c ON c.id = t.comercializadoraId " +
            "WHERE t.activa = 1 " +
            "ORDER BY c.nombre, t.nombre")
    List<TarifaConComercializadora> obtenerTodasLasTarifas();
}
