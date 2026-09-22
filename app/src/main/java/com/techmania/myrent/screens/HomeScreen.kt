package com.techmania.myrent.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.techmania.myrent.ui.theme.MyRentTheme

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    userName: String = "Rahul", 
    isLandlord: Boolean = false,
    propertyViewModel: PropertyViewModel = viewModel(),
    onPropertyClick: (String) -> Unit = {},
    onExploreClick: () -> Unit = {},
    onListingsClick: () -> Unit = {},
    onBookingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var appliedFilters by remember { mutableStateOf(FilterParams()) }

    var tempPropertyTypes by remember { mutableStateOf(appliedFilters.propertyTypes) }
    var tempPriceRange by remember { mutableStateOf(appliedFilters.priceRange) }
    var tempBhkConfigs by remember { mutableStateOf(appliedFilters.bhkConfigs) }
    var tempFurnishings by remember { mutableStateOf(appliedFilters.furnishings) }
    var tempAvailabilities by remember { mutableStateOf(appliedFilters.availabilities) }
    var tempAmenities by remember { mutableStateOf(appliedFilters.amenities) }
    var tempMinRating by remember { mutableStateOf(appliedFilters.minRating) }

    val trendingProperties = remember(propertyViewModel.properties) {
        propertyViewModel.properties.filter { p ->
            p.badge == "New" || (p.badge == "Available" && p.numericRating >= 4.0f)
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomNavigation(
                currentScreen = "Home",
                isLandlord = isLandlord,
                onNavClick = { label ->
                    when (label) {
                        "Explore" -> onExploreClick()
                        "Bookings" -> onBookingsClick()
                        "Listings" -> onListingsClick()
                        "Profile" -> onProfileClick()
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp)
            ) {
                item {
                    HeaderGreetingSection(userName = userName)
                }
                
                stickyHeader {
                    StickySearchBarSection(
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onFilterClick = {
                            tempPropertyTypes = appliedFilters.propertyTypes
                            tempPriceRange = appliedFilters.priceRange
                            tempBhkConfigs = appliedFilters.bhkConfigs
                            tempFurnishings = appliedFilters.furnishings
                            tempAvailabilities = appliedFilters.availabilities
                            tempAmenities = appliedFilters.amenities
                            tempMinRating = appliedFilters.minRating
                            showFilterSheet = true
                        }
                    )
                }
                
                item {
                    CategorySection(
                        selectedCategory = if (appliedFilters.propertyTypes.size == 1) appliedFilters.propertyTypes.first() else "All",
                        onCategorySelected = { category ->
                            appliedFilters = appliedFilters.copy(propertyTypes = setOf(category))
                        }
                    )
                }

                // New Attractive Horizontal Layer for Quick Discovery
                if (searchQuery.isEmpty() && trendingProperties.isNotEmpty()) {
                    item {
                        TrendingRowSection(
                            properties = trendingProperties,
                            onPropertyClick = onPropertyClick,
                            onLikeClick = { propertyViewModel.toggleLike(it) }
                        )
                    }
                }
                
                item {
                    FeaturedListingsSection(
                        properties = propertyViewModel.properties,
                        searchQuery = searchQuery,
                        filters = appliedFilters,
                        onPropertyClick = onPropertyClick,
                        onLikeClick = { propertyViewModel.toggleLike(it) }
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }

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
                        tempBhkConfigs = setOf("1 BHK")
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
fun HeaderGreetingSection(userName: String) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, $userName 👋",
                        color = Color(0xFF4B5563), // Darker grey for legibility on light background
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Find Your Home",
                        color = Color(0xFF1F2937), // Near black
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Notifications, null, tint = Color(0xFF1F2937), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StickySearchBarSection(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = 16.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFFFFF).copy(alpha = 0.95f)) // Slightly translucent for modern feel
                .border(2.dp, Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp), tint = Color.Black.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (searchQuery.isEmpty()) {
                        Text("Search area, city or projects...", color = Color.Black.copy(alpha = 0.3f), fontSize = 14.sp)
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onQueryChange,
                        textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                        cursorBrush = SolidColor(Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable { onFilterClick() }
                    .background(Color(0xFF4B4EFC))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Tune, null, modifier = Modifier.size(20.dp), tint = Color.White)
            }
        }
    }
}

@Composable
fun CategorySection(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("All", "Apartment", "Villa", "Studio", "PG / Hostel", "Commercial")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategory == category
            Surface(
                onClick = { onCategorySelected(category) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF4B4EFC) else Color.White,
                border = BorderStroke(1.dp, if (isSelected) Color(0xFF4B4EFC) else Color(0xFFE5E7EB)),
                modifier = Modifier.height(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = category,
                        color = if (isSelected) Color.White else Color(0xFF4B5563),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TrendingRowSection(
    properties: List<Property>, 
    onPropertyClick: (String) -> Unit, 
    onLikeClick: (Property) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = "Trending Near You", 
            fontSize = 17.sp, 
            fontWeight = FontWeight.Bold, 
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            properties.take(4).forEach { property ->
                CompactTrendingCard(property, { onPropertyClick(property.name) }, { onLikeClick(property) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactTrendingCard(property: Property, onClick: () -> Unit, onLikeClick: () -> Unit) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoader.Builder(context).components { add(VideoFrameDecoder.Factory()) }.build() }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFF9FAFB))
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
                    Box(modifier = Modifier.fillMaxSize().background(property.imageBg), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Home, null, modifier = Modifier.size(32.dp), tint = Color.White.copy(alpha = 0.5f))
                    }
                }

                // Status Badge Pill
                Surface(
                    color = property.badgeColor,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
                ) {
                    Text(
                        text = property.badge.uppercase(),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
                
                // Rating Overlay Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(8.dp).align(Alignment.BottomStart)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFB800), modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(property.rating, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                AutoResizingText(
                    text = property.name,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF1F2937),
                    modifier = Modifier.fillMaxWidth()
                )
                AutoResizingText(
                    text = "${property.bhk} • ${property.areaName}",
                    style = TextStyle(fontSize = 11.sp),
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = property.price, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4B4EFC))
                    Text(text = "/mo", fontSize = 11.sp, color = Color(0xFF9CA3AF), modifier = Modifier.padding(start = 2.dp))
                }
            }
        }
    }
}

