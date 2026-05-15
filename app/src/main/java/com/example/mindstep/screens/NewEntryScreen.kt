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
import androidx.compose.runtime.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.mindstep.utils.HapticHelper
import com.example.mindstep.utils.LocalHapticEnabled
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
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
import com.example.mindstep.data.local.EntrySettings
import com.example.mindstep.data.local.MindStepDatabase
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.health.connect.client.PermissionController
import com.example.mindstep.utils.HealthConnectManager
import com.example.mindstep.utils.anxietyLabels
import com.example.mindstep.utils.moodLabels
import com.example.mindstep.utils.valueColors
import kotlinx.coroutines.launch
import java.util.Locale


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
    val settingsDao = remember(database) { database.settingsDao() }

    val dbSettings by settingsDao.getSettings().collectAsState(initial = EntrySettings())
    val voiceInputEnabled = dbSettings?.voiceInput ?: false

    // Health Connect
    val healthConnectManager = remember { HealthConnectManager(context) }
    val isHealthConnectAvailable = remember { healthConnectManager.isAvailable() }
    var isImporting by remember { mutableStateOf(false) }

    var showHealthConnectHelp by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(
            providerPackageName = "com.google.android.apps.healthdata"
        )
    ) { granted ->
        android.util.Log.d("HealthConnect", "Permission result: $granted")
        if (granted.containsAll(HealthConnectManager.PERMISSIONS)) {
            coroutineScope.launch {
                isImporting = true
                try {
                    val data = healthConnectManager.readTodayData()
                    data.steps?.let { setSteps(it.toString()) }
                    data.sleepHours?.let { setSleep(it.toString()) }
                    data.waterGlasses?.let { setWaterGlasses(it.toString()) }
                    Toast.makeText(context, "Dados importados do Health Connect.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    android.util.Log.e("HealthConnect", "Error reading data", e)
                    Toast.makeText(context, "Erro ao ler dados: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isImporting = false
                }
            }
        } else {
            // Permissions not granted — show help option
            showHealthConnectHelp = true
            Toast.makeText(
                context,
                "Permissões não concedidas. Tente conceder manualmente nas definições do Health Connect.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun importHealthData() {
        coroutineScope.launch {
            try {
                if (healthConnectManager.hasPermissions()) {
                    isImporting = true
                    try {
                        val data = healthConnectManager.readTodayData()
                        data.steps?.let { setSteps(it.toString()) }
                        data.sleepHours?.let { setSleep(it.toString()) }
                        data.waterGlasses?.let { setWaterGlasses(it.toString()) }
                        Toast.makeText(context, "Dados importados do Health Connect.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("HealthConnect", "Error reading data", e)
                        Toast.makeText(context, "Erro ao ler dados: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isImporting = false
                    }
                } else {
                    android.util.Log.d("HealthConnect", "Launching permission request for: ${HealthConnectManager.PERMISSIONS}")
                    permissionLauncher.launch(HealthConnectManager.PERMISSIONS)
                }
            } catch (e: Exception) {
                android.util.Log.e("HealthConnect", "Error in importHealthData", e)
                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                isImporting = false
            }
        }
    }

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
        if (voiceInputEnabled) {
            recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        } else {
            Toast.makeText(context, "Esta funcionalidade tem de ser ativada nas configurações", Toast.LENGTH_SHORT).show()
        }
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
            setValue = setAnxiety,
            invertIndicator = true
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
                if (isHealthConnectAvailable) {
                    OutlinedButton(
                        onClick = { importHealthData() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        enabled = !isImporting,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(18.dp)
                                    .semantics { contentDescription = "A carregar" },
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("A importar...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Importar Health Connect",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Importar do Health Connect", fontWeight = FontWeight.Bold)
                        }
                    }
                    if (showHealthConnectHelp) {
                        OutlinedButton(
                            onClick = {
                                healthConnectManager.openHealthConnectSettings(context)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Abrir definições",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abrir Definições do Health Connect", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                TextInput(
                    value = sleep,
                    onValueChange = setSleep,
                    label = "Horas de sono",
                    hint = "Última noite"
                )
                TextInput(
                    value = steps,
                    onValueChange = setSteps,
                    label = "Passos",
                    hint = "Hoje"
                )
                TextInput(
                    value = waterGlasses,
                    onValueChange = setWaterGlasses,
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
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp),
                        enabled = voiceInputEnabled,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (voiceInputEnabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voz",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Voz",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column{
                    TextField(
                        value = notes.value,
                        onValueChange = { notes.value = it },
                        label = { Text("Notas") },
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
        val hapticOn = LocalHapticEnabled.current

        Button(
            onClick = {
                if (hapticOn) HapticHelper.heavyClick(context)
                save()
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(46.dp).fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Guardar Entrada",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EntryCard (title : String, description: String, labels: List<String>, value: Int, setValue: (Int) -> Unit, invertIndicator: Boolean = false) {
    val context = LocalContext.current
    val hapticOn = LocalHapticEnabled.current

    fun hapticTick() {
        if (hapticOn) HapticHelper.tick(context)
    }

    // For inverted indicators (e.g. anxiety), high value = bad, so reverse the color/emoji index
    val colorIndex = if (invertIndicator) (5 - value) else (value - 1)
    val isPositive = if (invertIndicator) value < 3 else value > 3
    val isNegative = if (invertIndicator) value > 3 else value < 3

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
                style = MaterialTheme.typography.labelMedium
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
                            Color(valueColors[colorIndex].toColorInt()).copy(
                                alpha = 0.1f
                            ),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(12.dp)

                ){
                    Icon(if(isPositive) Icons.Default.SentimentSatisfiedAlt
                        else if(isNegative) Icons.Default.SentimentVeryDissatisfied
                        else Icons.Default.SentimentNeutral,
                        contentDescription = null,
                        Modifier.size(30.dp),
                        tint = Color(valueColors[colorIndex].toColorInt()))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                ){
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { if (value > 1) { hapticTick(); setValue(value - 1) } },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Diminuir $title")
                        }
                        @OptIn(ExperimentalMaterial3Api::class)
                        Slider(
                            value = value.toFloat(),
                            onValueChange = { newVal ->
                                val newInt = newVal.toInt()
                                if (newInt != value) hapticTick()
                                setValue(newInt)
                            },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier
                                .weight(1f)
                                .semantics { contentDescription = "$title: ${labels[value - 1]}, $value de 5" },
                            thumb = {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            }
                        )
                        IconButton(
                            onClick = { if (value < 5) { hapticTick(); setValue(value + 1) } },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.ArrowBackIosNew,
                                contentDescription = "Aumentar $title",
                                modifier = Modifier.rotate(180f)
                            )
                        }
                    }
                    Text(
                        text = labels[value - 1],
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
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
