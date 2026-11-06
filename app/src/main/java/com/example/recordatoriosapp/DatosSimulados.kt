package com.example.recordatoriosapp

// Uso de Set para categorías que no deben repetirse
val categoriasUnicas: Set<String> = setOf("Trabajo", "Personal", "Estudio", "Salud")

// Uso de List de solo lectura para inicializar los datos
val recordatoriosSimulados: List<Recordatorio> = listOf(
    Recordatorio(
        id = 1L,
        titulo = "Terminar reporte mensual",
        categoria = "Trabajo",
        prioridad = Prioridad.Alta,
        estado = Estado.Pendiente
    ),
    Recordatorio(
        id = 2L,
        titulo = "Comprar mercaderia",
        categoria = "Personal",
        prioridad = Prioridad.Media,
        estado = Estado.Pendiente,
        descripcion = "Leche, pan, huevos, carne, verduras"
    ),
    Recordatorio(
        id = 3L,
        titulo = "Leer capítulo 4",
        categoria = "Estudio",
        prioridad = Prioridad.Baja,
        estado = Estado.Completada
    ),
    Recordatorio(
        id = 4L,
        titulo = "Cita médica",
        categoria = "Salud",
        prioridad = Prioridad.Alta,
        estado = Estado.Pendiente
    )
)