package org.example.project.ui.feed

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.project.R
import org.example.project.data.report.ReportModel
import org.example.project.location.getLocation

private val balooBhaijaan2Family = FontFamily(
    Font(R.font.baloobhaijaan2_regular,   FontWeight.Normal),
    Font(R.font.baloobhaijaan2_medium,    FontWeight.Medium),
    Font(R.font.baloobhaijaan2_semibold,  FontWeight.SemiBold),
    Font(R.font.baloobhaijaan2_bold,      FontWeight.Bold),
    Font(R.font.baloobhaijaan2_extrabold, FontWeight.ExtraBold)
)

private val WineColor = Color(0xFF8B0000)
private val LightWineColor = Color(0xFFA52A2A)
private val StarColor = Color(0xFFFFD700)
private val BgGray = Color(0xFFF0F0F0)

@Composable
fun MapView(
    reports: List<ReportModel>,
    onReportClicked: (ReportModel) -> Unit
) {
    LaunchedEffect(reports) {
        println("🗺️ MapView: Got ${reports.size} reports")
        reports.forEachIndexed { index, report ->
            println("🗺️ Report $index: ${report.wineryName}")
            println("🗺️   Location: ${report.location}")
        }
    }
    val context = LocalContext.current

    // Permission state + launcher
    val hasLocationPermission = remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasLocationPermission.value =
            (results[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                    (results[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
    }

    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        hasLocationPermission.value = fine || coarse
        if (!hasLocationPermission.value) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    val cameraState = rememberCameraPositionState {
        // אתחול ממרכז ישראל
        position = CameraPosition.fromLatLngZoom(LatLng(31.4, 34.8), 20.5f)
    }
    // Fetch location once we have permission
    LaunchedEffect(hasLocationPermission.value) {
        if (hasLocationPermission.value) {
            runCatching { withContext(Dispatchers.IO) { getLocation() } }
                .onSuccess { loc ->
                    val here = LatLng(loc.latitude, loc.longitude)
                    cameraState.move(CameraUpdateFactory.newLatLngZoom(here, 16f))
                }
        }
    }
    val markerPoints = remember(reports) {
        reports.mapNotNull { rpt ->
            val loc = rpt.location
            if (loc != null && !loc.lat.isNaN() && !loc.lng.isNaN()) LatLng(loc.lat, loc.lng) else null
        }
    }

// אם יש סיכות – נתאים את המצלמה לגבולות שלהן
    LaunchedEffect(markerPoints) {
        if (markerPoints.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            markerPoints.forEach { builder.include(it) }
            val bounds = builder.build()
            cameraState.move(CameraUpdateFactory.newLatLngBounds(bounds, 80))
        }
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, bottom = 80.dp),
        cameraPositionState = cameraState,
        properties = MapProperties(
            isMyLocationEnabled = hasLocationPermission.value
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = hasLocationPermission.value,
            zoomControlsEnabled = true,
        )
    ) {
        // Wine review markers
        reports.forEach { rpt ->
            val location = rpt.location
            if (location != null && !location.lat.isNaN() && !location.lng.isNaN()) {
                println("🗺️ ✅ Creating marker for ${rpt.wineryName} at: ${location.lat}, ${location.lng}")

                MarkerInfoWindow(
                    state = MarkerState(position = LatLng(location.lat, location.lng)),
                    onClick = { onReportClicked(rpt); true }
                ) {
                    WineMarkerContent(report = rpt)
                }
            } else {
                println("🗺️ ❌ Skipping ${rpt.wineryName} - invalid location: $location")
            }
        }
    }
}

@Composable
fun WineMarkerContent(report: ReportModel) {
    Card(
        modifier = Modifier
            .widthIn(max = 250.dp)
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Winery name
            Text(
                text = report.wineryName.ifBlank { "Unknown Winery" },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = balooBhaijaan2Family,
                    fontWeight = FontWeight.Bold,
                    color = WineColor
                ),
                maxLines = 1
            )

            // Rating stars
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (index < report.rating) StarColor else Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${report.rating}/5",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = balooBhaijaan2Family,
                        color = Color.Gray
                    )
                )
            }

            // Review preview
            if (report.content.isNotBlank()) {
                Text(
                    text = report.content.take(80) + if (report.content.length > 80) "..." else "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = balooBhaijaan2Family,
                        color = Color.DarkGray
                    ),
                    maxLines = 2
                )
            }

            // Reviewer
            if (report.userName.isNotBlank()) {
                Text(
                    text = "by ${report.userName}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = balooBhaijaan2Family,
                        fontWeight = FontWeight.Medium,
                        color = WineColor,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun FeedScreen(
    reports: List<ReportModel>,
    onReportClicked: (ReportModel) -> Unit,
    onPublishClicked: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray),
        contentAlignment = Alignment.Center
    ) {
        MapView(
            reports = reports,
            onReportClicked = onReportClicked
        )

        // Floating Action Button
        FloatingActionButton(
            onClick = onPublishClicked,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = WineColor,
            contentColor = Color.White,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New wine review")
                Text(
                    "Review",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = balooBhaijaan2Family,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}