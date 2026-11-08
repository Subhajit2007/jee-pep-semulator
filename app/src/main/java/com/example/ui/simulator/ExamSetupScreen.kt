package com.example.ui.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PhysicsColor
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardTranslucent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun ExamSetupScreen(
    onStartExam: (String, Int, Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("JEE Main Full Mock 1") }
    var totalQuestionsStr by remember { mutableStateOf("75") }
    var timeLimitStr by remember { mutableStateOf("180") }
    var nvtRangeStr by remember { mutableStateOf("21-25, 46-50, 71-75") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Title Header
        Column {
            Text(
                text = "OMR Exam Simulator",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Exact real exam interface with custom NVT keypad & live timer",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Quick Presets
        Text(
            text = "Quick Presets",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = CyanPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PresetCard(
                title = "JEE Main Full",
                subtitle = "75 Qs • 180 min",
                accentColor = CyanPrimary,
                modifier = Modifier.weight(1f),
                onClick = {
                    title = "JEE Main Full Mock"
                    totalQuestionsStr = "75"
                    timeLimitStr = "180"
                    nvtRangeStr = "21-25, 46-50, 71-75"
                }
            )

            PresetCard(
                title = "Rapid Sprint",
                subtitle = "30 Qs • 60 min",
                accentColor = VioletAccent,
                modifier = Modifier.weight(1f),
                onClick = {
                    title = "PCM Rapid Sprint"
                    totalQuestionsStr = "30"
                    timeLimitStr = "60"
                    nvtRangeStr = "9-10, 19-20, 29-30"
                }
            )

            PresetCard(
                title = "Subject Test",
                subtitle = "25 Qs • 60 min",
                accentColor = AmberWarning,
                modifier = Modifier.weight(1f),
                onClick = {
                    title = "Physics Sectional Mock"
                    totalQuestionsStr = "25"
                    timeLimitStr = "60"
                    nvtRangeStr = "21-25"
                }
            )
        }

        // Configuration Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCardTranslucent)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Exam Configuration",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Exam Name / Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_exam_title"),
                    colors = outlinedColors()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = totalQuestionsStr,
                        onValueChange = { totalQuestionsStr = it },
                        label = { Text("Total Questions") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("setup_total_questions"),
                        colors = outlinedColors()
                    )

                    OutlinedTextField(
                        value = timeLimitStr,
                        onValueChange = { timeLimitStr = it },
                        label = { Text("Time Limit (Mins)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("setup_time_limit"),
                        colors = outlinedColors()
                    )
                }

                OutlinedTextField(
                    value = nvtRangeStr,
                    onValueChange = { nvtRangeStr = it },
                    label = { Text("NVT Question Numbers/Ranges") },
                    placeholder = { Text("e.g. 21-25, 46-50, 71-75") },
                    supportingText = {
                        Text("These questions will use the in-app custom numerical keypad")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_nvt_range"),
                    colors = outlinedColors()
                )
            }
        }

        // Marking Scheme & Instructions Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceCard.copy(alpha = 0.6f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Marking Scheme & Exam Hall Protocol",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Standard MCQs: +4 for correct, -1 for incorrect, 0 if unattempted\n" +
                        "• Numerical Value Type (NVT): Type direct number/decimal using built-in keypad\n" +
                        "• Sticky Top Bar: Live countdown timer turns RED in last 2 minutes\n" +
                        "• Auto-Export: Submitting generates OMR_Analysis.csv in Downloads",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        // Start Button
        Button(
            onClick = {
                val total = totalQuestionsStr.toIntOrNull()?.coerceIn(1, 200) ?: 75
                val time = timeLimitStr.toIntOrNull()?.coerceIn(1, 360) ?: 180
                onStartExam(title, total, time, nvtRangeStr)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("start_exam_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyanPrimary,
                contentColor = BackgroundDark
            )
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Start Exam Simulator",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun PresetCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        color = SurfaceCardTranslucent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanPrimary,
    unfocusedBorderColor = SurfaceBorder,
    focusedLabelColor = CyanPrimary,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)
