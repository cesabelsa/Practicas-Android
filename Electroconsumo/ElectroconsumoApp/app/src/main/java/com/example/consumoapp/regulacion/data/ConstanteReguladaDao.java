package com.example.consumoapp.regulacion.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ConstanteReguladaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarTodos(List<ConstanteReguladaEntity> constantes);

    @Query("SELECT COUNT(*) FROM constante_regulada")
    int contar();

    @Query("SELECT * FROM constante_regulada " +
            "WHERE codigo = :codigo AND periodo = :periodo AND activa = 1 " +
            "AND fechaDesde <= :fecha " +
            "AND (fechaHasta IS NULL OR fechaHasta >= :fecha) " +
            "ORDER BY fechaDesde DESC LIMIT 1")
    ConstanteReguladaEntity buscarVigente(String codigo, String periodo, String fecha);

    @Query("SELECT * FROM constante_regulada ORDER BY codigo, periodo, fechaDesde DESC")
    List<ConstanteReguladaEntity> listarTodas();
}
