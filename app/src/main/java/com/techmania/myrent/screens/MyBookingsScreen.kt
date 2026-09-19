package com.techmania.myrent.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmania.myrent.ui.theme.MyRentTheme

// ─── Data Models ───────────────────────────────────────────────────────────────

data class Booking(
    val propertyName: String,
    val location: String,
    val bhk: String,
    val price: String,
    val status: BookingStatus,
    val since: String = "",
    val visitDate: String = "",
    val imageBg: Color = Color(0xFFC8D3F5)
)

enum class BookingStatus(val label: String, val color: Color) {
    ACTIVE("Active", Color(0xFF2E7D32)),
    VISIT_SET("Visit set", Color(0xFF4B4EFC)),
    PENDING("Pending", Color(0xFFFF9800)),
    COMPLETED("Completed", Color(0xFF757575)),
    CANCELLED("Cancelled", Color(0xFFD32F2F))
}

// ─── Sample Data ───────────────────────────────────────────────────────────────

private val sampleActiveBookings = listOf(
    Booking(
        propertyName = "Sunshine Apts",
        location = "Dwarka Sec 10",
        bhk = "2 BHK",
        price = "₹22,000/mo",
        status = BookingStatus.ACTIVE,
        since = "Since Jan 2025",
        imageBg = Color(0xFFC8D3F5)
    ),
    Booking(
        propertyName = "Green View Villa",
        location = "Vasant Kunj",
        bhk = "3 BHK",
        price = "₹45,000/mo",
        status = BookingStatus.VISIT_SET,
        visitDate = "Sat, 26 Apr · 11:00 AM",
        imageBg = Color(0xFFD5E8D4)
    )
)

private val samplePendingBookings = listOf(
    Booking(
        propertyName = "Lake View Residency",
        location = "Saket",
        bhk = "2 BHK",
        price = "₹30,000/mo",
        status = BookingStatus.PENDING,
        imageBg = Color(0xFFE8D5C8)
    )
)

private val samplePastBookings = emptyList<Booking>()

// ─── Main Screen ───────────────────────────────────────────────────────────────

@Composable
fun MyBookingsScreen(
    isLandlord: Boolean = false,
    onHomeClick: () -> Unit = {},
    onExploreClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onListingsClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active (2)", "Pending (1)", "Past")

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
        containerColor = Color(0xFFF8F9FB)
    ) { innerPadding ->
        // Center content and limit width for tablets
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
                // ── Dark Header ────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141414))
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp)
                        .padding(top = 28.dp, bottom = 20.dp)
                ) {
                    Text(
                        text = "My Bookings",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Track your rental requests & visits",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }

                // ── Tab Row ────────────────────────────────────────────────────
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 4.dp),
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
                                    .padding(vertical = 14.dp)
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSelected) Color(0xFF4B4EFC) else Color(0xFF8E8E93),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.5.dp)
                                        .background(
                                            if (isSelected) Color(0xFF4B4EFC) else Color.Transparent,
                                            RoundedCornerShape(2.dp)
                                        )
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
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    when (selectedTab) {
                        0 -> ActiveBookingsContent()
                        1 -> PendingBookingsContent()
                        2 -> PastBookingsContent()
                    }
                }
            }
        }
    }
}

// ─── Active Tab Content ────────────────────────────────────────────────────────

@Composable
fun ActiveBookingsContent() {
    sampleActiveBookings.forEach { booking ->
        when (booking.status) {
            BookingStatus.ACTIVE -> ActiveRentalCard(booking)
            BookingStatus.VISIT_SET -> VisitSetCard(booking)
            else -> ActiveRentalCard(booking)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // "No more active bookings" footer
    Spacer(modifier = Modifier.height(24.dp))
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Inventory2,
            contentDescription = null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No more active bookings",
            color = Color(0xFF8E8E93),
            fontSize = 14.sp
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
}

// ─── Active Rental Card (Sunshine Apts style) ──────────────────────────────────

@Composable
fun ActiveRentalCard(booking: Booking) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF0F0F2)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                // Property thumbnail
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(booking.imageBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

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
                            color = Color.Black
                        )
                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = booking.status.color.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = booking.status.label,
                                color = booking.status.color,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${booking.location} · ${booking.bhk}",
                        color = Color(0xFF8E8E93),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = booking.price,
                            color = Color(0xFF4B4EFC),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = booking.since,
                            color = Color(0xFF8E8E93),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF2F2F2))
            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pay rent button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.clickable { }
                ) {
                    Text(
                        text = "Pay rent",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                // Contact owner button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.clickable { }
                ) {
                    Text(
                        text = "Contact\nowner",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                // Vacate button (red text)
                Text(
                    text = "Vacate",
                    color = Color(0xFFD32F2F),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}

// ─── Visit Set Card (Green View Villa style) ────────────────────────────────────

@Composable
fun VisitSetCard(booking: Booking) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF0F0F2)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                // Property thumbnail
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(booking.imageBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

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
                            color = Color.Black
                        )
                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = booking.status.color.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = booking.status.label,
                                color = booking.status.color,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${booking.location} · ${booking.bhk}",
                        color = Color(0xFF8E8E93),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Visit date row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF4B4EFC),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = booking.visitDate,
                            color = Color(0xFF4B4EFC),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = booking.price,
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF2F2F2))
            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reschedule button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.clickable { }
                ) {
                    Text(
                        text = "Reschedule",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                // Get directions button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.clickable { }
                ) {
                    Text(
                        text = "Get directions",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                // Cancel button (red text)
                Text(
                    text = "Cancel",
                    color = Color(0xFFD32F2F),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}

// ─── Pending Tab Content ───────────────────────────────────────────────────────

@Composable
fun PendingBookingsContent() {
    if (samplePendingBookings.isEmpty()) {
        EmptyBookingsPlaceholder("No pending bookings")
    } else {
        samplePendingBookings.forEach { booking ->
            PendingBookingCard(booking)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                tint = Color(0xFFBDBDBD),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No more pending bookings",
                color = Color(0xFF8E8E93),
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PendingBookingCard(booking: Booking) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF0F0F2)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(booking.imageBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

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
                            color = Color.Black
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = booking.status.color.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = booking.status.label,
                                color = booking.status.color,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${booking.location} · ${booking.bhk}",
                        color = Color(0xFF8E8E93),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = booking.price,
                        color = Color(0xFF4B4EFC),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF2F2F2))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.clickable { }
                ) {
                    Text(
                        text = "Contact owner",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                Text(
                    text = "Cancel request",
                    color = Color(0xFFD32F2F),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}

// ─── Past Tab Content ──────────────────────────────────────────────────────────

@Composable
fun PastBookingsContent() {
    EmptyBookingsPlaceholder("No past bookings yet")
}

// ─── Empty State Placeholder ───────────────────────────────────────────────────

@Composable
fun EmptyBookingsPlaceholder(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Inventory2,
            contentDescription = null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = Color(0xFF8E8E93),
            fontSize = 15.sp
        )
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
