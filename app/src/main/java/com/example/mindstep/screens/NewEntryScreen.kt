package com.example.mindstep.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.example.mindstep.composables.TextInput

private val moodLabels = listOf("Muito mal", "Mal", "Neutro", "Bem", "Muito bem")
private val anxietyLabels = listOf("Muito baixa", "Baixa", "Moderada", "Alta", "Muito alta")
private val valueColors = listOf("#c10007", "#ca3500", "#a65f00", "#497d00", "#008236")

@Composable
fun NewEntryScreen() {
    val (mood, setMood) = remember { mutableStateOf(3) }
    val (anxiety, setAnxiety) = remember { mutableStateOf(3) }
    val (sleep, setSleep) = remember { mutableStateOf("8") }
    val (steps, setSteps) = remember { mutableStateOf("5000") }
    val (waterGlasses, setWaterGlasses) = remember { mutableStateOf("6") }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
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
                    .background(Color.White)
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
                .background(Color.White)
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
                            Color(android.graphics.Color.parseColor(valueColors[value - 1])).copy(
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
                        tint = Color(android.graphics.Color.parseColor(valueColors[value - 1])))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                ){
                    @OptIn(ExperimentalMaterial3Api::class)
                    Slider(
                        value = value.toFloat(),
                        onValueChange = { setValue(it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3,
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
                    )
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
            .background(Color.White),
    ) {
        NewEntryScreen()
    }
}
