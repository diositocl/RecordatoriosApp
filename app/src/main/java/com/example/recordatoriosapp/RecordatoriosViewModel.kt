package com.example.recordatoriosapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecordatoriosViewModel(
    private val repository: RecordatoriosRepository
) : ViewModel() {

    // --- ESTADO Y LÓGICA DEL LISTADO ---
    private val _listaUiState = MutableStateFlow<ListaRecordatoriosUiState>(ListaRecordatoriosUiState.Cargando)
    val listaUiState: StateFlow<ListaRecordatoriosUiState> = _listaUiState.asStateFlow()

    init {
        cargarRecordatorios()
    }

    fun cargarRecordatorios() {
        viewModelScope.launch {
            _listaUiState.value = ListaRecordatoriosUiState.Cargando
            delay(1500) // Simulación de carga asincrónica

            // Recolectamos los datos
            repository.observarRecordatorios().collect { recordatorios ->
                if (recordatorios.isEmpty()) {
                    _listaUiState.value = ListaRecordatoriosUiState.Vacia
                } else {
                    _listaUiState.value = ListaRecordatoriosUiState.ConDatos(recordatorios)
                }
            }
        }
    }

    // --- ESTADO Y LÓGICA DEL FORMULARIO DE CREACIÓN ---
    private val _crearUiState = MutableStateFlow(CrearRecordatorioUiState())
    val crearUiState: StateFlow<CrearRecordatorioUiState> = _crearUiState.asStateFlow()

    fun actualizarTitulo(nuevoTitulo: String) {
        _crearUiState.update {
            it.copy(
                titulo = nuevoTitulo,
                // Validación: al menos 4 caracteres (ignorando espacios)
                errorTitulo = if (nuevoTitulo.trim().length < 4 && nuevoTitulo.isNotEmpty()) "Mínimo 4 caracteres" else null
            )
        }
    }

    fun actualizarDescripcion(nuevaDesc: String) {
        // Validación: máximo 200 caracteres
        if (nuevaDesc.length <= 200) {
            _crearUiState.update { it.copy(descripcion = nuevaDesc) }
        }
    }

    fun actualizarPrioridad(nuevaPrioridad: Prioridad) {
        _crearUiState.update { it.copy(prioridad = nuevaPrioridad) }
    }

    fun guardarRecordatorio(categoria: String) {
        val estadoActual = _crearUiState.value

        if (estadoActual.titulo.trim().length < 4) {
            _crearUiState.update { it.copy(errorTitulo = "Mínimo 4 caracteres") }
            return
        }

        viewModelScope.launch {
            _crearUiState.update { it.copy(enviando = true, errorGeneral = null) }
            delay(1500) // Simulación de guardado asincrónico

            try {
                // Simulamos un ID único usando la hora actual
                val nuevoId = System.currentTimeMillis()
                val nuevoRecordatorio = Recordatorio(
                    id = nuevoId,
                    titulo = estadoActual.titulo.trim(),
                    categoria = categoria,
                    prioridad = estadoActual.prioridad,
                    estado = Estado.Pendiente,
                    descripcion = estadoActual.descripcion.ifBlank { null }
                )
                repository.guardar(nuevoRecordatorio)

                // Reiniciamos el formulario al terminar con éxito
                _crearUiState.update { CrearRecordatorioUiState(guardado = true) }
            } catch (e: Exception) {
                _crearUiState.update { it.copy(enviando = false, errorGeneral = "Error al guardar") }
            }
        }
    }

    fun resetearGuardado() {
        _crearUiState.update { it.copy(guardado = false) }
    }
}


class RecordatoriosViewModelFactory(private val repository: RecordatoriosRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordatoriosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordatoriosViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}