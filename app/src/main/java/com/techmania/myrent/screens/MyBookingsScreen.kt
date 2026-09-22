package com.techmania.myrent.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.techmania.myrent.ui.theme.MyRentTheme

// ─── Data Models moved to Models.kt ──────────────────────────────────────────

// ─── UI Helper ───────────────────────────────────────────────────────────────

@Composable
fun StatusBadge(status: String) {
    val statusColor = when (status) {
        "Active" -> Color(0xFF22C55E)
        "Visit set" -> Color(0xFF3B82F6)
        "Pending" -> Color(0xFFF59E0B)
        "Completed" -> Color(0xFF6B7280)
        else -> Color(0xFFEF4444)
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = statusColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = status.uppercase(),
            color = statusColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

// ─── Main Screen ───────────────────────────────────────────────────────────────

@Composable
fun MyBookingsScreen(
    isLandlord: Boolean = false,
    propertyViewModel: PropertyViewModel = viewModel(),
    onHomeClick: () -> Unit = {},
    onExploreClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onListingsClick: () -> Unit = {},
    onPropertyClick: (String) -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    
    val userBookings = propertyViewModel.bookings.filter { it.tenantId == userId }
    
    val activeBookings = userBookings.filter { it.status == "Active" || it.status == "Visit set" }
    val pendingBookings = userBookings.filter { it.status == "Pending" }
    val pastBookings = userBookings.filter { it.status == "Completed" || it.status == "Cancelled" }

    var selectedTab by remember { mutableIntStateOf(0) }
    var reschedulingBooking by remember { mutableStateOf<Booking?>(null) }

    val tabs = listOf(
        "Active (${activeBookings.size})",
        "Pending (${pendingBookings.size})",
        "Past (${pastBookings.size})"
    )

    if (reschedulingBooking != null) {
        ScheduleVisitDialog(
            propertyName = reschedulingBooking!!.propertyName,
            onDismiss = { reschedulingBooking = null },
            onConfirm = { date, time ->
                propertyViewModel.rescheduleVisit(reschedulingBooking!!, date, time)
                reschedulingBooking = null
            }
        )
    }

    Scaffold(
        bottomBar = {
            AppBottomNavigation(
                currentScreen = "Bookings",
                isLandlord = isLandlord,
                onNavClick = { label ->
                    when (label) {
                        "Home" -> onHomeClick()
                        "Explore" -> onExploreClick()
                        "Profile" -> onProfileClick()
                        "Listings" -> onListingsClick()
                    }
                }
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { innerPadding ->
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
            ) {
                // ── Modern Header (Compact) ───────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "My Bookings",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Manage active rentals & visits",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // ── Tab Row ────────────────────────────────────────────────────
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { selectedTab = index }
                                    .padding(vertical = 16.dp)
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSelected) Color(0xFF4B4EFC) else Color(0xFF6B7280),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isSelected) Color(0xFF4B4EFC) else Color.Transparent)
                                )
                            }
                        }
                    }
                }

                // ── Tab Content ────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> ActiveBookingsContent(activeBookings, onPropertyClick, onExploreClick, propertyViewModel, onReschedule = { reschedulingBooking = it })
                        1 -> PendingBookingsContent(pendingBookings, onPropertyClick, onExploreClick, propertyViewModel)
                        2 -> PastBookingsContent(pastBookings, onPropertyClick, onExploreClick)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ─── Active Tab Content ────────────────────────────────────────────────────────

