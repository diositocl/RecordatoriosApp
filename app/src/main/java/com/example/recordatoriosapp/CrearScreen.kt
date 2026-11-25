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

private val categoriasUnicas = listOf("Personal", "Trabajo", "Estudio", "Salud", "Otros")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearScreen(
    estado: CrearRecordatorioUiState,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onPrioridadChange: (Prioridad) -> Unit,
    onGuardar: (String) -> Unit,
    onCancelar: () -> Unit,
    onGuardadoExitoso: () -> Unit
) {
    var categoriaSeleccionada by remember { mutableStateOf(categoriasUnicas.first()) }

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
            CampoTextoControlado(
                valor = estado.titulo,
                etiqueta = "Título (Obligatorio)",
                onValorCambia = onTituloChange,
                mensajeError = estado.errorTitulo
            )

            CampoTextoControlado(
                valor = estado.descripcion,
                etiqueta = "Descripción (Opcional)",
                onValorCambia = onDescripcionChange
            )

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

            if (estado.errorGeneral != null) {
                Text(text = estado.errorGeneral, color = MaterialTheme.colorScheme.error)
            }

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