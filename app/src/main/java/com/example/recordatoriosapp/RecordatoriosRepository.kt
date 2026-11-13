package com.example.recordatoriosapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

// La interfaz que define qué podemos hacer con los datos
interface RecordatoriosRepository {
    fun observarRecordatorios(): Flow<List<Recordatorio>>
    suspend fun obtenerPorId(id: Long): Recordatorio?
    suspend fun guardar(recordatorio: Recordatorio)
}

// La implementación en memoria
class RecordatoriosEnMemoriaRepository : RecordatoriosRepository {

    // Iniciamos con una lista vacía como indica el PDF para poder probar el estado "Vacia"
    private val recordatorios = MutableStateFlow<List<Recordatorio>>(emptyList())

    override fun observarRecordatorios(): Flow<List<Recordatorio>> = recordatorios

    override suspend fun obtenerPorId(id: Long): Recordatorio? {
        return recordatorios.value.firstOrNull { it.id == id }
    }

    override suspend fun guardar(recordatorio: Recordatorio) {
        recordatorios.update { actuales ->
            actuales + recordatorio
        }
    }
}