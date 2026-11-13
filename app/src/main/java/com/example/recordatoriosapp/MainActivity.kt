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
import com.example.recordatoriosapp.ui.theme.RecordatoriosAppTheme // Asegúrate de que el nombre coincida con tu proyecto

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecordatoriosAppTheme {
                RecordatoriosApp()
            }
        }
    }
}

@Composable
fun RecordatoriosApp() {
    // Controlador de Navegación
    val navController = rememberNavController()

    // Instanciamos el repositorio y el ViewModel usando nuestro Factory
    val repositorio = RecordatoriosEnMemoriaRepository()
    val viewModel: RecordatoriosViewModel = viewModel(
        factory = RecordatoriosViewModelFactory(repositorio)
    )

    // Grafo de Navegación
    NavHost(navController = navController, startDestination = "listado") {

        // Pantalla de Listado
        composable("listado") {
            // Recolección consciente del ciclo de vida (Rúbrica y pág. 74)
            val listaUiState by viewModel.listaUiState.collectAsStateWithLifecycle()

            ListadoScreen(
                estado = listaUiState,
                onCrearRecordatorio = { navController.navigate("crear") },
                onRecordatorioClick = { id -> navController.navigate("detalle/$id") },
                onReintentar = { viewModel.cargarRecordatorios() }
            )
        }

        // Pantalla de Creación de Recordatorio
        composable("crear") {
            val crearUiState by viewModel.crearUiState.collectAsStateWithLifecycle()

            CrearScreen(
                estado = crearUiState,
                onTituloChange = viewModel::actualizarTitulo,
                onDescripcionChange = viewModel::actualizarDescripcion,
                onPrioridadChange = viewModel::actualizarPrioridad,
                onGuardar = viewModel::guardarRecordatorio,
                onCancelar = { navController.popBackStack() }, // Retorno correcto sin duplicar
                onGuardadoExitoso = {
                    viewModel.resetearGuardado()
                    navController.popBackStack()
                }
            )
        }

        //  Pantalla de D
        //  etalle
        composable(
            route = "detalle/{recordatorioId}",
            arguments = listOf(navArgument("recordatorioId") { type = NavType.LongType })
        ) { backStackEntry ->
            // Recuperamos únicamente el ID
            val id = backStackEntry.arguments?.getLong("recordatorioId") ?: 0L

            // Buscamos el recordatorio actual en el estado para mostrarlo
            val listaUiState by viewModel.listaUiState.collectAsStateWithLifecycle()
            val recordatorio = if (listaUiState is ListaRecordatoriosUiState.ConDatos) {
                (listaUiState as ListaRecordatoriosUiState.ConDatos).recordatorios.find { it.id == id }
            } else null

            DetalleScreen(
                recordatorio = recordatorio,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}