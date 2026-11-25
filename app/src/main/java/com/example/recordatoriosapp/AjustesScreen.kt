package com.example.recordatoriosapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recordatoriosapp.data.OrdenRecordatorio
import com.example.recordatoriosapp.data.UserPreferences
import com.example.recordatoriosapp.data.remote.ModoServidorSimulado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    preferences: UserPreferences,
    modoServidor: ModoServidorSimulado,
    onModoOscuroChange: (Boolean) -> Unit,
    onMostrarCompletadosChange: (Boolean) -> Unit,
    onOrdenChange: (OrdenRecordatorio) -> Unit,
    onModoServidorChange: (ModoServidorSimulado) -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Preferencias", style = MaterialTheme.typography.titleMedium)

            // Switch modo oscuro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Modo oscuro")
                Switch(
                    checked = preferences.modoOscuro,
                    onCheckedChange = onModoOscuroChange
                )
            }

            // Switch mostrar completados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mostrar completados")
                Switch(
                    checked = preferences.mostrarCompletados,
                    onCheckedChange = onMostrarCompletadosChange
                )
            }

            // Orden del listado
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ordenar listado por:", style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = preferences.ordenListado == OrdenRecordatorio.POR_FECHA,
                        onClick = { onOrdenChange(OrdenRecordatorio.POR_FECHA) },
                        label = { Text("Fecha") }
                    )
                    FilterChip(
                        selected = preferences.ordenListado == OrdenRecordatorio.POR_PRIORIDAD,
                        onClick = { onOrdenChange(OrdenRecordatorio.POR_PRIORIDAD) },
                        label = { Text("Prioridad") }
                    )
                }
            }

            HorizontalDivider()

            // Selector para probar errores de red en el servidor simulado
            Text(text = "Simulación de servidor", style = MaterialTheme.typography.titleMedium)

            val opcionesServidor = listOf(
                ModoServidorSimulado.NORMAL to "Normal (200 OK)",
                ModoServidorSimulado.SIN_CONEXION to "Sin conexión",
                ModoServidorSimulado.TIMEOUT to "Timeout",
                ModoServidorSimulado.ERROR_500 to "Error servidor (500)",
                ModoServidorSimulado.ERROR_400 to "Error solicitud (400)"
            )

            opcionesServidor.forEach { (modo, etiqueta) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = modoServidor == modo,
                        onClick = { onModoServidorChange(modo) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = etiqueta)
                }
            }
        }
    }
}