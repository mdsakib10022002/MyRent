package com.techmania.myrent.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.techmania.myrent.ui.theme.MyRentTheme
import java.text.NumberFormat
import java.util.*

@Composable
fun LandlordDashboardScreen(
    userName: String = "Rahul",
    isLandlord: Boolean = true,
    propertyViewModel: PropertyViewModel = viewModel(),
    onHomeClick: () -> Unit = {},
    onExploreClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAddNewClick: () -> Unit = {},
    onBookingsClick: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            AppBottomNavigation(
                currentScreen = "Listings",
                isLandlord = isLandlord,
                onNavClick = { label ->
                    when (label) {
                        "Home" -> onHomeClick()
                        "Explore" -> onExploreClick()
                        "Profile" -> onProfileClick()
                        "Bookings" -> onBookingsClick()
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewClick,
                containerColor = Color(0xFF4B4EFC),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Property")
            }
        },
        containerColor = Color(0xFFF8F9FB)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 800.dp)
            ) {
                item {
                    DashboardHeader(userName)
                }
                
                item {
                    StatsSection(
                        listingsCount = propertyViewModel.properties.size,
                        rentedCount = propertyViewModel.rentedCount,
                        enquiriesCount = propertyViewModel.enquiries.size
                    )
                }
                
                item {
                    EarningsSection(
                        totalEarnings = propertyViewModel.displayEarnings,
                        growth = propertyViewModel.earningsGrowth,
                        history = propertyViewModel.monthlyEarningsHistory,
                        selectedMonth = propertyViewModel.selectedMonth,
                        availableMonths = propertyViewModel.availableMonths,
                        onMonthSelected = { propertyViewModel.selectedMonth = it }
                    )
                }
                
                item {
                    MyListingsHeader()
                }
                
                if (propertyViewModel.properties.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No listings yet. Add your first property!", color = Color.Gray)
                        }
                    }
                } else {
                    items(
                        items = propertyViewModel.properties,
                        key = { it.name }
                    ) { property ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    propertyViewModel.removeProperty(property)
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp, vertical = 8.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(color),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )
                                }
                            }
                        ) {
                            DashboardPropertyItem(
                                name = property.name,
                                price = property.price,
                                config = property.bhk,
                                status = property.badge,
                                statusColor = property.badgeColor.copy(alpha = 0.1f),
                                statusTextColor = property.badgeColor,
                                icon = Icons.Default.Home,
                                iconBg = Color(0xFFE3F2FD),
                                mediaUris = property.mediaUris
                            )
                        }
                    }
                }
                
                item {
                    RecentEnquiriesHeader()
                }
                
                if (propertyViewModel.enquiries.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No recent enquiries", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(propertyViewModel.enquiries) { enquiry ->
                        EnquiryItem(
                            enquiry = enquiry,
                            onAccept = { propertyViewModel.respondToEnquiry(enquiry, true) },
                            onDecline = { propertyViewModel.respondToEnquiry(enquiry, false) }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun DashboardHeader(userName: String) {
    val initials = userName.split(" ")
        .filter { it.isNotEmpty() }
        .map { it[0].uppercase() }
        .joinToString("")
        .take(2)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141414))
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 28.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Good morning, $userName 👋",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Manage your\nproperties",
                    color = Color(0xFFFBF4D0),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 38.sp
                )
            }
            
            Surface(
                shape = CircleShape,
                color = Color(0xFF5C6BC0),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatsSection(listingsCount: Int, rentedCount: Int, enquiriesCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141414))
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(modifier = Modifier.weight(1f), count = listingsCount.toString(), label = "Listings")
        StatCard(modifier = Modifier.weight(1f), count = rentedCount.toString(), label = "Rented", countColor = Color(0xFF4DB6AC))
        StatCard(modifier = Modifier.weight(1f), count = enquiriesCount.toString(), label = "Enquiries", countColor = Color(0xFFFFB74D))
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, count: String, label: String, countColor: Color = Color.White) {
    Surface(
        modifier = modifier,
        color = Color(0xFF26262D),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count, color = countColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
    }
}

@Composable
fun EarningsSection(
    totalEarnings: Int, 
    growth: String, 
    history: List<Pair<String, Float>>,
    selectedMonth: String,
    availableMonths: List<String>,
    onMonthSelected: (String) -> Unit
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }
    val formattedEarnings = currencyFormatter.format(totalEarnings)
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Monthly earnings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Box {
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { expanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val year = Calendar.getInstance().get(Calendar.YEAR)
                            Text(text = "$selectedMonth $year", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        availableMonths.forEach { month ->
                            DropdownMenuItem(
                                text = { Text(month) },
                                onClick = {
                                    onMonthSelected(month)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = formattedEarnings, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = growth,
                    color = Color(0xFF2E7D32),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                history.forEach { (label, height) ->
                    ChartBar(
                        label = label,
                        heightFrac = height,
                        color = if (label == selectedMonth) Color(0xFF5C6BC0) else Color(0xFFD1D9FF)
                    )
                }
            }
        }
    }
}

@Composable
fun ChartBar(label: String, heightFrac: Float, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .width(45.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(heightFrac)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun MyListingsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "My listings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun DashboardPropertyItem(
    name: String,
    price: String,
    config: String,
    status: String,
    statusColor: Color,
    statusTextColor: Color,
    icon: ImageVector,
    iconBg: Color,
    mediaUris: List<String> = emptyList()
) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                if (mediaUris.isNotEmpty()) {
                    AsyncImage(
                        model = mediaUris.first(),
                        contentDescription = name,
                        imageLoader = imageLoader,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(icon, contentDescription = null, tint = Color.Gray.copy(alpha = 0.5f))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = "$price · $config", fontSize = 13.sp, color = Color.Gray)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = status,
                        color = statusTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.PhoneIphone, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun RecentEnquiriesHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Recent enquiries", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            text = "See all",
            color = Color(0xFF5C6BC0),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
fun EnquiryItem(
    enquiry: Enquiry,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFB)),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = enquiry.avatarBg,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = enquiry.initials, color = Color(0xFF5C6BC0), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = enquiry.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "${enquiry.propertyName} · ${enquiry.type}", fontSize = 12.sp, color = Color.Gray)
                if (enquiry.status != "Pending") {
                    Text(
                        text = enquiry.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (enquiry.status == "Accepted") Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    )
                }
            }
            
            if (enquiry.status == "Pending") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Accept", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onDecline,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Decline", fontSize = 12.sp, color = Color.Black)
                    }
                }
            }
            
            if (enquiry.showChatButton) {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Chat", fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(showBackground = true)
@Composable
fun LandlordDashboardScreenPreview() {
    MyRentTheme {
        LandlordDashboardScreen()
    }
}
