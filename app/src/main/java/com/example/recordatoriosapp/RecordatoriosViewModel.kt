package com.example.recordatoriosapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recordatoriosapp.data.OrdenRecordatorio
import com.example.recordatoriosapp.data.UserPreferences
import com.example.recordatoriosapp.data.UserPreferencesRepository
import com.example.recordatoriosapp.data.remote.ModoServidorSimulado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class RecordatoriosViewModel(
    private val repository: RecordatoriosRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Preferencias desde DataStore
    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    // Listado reactivo combinado con filtros y orden de DataStore
    val listaUiState: StateFlow<ListaRecordatoriosUiState> = combine(
        repository.observarRecordatorios(),
        preferencesRepository.preferences
    ) { recordatorios, prefs ->
        val filtrados = if (prefs.mostrarCompletados) {
            recordatorios
        } else {
            recordatorios.filter { it.estado !is Estado.Completada }
        }

        val ordenados = when (prefs.ordenListado) {
            OrdenRecordatorio.POR_FECHA -> filtrados.sortedByDescending { it.fechaCreacion }
            OrdenRecordatorio.POR_PRIORIDAD -> filtrados.sortedBy { pesoPrioridad(it.prioridad) }
        }

        if (ordenados.isEmpty()) {
            ListaRecordatoriosUiState.Vacia
        } else {
            ListaRecordatoriosUiState.ConDatos(ordenados)
        }
    }
        .catch { emit(ListaRecordatoriosUiState.Error("Error al leer datos locales")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ListaRecordatoriosUiState.Cargando
        )

    private val _syncUiState = MutableStateFlow(SyncUiState())
    val syncUiState: StateFlow<SyncUiState> = _syncUiState.asStateFlow()

    private val _crearUiState = MutableStateFlow(CrearRecordatorioUiState())
    val crearUiState: StateFlow<CrearRecordatorioUiState> = _crearUiState.asStateFlow()

    init {
        sincronizar()
    }

    fun observarPorId(id: String): Flow<Recordatorio?> {
        return repository.observarPorId(id)
    }

    fun actualizarTitulo(nuevoTitulo: String) {
        _crearUiState.update {
            it.copy(
                titulo = nuevoTitulo,
                errorTitulo = if (nuevoTitulo.trim().length < 4 && nuevoTitulo.isNotEmpty()) {
                    "Mínimo 4 caracteres"
                } else {
                    null
                }
            )
        }
    }

    fun actualizarDescripcion(nuevaDesc: String) {
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

            try {
                // ID unico para evitar choques al trabajar sin red
                val nuevoRecordatorio = Recordatorio(
                    id = UUID.randomUUID().toString(),
                    titulo = estadoActual.titulo.trim(),
                    categoria = categoria,
                    prioridad = estadoActual.prioridad,
                    estado = Estado.Pendiente,
                    descripcion = estadoActual.descripcion.ifBlank { null }
                )
                val resultado = repository.guardar(nuevoRecordatorio)
                procesarResultadoSync(resultado, "Recordatorio guardado y sincronizado")
                _crearUiState.update { CrearRecordatorioUiState(guardado = true) }
            } catch (e: Exception) {
                _crearUiState.update { it.copy(enviando = false, errorGeneral = "Error al guardar") }
            }
        }
    }

    fun resetearGuardado() {
        _crearUiState.update { it.copy(guardado = false) }
    }

    fun cambiarEstado(id: String, completado: Boolean) {
        viewModelScope.launch {
            val resultado = repository.cambiarEstado(id, completado)
            procesarResultadoSync(resultado, "Estado actualizado")
        }
    }

    fun eliminarRecordatorio(id: String) {
        viewModelScope.launch {
            val resultado = repository.eliminar(id)
            procesarResultadoSync(resultado, "Recordatorio eliminado")
        }
    }

    fun sincronizar() {
        viewModelScope.launch {
            _syncUiState.update { it.copy(sincronizando = true) }
            val resultado = repository.sincronizar()
            procesarResultadoSync(resultado, "Sincronización completada")
        }
    }

    fun cambiarModoServidor(modo: ModoServidorSimulado) {
        repository.cambiarModoServidor(modo)
        _syncUiState.update { it.copy(modoServidor = modo) }
    }

    fun limpiarMensajeSync() {
        _syncUiState.update { it.copy(mensaje = null, esError = false) }
    }

    fun cambiarModoOscuro(habilitado: Boolean) {
        viewModelScope.launch {
            preferencesRepository.actualizarModoOscuro(habilitado)
        }
    }

    fun cambiarMostrarCompletados(mostrar: Boolean) {
        viewModelScope.launch {
            preferencesRepository.actualizarMostrarCompletados(mostrar)
        }
    }

    fun cambiarOrden(orden: OrdenRecordatorio) {
        viewModelScope.launch {
            preferencesRepository.actualizarOrden(orden)
        }
    }

    private fun procesarResultadoSync(outcome: SyncOutcome, mensajeExito: String) {
        when (outcome) {
            is SyncOutcome.Exito -> {
                _syncUiState.update {
                    it.copy(sincronizando = false, mensaje = mensajeExito, esError = false)
                }
            }
            is SyncOutcome.SinConexion -> {
                _syncUiState.update {
                    it.copy(
                        sincronizando = false,
                        mensaje = "Sin conexión. Cambios guardados localmente.",
                        esError = true
                    )
                }
            }
            is SyncOutcome.Timeout -> {
                _syncUiState.update {
                    it.copy(
                        sincronizando = false,
                        mensaje = "Tiempo de espera agotado. Reintenta más tarde.",
                        esError = true
                    )
                }
            }
            is SyncOutcome.ErrorServidor -> {
                _syncUiState.update {
                    it.copy(
                        sincronizando = false,
                        mensaje = "Error del servidor (${outcome.code}). Datos seguros en local.",
                        esError = true
                    )
                }
            }
            is SyncOutcome.ErrorSolicitud -> {
                _syncUiState.update {
                    it.copy(
                        sincronizando = false,
                        mensaje = "Solicitud inválida (${outcome.code}).",
                        esError = true
                    )
                }
            }
            is SyncOutcome.ErrorDatos -> {
                _syncUiState.update {
                    it.copy(
                        sincronizando = false,
                        mensaje = "Error al procesar la respuesta del servidor.",
                        esError = true
                    )
                }
            }
        }
    }

    private fun pesoPrioridad(prioridad: Prioridad): Int = when (prioridad) {
        Prioridad.Alta -> 1
        Prioridad.Media -> 2
        Prioridad.Baja -> 3
    }
}

class RecordatoriosViewModelFactory(
    private val repository: RecordatoriosRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordatoriosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordatoriosViewModel(repository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}