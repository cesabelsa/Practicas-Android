package com.example.consumoapp.factura;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface SimulacionFacturaDao {
    @Insert long insertar(SimulacionFacturaEntity simulacion);

    @Query("SELECT * FROM simulacion_factura WHERE usuarioId = :usuarioId ORDER BY fechaCreacion DESC")
    List<SimulacionFacturaEntity> listarTodas(long usuarioId);

    @Query("SELECT * FROM simulacion_factura WHERE id = :simulacionId AND usuarioId = :usuarioId LIMIT 1")
    SimulacionFacturaEntity obtenerPorIdYUsuario(long simulacionId, long usuarioId);

    @Delete void eliminar(SimulacionFacturaEntity simulacion);

    @Query("DELETE FROM simulacion_factura WHERE usuarioId = :usuarioId")
    void eliminarTodas(long usuarioId);

    @Query("SELECT COUNT(*) FROM simulacion_factura WHERE usuarioId = :usuarioId")
    int contar(long usuarioId);
}
