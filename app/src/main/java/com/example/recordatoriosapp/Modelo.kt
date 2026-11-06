package com.example.recordatoriosapp

// Uso de sealed class para estados limitados y controlados
sealed class Prioridad(val nombre: String) {
    object Alta : Prioridad("Alta")
    object Media : Prioridad("Media")
    object Baja : Prioridad("Baja")
}

sealed class Estado(val nombre: String) {
    object Pendiente : Estado("Pendiente")
    object Completada : Estado("Completada")
}

// Uso de data class para modelar la entidad principal
data class Recordatorio(
    val id: Long,
    val titulo: String,
    val categoria: String,
    val prioridad: Prioridad,
    val estado: Estado,
    val descripcion: String? = null // Null safety para la nota opcional
)