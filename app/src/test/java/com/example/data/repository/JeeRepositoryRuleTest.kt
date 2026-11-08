package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.ExamResult
import com.example.data.model.TrackerLog
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class JeeRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun trackerLog_authenticatedUser_canCreateAndObserve() = runBlocking {
        val uid = signInTestUser("student1@test.com")
        val repo = TrackerRepository(firestore)

        val uniqueDay = (1..100).random().toLong()
        val log = TrackerLog(
            id = "day_${uniqueDay}_${UUID.randomUUID()}",
            userId = uid,
            dayNumber = uniqueDay,
            date = "2026-11-08",
            totalStudyHours = 7.5,
            pyqsSolved = 50,
            physicsHours = 2.5,
            chemistryHours = 2.5,
            mathsHours = 2.5,
            examMarks = 180.0,
            notes = "Solved Mechanics & Thermodynamics"
        )

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addTrackerLog(log)
        }

        val logs = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeTrackerLogs().first { list -> list.any { it.dayNumber == uniqueDay } }
        }
        assertTrue(logs.any { it.dayNumber == uniqueDay })
    }

    @Test
    fun plannerProgress_authenticatedUser_canSaveAndObserve() = runBlocking {
        val uid = signInTestUser("student2@test.com")
        val repo = PlannerRepository(firestore)

        val topics = setOf("physics_day1_0", "chemistry_day1_1")
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveCompletedTopics(topics)
        }

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observePlannerProgress().first { it.containsAll(topics) }
        }
        assertEquals(topics, observed)
    }

    @Test
    fun examResult_authenticatedUser_canSaveResult() = runBlocking {
        val uid = signInTestUser("student3@test.com")
        val repo = ExamRepository(firestore)

        val result = ExamResult(
            id = "exam_${UUID.randomUUID()}",
            userId = uid,
            examTitle = "JEE Full Test 1",
            totalQuestions = 75,
            attemptedCount = 68,
            timeLimitMinutes = 180,
            timeTakenSeconds = 10200,
            nvtRange = "21-25, 46-50, 71-75"
        )

        val id = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveExamResult(result)
        }
        assertNotNull(id)
    }

    @Test
    fun trackerLog_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = TrackerRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeTrackerLogs().first()
            }
            fail("Expected unauthenticated query to fail")
        } catch (e: Exception) {
            val firestoreError = e.unwrapFirestoreException()
            // May fail either at repository requireUserId() or Firestore PERMISSION_DENIED
            assertTrue(
                e is IllegalStateException ||
                    firestoreError?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
        }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 5000L
    }
}
