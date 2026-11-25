package com.example.recordatoriosapp

import com.example.recordatoriosapp.data.local.SyncStatus

sealed class Prioridad(val nombre: String) {
    object Alta : Prioridad("Alta")
    object Media : Prioridad("Media")
    object Baja : Prioridad("Baja")
}

sealed class Estado(val nombre: String) {
    object Pendiente : Estado("Pendiente")
    object Completada : Estado("Completada")
}

data class Recordatorio(
    val id: String,
    val titulo: String,
    val categoria: String,
    val prioridad: Prioridad,
    val estado: Estado,
    val descripcion: String? = null,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)