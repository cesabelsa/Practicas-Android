package com.example.consumoapp.factura;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Operaciones SQLite para los electrodomésticos del simulador.
 */
@Dao
public interface ElectrodomesticoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarTodos(List<ElectrodomesticoEntity> electrodomesticos);

    @Query("SELECT COUNT(*) FROM electrodomesticos")
    int contar();

    @Query("SELECT DISTINCT categoria FROM electrodomesticos ORDER BY categoria ASC")
    List<String> listarCategorias();

    @Query("SELECT * FROM electrodomesticos ORDER BY nombre ASC")
    List<ElectrodomesticoEntity> listarTodos();

    @Query("SELECT * FROM electrodomesticos WHERE categoria = :categoria ORDER BY nombre ASC")
    List<ElectrodomesticoEntity> listarPorCategoria(String categoria);
}
