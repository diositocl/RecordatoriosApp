package com.example.recordatoriosapp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recordatoriosapp.data.local.SyncStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    recordatorio: Recordatorio?,
    onVolver: () -> Unit,
    onCambiarEstado: (Boolean) -> Unit,
    onEliminar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (recordatorio != null) {
                        IconButton(onClick = onEliminar) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (recordatorio != null) {
                val estaCompletado = recordatorio.estado is Estado.Completada
                val textoSync = when (recordatorio.syncStatus) {
                    SyncStatus.SYNCED -> "Sincronizado"
                    SyncStatus.PENDING -> "Pendiente de sincronización"
                    SyncStatus.PENDING_DELETE -> "Pendiente de eliminación"
                    SyncStatus.ERROR -> "Error al sincronizar"
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = recordatorio.titulo, style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Categoría: ${recordatorio.categoria}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Prioridad: ${recordatorio.prioridad.nombre}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Estado: ${recordatorio.estado.nombre}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Sincronización: $textoSync", style = MaterialTheme.typography.bodyMedium)

                        recordatorio.descripcion?.let { desc ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Nota:", style = MaterialTheme.typography.titleMedium)
                            Text(text = desc, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Boton para cambiar entre completado y pendiente
                Button(
                    onClick = { onCambiarEstado(!estaCompletado) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estaCompletado) "Marcar como pendiente" else "Marcar como completado")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onEliminar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Eliminar recordatorio")
                }
            } else {
                MensajeEstado(
                    mensaje = "No se encontró el registro",
                    textoBoton = "Volver",
                    onAccion = onVolver
                )
            }
        }
    }
}