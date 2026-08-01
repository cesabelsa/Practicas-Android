package com.example.consumoapp.esios.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/** Operaciones locales para precios horarios e histórico ESIOS. */
@Dao
public interface PrecioLuzDao {

    // REPLACE permite actualizar una hora ya descargada sin crear duplicados.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarTodos(List<PrecioLuzEntity> precios);

    @Query("SELECT * FROM precios_luz ORDER BY fechaHora ASC")
    List<PrecioLuzEntity> listarTodos();

    @Query("SELECT * FROM precios_luz WHERE substr(fechaHora, 1, 10) = :fecha " +
            "ORDER BY fechaHora ASC")
    List<PrecioLuzEntity> listarPorFecha(String fecha);

    @Query("SELECT * FROM precios_luz WHERE substr(fechaHora, 1, 10) = :fecha " +
            "AND zona = :zona ORDER BY fechaHora ASC")
    List<PrecioLuzEntity> listarPorFechaYZona(String fecha, String zona);

    @Query("SELECT * FROM precios_luz WHERE fechaHora >= :inicio AND fechaHora <= :fin " +
            "ORDER BY fechaHora ASC")
    List<PrecioLuzEntity> listarPorRango(String inicio, String fin);

    @Query("SELECT MAX(substr(fechaHora, 1, 10)) FROM precios_luz")
    String obtenerFechaMasReciente();

    // Momento real de la descarga local más reciente.
    @Query("SELECT MAX(fechaDescarga) FROM precios_luz")
    Long obtenerUltimaFechaDescarga();

    // Devuelve las fechas disponibles sin repetir, de más reciente a más antigua.
    @Query("SELECT DISTINCT substr(fechaHora, 1, 10) FROM precios_luz " +
            "ORDER BY substr(fechaHora, 1, 10) DESC")
    List<String> listarFechasDisponibles();

    // Número de registros horarios guardados para un día concreto.
    @Query("SELECT COUNT(*) FROM precios_luz WHERE substr(fechaHora, 1, 10) = :fecha")
    int contarPorFecha(String fecha);

    // Número total de registros horarios almacenados.
    @Query("SELECT COUNT(*) FROM precios_luz")
    int contarTodos();

    // Primera fecha disponible en la caché local.
    @Query("SELECT MIN(substr(fechaHora, 1, 10)) FROM precios_luz")
    String obtenerFechaMasAntigua();

    @Query("SELECT AVG(precio) FROM precios_luz")
    Double obtenerPrecioMedioMwh();

    @Query("SELECT AVG(precioKwh) FROM precios_luz WHERE substr(fechaHora, 1, 10) = :fecha")
    Double obtenerPrecioMedioKwhPorFecha(String fecha);

    @Query("DELETE FROM precios_luz WHERE fechaHora < :limiteIso")
    void borrarAnterioresA(String limiteIso);

    // Elimina únicamente el día seleccionado.
    @Query("DELETE FROM precios_luz WHERE substr(fechaHora, 1, 10) = :fecha")
    void borrarFecha(String fecha);

    @Query("DELETE FROM precios_luz")
    void borrarTodos();
}
