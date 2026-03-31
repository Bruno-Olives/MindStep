package com.example.mindstep.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.example.mindstep.composables.TextInput
import androidx.core.graphics.toColorInt
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import com.example.mindstep.data.local.EntryEntity
import com.example.mindstep.data.local.MindStepDatabase
import kotlinx.coroutines.launch
import java.util.Locale

private val moodLabels = listOf("Muito mal", "Mal", "Neutro", "Bem", "Muito bem")
private val anxietyLabels = listOf("Muito baixa", "Baixa", "Moderada", "Alta", "Muito alta")
private val valueColors = listOf("#c10007", "#ca3500", "#a65f00", "#497d00", "#008236")

@Composable
fun NewEntryScreen(onSaveSuccess: () -> Unit = {}) {
    val (mood, setMood) = remember { mutableIntStateOf(3) }
    val (anxiety, setAnxiety) = remember { mutableIntStateOf(3) }
    val (sleep, setSleep) = remember { mutableStateOf("8") }
    val (steps, setSteps) = remember { mutableStateOf("5000") }
    val (waterGlasses, setWaterGlasses) = remember { mutableStateOf("6") }
    val notes = remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { MindStepDatabase.getDatabase(context.applicationContext) }
    val entryDao = remember(database) { database.entryDao() }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (spokenText.isNotEmpty()) {
                notes.value = spokenText
            }
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale agora")
            }
            try {
                speechLauncher.launch(speechIntent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "Reconhecimento de voz não disponível neste dispositivo.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permissão de microfone negada.", Toast.LENGTH_SHORT).show()
        }
    }

    fun save (){
        val sleepValue = sleep.toIntOrNull()
        val stepsValue = steps.toIntOrNull()
        val waterValue = waterGlasses.toIntOrNull()

        if (sleepValue == null || stepsValue == null || waterValue == null) {
            Toast.makeText(context, "Preencha sono, passos e agua com numeros validos.", Toast.LENGTH_SHORT).show()
            return
        }

        val entry = EntryEntity(
            createdAt = System.currentTimeMillis(),
            mood = mood,
            anxiety = anxiety,
            sleep = sleepValue,
            steps = stepsValue,
            waterGlasses = waterValue,
            notes = notes.value.trim()
        )

        coroutineScope.launch {
            runCatching { entryDao.insert(entry) }
                .onSuccess {
                    Toast.makeText(context, "Entrada guardada com sucesso.", Toast.LENGTH_SHORT).show()
                    onSaveSuccess()
                }
                .onFailure {
                    Toast.makeText(context, "Erro ao guardar entrada.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun voiceRecord() {
        recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        EntryCard(
            title = "Como está o seu humor?",
            description = "Avalie como se sente emocionalmente neste momento",
            labels = moodLabels,
            value = mood,
            setValue = setMood
        )
        EntryCard(
            title = "Qual o seu nível de ansiedade?",
            description = "Avalie o seu nível de ansiedade ou stress",
            labels = anxietyLabels,
            value = anxiety,
            setValue = setAnxiety
        )

        Card (
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                TextInput(
                    value = sleep,
                    setValue = setSleep,
                    label = "Horas de sono",
                    hint = "Última noite"
                )
                TextInput(
                    value = steps,
                    setValue = setSteps,
                    label = "Passos",
                    hint = "Hoje"
                )
                TextInput(
                    value = waterGlasses,
                    setValue = setWaterGlasses,
                    label = "Copos de água",
                    hint = "Hoje"
                )
            }
        }
        Card (
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notas (opcional)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    OutlinedButton(
                        onClick = { voiceRecord() },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voz",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Voz",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column{
                    TextField(
                        value = notes.value,
                        onValueChange = { notes.value = it },
                        minLines = 3,
                        maxLines = 5,
                        placeholder = {
                            Text(
                                "Adicione observaçoes sobre o seu dia...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Partilhe pensamentos, eventos ou observações relevantes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Button(
            onClick = { save() },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(46.dp).fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Guardar",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Guardar Entrada",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EntryCard (title : String, description: String, labels: List<String>, value: Int, setValue: (Int) -> Unit) {
    Card (
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.semantics{
                    liveRegion = LiveRegionMode.Polite
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card (
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .background(
                            Color(valueColors[value - 1].toColorInt()).copy(
                                alpha = 0.1f
                            ),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(12.dp)

                ){
                    Icon(if(value > 3) Icons.Default.SentimentSatisfiedAlt
                        else if(value < 3) Icons.Default.SentimentVeryDissatisfied
                        else Icons.Default.SentimentNeutral,
                        contentDescription = labels[value - 1],
                        Modifier.size(30.dp),
                        tint = Color(valueColors[value - 1].toColorInt()))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                ){
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { if (value > 1) setValue(value - 1) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Diminuir")
                        }
                        @OptIn(ExperimentalMaterial3Api::class)
                        Slider(
                            value = value.toFloat(),
                            onValueChange = { setValue(it.toInt()) },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier.weight(1f),
                            thumb = {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            }
                        )
                        IconButton(
                            onClick = { if (value < 5) setValue(value + 1) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ArrowBackIosNew,
                                contentDescription = "Aumentar",
                                modifier = Modifier.rotate(180f)
                            )
                        }
                    }
                    Text(text = labels[value - 1], fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Preview
@Composable
fun SimpleComposablePreview() {
    Column(
        modifier= Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
    ) {
        NewEntryScreen()
    }
}
