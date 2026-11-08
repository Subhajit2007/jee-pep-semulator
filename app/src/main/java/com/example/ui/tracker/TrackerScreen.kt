package com.example.ui.tracker

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TrackerLog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ChemistryColor
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MathsColor
import com.example.ui.theme.PhysicsColor
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardTranslucent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    var showLogForm by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<TrackerLog?>(null) }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearUserMessage()
        }
    }

    val logs = (uiState as? TrackerUiState.Success)?.logs ?: emptyList()
    val daysCount = logs.size
    val totalHours = logs.sumOf { it.totalStudyHours }
    val totalPyqs = logs.sumOf { it.pyqsSolved }
    val progressFraction = (daysCount / 100f).coerceIn(0f, 1f)

    // Calculate streak
    val streak = remember(logs) {
        calculateStreak(logs)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "100-Day JEE Challenge",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Consistent daily effort leads to AIR 1",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // CSV Export Button
                OutlinedButton(
                    onClick = { viewModel.exportToCsv(context) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CyanPrimary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(CyanPrimary, VioletAccent))),
                    modifier = Modifier.testTag("export_csv_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export CSV",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Circular & Linear Progress Dashboard Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardTranslucent)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Circular progress indicator
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(100.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = SurfaceCard,
                                strokeWidth = 9.dp
                            )
                            CircularProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier.fillMaxSize(),
                                color = CyanPrimary,
                                strokeWidth = 9.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$daysCount",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "/ 100 Days",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Quick Statistics grid
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatRow(
                                icon = Icons.Default.Timer,
                                iconColor = PhysicsColor,
                                label = "Total Study",
                                value = String.format(Locale.getDefault(), "%.1f hrs", totalHours)
                            )
                            StatRow(
                                icon = Icons.Default.MenuBook,
                                iconColor = ChemistryColor,
                                label = "PYQs Solved",
                                value = "$totalPyqs Qs"
                            )
                            StatRow(
                                icon = Icons.Default.Whatshot,
                                iconColor = AmberWarning,
                                label = "Day Streak",
                                value = "$streak Days 🔥"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Linear progress bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Challenge Completion",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "${(progressFraction * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = CyanPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CyanPrimary,
                            trackColor = SurfaceCard
                        )
                    }
                }
            }
        }

        // Action to open Log Form
        item {
            Button(
                onClick = { showLogForm = !showLogForm },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("log_metrics_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showLogForm) SurfaceCard else CyanPrimary,
                    contentColor = if (showLogForm) TextPrimary else BackgroundDark
                )
            ) {
                Icon(
                    imageVector = if (showLogForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showLogForm) "Cancel Logging" else "Log Today's Study Metrics",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Daily Logging Form (Expandable)
        if (showLogForm) {
            item {
                DailyLogForm(
                    nextDayNumber = (logs.maxOfOrNull { it.dayNumber } ?: 0L) + 1L,
                    isSaving = isSaving,
                    onSave = { day, date, hours, pyqs, p, c, m, marks, notes ->
                        viewModel.saveDailyLog(
                            dayNumber = day,
                            date = date,
                            totalStudyHours = hours,
                            pyqsSolved = pyqs,
                            physicsHours = p,
                            chemistryHours = c,
                            mathsHours = m,
                            examMarks = marks,
                            notes = notes,
                            onSuccess = { showLogForm = false }
                        )
                    }
                )
            }
        }

        // Logged Days Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Past Logged Days (${logs.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                if (logs.isNotEmpty()) {
                    Text(
                        text = "Backed up to Firestore",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSuccess
                    )
                }
            }
        }

        if (uiState is TrackerUiState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            }
        } else if (logs.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceCardTranslucent
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No study days logged yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap 'Log Today\\'s Study Metrics' above to start your 100-day journey!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(logs, key = { it.id.ifBlank { "${it.dayNumber}_${it.date}" } }) { log ->
                TrackerLogCard(
                    log = log,
                    onDelete = { logToDelete = log }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // padding for bottom bar
        }
    }

    // Delete confirmation dialog
    logToDelete?.let { log ->
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("Delete Day ${log.dayNumber} Log?") },
            text = { Text("This will permanently remove the logged entry for ${log.date} from your Firestore account.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLog(log.id)
                        logToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

@Composable
private fun StatRow(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
    }
}

@Composable
private fun DailyLogForm(
    nextDayNumber: Long,
    isSaving: Boolean,
    onSave: (Long, String, Double, Long, Double, Double, Double, Double, String) -> Unit
) {
    val currentDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var dayNumberStr by remember { mutableStateOf(nextDayNumber.coerceIn(1, 100).toString()) }
    var dateStr by remember { mutableStateOf(currentDateStr) }
    var totalHoursStr by remember { mutableStateOf("8.0") }
    var pyqsSolvedStr by remember { mutableStateOf("50") }
    var physicsHoursStr by remember { mutableStateOf("2.5") }
    var chemistryHoursStr by remember { mutableStateOf("2.5") }
    var mathsHoursStr by remember { mutableStateOf("3.0") }
    var examMarksStr by remember { mutableStateOf("") }
    var notesStr by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Log Study Session",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CyanPrimary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dayNumberStr,
                    onValueChange = { dayNumberStr = it },
                    label = { Text("Day # (1-100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_day_number"),
                    colors = outlinedFieldColors()
                )
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("input_date"),
                    colors = outlinedFieldColors()
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = totalHoursStr,
                    onValueChange = { totalHoursStr = it },
                    label = { Text("Total Hours") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_total_hours"),
                    colors = outlinedFieldColors()
                )
                OutlinedTextField(
                    value = pyqsSolvedStr,
                    onValueChange = { pyqsSolvedStr = it },
                    label = { Text("PYQs Solved") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_pyqs_solved"),
                    colors = outlinedFieldColors()
                )
            }

            Text(
                text = "Subject-Wise Time Allocation (Hours)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = physicsHoursStr,
                    onValueChange = { physicsHoursStr = it },
                    label = { Text("Physics", color = PhysicsColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = outlinedFieldColors()
                )
                OutlinedTextField(
                    value = chemistryHoursStr,
                    onValueChange = { chemistryHoursStr = it },
                    label = { Text("Chem", color = ChemistryColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = outlinedFieldColors()
                )
                OutlinedTextField(
                    value = mathsHoursStr,
                    onValueChange = { mathsHoursStr = it },
                    label = { Text("Maths", color = MathsColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = outlinedFieldColors()
                )
            }

            OutlinedTextField(
                value = examMarksStr,
                onValueChange = { examMarksStr = it },
                label = { Text("Mock Exam Marks (Optional, e.g. 210/300)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_exam_marks"),
                colors = outlinedFieldColors()
            )

            OutlinedTextField(
                value = notesStr,
                onValueChange = { notesStr = it },
                label = { Text("Topics Covered & Revision Notes") },
                placeholder = { Text("e.g. Rotational mechanics PYQs, Aldol condensation mechanism") },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_notes"),
                colors = outlinedFieldColors()
            )

            Button(
                onClick = {
                    val day = dayNumberStr.toLongOrNull() ?: nextDayNumber
                    val totalH = totalHoursStr.toDoubleOrNull() ?: 0.0
                    val pyqs = pyqsSolvedStr.toLongOrNull() ?: 0L
                    val phy = physicsHoursStr.toDoubleOrNull() ?: 0.0
                    val chem = chemistryHoursStr.toDoubleOrNull() ?: 0.0
                    val math = mathsHoursStr.toDoubleOrNull() ?: 0.0
                    val marks = examMarksStr.toDoubleOrNull() ?: 0.0
                    onSave(day, dateStr, totalH, pyqs, phy, chem, math, marks, notesStr)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_log_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = BackgroundDark
                ),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BackgroundDark)
                } else {
                    Text("Save to Firestore", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrackerLogCard(
    log: TrackerLog,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardTranslucent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "D${log.dayNumber}",
                            color = CyanPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Day ${log.dayNumber}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = log.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (log.examMarks > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberWarning.copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "Score: ${log.examMarks.toInt()}",
                                color = AmberWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete log",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricBadge(
                    label = "Total: ${log.totalStudyHours}h",
                    color = CyanPrimary
                )
                MetricBadge(
                    label = "${log.pyqsSolved} PYQs",
                    color = EmeraldSuccess
                )
                if (log.physicsHours > 0) {
                    MetricBadge(
                        label = "Phy: ${log.physicsHours}h",
                        color = PhysicsColor
                    )
                }
                if (log.chemistryHours > 0) {
                    MetricBadge(
                        label = "Chem: ${log.chemistryHours}h",
                        color = ChemistryColor
                    )
                }
                if (log.mathsHours > 0) {
                    MetricBadge(
                        label = "Math: ${log.mathsHours}h",
                        color = MathsColor
                    )
                }
            }

            if (log.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = log.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun MetricBadge(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanPrimary,
    unfocusedBorderColor = SurfaceBorder,
    focusedLabelColor = CyanPrimary,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)

private fun calculateStreak(logs: List<TrackerLog>): Int {
    if (logs.isEmpty()) return 0
    val sorted = logs.mapNotNull { it.dayNumber }.distinct().sortedDescending()
    var streak = 0
    var expected = sorted.firstOrNull() ?: return 0
    for (day in sorted) {
        if (day == expected) {
            streak++
            expected--
        } else {
            break
        }
    }
    return streak
}
