package com.example.recordatoriosapp.data.remote

// Modelo de transferencia para el servicio remoto
data class RecordatorioDto(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val prioridad: String,
    val completado: Boolean,
    val fechaCreacion: Long,
    val updatedAt: Long
)