@Composable
fun ActiveBookingsContent(bookings: List<Booking>, onPropertyClick: (String) -> Unit, onExploreClick: () -> Unit, propertyViewModel: PropertyViewModel, onReschedule: (Booking) -> Unit) {
    if (bookings.isEmpty()) {
        EmptyBookingsPlaceholder("No active bookings found", onExploreClick)
    } else {
        bookings.forEach { booking ->
            if (booking.status == "Visit set") {
                VisitSetCard(booking, onPropertyClick, propertyViewModel, onReschedule)
            } else {
                ActiveRentalCard(booking, onPropertyClick, propertyViewModel)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Info card about rental agreement
        Surface(
            color = Color(0xFFEEF2FF),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, null, tint = Color(0xFF4B4EFC), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Digital rental agreements are available for all active rentals.",
                    color = Color(0xFF1E1B4B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─── Active Rental Card (Sunshine Apts style) ──────────────────────────────────

@Composable
fun ActiveRentalCard(booking: Booking, onClick: (String) -> Unit, propertyViewModel: PropertyViewModel) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoader.Builder(context).components { add(VideoFrameDecoder.Factory()) }.build() }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(booking.propertyName) },
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Property thumbnail
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(booking.imageBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (booking.mediaUri.isNotEmpty()) {
                        AsyncImage(
                            model = booking.mediaUri,
                            contentDescription = booking.propertyName,
                            imageLoader = imageLoader,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = booking.propertyName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1F2937)
                        )
                        // Status badge
                        StatusBadge(booking.status)
                    }

                    Text(
                        text = "${booking.location} · ${booking.bhk}",
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = booking.price,
                                color = Color(0xFF4B4EFC),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "/mo",
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                            )
                        }
                        Text(
                            text = booking.since,
                            color = Color(0xFF9CA3AF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B4EFC)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Pay Rent", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Contact Owner", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier.size(44.dp).background(Color(0xFFFEE2E2), RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ─── Visit Set Card (Green View Villa style) ────────────────────────────────────

@Composable
fun VisitSetCard(booking: Booking, onClick: (String) -> Unit, propertyViewModel: PropertyViewModel, onReschedule: (Booking) -> Unit) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoader.Builder(context).components { add(VideoFrameDecoder.Factory()) }.build() }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(booking.propertyName) },
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Property thumbnail
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(booking.imageBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (booking.mediaUri.isNotEmpty()) {
                        AsyncImage(
                            model = booking.mediaUri,
                            contentDescription = booking.propertyName,
                            imageLoader = imageLoader,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = booking.propertyName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1F2937)
                        )
                        // Status badge
                        StatusBadge(booking.status)
                    }

                    Text(
                        text = "${booking.location} · ${booking.bhk}",
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Visit date row
                    Surface(
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF4B4EFC),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = booking.visitDate,
                                color = Color(0xFF1F2937),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1.2f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Map, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Directions", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onReschedule(booking) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Reschedule", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                }

                IconButton(
                    onClick = { propertyViewModel.cancelBooking(booking.id) },
                    modifier = Modifier.size(44.dp).background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Default.Close, null, tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun PendingBookingsContent(bookings: List<Booking>, onPropertyClick: (String) -> Unit, onExploreClick: () -> Unit, propertyViewModel: PropertyViewModel) {
    if (bookings.isEmpty()) {
        EmptyBookingsPlaceholder("No pending requests", onExploreClick)
    } else {
        bookings.forEach { booking ->
            PendingBookingCard(booking, onPropertyClick, propertyViewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PendingBookingCard(booking: Booking, onClick: (String) -> Unit, propertyViewModel: PropertyViewModel) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoader.Builder(context).components { add(VideoFrameDecoder.Factory()) }.build() }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(booking.propertyName) },
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(booking.imageBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (booking.mediaUri.isNotEmpty()) {
                        AsyncImage(
                            model = booking.mediaUri,
                            contentDescription = booking.propertyName,
                            imageLoader = imageLoader,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = booking.propertyName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1F2937)
                        )
                        // Status badge
                        StatusBadge(booking.status)
                    }

                    Text(
                        text = "${booking.location} · ${booking.bhk}",
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = booking.price,
                        color = Color(0xFF4B4EFC),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    if (booking.visitDate.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Visit: ${booking.visitDate}", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Text("Message Owner", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                }

                Button(
                    onClick = { propertyViewModel.cancelBooking(booking.id) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2))
                ) {
                    Text("Cancel", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
            }
        }
    }
}

// ─── Past Tab Content ──────────────────────────────────────────────────────────

@Composable
fun PastBookingsContent(bookings: List<Booking>, onPropertyClick: (String) -> Unit, onExploreClick: () -> Unit) {
    if (bookings.isEmpty()) {
        EmptyBookingsPlaceholder("No past bookings found", onExploreClick)
    } else {
        bookings.forEach { booking ->
            PastBookingCard(booking, onPropertyClick)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PastBookingCard(booking: Booking, onClick: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.8f) // Slightly faded for past bookings
            .clickable { onClick(booking.propertyName) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(booking.imageBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = booking.propertyName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1F2937)
                        )
                        // Status badge
                        StatusBadge(booking.status)
                    }

                    Text(
                        text = "${booking.location} · ${booking.bhk}",
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ─── Empty State Placeholder ───────────────────────────────────────────────────

@Composable
fun EmptyBookingsPlaceholder(message: String, onExploreClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFF3F4F6),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = message,
            color = Color(0xFF1F2937),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Browse premium homes and start your journey.",
            color = Color(0xFF6B7280),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onExploreClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B4EFC))
        ) {
            Text("Find Properties", fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(showBackground = true)
@Composable
fun MyBookingsScreenPreview() {
    MyRentTheme {
        MyBookingsScreen()
    }
}
