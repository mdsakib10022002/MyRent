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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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

    // Applied Filter State
    var appliedFilters by remember { mutableStateOf(FilterParams()) }

    // Temp states for bottom sheet
    var tempPropertyTypes by remember { mutableStateOf(appliedFilters.propertyTypes) }
    var tempPriceRange by remember { mutableStateOf(appliedFilters.priceRange) }
    var tempBhkConfigs by remember { mutableStateOf(appliedFilters.bhkConfigs) }
    var tempFurnishings by remember { mutableStateOf(appliedFilters.furnishings) }
    var tempAvailabilities by remember { mutableStateOf(appliedFilters.availabilities) }
    var tempAmenities by remember { mutableStateOf(appliedFilters.amenities) }
    var tempMinRating by remember { mutableStateOf(appliedFilters.minRating) }

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
        containerColor = Color(0xFFF8F9FB)
    ) { innerPadding ->
        // Use Box with Alignment.TopCenter and widthIn for adaptive layout
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
                    .widthIn(max = 800.dp) // Wider for home screen compared to login
            ) {
                item { HeaderGreetingSection(userName) }
                
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
                
                item {
                    FeaturedListingsSection(
                        properties = propertyViewModel.properties,
                        searchQuery = searchQuery,
                        filters = appliedFilters,
                        onPropertyClick = onPropertyClick,
                        onLikeClick = { propertyViewModel.toggleLike(it) }
                    )
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141414))
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 28.dp, bottom = 12.dp)
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
                    text = "Find your perfect\nrental home",
                    color = Color(0xFFFBF4D0),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 44.sp
                )
            }
            Surface(
                shape = CircleShape,
                color = Color(0xFF26262D),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
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
        color = Color(0xFF141414),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 24.dp)
                .height(60.dp)
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
                Icon(Icons.Default.Search, null, modifier = Modifier.size(24.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (searchQuery.isEmpty()) {
                        Text("Search area, city...", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onQueryChange,
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
                    .widthIn(min = 110.dp)
                    .background(Color(0xFF4B4EFC))
                    .clickable { onFilterClick() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FilterList, null, modifier = Modifier.size(20.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Filter", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
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
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.widthIn(max = 600.dp).padding(horizontal = 24.dp, vertical = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Filter Search", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                }
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), color = Color(0xFFF2F2F2))
                
                Column(modifier = Modifier.weight(1f, false).verticalScroll(rememberScrollState())) {
                    FilterSectionHeader("PROPERTY TYPE")
                    FlowRow(modifier = Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Arrangement.spacedBy(10.dp)) {
                        listOf("All", "Apartment", "Villa", "Studio", "PG / Hostel", "Commercial").forEach { 
                            FilterChipItem(it, selectedPropertyTypes.contains(it)) { onPropertyTypeToggle(it) }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    FilterSectionHeader("MONTHLY RENT (₹)")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("₹${priceRange.start.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("₹${priceRange.endInclusive.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    RangeSlider(
                        value = priceRange, 
                        onValueChange = onPriceRangeChange, 
                        valueRange = 5000f..100000f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4B4EFC),
                            activeTrackColor = Color(0xFF4B4EFC),
                            inactiveTrackColor = Color(0xFFE0E0E0)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    FilterSectionHeader("BHK CONFIGURATION")
                    FlowRow(modifier = Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Arrangement.spacedBy(10.dp)) {
                        listOf("1 BHK", "2 BHK", "3 BHK", "4+ BHK").forEach { 
                            FilterChipItem(it, selectedBhkConfigs.contains(it)) { onBhkToggle(it) }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    FilterSectionHeader("FURNISHING")
                    FlowRow(modifier = Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Arrangement.spacedBy(10.dp)) {
                        listOf("Any", "Fully Furnished", "Semi-Furnished", "Unfurnished").forEach { 
                            FilterChipItem(it, selectedFurnishings.contains(it)) { onFurnishingToggle(it) }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    FilterSectionHeader("AVAILABILITY")
                    FlowRow(modifier = Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Arrangement.spacedBy(10.dp)) {
                        listOf("Immediate", "Within 15 days", "Within 1 month").forEach { 
                            FilterChipItem(it, selectedAvailabilities.contains(it)) { onAvailabilityToggle(it) }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    FilterSectionHeader("AMENITIES")
                    FlowRow(modifier = Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Arrangement.spacedBy(10.dp)) {
                        listOf("Parking", "WiFi", "Gym", "Pool", "AC", "Lift", "Security", "Power backup").forEach { 
                            FilterChipItem(it, selectedAmenities.contains(it)) { onAmenityToggle(it) }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    FilterSectionHeader("MINIMUM RATING")
                    FlowRow(modifier = Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Arrangement.spacedBy(10.dp)) {
                        listOf("Any", "★ 1+", "★ 2+", "★ 3+", "★ 4+", "★ 4.5+").forEach {
                            FilterChipItem(it, minRating == it) { onRatingChange(it) }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                HorizontalDivider(color = Color(0xFFF2F2F2), modifier = Modifier.padding(bottom = 16.dp))
                
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = onReset, 
                        modifier = Modifier.weight(0.8f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) { 
                        Text("Reset all", color = Color.Gray, fontWeight = FontWeight.Bold) 
                    }
                    Button(
                        onClick = onApply, 
                        modifier = Modifier.weight(1.2f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B4EFC))
                    ) { 
                        Text("Apply filters", color = Color.White, fontWeight = FontWeight.Bold) 
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSectionHeader(title: String) {
    Text(
        text = title, 
        fontSize = 11.sp, 
        fontWeight = FontWeight.Bold, 
        color = Color.Gray, 
        letterSpacing = 0.5.sp, 
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun FilterChipItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) Color(0xFF4B4EFC) else Color.Transparent,
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFF2F2F2)),
        modifier = Modifier.height(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = label, 
                color = if (isSelected) Color.White else Color.Black.copy(0.6f), 
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun CategorySection(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("All", "Apartment", "Villa", "Studio", "PG / Hostel", "Commercial")
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp), Arrangement.spacedBy(10.dp)) {
        categories.forEach { category ->
            val isSelected = selectedCategory == category
            Surface(
                onClick = { onCategorySelected(category) },
                shape = RoundedCornerShape(22.dp),
                color = if (isSelected) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
                modifier = Modifier.height(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(category, color = if (isSelected) Color.White else Color.Black.copy(0.6f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FeaturedListingsSection(properties: List<Property>, searchQuery: String, filters: FilterParams, onPropertyClick: (String) -> Unit, onLikeClick: (Property) -> Unit) {
    val filtered = properties.filter { p ->
        p.name.contains(searchQuery, true) && (filters.propertyTypes.contains("All") || filters.propertyTypes.contains(p.type))
    }
    Column(Modifier.padding(horizontal = 24.dp)) {
        Text("Featured listings", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(18.dp))
        filtered.forEach { PropertyCard(it, { onPropertyClick(it.name) }, { onLikeClick(it) }); Spacer(Modifier.height(24.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyCard(property: Property, onClick: () -> Unit, onLikeClick: () -> Unit = {}) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFFF0F0F2), RoundedCornerShape(28.dp))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(Color(0xFFE8E8EC))
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(property.imageBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                // Available Badge
                Surface(
                    color = property.badgeColor,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                ) {
                    Text(
                        text = property.badge,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
                
                // Heart Icon
                Surface(
                    onClick = onLikeClick,
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (property.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            modifier = Modifier.size(20.dp),
                            tint = if (property.isLiked) Color.Red else Color.Red.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // Video Tour Button
                if (property.mediaUris.any { it.endsWith(".mp4") || it.contains("video") }) {
                    Surface(
                        color = Color(0xFF1C1C1E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Video tour",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = property.price,
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = " / month",
                            color = Color(0xFF8E8E93),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFCC00),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = property.rating,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                        Text(
                            text = " (${property.reviews})",
                            color = Color(0xFF8E8E93),
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = property.name,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = "${property.areaName}, ${property.city} - ${property.pincode}",
                    color = Color(0xFF8E8E93),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                Spacer(modifier = Modifier.height(18.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PropertyFeatureItem(Icons.Outlined.Bed, property.bhk)
                    PropertyFeatureItem(Icons.Outlined.SquareFoot, property.size)
                    PropertyFeatureItem(Icons.Outlined.Chair, property.furnishing)
                }
            }
        }
    }
}

@Composable
fun PropertyFeatureItem(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF8E8E93),
            fontWeight = FontWeight.Medium
        )
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
