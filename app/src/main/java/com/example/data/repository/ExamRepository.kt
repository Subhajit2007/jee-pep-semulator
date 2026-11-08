package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ExamResult
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

class ExamRepository(private val db: FirebaseFirestore) {

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

    fun observeExamResults(): Flow<List<ExamResult>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/examResults"
        emitAll(
            db.collection("users").document(uid).collection("examResults")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ExamResult::class.java)
                        .sortedByDescending { it.createdAt }
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

    suspend fun saveExamResult(result: ExamResult): String {
        val uid = requireUserId()
        val docRef = if (result.id.isNotBlank()) {
            db.collection("users").document(uid).collection("examResults").document(result.id)
        } else {
            db.collection("users").document(uid).collection("examResults").document()
        }

        val payload = mutableMapOf<String, Any>(
            "userId" to uid,
            "examTitle" to result.examTitle,
            "totalQuestions" to result.totalQuestions,
            "attemptedCount" to result.attemptedCount,
            "timeLimitMinutes" to result.timeLimitMinutes,
            "timeTakenSeconds" to result.timeTakenSeconds,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (result.nvtRange.isNotBlank()) payload["nvtRange"] = result.nvtRange
        if (result.answersSummary.isNotBlank()) payload["answersSummary"] = result.answersSummary

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

    suspend fun deleteExamResult(resultId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("examResults").document(resultId)
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
