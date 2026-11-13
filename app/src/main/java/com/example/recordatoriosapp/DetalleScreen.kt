package com.example.recordatoriosapp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    recordatorio: Recordatorio?,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = recordatorio.titulo, style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Categoría: ${recordatorio.categoria}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Prioridad: ${recordatorio.prioridad.nombre}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Estado: ${recordatorio.estado.nombre}", style = MaterialTheme.typography.bodyLarge)

                        recordatorio.descripcion?.let { desc ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Nota:", style = MaterialTheme.typography.titleMedium)
                            Text(text = desc, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
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