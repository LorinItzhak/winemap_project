package org.example.project.data.report

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val lat: Double = Double.NaN,
    val lng: Double = Double.NaN,
    val name: String = ""
)

@Serializable
data class ReportModel(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val wineryName: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val rating: Int = 0, // 1-5 stars
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val location: Location? = null
)