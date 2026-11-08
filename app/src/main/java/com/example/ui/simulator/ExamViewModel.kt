package com.example.ui.simulator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ExamConfig
import com.example.data.model.ExamQuestion
import com.example.data.model.ExamQuestionType
import com.example.data.model.ExamResult
import com.example.data.model.Subject
import com.example.data.repository.ExamRepository
import com.example.data.util.CsvExporter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class ExamStage {
    SETUP,
    ACTIVE,
    RESULT
}

data class ExamUiState(
    val stage: ExamStage = ExamStage.SETUP,
    val config: ExamConfig = ExamConfig(),
    val questions: List<ExamQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val remainingSeconds: Long = 0L,
    val timeTakenSeconds: Long = 0L,
    val isTimerRunning: Boolean = false,
    val isSubmitting: Boolean = false,
    val savedResultId: String? = null,
    val csvSavedPath: String? = null,
    val userMessage: String? = null
) {
    val currentQuestion: ExamQuestion?
        get() = questions.getOrNull(currentIndex)

    val attemptedCount: Int
        get() = questions.count { it.isAttempted }

    val markedForReviewCount: Int
        get() = questions.count { it.isMarkedForReview }

    val isLastTwoMinutes: Boolean
        get() = remainingSeconds in 1..120
}

