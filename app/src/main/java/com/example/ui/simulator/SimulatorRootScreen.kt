package com.example.ui.simulator

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SimulatorRootScreen(
    viewModel: ExamViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = state.stage != ExamStage.SETUP) {
        if (state.stage == ExamStage.RESULT) {
            viewModel.resetToSetup()
        }
    }

    when (state.stage) {
        ExamStage.SETUP -> {
            ExamSetupScreen(
                onStartExam = { title, total, time, nvtRange ->
                    viewModel.startExam(title, total, time, nvtRange)
                },
                modifier = modifier
            )
        }
        ExamStage.ACTIVE -> {
            ExamActiveScreen(
                state = state,
                viewModel = viewModel,
                modifier = modifier
            )
        }
        ExamStage.RESULT -> {
            ExamResultScreen(
                state = state,
                viewModel = viewModel,
                modifier = modifier
            )
        }
    }
}
