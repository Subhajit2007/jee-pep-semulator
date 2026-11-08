package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.TrackerLog
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TrackerRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeTrackerLogs(): Flow<List<TrackerLog>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/trackerLogs"
        emitAll(
            db.collection("users").document(uid).collection("trackerLogs")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(TrackerLog::class.java)
                        .sortedByDescending { it.dayNumber }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addTrackerLog(log: TrackerLog): String {
        val uid = requireUserId()
        val docRef = if (log.id.isNotBlank()) {
            db.collection("users").document(uid).collection("trackerLogs").document(log.id)
        } else {
            db.collection("users").document(uid).collection("trackerLogs").document("day_${log.dayNumber}")
        }

        val payload = mutableMapOf<String, Any>(
            "userId" to uid,
            "dayNumber" to log.dayNumber,
            "date" to log.date,
            "totalStudyHours" to log.totalStudyHours,
            "pyqsSolved" to log.pyqsSolved,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (log.physicsHours > 0) payload["physicsHours"] = log.physicsHours
        if (log.chemistryHours > 0) payload["chemistryHours"] = log.chemistryHours
        if (log.mathsHours > 0) payload["mathsHours"] = log.mathsHours
        if (log.examMarks != 0.0) payload["examMarks"] = log.examMarks
        if (log.notes.isNotBlank()) payload["notes"] = log.notes

        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteTrackerLog(logId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("trackerLogs").document(logId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
