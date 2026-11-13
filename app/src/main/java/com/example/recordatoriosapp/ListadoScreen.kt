package com.example.recordatoriosapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoScreen(
    estado: ListaRecordatoriosUiState,
    onCrearRecordatorio: () -> Unit,
    onRecordatorioClick: (Long) -> Unit,
    onReintentar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis Recordatorios") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCrearRecordatorio) {
                Icon(Icons.Filled.Add, contentDescription = "Crear Recordatorio")
            }
        }
    ) { innerPadding ->
        // Manejo de estados mutuamente excluyentes
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
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
                        onAccion = onReintentar
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
                            RecordatorioCard(
                                titulo = recordatorio.titulo,
                                categoria = recordatorio.categoria,
                                prioridad = recordatorio.prioridad.nombre,
                                estado = recordatorio.estado.nombre, // <- Parámetro enviado
                                onClick = { onRecordatorioClick(recordatorio.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}