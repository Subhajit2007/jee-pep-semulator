package com.example.ui.simulator

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamQuestionType
import com.example.data.model.Subject
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

@Composable
fun ExamResultScreen(
    state: ExamUiState,
    viewModel: ExamViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearUserMessage()
        }
    }

    val total = state.questions.size
    val attempted = state.attemptedCount
    val unattempted = total - attempted
    val minutes = state.timeTakenSeconds / 60
    val seconds = state.timeTakenSeconds % 60

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Scorecard Trophy Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardTranslucent)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = CyanPrimary,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Exam Simulation Completed!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = TextPrimary
                    )

                    Text(
                        text = state.config.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyanPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Stat Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ResultStatBox(
                            label = "Attempted",
                            value = "$attempted / $total",
                            color = EmeraldSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        ResultStatBox(
                            label = "Unattempted",
                            value = "$unattempted",
                            color = AmberWarning,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ResultStatBox(
                            label = "Time Taken",
                            value = "${minutes}m ${seconds}s",
                            color = PhysicsColor,
                            modifier = Modifier.weight(1f)
                        )
                        ResultStatBox(
                            label = "Marked Review",
                            value = "${state.markedForReviewCount}",
                            color = VioletAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Automatic CSV Generation Notification Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldSuccess.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = SurfaceCardTranslucent
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OMR_Analysis.csv Generated",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldSuccess
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.reExportCsv(context) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary)
                        ) {
                            Text("Re-Export", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "File saved to device Downloads: ${state.csvSavedPath ?: "Downloads/OMR_Analysis.csv"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Backed up to Firestore: users/{userId}/examResults",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Button(
                onClick = { viewModel.resetToSetup() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("new_exam_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = BackgroundDark
                )
            ) {
                Icon(imageVector = Icons.Default.Replay, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start New Exam Simulation", fontWeight = FontWeight.Bold)
            }
        }

        // Detailed Responses Breakdown Header
        item {
            Text(
                text = "Detailed Responses (${state.questions.size} Questions)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        // List of question responses
        items(state.questions) { q ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, SurfaceBorder, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardTranslucent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (q.isAttempted) EmeraldSuccess.copy(alpha = 0.2f) else SurfaceCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Q${q.questionNumber}",
                                color = if (q.isAttempted) EmeraldSuccess else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            val subjectColor = when (q.subject) {
                                Subject.PHYSICS -> PhysicsColor
                                Subject.CHEMISTRY -> ChemistryColor
                                Subject.MATHS -> MathsColor
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = q.subject.displayName,
                                    color = subjectColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = " • ${q.type.name}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = q.chapter,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    // Marked Answer Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (q.isAttempted) CyanPrimary.copy(alpha = 0.2f) else SurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (q.isAttempted) CyanPrimary.copy(alpha = 0.4f) else SurfaceBorder
                        )
                    ) {
                        Text(
                            text = if (q.isAttempted) "Ans: ${q.recordedAnswer}" else "Not Attempted",
                            color = if (q.isAttempted) CyanPrimary else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ResultStatBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = BackgroundDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
        }
    }
}
