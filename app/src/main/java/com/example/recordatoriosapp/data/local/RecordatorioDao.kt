package com.example.recordatoriosapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {

    // Listado completo observable (excluye los marcados para borrar)
    @Query("SELECT * FROM recordatorios WHERE sync_status != 'PENDING_DELETE' ORDER BY fecha_creacion DESC")
    fun observeAll(): Flow<List<RecordatorioEntity>>

    // Detalle de un recordatorio por ID
    @Query("SELECT * FROM recordatorios WHERE id = :id")
    fun observeById(id: String): Flow<RecordatorioEntity?>

    // Filtro por prioridad
    @Query("SELECT * FROM recordatorios WHERE prioridad = :prioridad AND sync_status != 'PENDING_DELETE' ORDER BY fecha_creacion DESC")
    fun observeByPrioridad(prioridad: String): Flow<List<RecordatorioEntity>>

    // Consultas puntuales para sincronizacion
    @Query("SELECT * FROM recordatorios WHERE id = :id")
    suspend fun findById(id: String): RecordatorioEntity?

    @Query("SELECT * FROM recordatorios WHERE sync_status = 'PENDING' OR sync_status = 'PENDING_DELETE' ORDER BY updated_at ASC")
    suspend fun findPendingSync(): List<RecordatorioEntity>

    @Upsert
    suspend fun save(recordatorio: RecordatorioEntity)

    @Upsert
    suspend fun saveAll(recordatorios: List<RecordatorioEntity>)

    @Query("UPDATE recordatorios SET completado = :completado, updated_at = :updatedAt, sync_status = 'PENDING' WHERE id = :id")
    suspend fun updateCompletado(id: String, completado: Boolean, updatedAt: Long)

    @Query("DELETE FROM recordatorios WHERE id = :id")
    suspend fun deleteById(id: String)

    @Delete
    suspend fun delete(recordatorio: RecordatorioEntity)
}