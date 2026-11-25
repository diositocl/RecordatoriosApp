package com.example.recordatoriosapp

import com.example.recordatoriosapp.data.local.RecordatorioDao
import com.example.recordatoriosapp.data.local.SyncStatus
import com.example.recordatoriosapp.data.remote.ModoServidorSimulado
import com.example.recordatoriosapp.data.remote.RecordatorioRemoteDataSource
import com.example.recordatoriosapp.data.remote.RemoteResult
import com.example.recordatoriosapp.data.toDomain
import com.example.recordatoriosapp.data.toDto
import com.example.recordatoriosapp.data.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Resultado de la sincronizacion para informar a la UI
sealed interface SyncOutcome {
    data object Exito : SyncOutcome
    data object SinConexion : SyncOutcome
    data object Timeout : SyncOutcome
    data class ErrorServidor(val code: Int) : SyncOutcome
    data class ErrorSolicitud(val code: Int) : SyncOutcome
    data object ErrorDatos : SyncOutcome
}

interface RecordatoriosRepository {
    fun observarRecordatorios(): Flow<List<Recordatorio>>
    fun observarPorId(id: String): Flow<Recordatorio?>
    fun observarPorPrioridad(prioridad: Prioridad): Flow<List<Recordatorio>>
    suspend fun obtenerPorId(id: String): Recordatorio?
    suspend fun guardar(recordatorio: Recordatorio): SyncOutcome
    suspend fun cambiarEstado(id: String, completado: Boolean): SyncOutcome
    suspend fun eliminar(id: String): SyncOutcome
    suspend fun sincronizar(): SyncOutcome
    fun cambiarModoServidor(modo: ModoServidorSimulado)
}

class LocalFirstRecordatoriosRepository(
    private val dao: RecordatorioDao,
    private val remote: RecordatorioRemoteDataSource
) : RecordatoriosRepository {

    // Evita que dos sincronizaciones corran al mismo tiempo
    private val syncMutex = Mutex()

    override fun observarRecordatorios(): Flow<List<Recordatorio>> {
        return dao.observeAll().map { lista -> lista.map { it.toDomain() } }
    }

    override fun observarPorId(id: String): Flow<Recordatorio?> {
        return dao.observeById(id).map { it?.toDomain() }
    }

    override fun observarPorPrioridad(prioridad: Prioridad): Flow<List<Recordatorio>> {
        return dao.observeByPrioridad(prioridad.nombre).map { lista -> lista.map { it.toDomain() } }
    }

    override suspend fun obtenerPorId(id: String): Recordatorio? {
        return dao.findById(id)?.toDomain()
    }

    override suspend fun guardar(recordatorio: Recordatorio): SyncOutcome {
        val existente = dao.findById(recordatorio.id)
        val entidad = recordatorio.toEntity(
            remoteId = existente?.remoteId,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
        // Primero se guarda en Room para respuesta inmediata offline
        dao.save(entidad)
        return sincronizar()
    }

    override suspend fun cambiarEstado(id: String, completado: Boolean): SyncOutcome {
        dao.updateCompletado(
            id = id,
            completado = completado,
            updatedAt = System.currentTimeMillis()
        )
        return sincronizar()
    }

    override suspend fun eliminar(id: String): SyncOutcome {
        val existente = dao.findById(id) ?: return SyncOutcome.Exito
        if (existente.remoteId == null) {
            // Si nunca se subio al servidor, se borra directo de Room
            dao.deleteById(id)
            return SyncOutcome.Exito
        }
        // Si ya existia en remoto, se marca pendiente de borrado
        dao.save(
            existente.copy(
                syncStatus = SyncStatus.PENDING_DELETE,
                updatedAt = System.currentTimeMillis()
            )
        )
        return sincronizar()
    }

    override suspend fun sincronizar(): SyncOutcome = syncMutex.withLock {
        val pendientes = dao.findPendingSync()

        // 1. Subir cambios y borrados locales pendientes
        for (entidad in pendientes) {
            if (entidad.syncStatus == SyncStatus.PENDING_DELETE) {
                when (val res = remote.deleteRecordatorio(entidad.id)) {
                    is RemoteResult.Success -> dao.deleteById(entidad.id)
                    else -> return@withLock mapearError(res)
                }
            } else {
                when (val res = remote.saveRecordatorio(entidad.toDto())) {
                    is RemoteResult.Success -> {
                        dao.save(
                            entidad.copy(
                                remoteId = res.data.id,
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                    }
                    else -> return@withLock mapearError(res)
                }
            }
        }

        // 2. Traer datos del servidor y mezclar sin pisar cambios pendientes
        when (val res = remote.getRecordatorios()) {
            is RemoteResult.Success -> {
                for (dto in res.data) {
                    val local = dao.findById(dto.id)
                    if (local == null || local.syncStatus == SyncStatus.SYNCED) {
                        dao.save(dto.toEntity())
                    }
                }
                SyncOutcome.Exito
            }
            else -> mapearError(res)
        }
    }

    override fun cambiarModoServidor(modo: ModoServidorSimulado) {
        remote.interceptor.modo = modo
    }

    private fun mapearError(result: RemoteResult<*>): SyncOutcome {
        return when (result) {
            is RemoteResult.NoConnection -> SyncOutcome.SinConexion
            is RemoteResult.Timeout -> SyncOutcome.Timeout
            is RemoteResult.HttpError -> {
                if (result.code in 500..599) {
                    SyncOutcome.ErrorServidor(result.code)
                } else {
                    SyncOutcome.ErrorSolicitud(result.code)
                }
            }
            is RemoteResult.InvalidData -> SyncOutcome.ErrorDatos
            is RemoteResult.Success -> SyncOutcome.Exito
        }
    }
}