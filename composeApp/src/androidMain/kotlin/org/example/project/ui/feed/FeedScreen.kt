package org.example.project.ui.feed

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.project.data.report.ReportModel
import org.example.project.location.getLocation

@Composable
fun MapView(
    reports: List<ReportModel>,
    onReportClicked: (ReportModel) -> Unit
) {
    val context = LocalContext.current

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

    val cameraState = rememberCameraPositionState()
    LaunchedEffect(hasLocationPermission.value) {
        if (hasLocationPermission.value) {
            runCatching { withContext(Dispatchers.IO) { getLocation() } }
                .onSuccess { loc ->
                    val here = LatLng(loc.latitude, loc.longitude)
                    cameraState.move(CameraUpdateFactory.newLatLngZoom(here, 10f))
                }
        }
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, bottom = 80.dp),
        cameraPositionState = cameraState,
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission.value),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = hasLocationPermission.value,
            zoomControlsEnabled = true,
        )
    ) {
        reports.forEach { rpt ->
            val lat = rpt.lat
            val lng = rpt.lng
            if (lat != null && lng != null) {
                val pos = LatLng(lat, lng)
                Marker(
                    state = MarkerState(position = pos),
                    title = if (rpt.name.isNotBlank()) rpt.name else "Unnamed Winery",
                    snippet = rpt.description.take(60),
                    onClick = {
                        onReportClicked(rpt)
                        true
                    }
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
            .background(Color(0xFFDCC8B6)),
        contentAlignment = Alignment.Center
    ) {
        MapView(
            reports = reports,
            onReportClicked = onReportClicked
        )
        SmallFloatingActionButton(
            onClick = onPublishClicked,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp, top = 16.dp),
            containerColor = Color(0xFF6B5B73),
            contentColor = Color.White,
        ) {
            Icon(Icons.Default.Add, contentDescription = "New winery")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    FeedScreen(
        reports = emptyList(),
        onReportClicked = {},
        onPublishClicked = {}
    )
}
