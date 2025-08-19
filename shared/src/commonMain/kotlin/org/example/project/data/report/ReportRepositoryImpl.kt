package org.example.project.data.report


import org.example.project.data.firebase.FirebaseRepository
import org.example.project.data.firebase.RemoteFirebaseRepository

class ReportRepositoryImpl(
    private val firebase: FirebaseRepository
) : ReportRepository {
    constructor() : this(RemoteFirebaseRepository())

    override suspend fun saveReport(
        userId: String,
        userName: String,
        wineryName: String,
        content: String,
        imageUrl: String,
        rating: Int,
        location: Location?
    ) {
        firebase.saveReport(userId, userName, wineryName, content, imageUrl, rating, location)
    }

    override suspend fun getReportsForUser(userId: String): List<ReportModel> =
        firebase.getReportsForUser(userId)

    override suspend fun getAllReports(): List<ReportModel> =
        firebase.getAllReports()

    override suspend fun updateReport(
        reportId: String,
        userName: String?,
        wineryName: String?,
        content: String?,
        imageUrl: String?,
        rating: Int?,
        location: Location?
    ) = firebase.updateReport(reportId, userName, wineryName, content, imageUrl, rating, location)

    override suspend fun deleteReport(reportId: String) =
        firebase.deleteReport(reportId)
}
