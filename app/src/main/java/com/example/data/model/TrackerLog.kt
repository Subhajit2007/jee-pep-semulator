package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class TrackerLog(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val dayNumber: Long = 1L,
    val date: String = "",
    val totalStudyHours: Double = 0.0,
    val pyqsSolved: Long = 0L,
    val physicsHours: Double = 0.0,
    val chemistryHours: Double = 0.0,
    val mathsHours: Double = 0.0,
    val examMarks: Double = 0.0,
    val notes: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)
