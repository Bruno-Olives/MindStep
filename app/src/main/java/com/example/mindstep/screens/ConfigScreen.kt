package com.example.mindstep.screens

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.mindstep.utils.LocalHapticEnabled
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.mindstep.composables.AboutAccessibilityCard
import com.example.mindstep.composables.SettingsSection
import com.example.mindstep.composables.SettingsSwitchRow
import com.example.mindstep.composables.TextInput
import com.example.mindstep.data.local.EntrySettings
import com.example.mindstep.data.local.MindStepDatabase
import com.example.mindstep.utils.NotificationReceiver
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Composable
fun ConfigScreen() {
    val context = LocalContext.current
    val database = remember { MindStepDatabase.getDatabase(context.applicationContext) }
    val settingsDao = remember(database) { database.settingsDao() }
    val coroutineScope = rememberCoroutineScope()
    
    val dbSettings by settingsDao.getSettings().collectAsState(initial = null)
    var settings by remember { mutableStateOf(EntrySettings()) }

    LaunchedEffect(dbSettings) {
        dbSettings?.let {
            settings = it
        }
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    fun scheduleNotifications(settings: EntrySettings) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // 1. Hydration Notification
        val waterIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", "Hora de beber água!")
            putExtra("message", "Mantenha-se hidratado para o seu bem-estar.")
            putExtra("id", 1001)
        }
        val waterPendingIntent = PendingIntent.getBroadcast(
            context, 1001, waterIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (settings.reminderWater && settings.waterInterval > 0) {
            val intervalMillis = settings.waterInterval * 60 * 1000L
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + intervalMillis,
                intervalMillis,
                waterPendingIntent
            )
        } else {
            alarmManager.cancel(waterPendingIntent)
        }

        // 2. Meditation Notification
        val meditationIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", "Momento de pausa")
            putExtra("message", "Está na hora da sua meditação diária.")
            putExtra("id", 1002)
        }
        val meditationPendingIntent = PendingIntent.getBroadcast(
            context, 1002, meditationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (settings.reminderMeditation) {
            val timeParts = settings.meditationTime.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DATE, 1)
                }
            }

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                meditationPendingIntent
            )
        } else {
            alarmManager.cancel(meditationPendingIntent)
        }
    }

    fun saveSettings() {
        coroutineScope.launch {
            runCatching { settingsDao.insert(settings) }
                .onSuccess {
                    scheduleNotifications(settings)
                    Toast.makeText(context, "Configurações guardadas com sucesso.", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(context, "Erro ao guardar configurações.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {

        // Conteúdo Principal
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            SettingsSection(title = "Saúde e Bem-estar", icon = Icons.Default.Favorite) {
                if (!hasNotificationPermission) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF9C4)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Notificações Desativadas",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF827717)
                            )
                            Text(
                                "Para receber lembretes de hidratação e meditação, por favor ative as notificações.",
                                fontSize = 12.sp,
                                color = Color(0xFF827717),
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(46.dp).fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Ativar Notificações", 
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                SettingsSwitchRow(
                    title = "Lembrete de Hidratação",
                    subtitle = "Notificações para beber água",
                    icon = Icons.Default.WaterDrop,
                    checked = settings.reminderWater,
                    enabled = hasNotificationPermission,
                    onCheckedChange = { settings = settings.copy(reminderWater = it) }
                )

                if (settings.reminderWater) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .background(Color(0xFFEEEEEE), shape = RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .fillMaxWidth()
                    ) {
                        TextInput(
                            value = settings.waterInterval.toString(),
                            onValueChange = {
                                val interval = it.toIntOrNull() ?: 0
                                settings = settings.copy(waterInterval = interval)
                            },
                            label = "Intervalo (minutos)",
                            enabled = hasNotificationPermission,
                        )
                    }
                }

                SettingsSwitchRow(
                    title = "Lembrete de Meditação",
                    subtitle = "Notificação diária para meditar",
                    icon = Icons.Default.SelfImprovement,
                    checked = settings.reminderMeditation,
                    enabled = hasNotificationPermission,
                    onCheckedChange = { settings = settings.copy(reminderMeditation = it) }
                )

                if (settings.reminderMeditation) {
                    val timeParts = settings.meditationTime.split(":")
                    val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
                    val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

                    val timePickerDialog = TimePickerDialog(
                        context,
                        { _, selectedHour, selectedMinute ->
                            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                            settings = settings.copy(meditationTime = formattedTime)
                        },
                        hour,
                        minute,
                        true
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .background(Color(0xFFEEEEEE), shape = RoundedCornerShape(12.dp))
                            .padding(16.dp)
                            .fillMaxWidth()
                        ) {
                        TextInput(
                            value = settings.meditationTime,
                            onValueChange = { },
                            label = "Hora da Meditação",
                            enabled = hasNotificationPermission,
                            readOnly = true,
                            onClick = { if (hasNotificationPermission) timePickerDialog.show() },
                            trailingIcon = {
                                IconButton(onClick = { }) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = "Selecionar hora"
                                    )
                                }
                            }
                        )
                    }
                }
            }

            SettingsSection(title = "Acessibilidade", icon = Icons.Default.Accessibility) {
                // Dark mode toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DarkMode,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Gray
                    )
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)) {
                        Text(
                            "Tema",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            when (settings.darkMode) {
                                null -> "Seguir sistema"
                                true -> "Modo escuro"
                                false -> "Modo claro"
                            },
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    val systemDark = isSystemInDarkTheme()
                    val isDark = settings.darkMode ?: systemDark
                    Switch(
                        checked = isDark,
                        onCheckedChange = { newValue ->
                            settings = settings.copy(darkMode = newValue)
                        }
                    )
                }

                SettingsSwitchRow(
                    title = "Reduzir Animações",
                    subtitle = "Simplifica as transições visuais",
                    icon = Icons.Default.MotionPhotosOff,
                    checked = settings.reduceAnimations,
                    onCheckedChange = { settings = settings.copy(reduceAnimations = it) }
                )

                SettingsSwitchRow(
                    title = "Feedback Háptico",
                    subtitle = "Vibrações ao interagir",
                    icon = Icons.Default.Vibration,
                    checked = settings.hapticFeedback,
                    onCheckedChange = { settings = settings.copy(hapticFeedback = it) }
                )

                SettingsSwitchRow(
                    title = "Entrada de Voz",
                    subtitle = "Ativar suporte para microfone",
                    icon = Icons.Default.Mic,
                    checked = settings.voiceInput,
                    onCheckedChange = { settings = settings.copy(voiceInput = it) }
                )
            }


            val haptic = LocalHapticFeedback.current
            val hapticOn = LocalHapticEnabled.current

            Button(
                onClick = {
                    if (hapticOn) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    saveSettings()
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(46.dp).fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Guardar",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Guardar Configurações",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            AboutAccessibilityCard()

            Spacer(modifier = Modifier.height(44.dp))
        }
    }
}

@Preview
@Composable
fun ConfigScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ConfigScreen()
    }
}
