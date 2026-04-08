package com.example.mindstep.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mindstep.composables.HistoryCard
import com.example.mindstep.data.local.MindStepDatabase
import com.example.mindstep.utils.exportHistoryAsText
import com.example.mindstep.utils.exportHistoryAsPdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val database = remember { MindStepDatabase.getDatabase(context.applicationContext) }
    val entries by database.entryDao().getAllEntries().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    fun export(format: String) {
        if (entries.isEmpty()) {
            Toast.makeText(context, "Não existem registos para exportar.", Toast.LENGTH_SHORT).show()
            return
        }

        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                when (format.lowercase(Locale.ROOT)) {
                    "txt" -> exportHistoryAsText(context, entries)
                    "pdf" -> exportHistoryAsPdf(context, entries)
                    else -> Result.failure(IllegalArgumentException("Formato nao suportado"))
                }
            }

            result
                .onSuccess {
                    Toast.makeText(context, "Ficheiro exportado para Downloads/MindStep.", Toast.LENGTH_LONG).show()
                }
                .onFailure {
                    Toast.makeText(context, "Não foi possível exportar o ficheiro.", Toast.LENGTH_LONG).show()
                }
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        if (entries.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                elevation = CardDefaults.cardElevation(8.dp),
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

        Row (
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Button(
                onClick = { export("txt") },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(46.dp).weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Exportar TXT",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Exportar TXT",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = { export("pdf") },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(46.dp).weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Imprimir PDF",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Imprimir PDF",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 64.dp, top = 16.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(entries, key = { it.id }) { entry ->
                HistoryCard(entry)
            }
        }
    }
}