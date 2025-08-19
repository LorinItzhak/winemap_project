package org.example.project.ui.report


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.R
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.rememberAsyncImagePainter
import org.example.project.CloudinaryUploader
import org.example.project.data.report.Location

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
fun NewReportScreen(
    pickedLocation: Location?,
    onImagePicked: (Uri) -> Unit = {},
    onPublish: (
        userName: String,
        wineryName: String,
        content: String,
        rating: Int,
        imageUrl: String,
        location: Location?
    ) -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }

    // Error message
    var errorText by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            onImagePicked(it)
            errorText = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraUri?.let {
                selectedImageUri = it
                onImagePicked(it)
                errorText = null
            }
        }
    }

    fun createImageUri(): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Couldn't create URI for camera image")
    }

    // Form fields
    var userName by remember { mutableStateOf("") }
    var wineryName by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }

    var showPicker by remember { mutableStateOf(false) }
    var currentPickedLocation by remember(pickedLocation) { mutableStateOf(pickedLocation) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "New Wine Review",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = balooBhaijaan2Family,
                    fontWeight = FontWeight.Bold,
                    color = WineColor
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Photo picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(2.dp, WineColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                selectedImageUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } ?: run {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Add wine photo",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Add photo button
                SmallFloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    containerColor = WineColor,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add photo")
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Select photo source") },
                    text = { Text("Take a new photo or select from gallery?") },
                    confirmButton = {
                        TextButton(onClick = {
                            galleryLauncher.launch("image/*")
                            showDialog = false
                        }) { Text("Gallery") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            cameraUri = createImageUri()
                            cameraLauncher.launch(cameraUri!!)
                            showDialog = false
                        }) { Text("Camera") }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Your name field
            OutlinedTextField(
                value = userName,
                onValueChange = {
                    userName = it
                    if (it.isNotBlank()) errorText = null
                },
                label = { Text("Your name") },
                placeholder = { Text("Enter your name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WineColor,
                    focusedLabelColor = WineColor
                )
            )

            Spacer(Modifier.height(8.dp))

            // Winery name field
            OutlinedTextField(
                value = wineryName,
                onValueChange = {
                    wineryName = it
                    if (it.isNotBlank()) errorText = null
                },
                label = { Text("Winery name") },
                placeholder = { Text("Name of the winery") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WineColor,
                    focusedLabelColor = WineColor
                )
            )

            Spacer(Modifier.height(8.dp))

            // Review content field
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    if (it.isNotBlank()) errorText = null
                },
                label = { Text("Your review") },
                placeholder = { Text("Share your wine tasting experience...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                singleLine = false,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WineColor,
                    focusedLabelColor = WineColor
                )
            )

            Spacer(Modifier.height(16.dp))

            // Rating section
            Text(
                text = "Rate this wine",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = balooBhaijaan2Family,
                    fontWeight = FontWeight.Bold,
                    color = WineColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Rating stars
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(5) { index ->
                    val starIndex = index + 1
                    IconButton(
                        onClick = {
                            rating = starIndex
                            errorText = null
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (starIndex <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Rate $starIndex stars",
                            tint = if (starIndex <= rating) StarColor else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            if (rating > 0) {
                Text(
                    text = "$rating/5 stars",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))

            // Location button
            OutlinedButton(
                onClick = { showPicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(2.dp, WineColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = 0.3f),
                    contentColor = WineColor
                )
            ) {
                Text(
                    if (currentPickedLocation == null) "📍 Add winery location" else "📍 Change location",
                    fontFamily = balooBhaijaan2Family,
                    fontWeight = FontWeight.Bold
                )
            }

            currentPickedLocation?.let { location ->
                Text(
                    "📍 ${location.name}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = WineColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            if (showPicker) {
                MapPickerDialog(
                    onDismiss = { showPicker = false },
                    onPicked = { lat, lng ->
                        // Create Location object with geocoded address
                        currentPickedLocation = Location(
                            lat = lat,
                            lng = lng,
                            name = "Selected location" // You can geocode this later
                        )
                        errorText = null
                        showPicker = false
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            // Error message
            if (errorText != null) {
                Text(
                    errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            // Publish button
            Button(
                onClick = {
                    // Validation
                    val missing = userName.isBlank() ||
                            wineryName.isBlank() ||
                            content.isBlank() ||
                            rating == 0 ||
                            selectedImageUri == null

                    if (missing) {
                        errorText = "Please fill all fields, add a photo, and rate the wine."
                        return@Button
                    }

                    selectedImageUri?.let { uri ->
                        uploading = true
                        errorText = null
                        CloudinaryUploader.upload(context, uri) { url ->
                            uploading = false
                            if (url == null) {
                                errorText = "Image upload failed. Please try again."
                            } else {
                                println("🎯 About to publish:")
                                println("🎯 currentPickedLocation: $currentPickedLocation")
                                onPublish(
                                    userName,
                                    wineryName,
                                    content,
                                    rating,
                                    url,
                                    currentPickedLocation
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WineColor,
                    contentColor = Color.White
                ),
                enabled = !uploading
            ) {
                if (uploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        "Publish Wine Review",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = balooBhaijaan2Family
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}