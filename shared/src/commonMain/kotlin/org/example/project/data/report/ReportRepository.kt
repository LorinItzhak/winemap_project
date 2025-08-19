package org.example.project.data.report
interface ReportRepository {
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