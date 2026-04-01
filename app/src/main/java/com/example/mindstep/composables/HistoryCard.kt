package com.example.mindstep.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mindstep.data.local.EntryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryCard(entry: EntryEntity) {
    val locale = remember { Locale.forLanguageTag("pt-PT") }
    val dateFormatter = remember { SimpleDateFormat("EEEE, d 'de' MMMM", locale) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", locale) }
    val formattedDate = if (entry.createdAt == 0L) {
        "Desconhecida"
    } else {
        dateFormatter.format(Date(entry.createdAt)).lowercase(locale)
    }
    val formattedTime = if (entry.createdAt == 0L) {
        ""
    } else {
        timeFormatter.format(Date(entry.createdAt)).lowercase(locale)
    }

    Card(elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column (
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                Row (
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ValueCard(title = "Humor", value = entry.mood.toString(), subtitle = "${entry.mood}/5")
                    ValueCard(title = "Ansiedade", value = entry.anxiety.toString(), subtitle = "${entry.anxiety}/5")
                }

                Row (
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ValueCard(title = "Sono", value = "${entry.sleep}h")
                    ValueCard(title = "Água", value = entry.waterGlasses.toString())
                }

                Row (modifier = Modifier.fillMaxWidth()) {
                    ValueCard(title = "Passos", value = entry.steps.toString())
                }

                if (entry.notes.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ValueCard(title = "Notas", value = entry.notes, isNotes = true)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.ValueCard (title: String, value: String, subtitle: String = "", isNotes: Boolean = false) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = if(isNotes) MaterialTheme.typography.labelMedium else if(subtitle.isNotBlank()) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
                fontWeight = if(isNotes) null else FontWeight.ExtraBold
            )
            if(subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}