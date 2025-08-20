package org.example.project.ui.report

import android.content.Context
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import coil3.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.example.project.R
import org.example.project.data.report.ReportModel
import org.example.project.data.report.Location
import java.util.Locale
import kotlin.coroutines.resumeWithException
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.ui.profile.ProfileScreen


private val balooBhaijaan2Family = FontFamily(
    Font(R.font.baloobhaijaan2_regular,   FontWeight.Normal),
    Font(R.font.baloobhaijaan2_medium,    FontWeight.Medium),
    Font(R.font.baloobhaijaan2_semibold,  FontWeight.SemiBold),
    Font(R.font.baloobhaijaan2_bold,      FontWeight.Bold),
    Font(R.font.baloobhaijaan2_extrabold, FontWeight.ExtraBold)
)

private val BgGray      = Color(0xFFF0F0F0)
private val WineColor   = Color(0xFF8B0000)

private val LabelGray   = Color(0xFF8D8D8D)
private val StarColor   = Color(0xFFFFD700)

@Composable
fun EditReportScreen(
    report: ReportModel,
    onSave: (userName: String, wineryName: String, content: String, rating: Int, location: Location?, imageUrl: String?) -> Unit
) {

    var localImageUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) localImageUri = uri }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Text fields
    var userName by remember { mutableStateOf(report.userName) }
    var wineryName by remember { mutableStateOf(report.wineryName) }
    var content by remember { mutableStateOf(report.content) }
    var rating by remember { mutableStateOf(report.rating) }

    // Location (pre-filled from report; stays as-is unless user changes it)
    var draftLocation by remember { mutableStateOf(report.location) }
    var showPicker by remember { mutableStateOf(false) }
    var locationErr by remember { mutableStateOf<String?>(null) }

    val scroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = localImageUri ?: report.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )

                SmallFloatingActionButton(
                    onClick = {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    containerColor = Color(0xFF8B0000),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Change photo")
                }

                if (localImageUri != null) {
                    SmallFloatingActionButton(
                        onClick = { localImageUri = null },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        containerColor = Color(0xFF90D1D8),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Revert to original")
                    }
                }
            }

            // User name field
            LabeledEditor("Your name", userName) { userName = it }

            // Winery name field
            LabeledEditor("Winery name", wineryName) { wineryName = it }

            // Content/Review field
            LabeledEditor("Your review", content, maxLines = 4) { content = it }

            // Rating section
            Text(
                text = "Rating",
                style = MaterialTheme.typography.titleMedium,
                color = LabelGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            RatingBar(
                rating = rating,
                onRatingChange = { rating = it }
            )

            // Location section
            LocationEditor(
                location = draftLocation,
                onChangeClick = {
                    showPicker = true
                    locationErr = null
                }
            )

            locationErr?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(Modifier.height(96.dp))
        }

        // Save button
        Button(
            onClick = {
                var finalUrl: String? = null
                if (localImageUri != null) {
                    scope.launch {
                        try {
                            finalUrl = uploadToCloudinary(context, localImageUri!!)
                            onSave(userName, wineryName, content, rating, draftLocation, finalUrl)
                        } catch (_: Throwable) {
                            onSave(userName, wineryName, content, rating, draftLocation, report.imageUrl)
                        }
                    }
                } else {
                    onSave(userName, wineryName, content, rating, draftLocation, null)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WineColor,
                contentColor = Color.White
            )
        ) {
            Text(
                "Save changes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Map picker dialog
        if (showPicker) {
            MapPickerDialog(
                onDismiss = { showPicker = false },
                onPicked = { lat, lng ->
                    // Create new Location object
                    scope.launch {
                        val address = try {
                            withContext(Dispatchers.IO) {
                                val list = Geocoder(context, Locale.getDefault()).getFromLocation(lat, lng, 1)
                                list?.firstOrNull()?.getAddressLine(0) ?: "Unknown location"
                            }
                        } catch (_: Exception) {
                            "Unknown location"
                        }
                        draftLocation = Location(lat = lat, lng = lng, name = address)
                    }
                    showPicker = false
                }
            )
        }
    }
}

@Composable
private fun RatingBar(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    maxRating: Int = 5
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        repeat(maxRating) { index ->
            val starIndex = index + 1
            IconButton(
                onClick = { onRatingChange(starIndex) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (starIndex <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Rate $starIndex stars",
                    tint = if (starIndex <= rating) StarColor else LabelGray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

suspend fun uploadToCloudinary(ctx: Context, uri: Uri): String =
    withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { cont ->
            MediaManager.get().upload(uri)
                .option("resource_type", "image")
                .callback(object : com.cloudinary.android.callback.UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, total: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                        val url = (resultData["secure_url"] ?: resultData["url"])?.toString()
                        if (url != null) cont.resume(url) {} else cont.resumeWithException(IllegalStateException("No URL"))
                    }
                    override fun onError(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
                        cont.resumeWithException(RuntimeException(error?.description ?: "Upload failed"))
                    }
                    override fun onReschedule(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
                        cont.resumeWithException(RuntimeException(error?.description ?: "Upload rescheduled"))
                    }
                })
                .dispatch(ctx)
        }
    }

@Composable
private fun LabeledEditor(
    label: String,
    value: String,
    maxLines: Int = 1,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        label = {
            Text(
                text = label,
                color = LabelGray,
            )
        },
        maxLines = maxLines,
        shape = RoundedCornerShape(8.dp),
    )
}

@Composable
private fun LocationEditor(
    location: Location?,
    onChangeClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = location?.name ?: "No location selected",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        OutlinedButton(
            onClick = onChangeClick,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, WineColor),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White.copy(alpha = 0.3f),
                contentColor = WineColor
            )
        ) {
            Text(
                if (location == null) "Pick location" else "Change location",
                fontFamily = balooBhaijaan2Family,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditReportScreenPreview() {
  // sample ReportModel so the screen can render
  val sampleReport = ReportModel(
    id = "1",
    userId = "user1",
    userName = "John Doe",
    wineryName = "Best Winery",
    content = "Great wine with fruity notes",
    rating = 4,
    imageUrl = "https://picsum.photos/400",
    location = Location(32.06, 34.79, "Tel Aviv, Israel")
  )

  EditReportScreen(
    report = sampleReport,
    onSave = { _, _, _, _, _, _ -> }
  )
}
