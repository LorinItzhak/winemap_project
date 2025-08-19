package org.example.project.data.report

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReportViewModel(
    private val repo: ReportRepository = ReportRepositoryImpl(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    @Suppress("unused")
    constructor() : this(
        ReportRepositoryImpl(),
        CoroutineScope(Dispatchers.Default + SupervisorJob())
    )

    fun saveReport(
        userId: String,
        userName: String,
        wineryName: String,
        content: String,
        imageUrl: String,
        rating: Int,
        location: Location? = null
    ) {
        scope.launch {
            _uiState.value = ReportUiState.Saving
            try {
                repo.saveReport(userId, userName, wineryName, content, imageUrl, rating, location)
                _uiState.value = ReportUiState.SaveSuccess
            } catch (e: Throwable) {
                _uiState.value = ReportUiState.SaveError(e)
            }
        }
    }

    fun loadReportsForUser(userId: String) {
        scope.launch {
            _uiState.value = ReportUiState.LoadingReports
            try {
                val list = repo.getReportsForUser(userId)
                _uiState.value = ReportUiState.ReportsLoaded(list)
            } catch (e: Throwable) {
                _uiState.value = ReportUiState.LoadError(e)
            }
        }
    }

    fun loadAllReports() {
        scope.launch {
            _uiState.value = ReportUiState.LoadingReports
            try {
                val list = repo.getAllReports()
                _uiState.value = ReportUiState.ReportsLoaded(list)
            } catch (e: Throwable) {
                _uiState.value = ReportUiState.LoadError(e)
            }
        }
    }

    fun updateReport(
        reportId: String,
        userName: String? = null,
        wineryName: String? = null,
        content: String? = null,
        imageUrl: String? = null,
        rating: Int? = null,
        location: Location? = null
    ) {
        scope.launch {
            _uiState.value = ReportUiState.Saving
            try {
                repo.updateReport(reportId, userName, wineryName, content, imageUrl, rating, location)
                _uiState.value = ReportUiState.UpdateSuccess
            } catch (e: Throwable) {
                _uiState.value = ReportUiState.UpdateError(e)
            }
        }
    }

    fun deleteReport(reportId: String) {
        scope.launch {
            _uiState.value = ReportUiState.Saving
            try {
                repo.deleteReport(reportId)
                _uiState.value = ReportUiState.DeleteSuccess
            } catch (e: Throwable) {
                _uiState.value = ReportUiState.DeleteError(e)
            }
        }
    }
}