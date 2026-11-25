package com.example.recordatoriosapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Instancia unica de DataStore para toda la app
val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

enum class OrdenRecordatorio {
    POR_FECHA,
    POR_PRIORIDAD
}

data class UserPreferences(
    val modoOscuro: Boolean = false,
    val mostrarCompletados: Boolean = true,
    val ordenListado: OrdenRecordatorio = OrdenRecordatorio.POR_FECHA
)

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    // Claves tipadas para leer y escribir en DataStore
    private object Keys {
        val MODO_OSCURO = booleanPreferencesKey("modo_oscuro")
        val MOSTRAR_COMPLETADOS = booleanPreferencesKey("mostrar_completados")
        val ORDEN_LISTADO = stringPreferencesKey("orden_listado")
    }

    // Flujo observable con valores por defecto si falta un dato o falla la lectura
    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { prefs ->
            val ordenGuardado = prefs[Keys.ORDEN_LISTADO]
            val orden = OrdenRecordatorio.entries
                .firstOrNull { it.name == ordenGuardado }
                ?: OrdenRecordatorio.POR_FECHA

            UserPreferences(
                modoOscuro = prefs[Keys.MODO_OSCURO] ?: false,
                mostrarCompletados = prefs[Keys.MOSTRAR_COMPLETADOS] ?: true,
                ordenListado = orden
            )
        }

    suspend fun actualizarModoOscuro(habilitado: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.MODO_OSCURO] = habilitado
        }
    }

    suspend fun actualizarMostrarCompletados(mostrar: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.MOSTRAR_COMPLETADOS] = mostrar
        }
    }

    suspend fun actualizarOrden(orden: OrdenRecordatorio) {
        dataStore.edit { prefs ->
            prefs[Keys.ORDEN_LISTADO] = orden.name
        }
    }
}