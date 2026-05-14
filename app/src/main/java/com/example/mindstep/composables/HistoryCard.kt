package com.example.mindstep.composables

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import com.example.mindstep.utils.LocalHapticEnabled
import androidx.compose.ui.unit.dp
import com.example.mindstep.data.local.EntryEntity
import com.example.mindstep.data.local.MindStepDatabase
import com.example.mindstep.utils.anxietyLabels
import com.example.mindstep.utils.moodLabels
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HistoryCard(entry: EntryEntity) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val hapticOn = LocalHapticEnabled.current
    val database = remember { MindStepDatabase.getDatabase(context.applicationContext) }
    val entryDao = remember(database) { database.entryDao() }
    val locale = remember { Locale.forLanguageTag("pt-PT") }
    val lisbonTimeZone = remember { TimeZone.getTimeZone("Europe/Lisbon") }
    val dateFormatter = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", locale).apply {
            timeZone = lisbonTimeZone
        }
    }
    val timeFormatter = remember {
        SimpleDateFormat("HH:mm", locale).apply {
            timeZone = lisbonTimeZone
        }
    }
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

    fun delete() {
        if (hapticOn) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        AlertDialog.Builder(context)
            .setTitle("Apagar registo")
            .setMessage("Tem a certeza que quer apagar este registo?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Apagar") { _, _ ->
                coroutineScope.launch {
                    runCatching { entryDao.deleteById(entry.id) }
                        .onFailure {
                            Toast.makeText(context, "Não foi possível apagar o registo.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .show()
    }

    Card(elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, top = 8.dp)
            ) {
                Column() {
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
                }
                IconButton(
                    onClick = { delete() },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Apagar registo", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(8.dp))

            Column (
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                Row (
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ValueCard(title = "Humor", value = moodLabels[entry.mood-1], subtitle = "${entry.mood}/5")
                    ValueCard(title = "Ansiedade", value = anxietyLabels[entry.anxiety-1], subtitle = "${entry.anxiety}/5")
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