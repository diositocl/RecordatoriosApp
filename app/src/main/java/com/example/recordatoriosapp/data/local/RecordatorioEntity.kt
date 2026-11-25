package com.example.recordatoriosapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SyncStatus {
    SYNCED,
    PENDING,
    PENDING_DELETE,
    ERROR
}

// Indice unico en remote_id para evitar duplicados al sincronizar
@Entity(
    tableName = "recordatorios",
    indices = [
        Index(value = ["remote_id"], unique = true)
    ]
)
data class RecordatorioEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "remote_id")
    val remoteId: String?,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val prioridad: String,
    val completado: Boolean,
    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus
)