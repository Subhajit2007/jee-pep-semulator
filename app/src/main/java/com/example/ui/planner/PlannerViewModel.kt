package com.example.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PlannerTopic
import com.example.data.model.Subject
import com.example.data.repository.PlannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlannerUiState(
    val allTopics: List<PlannerTopic> = emptyList(),
    val completedTopicIds: Set<String> = emptySet(),
    val selectedSubject: Subject? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val userMessage: String? = null
) {
    val filteredTopics: List<PlannerTopic>
        get() = allTopics.filter { topic ->
            val matchesSubject = selectedSubject == null || topic.subject == selectedSubject
            val matchesSearch = searchQuery.isBlank() ||
                topic.chapter.contains(searchQuery, ignoreCase = true) ||
                topic.keyConcepts.contains(searchQuery, ignoreCase = true) ||
                "day ${topic.dayNumber}".contains(searchQuery, ignoreCase = true)
            matchesSubject && matchesSearch
        }

    val totalCount: Int get() = allTopics.size
    val completedCount: Int get() = allTopics.count { completedTopicIds.contains(it.id) }
    val progressPercentage: Float get() = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    fun subjectProgress(subject: Subject): Float {
        val subjectTopics = allTopics.filter { it.subject == subject }
        if (subjectTopics.isEmpty()) return 0f
        val completed = subjectTopics.count { completedTopicIds.contains(it.id) }
        return completed.toFloat() / subjectTopics.size
    }
}

class PlannerViewModel(private val repository: PlannerRepository) : ViewModel() {

    private val allTopics = repository.getCurated70DayTopics()

    private val _selectedSubject = MutableStateFlow<Subject?>(null)
    val selectedSubject: StateFlow<Subject?> = _selectedSubject.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PlannerUiState> = combine(
        repository.observePlannerProgress(),
        _selectedSubject,
        _searchQuery,
        _userMessage
    ) { completedIds, subject, query, message ->
        PlannerUiState(
            allTopics = allTopics,
            completedTopicIds = completedIds,
            selectedSubject = subject,
            searchQuery = query,
            isLoading = false,
            userMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = PlannerUiState(allTopics = allTopics, isLoading = true)
    )

    fun selectSubject(subject: Subject?) {
        _selectedSubject.value = subject
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTopic(topicId: String) {
        val currentCompleted = uiState.value.completedTopicIds.toMutableSet()
        if (currentCompleted.contains(topicId)) {
            currentCompleted.remove(topicId)
        } else {
            currentCompleted.add(topicId)
        }

        viewModelScope.launch {
            try {
                repository.saveCompletedTopics(currentCompleted)
            } catch (e: Exception) {
                _userMessage.value = "Failed to sync: ${e.localizedMessage}"
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
