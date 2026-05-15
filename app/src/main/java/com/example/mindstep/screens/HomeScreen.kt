package com.example.mindstep.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindstep.data.local.EntryEntity
import com.example.mindstep.data.local.MindStepDatabase
import com.example.mindstep.utils.LocalReduceAnimations
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val database = remember { MindStepDatabase.getDatabase(context.applicationContext) }
    val allEntries by database.entryDao().getAllEntries().collectAsState(initial = emptyList())

    val lisbonTz = remember { TimeZone.getTimeZone("Europe/Lisbon") }

    val now = remember { Calendar.getInstance(lisbonTz) }
    val startOfWeek = remember {
        Calendar.getInstance(lisbonTz).apply {
            time = now.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                add(Calendar.DAY_OF_MONTH, -1)
            }
        }.timeInMillis
    }
    val startOfPrevWeek = remember {
        Calendar.getInstance(lisbonTz).apply {
            timeInMillis = startOfWeek
            add(Calendar.WEEK_OF_YEAR, -1)
        }.timeInMillis
    }
    val startOfMonth = remember {
        Calendar.getInstance(lisbonTz).apply {
            time = now.time
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val thisWeekEntries = allEntries.filter { it.createdAt >= startOfWeek }
    val prevWeekEntries = allEntries.filter { it.createdAt >= startOfPrevWeek && it.createdAt < startOfWeek }
    val monthEntries = allEntries.filter { it.createdAt >= startOfMonth }

    val avgMood = thisWeekEntries.averageOrNull { it.mood.toDouble() }
    val avgSleep = thisWeekEntries.averageOrNull { it.sleep.toDouble() }
    val avgSteps = thisWeekEntries.averageOrNull { it.steps.toDouble() }
    val avgWater = thisWeekEntries.averageOrNull { it.waterGlasses.toDouble() }

    val prevAvgMood = prevWeekEntries.averageOrNull { it.mood.toDouble() }
    val prevAvgSleep = prevWeekEntries.averageOrNull { it.sleep.toDouble() }
    val prevAvgSteps = prevWeekEntries.averageOrNull { it.steps.toDouble() }
    val prevAvgWater = prevWeekEntries.averageOrNull { it.waterGlasses.toDouble() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Visão Semanal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        if (thisWeekEntries.isEmpty()) {
            Card(
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ainda não tem registos esta semana.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SummaryCard(
                    icon = Icons.Outlined.Mood,
                    iconColor = Color(0xFF6750A4),
                    borderColor = Color(0xFFD0BCFF),
                    title = "Humor Médio",
                    value = String.format("%.1f/5", avgMood),
                    subtitle = "Últimos 7 dias",
                    changePercent = percentChange(avgMood, prevAvgMood)
                )
                SummaryCard(
                    icon = Icons.Outlined.Bedtime,
                    iconColor = Color(0xFF6750A4),
                    borderColor = Color(0xFFCCC2DC),
                    title = "Sono Médio",
                    value = String.format("%.1fh", avgSleep),
                    subtitle = "Por noite",
                    changePercent = percentChange(avgSleep, prevAvgSleep)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SummaryCard(
                    icon = Icons.AutoMirrored.Outlined.DirectionsWalk,
                    iconColor = Color(0xFF008236),
                    borderColor = Color(0xFFA8DAB5),
                    title = "Passos Médios",
                    value = String.format("%.0f", avgSteps),
                    subtitle = "Por dia",
                    changePercent = percentChange(avgSteps, prevAvgSteps)
                )
                SummaryCard(
                    icon = Icons.Outlined.WaterDrop,
                    iconColor = Color(0xFF0077B6),
                    borderColor = Color(0xFFA2D2E2),
                    title = "Hidratação",
                    value = String.format("%.1f", avgWater),
                    subtitle = "Copos/dia",
                    changePercent = percentChange(avgWater, prevAvgWater)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        MentalWellbeingSection(monthEntries, lisbonTz)

        Spacer(Modifier.height(24.dp))

        PhysicalActivitySection(monthEntries, lisbonTz)

        Spacer(Modifier.height(64.dp))
    }
}

@Composable
private fun MentalWellbeingSection(monthEntries: List<EntryEntity>, timeZone: TimeZone) {
    var showInfo by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Bem-Estar Mental e Sono",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(
            onClick = { showInfo = !showInfo },
            modifier = Modifier
                .size(48.dp)
                .semantics {
                    stateDescription = if (showInfo) "Aberto" else "Fechado"
                    contentDescription = if (showInfo) "Fechar informações do gráfico" else "Abrir informações do gráfico"
                }
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (monthEntries.isEmpty()) {
        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Ainda não tem registos este mês.",
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    val locale = remember { Locale.forLanguageTag("pt-PT") }
    val dayFormatter = remember {
        SimpleDateFormat("EEEE", locale).apply { this.timeZone = timeZone }
    }

    data class DayData(val label: String, val mood: Double, val anxiety: Double, val sleep: Double)

    val grouped = monthEntries.groupBy { entry ->
        Calendar.getInstance(timeZone).apply {
            timeInMillis = entry.createdAt
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }.toSortedMap()

    val chartData = grouped.map { (dayMillis, entries) ->
        DayData(
            label = dayFormatter.format(Date(dayMillis)),
            mood = entries.map { it.mood.toDouble() }.average(),
            anxiety = entries.map { it.anxiety.toDouble() }.average(),
            sleep = entries.map { it.sleep.toDouble() }.average()
        )
    }

    val allMoods = chartData.map { it.mood }
    val allAnxiety = chartData.map { it.anxiety }
    val allSleep = chartData.map { it.sleep }

    val avgMood = allMoods.average()
    val avgAnxiety = allAnxiety.average()
    val avgSleep = allSleep.average()

    val moodTrend = if (chartData.size >= 2) percentChange(chartData.last().mood, chartData.first().mood) else null
    val anxietyTrend = if (chartData.size >= 2) percentChange(chartData.last().anxiety, chartData.first().anxiety) else null
    val sleepTrend = if (chartData.size >= 2) percentChange(chartData.last().sleep, chartData.first().sleep) else null

    fun trendLabel(pct: Double?): String {
        if (pct == null) return "estável"
        return if (pct >= 0) "crescente de ${String.format("%.0f", kotlin.math.abs(pct))}%"
        else "decrescente de ${String.format("%.0f", kotlin.math.abs(pct))}%"
    }

    val reduceAnimations = LocalReduceAnimations.current

    @Composable
    fun MentalInfoCard() {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { liveRegion = LiveRegionMode.Polite },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Gráfico de Humor: tendência ${trendLabel(moodTrend)}. Média: ${String.format("%.1f", avgMood)}/5. Valor máximo: ${String.format("%.0f", allMoods.max())}/5. Valor mínimo: ${String.format("%.0f", allMoods.min())}/5.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Gráfico de Ansiedade: tendência ${trendLabel(anxietyTrend)}. Média: ${String.format("%.1f", avgAnxiety)}/5. Valor máximo: ${String.format("%.0f", allAnxiety.max())}/5. Valor mínimo: ${String.format("%.0f", allAnxiety.min())}/5.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Gráfico de Sono: tendência ${trendLabel(sleepTrend)}. Média: ${String.format("%.1f", avgSleep)}h. Valor máximo: ${String.format("%.0f", allSleep.max())}h. Valor mínimo: ${String.format("%.0f", allSleep.min())}h.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    if (reduceAnimations) {
        if (showInfo) { MentalInfoCard() }
    } else {
        AnimatedVisibility(visible = showInfo) { MentalInfoCard() }
    }

    Spacer(Modifier.height(12.dp))

    val moodColor = Color(0xFF4285F4)
    val anxietyColor = Color(0xFFFF9800)
    val sleepColor = Color(0xFF7C4DFF)

    val maxY = maxOf(
        allMoods.max(),
        allAnxiety.max(),
        allSleep.max(),
        5.0
    ).let { kotlin.math.ceil(it).toFloat() + 1f }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        val gridLineColor = MaterialTheme.colorScheme.outlineVariant
        val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val labelArgb = labelColor.let {
                    android.graphics.Color.argb(
                        (it.alpha * 255).toInt(),
                        (it.red * 255).toInt(),
                        (it.green * 255).toInt(),
                        (it.blue * 255).toInt()
                    )
                }

                val mentalChartDescription = "Gráfico mensal de bem-estar mental. " +
                    "Humor: média ${String.format("%.1f", avgMood)} de 5, tendência ${trendLabel(moodTrend)}. " +
                    "Ansiedade: média ${String.format("%.1f", avgAnxiety)} de 5, tendência ${trendLabel(anxietyTrend)}. " +
                    "Sono: média ${String.format("%.1f", avgSleep)} horas, tendência ${trendLabel(sleepTrend)}."

                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = mentalChartDescription }
                ) {
                    val leftPadding = 40f
                    val bottomPadding = 40f
                    val chartWidth = size.width - leftPadding
                    val chartHeight = size.height - bottomPadding

                    val gridLines = maxY.toInt()
                    for (i in 0..gridLines) {
                        val y = chartHeight - (i.toFloat() / maxY) * chartHeight
                        drawLine(
                            color = gridLineColor,
                            start = Offset(leftPadding, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            i.toString(),
                            10f,
                            y + 5f,
                            android.graphics.Paint().apply {
                                color = labelArgb
                                textSize = 28f
                                textAlign = android.graphics.Paint.Align.LEFT
                            }
                        )
                    }

                    val labels = chartData.map { it.label }
                    if (labels.isNotEmpty()) {
                        val step = if (labels.size > 1) chartWidth / (labels.size - 1) else chartWidth
                        labels.forEachIndexed { index, label ->
                            val x = leftPadding + index * step
                            drawContext.canvas.nativeCanvas.drawText(
                                label,
                                x,
                                size.height - 5f,
                                android.graphics.Paint().apply {
                                    color = labelArgb
                                    textSize = 26f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }
                    }

                    fun drawChartLine(data: List<Double>, color: Color) {
                        if (data.isEmpty()) return
                        val step = if (data.size > 1) chartWidth / (data.size - 1) else 0f

                        if (data.size >= 2) {
                            val path = Path()
                            data.forEachIndexed { index, value ->
                                val x = leftPadding + index * step
                                val y = chartHeight - (value.toFloat() / maxY) * chartHeight
                                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(path, color, style = Stroke(width = 5f, cap = StrokeCap.Round))
                        }

                        data.forEachIndexed { index, value ->
                            val x = leftPadding + index * step
                            val y = chartHeight - (value.toFloat() / maxY) * chartHeight
                            drawCircle(color, radius = 10f, center = Offset(x, y))
                        }
                    }

                    drawChartLine(allMoods, moodColor)
                    drawChartLine(allAnxiety, anxietyColor)
                    drawChartLine(allSleep, sleepColor)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(color = moodColor, label = "Humor")
                Spacer(Modifier.width(16.dp))
                ChartLegendItem(color = anxietyColor, label = "Ansiedade")
                Spacer(Modifier.width(16.dp))
                ChartLegendItem(color = sleepColor, label = "Sono")
            }
        }
    }
}

@Composable
private fun PhysicalActivitySection(monthEntries: List<EntryEntity>, timeZone: TimeZone) {
    var showInfo by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Atividade Física e Hidratação",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(
            onClick = { showInfo = !showInfo },
            modifier = Modifier
                .size(48.dp)
                .semantics {
                    stateDescription = if (showInfo) "Aberto" else "Fechado"
                    contentDescription = if (showInfo) "Fechar informações do gráfico" else "Abrir informações do gráfico"
                }
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (monthEntries.isEmpty()) {
        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Ainda não tem registos este mês.",
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    val locale = remember { Locale.forLanguageTag("pt-PT") }
    val dayFormatter = remember {
        SimpleDateFormat("EEEE", locale).apply { this.timeZone = timeZone }
    }

    data class DayData(val label: String, val steps: Double, val water: Double)

    val grouped = monthEntries.groupBy { entry ->
        Calendar.getInstance(timeZone).apply {
            timeInMillis = entry.createdAt
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }.toSortedMap()

    val chartData = grouped.map { (dayMillis, entries) ->
        DayData(
            label = dayFormatter.format(Date(dayMillis)),
            steps = entries.map { it.steps.toDouble() }.average(),
            water = entries.map { it.waterGlasses.toDouble() }.average()
        )
    }

    val allSteps = chartData.map { it.steps }
    val allWater = chartData.map { it.water }

    val avgSteps = allSteps.average()
    val avgWater = allWater.average()

    val stepsTrend = if (chartData.size >= 2) percentChange(chartData.last().steps, chartData.first().steps) else null
    val waterTrend = if (chartData.size >= 2) percentChange(chartData.last().water, chartData.first().water) else null

    fun trendLabel(pct: Double?): String {
        if (pct == null) return "estável"
        return if (pct >= 0) "crescente de ${String.format("%.0f", kotlin.math.abs(pct))}%"
        else "decrescente de ${String.format("%.0f", kotlin.math.abs(pct))}%"
    }

    val reduceAnimations = LocalReduceAnimations.current

    @Composable
    fun PhysicalInfoCard() {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { liveRegion = LiveRegionMode.Polite },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Gráfico de Passos: tendência ${trendLabel(stepsTrend)}. Média: ${String.format("%.1f", avgSteps)}. Valor máximo: ${String.format("%.0f", allSteps.max())}. Valor mínimo: ${String.format("%.0f", allSteps.min())}.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Gráfico de Água: tendência ${trendLabel(waterTrend)}. Média: ${String.format("%.1f", avgWater)} copos. Valor máximo: ${String.format("%.0f", allWater.max())} copos. Valor mínimo: ${String.format("%.0f", allWater.min())} copos.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    if (reduceAnimations) {
        if (showInfo) { PhysicalInfoCard() }
    } else {
        AnimatedVisibility(visible = showInfo) { PhysicalInfoCard() }
    }

    Spacer(Modifier.height(12.dp))

    val stepsColor = Color(0xFF2E7D32)
    val waterColor = Color(0xFF0097A7)

    val maxYSteps = allSteps.max().let {
        val rounded = kotlin.math.ceil(it / 1000.0) * 1000.0
        if (rounded == 0.0) 1000.0 else rounded
    }.toFloat()

    val maxYWater = allWater.max().let {
        val rounded = kotlin.math.ceil(it).toDouble() + 1.0
        if (rounded <= 1.0) 10.0 else rounded
    }.toFloat()

    val stepsGridStep = when {
        maxYSteps <= 1000f -> 250f
        else -> (maxYSteps / 4f).let { kotlin.math.ceil(it.toDouble() / 500.0).toFloat() * 500f }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        val gridLineColor = MaterialTheme.colorScheme.outlineVariant
        val stepsArgb = stepsColor.let {
            android.graphics.Color.argb(
                (it.alpha * 255).toInt(),
                (it.red * 255).toInt(),
                (it.green * 255).toInt(),
                (it.blue * 255).toInt()
            )
        }
        val waterArgb = waterColor.let {
            android.graphics.Color.argb(
                (it.alpha * 255).toInt(),
                (it.red * 255).toInt(),
                (it.green * 255).toInt(),
                (it.blue * 255).toInt()
            )
        }
        val labelArgb = MaterialTheme.colorScheme.onSurfaceVariant.let {
            android.graphics.Color.argb(
                (it.alpha * 255).toInt(),
                (it.red * 255).toInt(),
                (it.green * 255).toInt(),
                (it.blue * 255).toInt()
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val physicalChartDescription = "Gráfico mensal de atividade física. " +
                    "Passos: média ${String.format("%.0f", avgSteps)}, tendência ${trendLabel(stepsTrend)}. " +
                    "Água: média ${String.format("%.1f", avgWater)} copos, tendência ${trendLabel(waterTrend)}."

                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = physicalChartDescription }
                ) {
                    val leftPadding = 80f
                    val rightPadding = 60f
                    val bottomPadding = 40f
                    val chartWidth = size.width - leftPadding - rightPadding
                    val chartHeight = size.height - bottomPadding

                    // Left axis grid lines (Passos)
                    var gridVal = 0f
                    while (gridVal <= maxYSteps) {
                        val y = chartHeight - (gridVal / maxYSteps) * chartHeight
                        drawLine(
                            color = gridLineColor,
                            start = Offset(leftPadding, y),
                            end = Offset(leftPadding + chartWidth, y),
                            strokeWidth = 1f
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            gridVal.toInt().toString(),
                            leftPadding - 10f,
                            y + 5f,
                            android.graphics.Paint().apply {
                                color = stepsArgb
                                textSize = 26f
                                textAlign = android.graphics.Paint.Align.RIGHT
                            }
                        )
                        gridVal += stepsGridStep
                    }

                    // Right axis labels (Água)
                    val waterGridStep = if (maxYWater <= 12f) 2f else kotlin.math.ceil(maxYWater / 5.0).toFloat()
                    var waterGridVal = 0f
                    while (waterGridVal <= maxYWater) {
                        val y = chartHeight - (waterGridVal / maxYWater) * chartHeight
                        drawContext.canvas.nativeCanvas.drawText(
                            waterGridVal.toInt().toString(),
                            leftPadding + chartWidth + 10f,
                            y + 5f,
                            android.graphics.Paint().apply {
                                color = waterArgb
                                textSize = 26f
                                textAlign = android.graphics.Paint.Align.LEFT
                            }
                        )
                        waterGridVal += waterGridStep
                    }

                    // X-axis labels
                    val labels = chartData.map { it.label }
                    if (labels.isNotEmpty()) {
                        val step = if (labels.size > 1) chartWidth / (labels.size - 1) else chartWidth
                        labels.forEachIndexed { index, label ->
                            val x = leftPadding + index * step
                            drawContext.canvas.nativeCanvas.drawText(
                                label,
                                x,
                                size.height - 5f,
                                android.graphics.Paint().apply {
                                    color = labelArgb
                                    textSize = 26f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }
                    }

                    // Draw steps line (left axis)
                    fun drawChartLine(data: List<Double>, lineColor: Color, scaleMax: Float) {
                        if (data.isEmpty()) return
                        val step = if (data.size > 1) chartWidth / (data.size - 1) else 0f

                        if (data.size >= 2) {
                            val path = Path()
                            data.forEachIndexed { index, value ->
                                val x = leftPadding + index * step
                                val y = chartHeight - (value.toFloat() / scaleMax) * chartHeight
                                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(path, lineColor, style = Stroke(width = 5f, cap = StrokeCap.Round))
                        }

                        data.forEachIndexed { index, value ->
                            val x = leftPadding + index * step
                            val y = chartHeight - (value.toFloat() / scaleMax) * chartHeight
                            drawCircle(lineColor, radius = 10f, center = Offset(x, y))
                        }
                    }

                    drawChartLine(allSteps, stepsColor, maxYSteps)
                    drawChartLine(allWater, waterColor, maxYWater)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(color = stepsColor, label = "Passos")
                Spacer(Modifier.width(16.dp))
                ChartLegendItem(color = waterColor, label = "Água")
            }
        }
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics(mergeDescendants = true) { }
    ) {
        Canvas(modifier = Modifier
            .size(10.dp)
            .clearAndSetSemantics { }
        ) {
            drawCircle(color)
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RowScope.SummaryCard(
    icon: ImageVector,
    iconColor: Color,
    borderColor: Color,
    title: String,
    value: String,
    subtitle: String,
    changePercent: Double?
) {
    val changeLabel = when {
        changePercent == null -> ""
        changePercent > 0 -> ", aumento de ${String.format("%.0f", kotlin.math.abs(changePercent))} por cento"
        changePercent < 0 -> ", diminuição de ${String.format("%.0f", kotlin.math.abs(changePercent))} por cento"
        else -> ", sem alteração"
    }
    Card(
        modifier = Modifier
            .weight(1f)
            .semantics(mergeDescendants = true) {
                contentDescription = "$title: $value. $subtitle$changeLabel"
            },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
                if (changePercent != null) {
                    ChangeChip(changePercent)
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChangeChip(percent: Double) {
    val isPositive = percent > 0
    val isNeutral = percent == 0.0
    val bg = when {
        isPositive -> Color(0xFFE8F5E9)
        isNeutral -> Color(0xFFE0E0E0)
        else -> Color(0xFFFFEBEE)
    }
    val fg = when {
        isPositive -> Color(0xFF2E7D32)
        isNeutral -> Color(0xFF616161)
        else -> Color(0xFFC62828)
    }
    val arrow = when {
        isPositive -> "↑"
        isNeutral -> "—"
        else -> "↓"
    }

    val changeDescription = when {
        isPositive -> "Aumento de ${String.format("%.0f", kotlin.math.abs(percent))} por cento"
        isNeutral -> "Sem alteração"
        else -> "Diminuição de ${String.format("%.0f", kotlin.math.abs(percent))} por cento"
    }
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = changeDescription
        }
    ) {
        Text(
            text = "$arrow ${String.format("%.0f", kotlin.math.abs(percent))}%",
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun <T> List<T>.averageOrNull(selector: (T) -> Double): Double? {
    if (isEmpty()) return null
    return map(selector).average()
}

private fun percentChange(current: Double?, previous: Double?): Double? {
    if (current == null) return null
    if (previous == null || previous == 0.0) return null
    return ((current - previous) / previous) * 100
}
