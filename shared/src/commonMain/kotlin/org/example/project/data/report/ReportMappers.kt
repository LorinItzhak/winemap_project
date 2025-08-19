package org.example.project.data.report

fun Reports.toModel() = ReportModel(
    id = id,
    userId = userId,
    userName = userName,
    wineryName = wineryName,
    content = content,
    imageUrl = imageUrl,
    rating = rating.toInt(),
    createdAt = createdAt,
    location = if (locationName != null && locationLat != null && locationLng != null) {
        Location(
            lat = locationLat,
            lng = locationLng,
            name = locationName
        )
    } else null
)