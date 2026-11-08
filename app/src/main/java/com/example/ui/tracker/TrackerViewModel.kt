package com.example.ui.tracker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.TrackerLog
import com.example.data.repository.TrackerRepository
import com.example.data.util.CsvExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface TrackerUiState {
    data object Loading : TrackerUiState
    data class Success(val logs: List<TrackerLog>) : TrackerUiState
    data class Error(val message: String) : TrackerUiState
}

class TrackerViewModel(private val repository: TrackerRepository) : ViewModel() {

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    val uiState: StateFlow<TrackerUiState> = repository.observeTrackerLogs()
        .map<List<TrackerLog>, TrackerUiState> { TrackerUiState.Success(it) }
        .catch { error ->
            emit(TrackerUiState.Error(error.localizedMessage ?: "Failed to load study tracker logs"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = TrackerUiState.Loading
        )

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun saveDailyLog(
        dayNumber: Long,
        date: String,
        totalStudyHours: Double,
        pyqsSolved: Long,
        physicsHours: Double,
        chemistryHours: Double,
        mathsHours: Double,
        examMarks: Double,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val formattedDate = if (date.isBlank()) {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                } else date

                val log = TrackerLog(
                    id = "day_$dayNumber",
                    dayNumber = dayNumber,
                    date = formattedDate,
                    totalStudyHours = totalStudyHours,
                    pyqsSolved = pyqsSolved,
                    physicsHours = physicsHours,
                    chemistryHours = chemistryHours,
                    mathsHours = mathsHours,
                    examMarks = examMarks,
                    notes = notes
                )
                repository.addTrackerLog(log)
                _userMessage.value = "Day $dayNumber progress saved to Firestore!"
                onSuccess()
            } catch (e: Exception) {
                _userMessage.value = "Failed to save: ${e.localizedMessage}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deleteLog(logId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTrackerLog(logId)
                _userMessage.value = "Log deleted"
            } catch (e: Exception) {
                _userMessage.value = "Failed to delete: ${e.localizedMessage}"
            }
        }
    }

    fun exportToCsv(context: Context) {
        val currentLogs = (uiState.value as? TrackerUiState.Success)?.logs ?: emptyList()
        if (currentLogs.isEmpty()) {
            _userMessage.value = "No study logs to export. Log at least one day first!"
            return
        }

        viewModelScope.launch {
            val result = CsvExporter.exportTrackerLogs(context, currentLogs)
            result.onSuccess { path ->
                _userMessage.value = "Exported ${currentLogs.size} logs to $path"
            }.onFailure { error ->
                _userMessage.value = "CSV Export failed: ${error.localizedMessage}"
            }
        }
    }
}
