package com.example.data.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.model.ExamQuestion
import com.example.data.model.TrackerLog
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun exportTrackerLogs(context: Context, logs: List<TrackerLog>): Result<String> {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "JEE_100Day_Tracker_$timestamp.csv"

            val header = "Day Number,Date,Total Study Hours,PYQs Solved,Physics Hours,Chemistry Hours,Maths Hours,Exam Marks,Notes\n"
            val rows = StringBuilder(header)
            logs.sortedBy { it.dayNumber }.forEach { log ->
                val safeNotes = "\"" + log.notes.replace("\"", "\"\"") + "\""
                rows.append("${log.dayNumber},${log.date},${log.totalStudyHours},${log.pyqsSolved},${log.physicsHours},${log.chemistryHours},${log.mathsHours},${log.examMarks},$safeNotes\n")
            }

            val savedLocation = writeCsvToDownloads(context, filename, rows.toString())
            Result.success(savedLocation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportOmrAnalysis(
        context: Context,
        examTitle: String,
        questions: List<ExamQuestion>,
        timeTakenSeconds: Long
    ): Result<String> {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val safeTitle = examTitle.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val filename = "OMR_Analysis_${safeTitle}_$timestamp.csv"

            val summaryHeader = "# Exam: $examTitle\n# Time Taken: ${timeTakenSeconds / 60}m ${timeTakenSeconds % 60}s\n# Total Questions: ${questions.size}\n# Attempted: ${questions.count { it.isAttempted }}\n\n"
            val tableHeader = "Question No,Subject,Type,Chapter,Marked Answer,Status,Review Flag\n"
            val sb = StringBuilder(summaryHeader).append(tableHeader)

            questions.forEach { q ->
                val status = if (q.isAttempted) "Attempted" else "Unattempted"
                val review = if (q.isMarkedForReview) "Marked" else "Normal"
                val ans = if (q.recordedAnswer.contains(",")) "\"${q.recordedAnswer}\"" else q.recordedAnswer
                sb.append("${q.questionNumber},${q.subject.displayName},${q.type.name},${q.chapter},$ans,$status,$review\n")
            }

            val savedLocation = writeCsvToDownloads(context, filename, sb.toString())
            Result.success(savedLocation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun writeCsvToDownloads(context: Context, filename: String, content: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IllegalStateException("Could not create MediaStore entry for Downloads")
            resolver.openOutputStream(uri)?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
                stream.flush()
            }
            return "Downloads/$filename"
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, filename)
            FileOutputStream(file).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
                fos.flush()
            }
            return file.absolutePath
        }
    }
}
