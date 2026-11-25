package com.example.recordatoriosapp

import com.example.recordatoriosapp.data.remote.ModoServidorSimulado

data class CrearRecordatorioUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val prioridad: Prioridad = Prioridad.Media,
    val errorTitulo: String? = null,
    val enviando: Boolean = false,
    val guardado: Boolean = false,
    val errorGeneral: String? = null
)

sealed interface ListaRecordatoriosUiState {
    object Cargando : ListaRecordatoriosUiState
    object Vacia : ListaRecordatoriosUiState
    data class ConDatos(val recordatorios: List<Recordatorio>) : ListaRecordatoriosUiState
    data class Error(val mensaje: String) : ListaRecordatoriosUiState
}

// Estado de la sincronizacion y modo de prueba del servidor
data class SyncUiState(
    val sincronizando: Boolean = false,
    val mensaje: String? = null,
    val esError: Boolean = false,
    val modoServidor: ModoServidorSimulado = ModoServidorSimulado.NORMAL
)