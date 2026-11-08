package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class Subject(val displayName: String) {
    PHYSICS("Physics"),
    CHEMISTRY("Chemistry"),
    MATHS("Mathematics")
}

data class PlannerTopic(
    val id: String,
    val dayNumber: Int,
    val subject: Subject,
    val chapter: String,
    val topicTitle: String,
    val keyConcepts: String,
    val targetPYQs: Int,
    val isHighWeightage: Boolean = false
)

@IgnoreExtraProperties
data class PlannerProgress(
    @DocumentId
    val id: String = "status",
    val userId: String = "",
    val completedTopicIds: List<String> = emptyList(),
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)
