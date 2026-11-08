package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.repository.ExamRepository
import com.example.data.repository.PlannerRepository
import com.example.data.repository.TrackerRepository
import com.example.ui.auth.signOutUser
import com.example.ui.planner.PlannerScreen
import com.example.ui.planner.PlannerViewModel
import com.example.ui.simulator.ExamViewModel
import com.example.ui.simulator.SimulatorRootScreen
import com.example.ui.tracker.TrackerScreen
import com.example.ui.tracker.TrackerViewModel
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardTranslucent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

enum class NavDestination(val label: String, val icon: ImageVector, val tag: String) {
    TRACKER("Tracker", Icons.Default.Analytics, "nav_tracker"),
    PLANNER("Planner", Icons.Default.AssignmentTurnedIn, "nav_planner"),
    SIMULATOR("Simulator", Icons.Default.Timer, "nav_simulator")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentUserId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val user = remember { Firebase.auth.currentUser }

    // Repositories & ViewModels
    val trackerRepo = remember(currentUserId) { TrackerRepository(context) }
    val trackerViewModel = remember(currentUserId) { TrackerViewModel(trackerRepo) }

    val plannerRepo = remember(currentUserId) { PlannerRepository(context) }
    val plannerViewModel = remember(currentUserId) { PlannerViewModel(plannerRepo) }

    val examRepo = remember(currentUserId) { ExamRepository(context) }
    val examViewModel = remember(currentUserId) { ExamViewModel(examRepo) }

    var selectedDestination by remember { mutableStateOf(NavDestination.TRACKER) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(CyanPrimary, VioletAccent)))
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(BackgroundDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "JEE Prep Simulator",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            val email = user?.email ?: "Aspirant"
                            Text(
                                text = email,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            signOutUser(credentialManager, coroutineScope)
                        },
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceCard,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(
                        androidx.compose.foundation.BorderStroke(0.5.dp, SurfaceBorder)
                    )
            ) {
                NavDestination.entries.forEach { destination ->
                    val isSelected = selectedDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanPrimary,
                            selectedTextColor = CyanPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CyanPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(destination.tag)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedDestination) {
                NavDestination.TRACKER -> {
                    TrackerScreen(viewModel = trackerViewModel)
                }
                NavDestination.PLANNER -> {
                    PlannerScreen(viewModel = plannerViewModel)
                }
                NavDestination.SIMULATOR -> {
                    SimulatorRootScreen(viewModel = examViewModel)
                }
            }
        }
    }
}
