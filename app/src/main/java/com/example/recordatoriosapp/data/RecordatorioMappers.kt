package com.example.recordatoriosapp.data

import com.example.recordatoriosapp.Estado
import com.example.recordatoriosapp.Prioridad
import com.example.recordatoriosapp.Recordatorio
import com.example.recordatoriosapp.data.local.RecordatorioEntity
import com.example.recordatoriosapp.data.local.SyncStatus
import com.example.recordatoriosapp.data.remote.RecordatorioDto

// Mapeo de texto guardado a Prioridad
private fun String.toPrioridad(): Prioridad = when (this) {
    Prioridad.Alta.nombre -> Prioridad.Alta
    Prioridad.Baja.nombre -> Prioridad.Baja
    else -> Prioridad.Media
}

// Entity -> Modelo de dominio/UI
fun RecordatorioEntity.toDomain(): Recordatorio {
    return Recordatorio(
        id = id,
        titulo = titulo,
        categoria = categoria,
        prioridad = prioridad.toPrioridad(),
        estado = if (completado) Estado.Completada else Estado.Pendiente,
        descripcion = descripcion.ifBlank { null },
        fechaCreacion = fechaCreacion,
        syncStatus = syncStatus
    )
}

// Modelo de dominio/UI -> Entity
fun Recordatorio.toEntity(
    remoteId: String? = null,
    updatedAt: Long = System.currentTimeMillis(),
    syncStatus: SyncStatus = this.syncStatus
): RecordatorioEntity {
    return RecordatorioEntity(
        id = id,
        remoteId = remoteId,
        titulo = titulo,
        descripcion = descripcion.orEmpty(),
        categoria = categoria,
        prioridad = prioridad.nombre,
        completado = estado is Estado.Completada,
        fechaCreacion = fechaCreacion,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )
}

// Entity -> DTO para enviar por Retrofit
fun RecordatorioEntity.toDto(): RecordatorioDto {
    return RecordatorioDto(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        categoria = categoria,
        prioridad = prioridad,
        completado = completado,
        fechaCreacion = fechaCreacion,
        updatedAt = updatedAt
    )
}

// DTO recibido del servidor -> Entity para Room
fun RecordatorioDto.toEntity(): RecordatorioEntity {
    return RecordatorioEntity(
        id = id,
        remoteId = id,
        titulo = titulo,
        descripcion = descripcion,
        categoria = categoria,
        prioridad = prioridad,
        completado = completado,
        fechaCreacion = fechaCreacion,
        updatedAt = updatedAt,
        syncStatus = SyncStatus.SYNCED
    )
}