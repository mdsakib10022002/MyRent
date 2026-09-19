package com.techmania.myrent.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.techmania.myrent.ui.theme.MyRentTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    userName: String = "Rahul",
    isLandlord: Boolean = false,
    propertyViewModel: PropertyViewModel = viewModel(),
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onListingsClick: () -> Unit = {},
    onBookingsClick: () -> Unit = {},
    onPropertyClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isPreview = LocalInspectionMode.current

    // Map States
    var showUserLocation by remember { mutableStateOf(false) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    val delhi = LatLng(28.6139, 77.2090)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(delhi, 12f)
    }

    // Applied Filter State
    var appliedFilters by remember { mutableStateOf(FilterParams()) }

    // Filter States (Temp)
    var tempPropertyTypes by remember { mutableStateOf(appliedFilters.propertyTypes) }
    var tempPriceRange by remember { mutableStateOf(appliedFilters.priceRange) }
    var tempBhkConfigs by remember { mutableStateOf(appliedFilters.bhkConfigs) }
    var tempFurnishings by remember { mutableStateOf(appliedFilters.furnishings) }
    var tempAvailabilities by remember { mutableStateOf(appliedFilters.availabilities) }
    var tempAmenities by remember { mutableStateOf(appliedFilters.amenities) }
    var tempMinRating by remember { mutableStateOf(appliedFilters.minRating) }

    val nearbyProperties = propertyViewModel.properties

    var selectedPropertyName by remember { mutableStateOf("") }

    val filteredProperties = nearbyProperties.filter { p ->
        val matchesSearch = p.name.contains(searchQuery, ignoreCase = true) ||
                p.city.contains(searchQuery, ignoreCase = true) ||
                p.areaName.contains(searchQuery, ignoreCase = true) ||
                p.pincode.contains(searchQuery, ignoreCase = true)
        
        val matchesType = appliedFilters.propertyTypes.contains("All") || appliedFilters.propertyTypes.contains(p.type)
        val matchesPrice = p.numericPrice.toFloat() >= appliedFilters.priceRange.start && p.numericPrice.toFloat() <= appliedFilters.priceRange.endInclusive
        val matchesBhk = appliedFilters.bhkConfigs.contains(p.bhk)
        val matchesFurnishing = appliedFilters.furnishings.contains("Any") || appliedFilters.furnishings.contains(p.furnishing)
        val matchesAvailability = appliedFilters.availabilities.contains(p.availability)
        val matchesAmenities = appliedFilters.amenities.all { p.amenities.contains(it) }
        
        val requiredRating = when (appliedFilters.minRating) {
            "★ 3+" -> 3.0f
            "★ 4+" -> 4.0f
            "★ 4.5+" -> 4.5f
            else -> 0f
        }
        val matchesRating = p.numericRating >= requiredRating

        matchesSearch && matchesType && matchesPrice && matchesBhk && matchesFurnishing && matchesAvailability && matchesAmenities && matchesRating
    }

    val displayProperty = filteredProperties.find { it.name == selectedPropertyName } ?: filteredProperties.firstOrNull()

    Scaffold(
        bottomBar = {
            AppBottomNavigation(
                currentScreen = "Explore",
                isLandlord = isLandlord,
                onNavClick = { label ->
                    when (label) {
                        "Home" -> onHomeClick()
                        "Bookings" -> onBookingsClick()
                        "Listings" -> onListingsClick()
                        "Profile" -> onProfileClick()
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F9FB)
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
        ) {
            // GOOGLE MAPS - Wrapped for Preview Stability
            if (isPreview) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Map View Placeholder (Preview)", color = Color.Gray)
                }
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, mapToolbarEnabled = false),
                    properties = MapProperties(isMyLocationEnabled = showUserLocation, mapType = mapType)
                ) {
                    filteredProperties.forEach { property ->
                        MarkerComposable(
                            state = MarkerState(position = LatLng(property.latitude, property.longitude)),
                            onClick = {
                                selectedPropertyName = property.name
                                false 
                            }
                        ) {
                            PricePin(
                                price = if (property.numericPrice >= 1000) "₹${property.numericPrice / 1000}K" else "₹${property.numericPrice}",
                                isSelected = displayProperty?.name == property.name
                            )
                        }
                    }
                }
            }

            // TOP UI: GREETING AND SEARCH BAR
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141414))
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 20.dp, bottom = 8.dp),
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
                            Spacer(modifier = Modifier.height(6.6.dp))
                            Text(
                                text = "Explore rentals",
                                color = Color(0xFFFBF4D0),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF26262D),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 12.dp, bottom = 20.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF26262D)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search area, city...",
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 14.sp
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                                    cursorBrush = SolidColor(Color.White),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(min = 90.dp)
                                .background(Color(0xFF4B4EFC))
                                .clickable { 
                                    tempPropertyTypes = appliedFilters.propertyTypes
                                    tempPriceRange = appliedFilters.priceRange
                                    tempBhkConfigs = appliedFilters.bhkConfigs
                                    tempFurnishings = appliedFilters.furnishings
                                    tempAvailabilities = appliedFilters.availabilities
                                    tempAmenities = appliedFilters.amenities
                                    tempMinRating = appliedFilters.minRating
                                    showFilterSheet = true 
                                }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // PROPERTY COUNT OVERLAY
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(top = 180.dp, start = 20.dp)
                    .align(Alignment.TopStart),
                shadowElevation = 4.dp
            ) {
                Text(
                    "${filteredProperties.size} properties nearby",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // MAP CONTROLS (Zoom, GPS, MapType)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 180.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapControlButton(Icons.Default.Layers, onClick = { 
                    mapType = if (mapType == MapType.NORMAL) MapType.HYBRID else MapType.NORMAL
                })
                MapControlButton(Icons.Default.Add, onClick = { 
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        cameraPositionState.position.target,
                        cameraPositionState.position.zoom + 1f
                    )
                })
                MapControlButton(Icons.Default.Remove, onClick = { 
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        cameraPositionState.position.target,
                        cameraPositionState.position.zoom - 1f
                    )
                })
                MapControlButton(
                    Icons.Default.MyLocation, 
                    tint = if (showUserLocation) Color(0xFF4B4EFC) else Color.Black,
                    onClick = { 
                        showUserLocation = !showUserLocation
                    }
                )
            }

            // PROPERTY PREVIEW CARD
            if (displayProperty != null) {
                val property = displayProperty
                val context = LocalContext.current
                val imageLoader = remember {
                    ImageLoader.Builder(context)
                        .components {
                            add(VideoFrameDecoder.Factory())
                        }
                        .build()
                }

                Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter), contentAlignment = Alignment.BottomCenter) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .padding(bottom = 16.dp) 
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .width(40.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .align(Alignment.CenterHorizontally)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFC8D3F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (property.mediaUris.isNotEmpty()) {
                                    AsyncImage(
                                        model = property.mediaUris.first(),
                                        contentDescription = property.name,
                                        imageLoader = imageLoader,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF4B4EFC).copy(alpha = 0.5f))
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(property.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${property.areaName} · ${property.bhk}", fontSize = 13.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${property.price}/mo", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF4B4EFC))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFCC00), modifier = Modifier.size(14.dp))
                                    Text(" ${property.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Button(
                                onClick = { onPropertyClick(property.name) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("View")
                            }
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(filteredProperties.size.coerceAtMost(5)) { index ->
                                val isSelected = displayProperty.name == filteredProperties.getOrNull(index)?.name
                                val indicatorColor = if (isSelected) Color(0xFF4B4EFC) else Color.LightGray
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .size(if (isSelected) 10.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(indicatorColor)
                                )
                            }
                        }
                    }
                }
            }

            // FILTER BOTTOM SHEET
            if (showFilterSheet) {
                FilterSearchBottomSheet(
                    onDismiss = { showFilterSheet = false },
                    sheetState = sheetState,
                    selectedPropertyTypes = tempPropertyTypes,
                    onPropertyTypeToggle = { type ->
                        tempPropertyTypes = if (type == "All") setOf("All") else {
                            val newSet = tempPropertyTypes.toMutableSet()
                            newSet.remove("All")
                            if (newSet.contains(type)) newSet.remove(type) else newSet.add(type)
                            if (newSet.isEmpty()) setOf("All") else newSet
                        }
                    },
                    priceRange = tempPriceRange,
                    onPriceRangeChange = { tempPriceRange = it },
                    selectedBhkConfigs = tempBhkConfigs,
                    onBhkToggle = { bhk ->
                        tempBhkConfigs = if (tempBhkConfigs.contains(bhk)) {
                            if (tempBhkConfigs.size > 1) tempBhkConfigs - bhk else tempBhkConfigs
                        } else tempBhkConfigs + bhk
                    },
                    selectedFurnishings = tempFurnishings,
                    onFurnishingToggle = { f ->
                        tempFurnishings = if (f == "Any") setOf("Any") else {
                            val newSet = tempFurnishings.toMutableSet()
                            newSet.remove("Any")
                            if (newSet.contains(f)) newSet.remove(f) else newSet.add(f)
                            if (newSet.isEmpty()) setOf("Any") else newSet
                        }
                    },
                    selectedAvailabilities = tempAvailabilities,
                    onAvailabilityToggle = { a ->
                        tempAvailabilities = if (tempAvailabilities.contains(a)) {
                            if (tempAvailabilities.size > 1) tempAvailabilities - a else tempAvailabilities
                        } else tempAvailabilities + a
                    },
                    selectedAmenities = tempAmenities,
                    onAmenityToggle = { am ->
                        tempAmenities = if (tempAmenities.contains(am)) tempAmenities - am else tempAmenities + am
                    },
                    minRating = tempMinRating,
                    onRatingChange = { tempMinRating = it },
                    onReset = {
                        tempPropertyTypes = setOf("All")
                        tempPriceRange = 5000f..80000f
                        tempBhkConfigs = setOf("2 BHK")
                        tempFurnishings = setOf("Any")
                        tempAvailabilities = setOf("Immediate")
                        tempAmenities = setOf("Parking", "WiFi", "AC")
                        tempMinRating = "★ 3+"
                    },
                    onApply = {
                        appliedFilters = FilterParams(
                            propertyTypes = tempPropertyTypes,
                            priceRange = tempPriceRange,
                            bhkConfigs = tempBhkConfigs,
                            furnishings = tempFurnishings,
                            availabilities = tempAvailabilities,
                            amenities = tempAmenities,
                            minRating = tempMinRating
                        )
                        showFilterSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun PricePin(
    modifier: Modifier = Modifier,
    price: String,
    isSelected: Boolean,
    backgroundColor: Color = Color.White
) {
    val finalBgColor = if (isSelected) Color(0xFF1C1C1E) else backgroundColor
    val textColor = if (isSelected) Color.White else if (backgroundColor == Color.White) Color(0xFF4B4EFC) else Color.White

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                color = finalBgColor,
                shape = RoundedCornerShape(12.dp),
                border = if (!isSelected && backgroundColor == Color.White) BorderStroke(1.dp, Color(0xFF4B4EFC)) else null,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = price,
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(y = (-4).dp)
                    .clip(TriangleShape)
                    .background(finalBgColor)
            )
        }
    }
}

@Composable
fun MapControlButton(icon: ImageVector, tint: Color = Color.Black, onClick: () -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.size(40.dp).clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = tint)
        }
    }
}

val TriangleShape = object : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width / 2, size.height)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(showBackground = true)
@Composable
fun ExploreScreenPreview() {
    MyRentTheme {
        ExploreScreen()
    }
}
