package com.example.recordatoriosapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearScreen(
    estado: CrearRecordatorioUiState,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onPrioridadChange: (Prioridad) -> Unit,
    onGuardar: (String) -> Unit, // Recibe la categoría elegida
    onCancelar: () -> Unit,
    onGuardadoExitoso: () -> Unit
) {
    // Manejo local de la categoría usando  DatosSimulados
    var categoriaSeleccionada by remember { mutableStateOf(categoriasUnicas.first()) }

    // Reacciona cuando el ViewModel avisa que se guardó correctamente (basado en pág. 41 y 63)
    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            onGuardadoExitoso()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Recordatorio") },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Cancelar")
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            CampoTextoControlado(
                valor = estado.titulo,
                etiqueta = "Título (Obligatorio)",
                onValorCambia = onTituloChange,
                mensajeError = estado.errorTitulo
            )

            // Descripción
            CampoTextoControlado(
                valor = estado.descripcion,
                etiqueta = "Descripción (Opcional)",
                onValorCambia = onDescripcionChange
            )

            // Selector de Categoría (Opciones definidas)
            Text("Categoría:", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categoriasUnicas.forEach { cat ->
                    FilterChip(
                        selected = (cat == categoriaSeleccionada),
                        onClick = { categoriaSeleccionada = cat },
                        label = { Text(cat) }
                    )
                }
            }

            // Selector de Prioridad
            Text("Prioridad:", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Prioridad.Alta, Prioridad.Media, Prioridad.Baja).forEach { prio ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (estado.prioridad == prio),
                            onClick = { onPrioridadChange(prio) }
                        )
                        Text(prio.nombre)
                    }
                }
            }

            // Mensaje de error general de guardado
            if (estado.errorGeneral != null) {
                Text(text = estado.errorGeneral, color = MaterialTheme.colorScheme.error)
            }

            // Botones Cancelar y Guardar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancelar, enabled = !estado.enviando) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onGuardar(categoriaSeleccionada) },
                    // Regla de Rúbrica: Deshabilitado si hay error o enviando
                    enabled = estado.errorTitulo == null && estado.titulo.trim().length >= 4 && !estado.enviando
                ) {
                    if (estado.enviando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}