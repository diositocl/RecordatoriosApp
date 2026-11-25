package com.example.recordatoriosapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recordatoriosapp.data.local.SyncStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoScreen(
    estado: ListaRecordatoriosUiState,
    syncState: SyncUiState,
    onCrearRecordatorio: () -> Unit,
    onAbrirAjustes: () -> Unit,
    onRecordatorioClick: (String) -> Unit,
    onCambiarEstado: (String, Boolean) -> Unit,
    onSincronizar: () -> Unit,
    onLimpiarMensaje: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensajes de sincronizacion o errores de red
    LaunchedEffect(syncState.mensaje) {
        val mensaje = syncState.mensaje
        if (mensaje != null) {
            snackbarHostState.showSnackbar(
                message = mensaje,
                duration = SnackbarDuration.Short
            )
            onLimpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mis Recordatorios") },
                actions = {
                    IconButton(
                        onClick = onSincronizar,
                        enabled = !syncState.sincronizando
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Sincronizar")
                    }
                    IconButton(onClick = onAbrirAjustes) {
                        Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCrearRecordatorio) {
                Icon(Icons.Filled.Add, contentDescription = "Crear Recordatorio")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (syncState.sincronizando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (estado) {
                    is ListaRecordatoriosUiState.Cargando -> {
                        IndicadorDeCarga()
                    }
                    is ListaRecordatoriosUiState.Vacia -> {
                        MensajeEstado(
                            mensaje = "No hay recordatorios por el momento.",
                            textoBoton = "Crear uno nuevo",
                            onAccion = onCrearRecordatorio
                        )
                    }
                    is ListaRecordatoriosUiState.Error -> {
                        MensajeEstado(
                            mensaje = estado.mensaje,
                            textoBoton = "Reintentar",
                            onAccion = onSincronizar
                        )
                    }
                    is ListaRecordatoriosUiState.ConDatos -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = estado.recordatorios,
                                key = { recordatorio -> recordatorio.id }
                            ) { recordatorio ->
                                val textoEstado = if (recordatorio.syncStatus == SyncStatus.PENDING) {
                                    "${recordatorio.estado.nombre} • Pendiente sync"
                                } else {
                                    recordatorio.estado.nombre
                                }

                                RecordatorioCard(
                                    titulo = recordatorio.titulo,
                                    categoria = recordatorio.categoria,
                                    prioridad = recordatorio.prioridad.nombre,
                                    estado = textoEstado,
                                    onClick = { onRecordatorioClick(recordatorio.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}