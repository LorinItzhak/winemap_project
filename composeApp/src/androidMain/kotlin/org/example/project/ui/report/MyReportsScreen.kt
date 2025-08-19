// MyReportsScreen.kt
package org.example.project.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
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

private val WineColor = Color(0xFF8B0000)
private val LightWineColor = Color(0xFFA52A2A)
private val StarColor = Color(0xFFFFD700)
private val BgGray = Color(0xFFF0F0F0)

@Composable
fun MyReportsScreen(
    reports: List<ReportModel>,
    onPublishClicked: () -> Unit,
    onItemClick: (ReportModel) -> Unit = {},
    isLoading: Boolean = false
) {
    val sortedReports = reports.sortedByDescending { it.createdAt }

    Box(
        Modifier
            .fillMaxSize()
            .background(BgGray)
    ) {
        if (sortedReports.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "No wine reviews yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Gray
                    )
                    Text(
                        "Start by adding your first wine review!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                items(sortedReports, key = { it.id }) { rpt ->
                    ReportItem(rpt = rpt, onClick = { onItemClick(rpt) })
                }
            }
        }

        FloatingActionButton(
            onClick = onPublishClicked,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            containerColor = WineColor,
            contentColor = Color.White,
        ) {
            Icon(Icons.Default.Add, contentDescription = "New wine review")
        }
    }
}

@Composable
private fun ReportItem(
    rpt: ReportModel,
    onClick: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Image section
            if (rpt.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = rpt.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No image",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Winery name
                Text(
                    text = rpt.wineryName.ifBlank { "Unknown Winery" },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = balooBhaijaan2Family,
                        fontWeight = FontWeight.Bold,
                        color = WineColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                            tint = if (index < rpt.rating) StarColor else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${rpt.rating}/5",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    )
                }

                // Review content
                if (rpt.content.isNotBlank()) {
                    Text(
                        text = rpt.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = balooBhaijaan2Family,
                            fontWeight = FontWeight.Normal,
                            color = Color.DarkGray
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom row with location and date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location
                    Text(
                        text = rpt.location?.name?.take(30) ?: "No location",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = balooBhaijaan2Family,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    // Date
                    Text(
                        text = formatDate(rpt.createdAt),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = balooBhaijaan2Family,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray
                        )
                    )
                }

                // Reviewer name
                if (rpt.userName.isNotBlank()) {
                    Text(
                        text = "by ${rpt.userName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = balooBhaijaan2Family,
                            fontWeight = FontWeight.Medium,
                            color = WineColor,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Unknown date"
    }
}