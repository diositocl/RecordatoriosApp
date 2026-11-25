package com.example.recordatoriosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recordatoriosapp.data.UserPreferencesRepository
import com.example.recordatoriosapp.data.local.AppDatabase
import com.example.recordatoriosapp.data.remote.RecordatorioRemoteDataSource
import com.example.recordatoriosapp.data.userPreferencesDataStore
import com.example.recordatoriosapp.ui.theme.RecordatoriosAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dependencias principales
        val database = AppDatabase.getDatabase(applicationContext)
        val remoteDataSource = RecordatorioRemoteDataSource()
        val repository = LocalFirstRecordatoriosRepository(
            dao = database.recordatorioDao(),
            remote = remoteDataSource
        )
        val preferencesRepository = UserPreferencesRepository(
            dataStore = applicationContext.userPreferencesDataStore
        )
        val factory = RecordatoriosViewModelFactory(repository, preferencesRepository)

        setContent {
            RecordatoriosApp(factory = factory)
        }
    }
}

@Composable
fun RecordatoriosApp(factory: RecordatoriosViewModelFactory) {
    val navController = rememberNavController()
    val viewModel: RecordatoriosViewModel = viewModel(factory = factory)
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    // Aplica el tema oscuro segun DataStore
    RecordatoriosAppTheme(darkTheme = preferences.modoOscuro) {
        NavHost(navController = navController, startDestination = "listado") {

            composable("listado") {
                val listaUiState by viewModel.listaUiState.collectAsStateWithLifecycle()
                val syncUiState by viewModel.syncUiState.collectAsStateWithLifecycle()

                ListadoScreen(
                    estado = listaUiState,
                    syncState = syncUiState,
                    onCrearRecordatorio = { navController.navigate("crear") },
                    onAbrirAjustes = { navController.navigate("ajustes") },
                    onRecordatorioClick = { id -> navController.navigate("detalle/$id") },
                    onCambiarEstado = viewModel::cambiarEstado,
                    onSincronizar = viewModel::sincronizar,
                    onLimpiarMensaje = viewModel::limpiarMensajeSync
                )
            }

            composable("crear") {
                val crearUiState by viewModel.crearUiState.collectAsStateWithLifecycle()

                CrearScreen(
                    estado = crearUiState,
                    onTituloChange = viewModel::actualizarTitulo,
                    onDescripcionChange = viewModel::actualizarDescripcion,
                    onPrioridadChange = viewModel::actualizarPrioridad,
                    onGuardar = viewModel::guardarRecordatorio,
                    onCancelar = { navController.popBackStack() },
                    onGuardadoExitoso = {
                        viewModel.resetearGuardado()
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "detalle/{recordatorioId}",
                arguments = listOf(navArgument("recordatorioId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("recordatorioId") ?: ""
                val recordatorio by viewModel.observarPorId(id)
                    .collectAsStateWithLifecycle(initialValue = null)

                DetalleScreen(
                    recordatorio = recordatorio,
                    onVolver = { navController.popBackStack() },
                    onCambiarEstado = { completado -> viewModel.cambiarEstado(id, completado) },
                    onEliminar = {
                        viewModel.eliminarRecordatorio(id)
                        navController.popBackStack()
                    }
                )
            }

            composable("ajustes") {
                val syncUiState by viewModel.syncUiState.collectAsStateWithLifecycle()

                AjustesScreen(
                    preferences = preferences,
                    modoServidor = syncUiState.modoServidor,
                    onModoOscuroChange = viewModel::cambiarModoOscuro,
                    onMostrarCompletadosChange = viewModel::cambiarMostrarCompletados,
                    onOrdenChange = viewModel::cambiarOrden,
                    onModoServidorChange = viewModel::cambiarModoServidor,
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}