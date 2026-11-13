package com.example.recordatoriosapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Tarjeta reutilizable para el listado (Actualizado con "estado")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordatorioCard(
    titulo: String,
    categoria: String,
    prioridad: String,
    estado: String, // <- Nuevo parámetro agregado
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Categoría: $categoria", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Prioridad: $prioridad", style = MaterialTheme.typography.bodySmall)
            Text(text = "Estado: $estado", style = MaterialTheme.typography.bodySmall) // <- Agregado para cumplir Consigna 1
        }
    }
}

// Campo de texto controlado con validación
@Composable
fun CampoTextoControlado(
    valor: String,
    etiqueta: String,
    onValorCambia: (String) -> Unit,
    mensajeError: String? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        isError = mensajeError != null,
        supportingText = {
            if (mensajeError != null) {
                Text(mensajeError)
            }
        }
    )
}

// Componentes para comunicar carga, vacío o error
@Composable
fun IndicadorDeCarga(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun MensajeEstado(
    mensaje: String,
    textoBoton: String,
    onAccion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = mensaje, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAccion) {
            Text(textoBoton)
        }
    }
}