package com.example.recordatoriosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PantallaRecordatorios(recordatoriosSimulados)
            }
        }
    }
}

@Composable
fun PantallaRecordatorios(recordatorios: List<Recordatorio>) {
    // Usamos Column
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Mis Recordatorios",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Iteramos la lista de Kotlin manualmente dentro del Column
        recordatorios.forEach { recordatorio ->
            TarjetaRecordatorio(recordatorio)
        }
    }
}

@Composable
fun TarjetaRecordatorio(recordatorio: Recordatorio) {
    // Card básico
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = recordatorio.titulo, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text("Categoría: ")
                Text(text = recordatorio.categoria)
            }
            Text(text = "Prioridad: ${recordatorio.prioridad.nombre}")
            Text(text = "Estado: ${recordatorio.estado.nombre}")

            // Uso de let para valores anulables
            recordatorio.descripcion?.let { nota ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Nota: $nota")
            }
        }
    }
}