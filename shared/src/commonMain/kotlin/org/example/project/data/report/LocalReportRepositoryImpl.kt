package org.example.project.data.report

import kotlinx.datetime.Clock

class LocalReportRepositoryImpl(
    private val localDataSource: LocalReportDataSource = LocalReportDataSource()
) : ReportRepository {

    override suspend fun saveReport(
        userId: String,
        userName: String,
        wineryName: String,
        content: String,
        imageUrl: String,
        rating: Int,
        location: Location?
    ) {
        val report = ReportModel(
            id = generateId(),
            userId = userId,
            userName = userName,
            wineryName = wineryName,
            content = content,
            imageUrl = imageUrl,
            rating = rating,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            location = location
        )

        localDataSource.upsert(report)
    }

    override suspend fun getReportsForUser(userId: String): List<ReportModel> {
        return localDataSource.getByUser(userId).map { it.toModel() }
    }

    override suspend fun getAllReports(): List<ReportModel> {
        val dbReports = localDataSource.getAll()
        val models = dbReports.map { it.toModel() }

        // Debug location data
        models.forEach { model ->
            if (model.location != null) {
                println("📍 Post: ${model.wineryName} - Location: ${model.location.lat}, ${model.location.lng}")
            }
        }

        return models
    }

    override suspend fun updateReport(
        reportId: String,
        userName: String?,
        wineryName: String?,
        content: String?,
        imageUrl: String?,
        rating: Int?,
        location: Location?
    ) {
        // Not implemented yet
    }

    override suspend fun deleteReport(reportId: String) {
        localDataSource.deleteById(reportId)
    }

    private fun generateId(): String {
        return "report_${Clock.System.now().toEpochMilliseconds()}_${(1000..9999).random()}"
    }
}