class ExamViewModel(private val repository: ExamRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamUiState())
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startExam(
        title: String,
        totalQuestions: Int,
        timeLimitMinutes: Int,
        nvtRangeDescription: String
    ) {
        timerJob?.cancel()

        val nvtNumbers = ExamConfig.parseRange(nvtRangeDescription, totalQuestions)
        val config = ExamConfig(
            title = title.ifBlank { "JEE Main Mock" },
            totalQuestions = totalQuestions,
            timeLimitMinutes = timeLimitMinutes,
            nvtRangeDescription = nvtRangeDescription
        )

        // Generate Questions with Subject and Chapter mappings
        val questionsList = mutableListOf<ExamQuestion>()
        val perSubject = totalQuestions / 3
        val chaptersPhy = listOf("Mechanics", "Thermodynamics", "Electrodynamics", "Optics", "Modern Physics")
        val chaptersChem = listOf("Physical Chemistry", "Inorganic Coordination", "Organic Reactions", "Chemical Bonding")
        val chaptersMath = listOf("Calculus", "Vectors & 3D", "Coordinate Geometry", "Algebra", "Probability")

        for (i in 1..totalQuestions) {
            val subject = when {
                i <= perSubject -> Subject.PHYSICS
                i <= perSubject * 2 -> Subject.CHEMISTRY
                else -> Subject.MATHS
            }
            val chapter = when (subject) {
                Subject.PHYSICS -> chaptersPhy[(i - 1) % chaptersPhy.size]
                Subject.CHEMISTRY -> chaptersChem[(i - 1) % chaptersChem.size]
                Subject.MATHS -> chaptersMath[(i - 1) % chaptersMath.size]
            }
            val isNvt = nvtNumbers.contains(i)

            questionsList.add(
                ExamQuestion(
                    questionNumber = i,
                    subject = subject,
                    type = if (isNvt) ExamQuestionType.NVT else ExamQuestionType.MCQ,
                    chapter = chapter
                )
            )
        }

        val totalSeconds = (timeLimitMinutes * 60).toLong()

        _uiState.value = ExamUiState(
            stage = ExamStage.ACTIVE,
            config = config,
            questions = questionsList,
            currentIndex = 0,
            remainingSeconds = totalSeconds,
            timeTakenSeconds = 0L,
            isTimerRunning = true
        )

        startTimer()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && _uiState.value.stage == ExamStage.ACTIVE) {
                delay(1000L)
                _uiState.value = _uiState.value.copy(
                    remainingSeconds = _uiState.value.remainingSeconds - 1,
                    timeTakenSeconds = _uiState.value.timeTakenSeconds + 1
                )
            }
            if (_uiState.value.remainingSeconds <= 0 && _uiState.value.stage == ExamStage.ACTIVE) {
                _uiState.value = _uiState.value.copy(
                    userMessage = "Time is up! Exam auto-submitting..."
                )
            }
        }
    }

    fun selectOption(option: String) {
        val current = _uiState.value.currentQuestion ?: return
        if (current.type != ExamQuestionType.MCQ) return

        val updated = current.copy(markedOption = option)
        updateCurrentQuestion(updated)
    }

    fun clearOption() {
        val current = _uiState.value.currentQuestion ?: return
        if (current.type == ExamQuestionType.MCQ) {
            updateCurrentQuestion(current.copy(markedOption = null))
        } else {
            updateCurrentQuestion(current.copy(nvtAnswer = ""))
        }
    }

    // Custom NVT Keypad methods
    fun appendNvtInput(char: String) {
        val current = _uiState.value.currentQuestion ?: return
        if (current.type != ExamQuestionType.NVT) return

        val currentVal = current.nvtAnswer
        val newVal = when (char) {
            "-" -> {
                if (currentVal.isEmpty()) "-"
                else if (currentVal.startsWith("-")) currentVal.substring(1)
                else "-$currentVal"
            }
            "." -> {
                if (currentVal.contains(".")) currentVal
                else if (currentVal.isEmpty() || currentVal == "-") currentVal + "0."
                else currentVal + "."
            }
            else -> {
                if (currentVal.length < 10) currentVal + char else currentVal
            }
        }
        updateCurrentQuestion(current.copy(nvtAnswer = newVal))
    }

    fun backspaceNvt() {
        val current = _uiState.value.currentQuestion ?: return
        if (current.type != ExamQuestionType.NVT) return
        val currentVal = current.nvtAnswer
        if (currentVal.isNotEmpty()) {
            updateCurrentQuestion(current.copy(nvtAnswer = currentVal.dropLast(1)))
        }
    }

    fun toggleMarkForReview() {
        val current = _uiState.value.currentQuestion ?: return
        updateCurrentQuestion(current.copy(isMarkedForReview = !current.isMarkedForReview))
    }

    private fun updateCurrentQuestion(updated: ExamQuestion) {
        val list = _uiState.value.questions.toMutableList()
        val idx = _uiState.value.currentIndex
        if (idx in list.indices) {
            list[idx] = updated
            _uiState.value = _uiState.value.copy(questions = list)
        }
    }

    fun jumpToQuestion(index: Int) {
        if (index in _uiState.value.questions.indices) {
            _uiState.value = _uiState.value.copy(currentIndex = index)
        }
    }

    fun nextQuestion() {
        val next = _uiState.value.currentIndex + 1
        if (next < _uiState.value.questions.size) {
            _uiState.value = _uiState.value.copy(currentIndex = next)
        }
    }

    fun prevQuestion() {
        val prev = _uiState.value.currentIndex - 1
        if (prev >= 0) {
            _uiState.value = _uiState.value.copy(currentIndex = prev)
        }
    }

    fun submitExam(context: Context) {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isSubmitting = true)

        viewModelScope.launch {
            val state = _uiState.value
            val timeTaken = state.timeTakenSeconds
            val attempted = state.attemptedCount

            // Format answers summary
            val summaryObj = JSONObject()
            state.questions.forEach { q ->
                if (q.isAttempted) {
                    summaryObj.put(q.questionNumber.toString(), q.recordedAnswer)
                }
            }

            val examResult = ExamResult(
                examTitle = state.config.title,
                totalQuestions = state.config.totalQuestions.toLong(),
                attemptedCount = attempted.toLong(),
                timeLimitMinutes = state.config.timeLimitMinutes.toLong(),
                timeTakenSeconds = timeTaken,
                nvtRange = state.config.nvtRangeDescription,
                answersSummary = summaryObj.toString()
            )

            // 1. Save to Firestore
            var savedId: String? = null
            try {
                savedId = repository.saveExamResult(examResult)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    userMessage = "Warning: Cloud sync error (${e.localizedMessage}), saving locally..."
                )
            }

            // 2. Export OMR_Analysis.csv to Downloads
            val csvResult = CsvExporter.exportOmrAnalysis(
                context = context,
                examTitle = state.config.title,
                questions = state.questions,
                timeTakenSeconds = timeTaken
            )

            val savedCsvPath = csvResult.getOrNull()

            _uiState.value = _uiState.value.copy(
                stage = ExamStage.RESULT,
                isSubmitting = false,
                savedResultId = savedId,
                csvSavedPath = savedCsvPath,
                userMessage = if (savedCsvPath != null) "OMR Analysis CSV saved to $savedCsvPath" else null
            )
        }
    }

    fun reExportCsv(context: Context) {
        val state = _uiState.value
        viewModelScope.launch {
            val result = CsvExporter.exportOmrAnalysis(
                context = context,
                examTitle = state.config.title,
                questions = state.questions,
                timeTakenSeconds = state.timeTakenSeconds
            )
            result.onSuccess { path ->
                _uiState.value = _uiState.value.copy(
                    csvSavedPath = path,
                    userMessage = "CSV exported to $path"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    userMessage = "Export failed: ${err.localizedMessage}"
                )
            }
        }
    }

    fun resetToSetup() {
        timerJob?.cancel()
        _uiState.value = ExamUiState(stage = ExamStage.SETUP)
    }

    fun clearUserMessage() {
        _uiState.value = _uiState.value.copy(userMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
