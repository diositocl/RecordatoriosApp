package com.example.recordatoriosapp

// Estado inmutable para el Formulario de Creación
data class CrearRecordatorioUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val prioridad: Prioridad = Prioridad.Media, // Usamos tu clase del M1
    val errorTitulo: String? = null,
    val enviando: Boolean = false,
    val guardado: Boolean = false,
    val errorGeneral: String? = null
)

// Estados mutuamente excluyentes para el Listado
sealed interface ListaRecordatoriosUiState {
    object Cargando : ListaRecordatoriosUiState
    object Vacia : ListaRecordatoriosUiState
    data class ConDatos(val recordatorios: List<Recordatorio>) : ListaRecordatoriosUiState
    data class Error(val mensaje: String) : ListaRecordatoriosUiState
}