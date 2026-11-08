package com.example.ui.simulator

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamQuestion
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
import java.util.Locale

@Composable
fun ExamActiveScreen(
    state: ExamUiState,
    viewModel: ExamViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSubmitDialog by remember { mutableStateOf(false) }

    val question = state.currentQuestion ?: return

    // Timer formatting
    val hours = state.remainingSeconds / 3600
    val minutes = (state.remainingSeconds % 3600) / 60
    val seconds = state.remainingSeconds % 60
    val formattedTimer = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

    val timerColor by animateColorAsState(
        targetValue = if (state.isLastTwoMinutes) RoseDanger else CyanPrimary,
        label = "timer_color"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // --- 1. STICKY TOP BAR ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceCard,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timer Container (turns red in last 2 mins)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (state.isLastTwoMinutes) RoseDanger.copy(alpha = 0.2f) else SurfaceCardTranslucent)
                            .border(
                                1.dp,
                                if (state.isLastTwoMinutes) RoseDanger else SurfaceBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isLastTwoMinutes) Icons.Default.Warning else Icons.Default.Timer,
                            contentDescription = null,
                            tint = timerColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedTimer,
                            color = timerColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.testTag("exam_timer_display")
                        )
                    }

                    // Answered live counter
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldSuccess.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "Answered: ${state.attemptedCount} / ${state.questions.size}",
                            color = EmeraldSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("answered_counter_badge")
                        )
                    }

                    // Submit Exam Button
                    Button(
                        onClick = { showSubmitDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoseDanger,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("top_submit_button")
                    ) {
                        Text(
                            text = "Submit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- Question Number Strip (Quick jump) ---
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(state.questions) { index, q ->
                        QuestionChip(
                            number = q.questionNumber,
                            isSelected = index == state.currentIndex,
                            isAttempted = q.isAttempted,
                            isReview = q.isMarkedForReview,
                            onClick = { viewModel.jumpToQuestion(index) }
                        )
                    }
                }
            }
        }

        // --- 2. QUESTION & OMR AREA (Scrollable) ---
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Question Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardTranslucent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Question ${question.questionNumber}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            val subjectColor = when (question.subject) {
                                Subject.PHYSICS -> PhysicsColor
                                Subject.CHEMISTRY -> ChemistryColor
                                Subject.MATHS -> MathsColor
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = subjectColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = question.subject.displayName,
                                    color = subjectColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Mark for Review toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleMarkForReview() }
                                .background(
                                    if (question.isMarkedForReview) VioletAccent.copy(alpha = 0.2f) else Color.Transparent
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (question.isMarkedForReview) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Review",
                                tint = if (question.isMarkedForReview) VioletAccent else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Review",
                                fontSize = 11.sp,
                                color = if (question.isMarkedForReview) VioletAccent else TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Topic: ${question.chapter} • ${if (question.type == ExamQuestionType.MCQ) "Multiple Choice (+4, -1)" else "Numerical Value Type (+4, 0)"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // --- ANSWER INPUT AREA ---
            if (question.type == ExamQuestionType.MCQ) {
                // Radio-button style OMR Bubbles
                McqOmrGrid(
                    selectedOption = question.markedOption,
                    onOptionSelect = { option -> viewModel.selectOption(option) }
                )
            } else {
                // Custom Built-In Compose Numerical Keypad (NO Android System Keyboard!)
                NvtKeypadSection(
                    currentValue = question.nvtAnswer,
                    onAppend = { char -> viewModel.appendNvtInput(char) },
                    onBackspace = { viewModel.backspaceNvt() },
                    onClear = { viewModel.clearOption() }
                )
            }

            // Clear Selection Button
            if (question.isAttempted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { viewModel.clearOption() },
                        colors = ButtonDefaults.textButtonColors(contentColor = RoseDanger)
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Answer", fontSize = 12.sp)
                    }
                }
            }
        }

        // --- 3. BOTTOM STICKY NAVIGATION ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceCard,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.prevQuestion() },
                    enabled = state.currentIndex > 0,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder)),
                    modifier = Modifier.testTag("prev_question_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Previous")
                }

                Text(
                    text = "${question.questionNumber} / ${state.questions.size}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )

                Button(
                    onClick = {
                        if (state.currentIndex == state.questions.size - 1) {
                            showSubmitDialog = true
                        } else {
                            viewModel.nextQuestion()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = BackgroundDark
                    ),
                    modifier = Modifier.testTag("next_question_button")
                ) {
                    Text(
                        text = if (state.currentIndex == state.questions.size - 1) "Review & Submit" else "Save & Next",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitDialog) {
        val attempted = state.attemptedCount
        val total = state.questions.size
        val unattempted = total - attempted
        val review = state.markedForReviewCount

        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = {
                Text(
                    text = "Submit Exam Simulation?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Are you sure you want to finalize and submit your test?")
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BackgroundDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• Attempted: $attempted questions", color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                            Text("• Unattempted: $unattempted questions", color = AmberWarning)
                            Text("• Marked for Review: $review questions", color = VioletAccent)
                            Text("• Time Remaining: $formattedTimer", color = CyanPrimary)
                        }
                    }
                    Text(
                        text = "Results will be saved to your Firestore account and an OMR_Analysis.csv file will be generated to Downloads.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitExam(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = BackgroundDark),
                    modifier = Modifier.testTag("confirm_submit_button")
                ) {
                    Text("Submit Test Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Return to Test", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

@Composable
private fun QuestionChip(
    number: Int,
    isSelected: Boolean,
    isAttempted: Boolean,
    isReview: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isReview -> VioletAccent
        isAttempted -> EmeraldSuccess
        else -> SurfaceCard
    }

    val textColor = when {
        isReview || isAttempted -> BackgroundDark
        else -> TextSecondary
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .background(bgColor)
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) CyanPrimary else SurfaceBorder,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$number",
            color = textColor,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun McqOmrGrid(
    selectedOption: String?,
    onOptionSelect: (String) -> Unit
) {
    val options = listOf("A", "B", "C", "D")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardTranslucent)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "OMR Response Sheet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CyanPrimary
            )
            Text(
                text = "Tap a bubble to darken your response (A, B, C, or D):",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                options.forEach { option ->
                    val isSelected = selectedOption == option
                    OmrBubble(
                        letter = option,
                        isSelected = isSelected,
                        onClick = { onOptionSelect(option) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OmrBubble(
    letter: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(if (isSelected) CyanPrimary else SurfaceCard)
                .border(
                    width = if (isSelected) 2.5.dp else 1.5.dp,
                    color = if (isSelected) Color.White else SurfaceBorder,
                    shape = CircleShape
                )
                .testTag("omr_bubble_$letter"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                color = if (isSelected) BackgroundDark else TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isSelected) "Marked" else "Option $letter",
            fontSize = 11.sp,
            color = if (isSelected) CyanPrimary else TextMuted
        )
    }
}

// Custom built-in Compose Keypad for Numerical Value Type questions (No Native Android System Keyboard)
@Composable
private fun NvtKeypadSection(
    currentValue: String,
    onAppend: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardTranslucent)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Numerical Value Response (NVT)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CyanPrimary
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AmberWarning.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Built-in Keypad",
                        fontSize = 10.sp,
                        color = AmberWarning,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Digital value display box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundDark)
                    .border(1.5.dp, if (currentValue.isNotBlank()) CyanPrimary else SurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp)
                    .testTag("nvt_display_box"),
                contentAlignment = Alignment.Center
            ) {
                if (currentValue.isBlank()) {
                    Text(
                        text = "Tap keypad numbers below to enter value",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = currentValue,
                        color = CyanPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Built-In Numerical Keypad Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeypadRow(listOf("1", "2", "3"), onAppend)
                KeypadRow(listOf("4", "5", "6"), onAppend)
                KeypadRow(listOf("7", "8", "9"), onAppend)

                // Row with negative sign, 0, and decimal point
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KeypadKey(label = "± Negative (-)", modifier = Modifier.weight(1f)) { onAppend("-") }
                    KeypadKey(label = "0", modifier = Modifier.weight(1f)) { onAppend("0") }
                    KeypadKey(label = ". (Decimal)", modifier = Modifier.weight(1f)) { onAppend(".") }
                }

                // Action Row: Backspace & Clear
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onBackspace,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("nvt_key_backspace"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceCard,
                            contentColor = AmberWarning
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Backspace, contentDescription = "Backspace", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backspace", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onClear,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("nvt_key_clear"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceCard,
                            contentColor = RoseDanger
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear All", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadRow(
    keys: List<String>,
    onKeyClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { key ->
            KeypadKey(
                label = key,
                modifier = Modifier.weight(1f),
                onClick = { onKeyClick(key) }
            )
        }
    }
}

@Composable
private fun KeypadKey(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
            .testTag("nvt_key_$label"),
        color = BackgroundDark,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
