package org.example.project.data.firebase

import org.example.project.data.report.ReportModel
import org.example.project.data.report.Location

interface FirebaseRepository {
    // --- Authentication ---
    suspend fun signUp(email: String, password: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
    fun currentUserUid(): String?
    fun currentUserEmail(): String?
    suspend fun updatePassword(newPassword: String)
    suspend fun saveUserProfile(uid: String, email: String)

    // --- Reports (Posts) ---
    suspend fun saveReport(
        userId: String,
        userName: String,
        wineryName: String,
        content: String,
        imageUrl: String,
        rating: Int,
        location: Location? = null
    )

    suspend fun getReportsForUser(userId: String): List<ReportModel>
    suspend fun getAllReports(): List<ReportModel>

    suspend fun updateReport(
        reportId: String,
        userName: String? = null,
        wineryName: String? = null,
        content: String? = null,
        imageUrl: String? = null,
        rating: Int? = null,
        location: Location? = null
    )

    suspend fun deleteReport(reportId: String)
}