@Composable
fun FeaturedListingsSection(
    properties: List<Property>, 
    searchQuery: String, 
    filters: FilterParams, 
    onPropertyClick: (String) -> Unit, 
    onLikeClick: (Property) -> Unit
) {
    val filtered = remember(properties, searchQuery, filters) {
        properties.filter { p ->
            val matchesSearch = p.name.contains(searchQuery, ignoreCase = true) ||
                    p.areaName.contains(searchQuery, ignoreCase = true) ||
                    p.city.contains(searchQuery, ignoreCase = true)
            
            val matchesType = filters.propertyTypes.contains("All") || filters.propertyTypes.contains(p.type)
            
            matchesSearch && matchesType
        }
    }
    
    Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        if (filtered.isNotEmpty()) {
            Text("All Featured Homes", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Spacer(Modifier.height(12.dp))
            filtered.forEach {
                PropertyCard(it, { onPropertyClick(it.name) }, { onLikeClick(it) })
                Spacer(Modifier.height(14.dp))
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No properties found matching your search.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyCard(property: Property, onClick: () -> Unit, onLikeClick: () -> Unit = {}) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoader.Builder(context).components { add(VideoFrameDecoder.Factory()) }.build() }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFF3F4F6))
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
                    Box(modifier = Modifier.fillMaxSize().background(property.imageBg), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Home, null, modifier = Modifier.size(40.dp), tint = Color.White.copy(alpha = 0.4f))
                    }
                }

                // Modern Colored Badge Pill
                Surface(
                    color = property.badgeColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart)
                ) {
                    Text(
                        text = property.badge.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                
                // Favorite Button
                Surface(
                    onClick = onLikeClick,
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .padding(12.dp)
                        .size(36.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (property.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            modifier = Modifier.size(18.dp),
                            tint = if (property.isLiked) Color.Red else Color(0xFF9CA3AF)
                        )
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        AutoResizingText(
                            text = property.price,
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF4B4EFC)
                        )
                        Text(
                            text = "/mo",
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFB800), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = property.rating, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1F2937))
                        Text(text = " (${property.reviews})", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                AutoResizingText(
                    text = property.name,
                    style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF1F2937),
                    modifier = Modifier.fillMaxWidth()
                )
                
                AutoResizingText(
                    text = "${property.areaName}, ${property.city}",
                    style = TextStyle(fontSize = 13.sp),
                    color = Color(0xFF6B7280),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF3F4F6))
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PropertyFeatureItem(Icons.Outlined.Bed, property.bhk, Modifier.weight(1f))
                    PropertyFeatureItem(Icons.Outlined.SquareFoot, property.size, Modifier.weight(1f))
                    PropertyFeatureItem(Icons.Outlined.Chair, property.furnishing.split(" ").firstOrNull() ?: property.furnishing, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun PropertyFeatureItem(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        AutoResizingText(
            text = label,
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
            color = Color(0xFF6B7280),
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSearchBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    selectedPropertyTypes: Set<String>,
    onPropertyTypeToggle: (String) -> Unit,
    priceRange: ClosedFloatingPointRange<Float>,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    selectedBhkConfigs: Set<String>,
    onBhkToggle: (String) -> Unit,
    selectedFurnishings: Set<String>,
    onFurnishingToggle: (String) -> Unit,
    selectedAvailabilities: Set<String>,
    onAvailabilityToggle: (String) -> Unit,
    selectedAmenities: Set<String>,
    onAmenityToggle: (String) -> Unit,
    minRating: String,
    onRatingChange: (String) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFE5E7EB)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F2937)
                )
                TextButton(onClick = onReset) {
                    Text("Reset all", color = Color(0xFF4B4EFC), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                FilterSectionHeader("Property Type")
                val propertyTypes = listOf("All", "Apartment", "Villa", "Studio", "PG / Hostel", "Commercial")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    propertyTypes.forEach { type ->
                        FilterChipItem(
                            label = type,
                            isSelected = selectedPropertyTypes.contains(type),
                            onClick = { onPropertyTypeToggle(type) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                FilterSectionHeader("Price Range (Monthly)")
                Text(
                    text = "₹${priceRange.start.toInt()} - ₹${priceRange.endInclusive.toInt()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B4EFC)
                )
                RangeSlider(
                    value = priceRange,
                    onValueChange = onPriceRangeChange,
                    valueRange = 5000f..150000f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF4B4EFC),
                        activeTrackColor = Color(0xFF4B4EFC),
                        inactiveTrackColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                FilterSectionHeader("BHK Configuration")
                val bhkConfigs = listOf("1 BHK", "2 BHK", "3 BHK", "4+ BHK")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bhkConfigs.forEach { bhk ->
                        FilterChipItem(
                            label = bhk,
                            isSelected = selectedBhkConfigs.contains(bhk),
                            onClick = { onBhkToggle(bhk) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                FilterSectionHeader("Furnishing")
                val furnishings = listOf("Any", "Unfurnished", "Semi-furnished", "Fully-furnished")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    furnishings.forEach { f ->
                        FilterChipItem(
                            label = f,
                            isSelected = selectedFurnishings.contains(f),
                            onClick = { onFurnishingToggle(f) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                FilterSectionHeader("Availability")
                val availabilities = listOf("Immediate", "Within 15 days", "Within 1 month")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availabilities.forEach { a ->
                        FilterChipItem(
                            label = a,
                            isSelected = selectedAvailabilities.contains(a),
                            onClick = { onAvailabilityToggle(a) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                FilterSectionHeader("Popular Amenities")
                val amenitiesList = listOf("Parking", "WiFi", "AC", "Gym", "Power Backup", "Security", "Swimming Pool")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    amenitiesList.forEach { am ->
                        FilterChipItem(
                            label = am,
                            isSelected = selectedAmenities.contains(am),
                            onClick = { onAmenityToggle(am) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                FilterSectionHeader("Minimum Rating")
                val ratings = listOf("Any", "★ 3+", "★ 4+", "★ 4.5+")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ratings.forEach { r ->
                        FilterChipItem(
                            label = r,
                            isSelected = minRating == r,
                            onClick = { onRatingChange(r) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B4EFC))
            ) {
                Text("Apply Filters", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FilterSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF374151),
        modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
    )
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color(0xFFEEF2FF) else Color.White,
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF4B4EFC) else Color(0xFFE5E7EB)),
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = label,
                color = if (isSelected) Color(0xFF4B4EFC) else Color(0xFF6B7280),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() { 
    MyRentTheme {
        HomeScreen() 
    }
}
