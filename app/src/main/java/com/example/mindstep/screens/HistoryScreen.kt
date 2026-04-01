package com.example.mindstep.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.example.mindstep.data.local.MindStepDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val database = remember { MindStepDatabase.getDatabase(context.applicationContext) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val entries by database.entryDao().getAllEntries().collectAsState(initial = emptyList())


    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        if (entries.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Ainda não tem registos no seu historico.")
                }
            }
            return
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(entries, key = { it.id }) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Entrada #${entry.id}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Data: ${if (entry.createdAt == 0L) "Desconhecida" else dateFormatter.format(Date(entry.createdAt))}")
                        Text("Humor: ${entry.mood}")
                        Text("Ansiedade: ${entry.anxiety}")
                        Text("Sono: ${entry.sleep}")
                        Text("Passos: ${entry.steps}")
                        Text("Agua: ${entry.waterGlasses}")
                        Text("Notas: ${entry.notes.ifBlank { "-" }}")
                    }
                }
            }
        }
    }
}