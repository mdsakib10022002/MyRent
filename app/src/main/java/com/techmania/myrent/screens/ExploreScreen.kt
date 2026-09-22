package com.techmania.myrent.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.techmania.myrent.ui.theme.MyRentTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
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
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isPreview = LocalInspectionMode.current
    val scope = rememberCoroutineScope()

    // Visibility States
    var isHeaderVisible by remember { mutableStateOf(true) }
    var isBottomBarVisible by remember { mutableStateOf(true) }
    
    // Bottom Sheet Scaffold State
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    // Nested Scroll for Bottom Bar Hiding
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -1) { // Scrolling Down
                    isBottomBarVisible = false
                } else if (available.y > 1) { // Scrolling Up
                    isBottomBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

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
        
        val isVisible = p.badge == "New" || p.badge == "Available"

        matchesSearch && matchesType && matchesPrice && matchesBhk && matchesFurnishing && matchesAvailability && matchesAmenities && matchesRating && isVisible
    }

    // Auto-direct map and preview on search
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() && filteredProperties.isNotEmpty()) {
            val firstMatch = filteredProperties.first()
            selectedPropertyName = firstMatch.name
            
            // Ensure UI overlays are visible during search
            isHeaderVisible = true
            
            // Animate map to the found property
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(firstMatch.latitude, firstMatch.longitude),
                    15f
                )
            )
            
            // Automatically reveal the property list sheet
            scaffoldState.bottomSheetState.partialExpand()
        }
    }

    val displayProperty = filteredProperties.find { it.name == selectedPropertyName } ?: filteredProperties.firstOrNull()

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = isBottomBarVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
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
            }
        },
        containerColor = Color(0xFFF8F9FB),
        modifier = Modifier.nestedScroll(nestedScrollConnection)
    ) { innerPadding ->
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 140.dp,
            sheetContainerColor = Color.White,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetDragHandle = {
                BottomSheetDefaults.DragHandle(color = Color.LightGray)
            },
            sheetContent = {
                PropertySheetContent(
                    properties = filteredProperties,
                    onPropertyClick = onPropertyClick
                )
            },
            containerColor = Color.Transparent
        ) { 
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (isBottomBarVisible) innerPadding.calculateBottomPadding() else 0.dp)
                    .consumeWindowInsets(innerPadding)
            ) {
                // GOOGLE MAPS - Wrapped for Preview Stability
                if (isPreview) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0))
                            .clickable { 
                                isHeaderVisible = !isHeaderVisible
                                scope.launch {
                                    if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                        scaffoldState.bottomSheetState.hide()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Map View Placeholder (Preview)", color = Color.Gray)
                    }
                } else {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, mapToolbarEnabled = false),
                        properties = MapProperties(isMyLocationEnabled = showUserLocation, mapType = mapType),
                        onMapClick = { 
                            isHeaderVisible = !isHeaderVisible
                            scope.launch { scaffoldState.bottomSheetState.hide() }
                        }
                    ) {
                        filteredProperties.forEach { property ->
                            MarkerComposable(
                                state = MarkerState(position = LatLng(property.latitude, property.longitude)),
                                onClick = {
                                    selectedPropertyName = property.name
                                    scope.launch { scaffoldState.bottomSheetState.partialExpand() }
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

                // TOP UI: COMPACT FLOATING SEARCH BAR
                AnimatedVisibility(
                    visible = isHeaderVisible,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.95f))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp)),
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
                                    tint = Color(0xFF000000),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search properties, areas...",
                                            color = Color(0xFF9CA3AF),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        textStyle = TextStyle(color = Color(0xFF1F2937), fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                        cursorBrush = SolidColor(Color(0xFF4B4EFC)),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
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
                                    .background(Color(0xFF000000))
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Tune, null, modifier = Modifier.size(20.dp), tint = Color.White)
                            }
                        }
                    }
                }

                // PROPERTY COUNT OVERLAY
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .padding(top = 100.dp, start = 16.dp)
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

                // MAP CONTROLS (Removed as requested)

                // FILTER BOTTOM SHEET
                if (showFilterSheet) {
                    FilterSearchBottomSheet(
                        onDismiss = { showFilterSheet = false },
                        sheetState = filterSheetState,
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
}

@Composable
fun PropertySheetContent(
    properties: List<Property>,
    onPropertyClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "${properties.size} Properties Found",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(properties) { property ->
                PropertyListItem(property, { onPropertyClick(property.name) })
            }
        }
    }
}

@Composable
fun PropertyListItem(property: Property, onClick: () -> Unit) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoader.Builder(context).components { add(VideoFrameDecoder.Factory()) }.build() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF3F4F6)),
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
                Icon(Icons.Default.Home, null, tint = Color(0xFF4B4EFC).copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AutoResizingText(
                    text = property.name,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF1F2937),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = property.badgeColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = property.badge.uppercase(),
                        color = property.badgeColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
            AutoResizingText(
                text = "${property.areaName} · ${property.bhk}",
                style = TextStyle(fontSize = 13.sp),
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f, fill = false), verticalAlignment = Alignment.CenterVertically) {
                    AutoResizingText(
                        text = property.price,
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF4B4EFC)
                    )
                    Text("/mo", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 2.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFCC00), modifier = Modifier.size(14.dp))
                    Text(" ${property.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
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
