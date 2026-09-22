package com.techmania.myrent.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.techmania.myrent.ui.theme.MyRentTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    onBackClick: () -> Unit,
    propertyName: String,
    propertyViewModel: PropertyViewModel = viewModel()
) {
    val property = propertyViewModel.properties.find { it.name == propertyName }
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }

    var showRatingDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            PropertyDetailBottomBar(
                onScheduleClick = { showScheduleDialog = true },
                onRequestToRentClick = {
                    if (property != null) {
                        propertyViewModel.requestToRent(property)
                        Toast.makeText(context, "Rent request sent for ${property.name}", Toast.LENGTH_LONG).show()
                        onBackClick() // Go back after request
                    }
                },
                isRented = property?.badge == "Rented"
            )
        },
        containerColor = Color(0xFFF8F9FB)
    ) { innerPadding ->
        if (property == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Property not found")
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 800.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Image Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .background(property.imageBg)
                    ) {
                        if (property.mediaUris.isNotEmpty()) {
                            val pagerState = rememberPagerState(pageCount = { property.mediaUris.size })
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                AsyncImage(
                                    model = property.mediaUris[page],
                                    contentDescription = null,
                                    imageLoader = imageLoader,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            if (property.mediaUris.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(property.mediaUris.size) { index ->
                                        val isSelected = pagerState.currentPage == index
                                        Box(
                                            modifier = Modifier
                                                .size(if (isSelected) 8.dp else 6.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.5f))
                                        )
                                    }
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(120.dp)
                                    .align(Alignment.Center),
                                tint = Color.White.copy(alpha = 0.3f)
                            )
                        }

                        // Top Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White, CircleShape)
                            ) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back", tint = Color.Black)
                            }

                            IconButton(
                                onClick = { propertyViewModel.toggleLike(property) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (property.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = Color.Red
                                )
                            }
                        }
                    }

                    // Property Info Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = property.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = property.price,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4B4EFC)
                                )
                                Text(
                                    text = "/mo",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF4B4EFC), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${property.areaName}, ${property.city}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF4B4EFC),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            // Interactive Rating Chip
                            Surface(
                                onClick = { showRatingDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFF9E6),
                                border = BorderStroke(1.dp, Color(0xFFFFCC00).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFCC00), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(property.rating, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(" (${property.reviews})", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PropertySpecCard(Modifier.weight(1f), property.bhk, "Type")
                            PropertySpecCard(Modifier.weight(1f), property.size, "Area")
                            PropertySpecCard(Modifier.weight(1f), "4th", "Floor")
                            PropertySpecCard(Modifier.weight(1f), property.furnishing.split(" ").first(), "Furnish")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            property.amenities.forEach { amenity ->
                                val icon = when (amenity) {
                                    "Parking" -> Icons.Default.LocalParking
                                    "AC" -> Icons.Default.AcUnit
                                    "WiFi" -> Icons.Default.Wifi
                                    "Lift" -> Icons.Default.Elevator
                                    "Security" -> Icons.Default.Lock
                                    "Gym" -> Icons.Default.FitnessCenter
                                    "Pool" -> Icons.Default.Pool
                                    "Power backup" -> Icons.Default.BatteryChargingFull
                                    else -> Icons.Default.Check
                                }
                                AmenityChip(icon, amenity)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = if (property.description.isNotEmpty()) property.description else "This property is a ${property.furnishing} ${property.type} located in ${property.areaName}. It offers various amenities like ${property.amenities.joinToString(", ")}. Perfect for someone looking for a comfortable living space in ${property.city}.",
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = Color.Black.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        OwnerSection()
                        
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }

    if (showRatingDialog && property != null) {
        RatingDialog(
            currentRating = property.numericRating,
            onDismiss = { showRatingDialog = false },
            onRatingSubmit = { rating ->
                propertyViewModel.rateProperty(property.id, rating)
                showRatingDialog = false
            }
        )
    }

    if (showScheduleDialog && property != null) {
        ScheduleVisitDialog(
            propertyName = property.name,
            onDismiss = { showScheduleDialog = false },
            onConfirm = { date, time ->
                propertyViewModel.scheduleVisit(property, date, time)
                Toast.makeText(context, "Visit request sent for $date at $time", Toast.LENGTH_LONG).show()
                showScheduleDialog = false
                onBackClick()
            }
        )
    }
}


@Composable
fun RatingDialog(
    currentRating: Float,
    onDismiss: () -> Unit,
    onRatingSubmit: (Float) -> Unit
) {
    var rating by remember { mutableFloatStateOf(0f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate this Property", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("How was your experience?", fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        val starIndex = index + 1
                        val isSelected = rating >= starIndex
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFFFFCC00) else Color.Gray,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { rating = starIndex.toFloat() }
                        )
                    }
                }
                if (rating > 0) {
                    Text(
                        text = when(rating.toInt()) {
                            1 -> "Poor"
                            2 -> "Fair"
                            3 -> "Good"
                            4 -> "Very Good"
                            5 -> "Excellent"
                            else -> ""
                        },
                        modifier = Modifier.padding(top = 8.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4B4EFC)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onRatingSubmit(rating) },
                enabled = rating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E))
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun PropertySpecCard(modifier: Modifier = Modifier, value: String, label: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AutoResizingText(
                text = value,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            AutoResizingText(
                text = label,
                style = TextStyle(fontSize = 12.sp),
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AmenityChip(icon: ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontSize = 13.sp, color = Color.Black.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun OwnerSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF6C63FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("RK", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Rajesh Kumar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Property owner · Verified", fontSize = 13.sp, color = Color.Gray)
            }
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Chat", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PropertyDetailBottomBar(onScheduleClick: () -> Unit, onRequestToRentClick: () -> Unit, isRented: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .padding(20.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onScheduleClick,
                    enabled = !isRented,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.LightGray),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Text("Schedule Visit", color = if (isRented) Color.Gray else Color.Black, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onRequestToRentClick,
                    enabled = !isRented,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E))
                ) {
                    Text(if (isRented) "Already Rented" else "Request to Rent", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(showBackground = true)
@Composable
fun PropertyDetailScreenPreview() {
    MyRentTheme {
        PropertyDetailScreen(onBackClick = {}, propertyName = "Sunshine Apartments")
    }
}
