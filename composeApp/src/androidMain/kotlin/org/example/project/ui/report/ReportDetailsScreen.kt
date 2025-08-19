package org.example.project.ui.report

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.project.R
import org.example.project.data.report.ReportModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val balooBhaijaan2Family = FontFamily(
    Font(R.font.baloobhaijaan2_regular,   FontWeight.Normal),
    Font(R.font.baloobhaijaan2_medium,    FontWeight.Medium),
    Font(R.font.baloobhaijaan2_semibold,  FontWeight.SemiBold),
    Font(R.font.baloobhaijaan2_bold,      FontWeight.Bold),
    Font(R.font.baloobhaijaan2_extrabold, FontWeight.ExtraBold)
)

private val BgGray = Color(0xFFF3F3F3)
private val WineColor = Color(0xFF8B0000)
private val LightWineColor = Color(0xFFA52A2A)
private val StarColor = Color(0xFFFFD700)
private val LabelGray = Color(0xFF8D8D8D)
private val CardStroke = Color(0xFFD6D6D6)

@Composable
fun ReportDetailsScreen(
    report: ReportModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        val scroll = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Wine photo
            AsyncImage(
                model = report.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // Winery name (main title)
            Text(
                text = report.wineryName.ifBlank { "Unknown Winery" },
                fontFamily = balooBhaijaan2Family,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = WineColor
            )

            // Rating section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Rating",
                        fontFamily = balooBhaijaan2Family,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = WineColor
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (index < report.rating) StarColor else Color.LightGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${report.rating}/5 stars",
                            fontFamily = balooBhaijaan2Family,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Review content
            if (report.content.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Review",
                            fontFamily = balooBhaijaan2Family,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = WineColor
                        )

                        Text(
                            text = report.content,
                            fontFamily = balooBhaijaan2Family,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            // Reviewer and date info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (report.userName.isNotBlank()) {
                        InlineLabel("Reviewed by:", report.userName)
                    }

                    InlineLabel("Date:", formatDate(report.createdAt))
                }
            }

            // Location section
            val location = report.location
            if (location != null && !location.lat.isNaN() && !location.lng.isNaN()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Winery Location",
                            fontFamily = balooBhaijaan2Family,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = WineColor
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Filled.Place,
                                contentDescription = null,
                                tint = WineColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = location.name.ifBlank {
                                    String.format(Locale.getDefault(), "%.5f, %.5f", location.lat, location.lng)
                                },
                                fontFamily = balooBhaijaan2Family,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }

                        // Map
                        val spot = LatLng(location.lat, location.lng)
                        val cameraState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(spot, 16f)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            GoogleMap(
                                cameraPositionState = cameraState,
                                properties = MapProperties(isMyLocationEnabled = false),
                                uiSettings = MapUiSettings(
                                    myLocationButtonEnabled = false,
                                    zoomControlsEnabled = true
                                )
                            ) {
                                Marker(
                                    state = MarkerState(position = spot),
                                    title = report.wineryName.ifBlank { "Winery" }
                                )
                            }
                        }

                        // Open in Maps button
                        val context = LocalContext.current
                        TextButton(
                            onClick = {
                                val name = Uri.encode(report.wineryName.ifBlank { "Winery" })
                                val gmm = Uri.parse("geo:${location.lat},${location.lng}?q=${location.lat},${location.lng}($name)")
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, gmm)
                                            .setPackage("com.google.android.apps.maps")
                                    )
                                }.onFailure {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, gmm))
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = WineColor)
                        ) {
                            Text(
                                "Open in Google Maps",
                                fontFamily = balooBhaijaan2Family,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                // No location placeholder
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No location provided",
                            color = LabelGray,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = balooBhaijaan2Family
                        )
                    }
                }
            }

            Spacer(Modifier.height(120.dp))
        }

        // Bottom action buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WineColor,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Edit Review",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = balooBhaijaan2Family
                    )
                )
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    "Delete Review",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = balooBhaijaan2Family
                    )
                )
            }
        }
    }
}

@Composable
fun InlineLabel(label: String, value: String, modifier: Modifier = Modifier) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontFamily = balooBhaijaan2Family, fontWeight = FontWeight.Bold, color = WineColor)) {
                append(label)
                if (!label.endsWith(" ")) append(" ")
            }
            withStyle(SpanStyle(fontFamily = balooBhaijaan2Family, fontWeight = FontWeight.Medium)) {
                append(value)
            }
        },
        style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = balooBhaijaan2Family,
            fontSize = 16.sp
        ),
        modifier = modifier
    )
}

private fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Unknown date"
    }
}