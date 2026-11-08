package com.example.ui.planner

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PlannerTopic
import com.example.data.model.Subject
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ChemistryColor
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MathsColor
import com.example.ui.theme.PhysicsColor
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardTranslucent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Accordion expand/collapse state for days
    val expandedDays = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    // Group topics by Day Number
    val groupedByDay = remember(uiState.filteredTopics) {
        uiState.filteredTopics.groupBy { it.dayNumber }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "70-Day Study Planner",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Complete JEE Main & Advanced curriculum roadmap",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Cloud synced",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Auto-Synced",
                        fontSize = 11.sp,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Progress Overview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardTranslucent)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall Syllabus Coverage",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "${uiState.completedCount} / ${uiState.totalCount} Done (${(uiState.progressPercentage * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { uiState.progressPercentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = CyanPrimary,
                        trackColor = SurfaceCard
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subject Breakdown bars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SubjectMiniProgress(
                            name = "Physics",
                            fraction = uiState.subjectProgress(Subject.PHYSICS),
                            color = PhysicsColor,
                            modifier = Modifier.weight(1f)
                        )
                        SubjectMiniProgress(
                            name = "Chemistry",
                            fraction = uiState.subjectProgress(Subject.CHEMISTRY),
                            color = ChemistryColor,
                            modifier = Modifier.weight(1f)
                        )
                        SubjectMiniProgress(
                            name = "Maths",
                            fraction = uiState.subjectProgress(Subject.MATHS),
                            color = MathsColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Search & Subject Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search chapter, concept, or day...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = TextSecondary)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("planner_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedSubject == null,
                        onClick = { viewModel.selectSubject(null) },
                        label = { Text("All Subjects", fontSize = 12.sp) },
                        colors = filterChipColors()
                    )
                    FilterChip(
                        selected = uiState.selectedSubject == Subject.PHYSICS,
                        onClick = { viewModel.selectSubject(Subject.PHYSICS) },
                        label = { Text("Physics", fontSize = 12.sp) },
                        colors = filterChipColors(PhysicsColor)
                    )
                    FilterChip(
                        selected = uiState.selectedSubject == Subject.CHEMISTRY,
                        onClick = { viewModel.selectSubject(Subject.CHEMISTRY) },
                        label = { Text("Chemistry", fontSize = 12.sp) },
                        colors = filterChipColors(ChemistryColor)
                    )
                    FilterChip(
                        selected = uiState.selectedSubject == Subject.MATHS,
                        onClick = { viewModel.selectSubject(Subject.MATHS) },
                        label = { Text("Maths", fontSize = 12.sp) },
                        colors = filterChipColors(MathsColor)
                    )
                }
            }
        }

        if (uiState.isLoading) {
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
        } else if (groupedByDay.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceCardTranslucent
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No topics found matching '${uiState.searchQuery}'",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            // Render day-by-day accordion list
            groupedByDay.keys.sorted().forEach { day ->
                val dayTopics = groupedByDay[day] ?: emptyList()
                val dayCompleted = dayTopics.count { uiState.completedTopicIds.contains(it.id) }
                val isExpanded = expandedDays[day] ?: true // default expanded

                item(key = "day_$day") {
                    DayAccordionCard(
                        dayNumber = day,
                        topics = dayTopics,
                        completedCount = dayCompleted,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedDays[day] = !isExpanded
                        },
                        completedTopicIds = uiState.completedTopicIds,
                        onToggleTopic = { topicId ->
                            viewModel.toggleTopic(topicId)
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SubjectMiniProgress(
    name: String,
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
            Text(text = "${(fraction * 100).toInt()}%", fontSize = 11.sp, color = TextSecondary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = SurfaceCard
        )
    }
}

@Composable
private fun DayAccordionCard(
    dayNumber: Int,
    topics: List<PlannerTopic>,
    completedCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    completedTopicIds: Set<String>,
    onToggleTopic: (String) -> Unit
) {
    val allDayDone = completedCount == topics.size && topics.isNotEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (allDayDone) EmeraldSuccess.copy(alpha = 0.5f) else SurfaceBorder,
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (allDayDone) SurfaceCard.copy(alpha = 0.9f) else SurfaceCardTranslucent
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (allDayDone) EmeraldSuccess.copy(alpha = 0.2f) else CyanPrimary.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (allDayDone) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = "D$dayNumber",
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Day $dayNumber Schedule",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "$completedCount of ${topics.size} topics completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (allDayDone) EmeraldSuccess else TextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSecondary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    topics.forEach { topic ->
                        val isDone = completedTopicIds.contains(topic.id)
                        TopicItemRow(
                            topic = topic,
                            isDone = isDone,
                            onToggle = { onToggleTopic(topic.id) }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun TopicItemRow(
    topic: PlannerTopic,
    isDone: Boolean,
    onToggle: () -> Unit
) {
    val subjectColor = when (topic.subject) {
        Subject.PHYSICS -> PhysicsColor
        Subject.CHEMISTRY -> ChemistryColor
        Subject.MATHS -> MathsColor
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onToggle() },
        color = BackgroundDark.copy(alpha = 0.6f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isDone) EmeraldSuccess.copy(alpha = 0.4f) else SurfaceBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldSuccess,
                    uncheckedColor = TextMuted,
                    checkmarkColor = BackgroundDark
                ),
                modifier = Modifier.testTag("checkbox_${topic.id}")
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Subject tag
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = subjectColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = topic.subject.displayName,
                            color = subjectColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (topic.isHighWeightage) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AmberWarning.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "High Yield",
                                color = AmberWarning,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "${topic.targetPYQs} PYQs",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = topic.chapter,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDone) TextMuted else TextPrimary
                    )
                )

                Text(
                    text = topic.keyConcepts,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDone) TextMuted else TextSecondary,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun filterChipColors(selectedColor: Color = CyanPrimary) = FilterChipDefaults.filterChipColors(
    containerColor = SurfaceCard,
    selectedContainerColor = selectedColor.copy(alpha = 0.25f),
    selectedLabelColor = selectedColor,
    labelColor = TextSecondary
)
