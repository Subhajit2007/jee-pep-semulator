package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ExamQuestionType {
    MCQ,
    NVT // Numerical Value Type
}

data class ExamQuestion(
    val questionNumber: Int,
    val subject: Subject,
    val type: ExamQuestionType,
    val chapter: String,
    val markedOption: String? = null,
    val nvtAnswer: String = "",
    val isMarkedForReview: Boolean = false
) {
    val isAttempted: Boolean
        get() = if (type == ExamQuestionType.MCQ) !markedOption.isNullOrBlank() else nvtAnswer.isNotBlank()

    val recordedAnswer: String
        get() = if (type == ExamQuestionType.MCQ) markedOption ?: "-" else if (nvtAnswer.isNotBlank()) nvtAnswer else "-"
}

data class ExamConfig(
    val title: String = "JEE Main Full Mock 1",
    val totalQuestions: Int = 75,
    val timeLimitMinutes: Int = 180,
    val nvtRangeDescription: String = "21-25, 46-50, 71-75"
) {
    val nvtNumbers: Set<Int> by lazy {
        parseRange(nvtRangeDescription, totalQuestions)
    }

    companion object {
        fun parseRange(rangeText: String, total: Int): Set<Int> {
            val result = mutableSetOf<Int>()
            val parts = rangeText.split(",", ";", " ")
            for (part in parts) {
                val trimmed = part.trim()
                if (trimmed.isEmpty()) continue
                if (trimmed.contains("-")) {
                    val bounds = trimmed.split("-")
                    if (bounds.size == 2) {
                        val start = bounds[0].trim().toIntOrNull() ?: continue
                        val end = bounds[1].trim().toIntOrNull() ?: continue
                        for (i in minOf(start, end)..maxOf(start, end)) {
                            if (i in 1..total) result.add(i)
                        }
                    }
                } else {
                    val num = trimmed.toIntOrNull()
                    if (num != null && num in 1..total) {
                        result.add(num)
                    }
                }
            }
            return result
        }
    }
}

@IgnoreExtraProperties
data class ExamResult(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val examTitle: String = "",
    val totalQuestions: Long = 75L,
    val attemptedCount: Long = 0L,
    val timeLimitMinutes: Long = 180L,
    val timeTakenSeconds: Long = 0L,
    val nvtRange: String = "",
    val answersSummary: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)
