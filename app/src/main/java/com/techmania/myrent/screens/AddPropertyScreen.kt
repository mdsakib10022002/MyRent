package com.techmania.myrent.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.techmania.myrent.ui.theme.MyRentTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddPropertyScreen(
    userName: String = "Rahul",
    propertyViewModel: PropertyViewModel,
    onBackClick: () -> Unit = {},
    onPublishClick: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Apartment") }
    var selectedBhk by remember { mutableStateOf("1 BHK") }
    var area by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var rent by remember { mutableStateOf("") }
    
    val propertyTypes = listOf("Apartment", "Villa", "Studio", "PG / Hostel", "Commercial")
    val bhkConfigs = listOf("1 BHK", "2 BHK", "3 BHK", "4+ BHK")
    val amenitiesList = listOf("Parking", "WiFi", "AC", "Gym", "Lift", "Security", "Water 24/7", "Power backup")
    
    var selectedAmenities by remember { mutableStateOf(setOf("Parking", "WiFi", "AC", "Water 24/7")) }

    // Media states
    var selectedMedia by remember { mutableStateOf(listOf<Uri>()) }
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    
    // Coil ImageLoader with Video Support for Thumbnails
    val imageLoader = remember {
        if (isPreview) null else {
            ImageLoader.Builder(context)
                .components {
                    add(VideoFrameDecoder.Factory())
                }
                .build()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            selectedMedia = (selectedMedia + uris).distinct()
        }
    )

    val darkBlue = Color(0xFF141414)
    val primaryBlue = Color(0xFF4B4EFC)
    val fieldBackground = Color(0xFFF7F7F2)
    val borderColor = Color(0xFFE0E0E0)

    Scaffold(
        containerColor = Color(0xFFF8F9FB),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 600.dp) // Optimized for tablet
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(darkBlue)
                        .statusBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column {
                        Text(
                            text = "Good morning, $userName 👋",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "List your property",
                            color = Color(0xFFFBF4D0),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 28.sp
                        )
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = primaryBlue,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = primaryBlue,
                                height = 2.dp
                            )
                        }
                    },
                    divider = { }
                ) {
                    val tabs = listOf("Basic Info", "Photos/Video", "Pricing")
                    tabs.forEachIndexed { index, tabTitle ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    text = tabTitle, 
                                    fontSize = 15.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) primaryBlue else Color.Gray
                                ) 
                            }
                        )
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    when (selectedTab) {
                        0 -> { // Basic Info
                            InputFieldLabel("PROPERTY TITLE")
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = { Text("e.g. Spacious 2BHK in Noida Sector 62", fontSize = 14.sp, color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = borderColor,
                                    unfocusedBorderColor = borderColor,
                                    focusedContainerColor = fieldBackground,
                                    unfocusedContainerColor = fieldBackground
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    InputFieldLabel("TYPE")
                                    DropDownField(selectedType, propertyTypes, fieldBackground, borderColor) { selectedType = it }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    InputFieldLabel("BHK")
                                    DropDownField(selectedBhk, bhkConfigs, fieldBackground, borderColor) { selectedBhk = it }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    InputFieldLabel("AREA (SQ FT)")
                                    OutlinedTextField(
                                        value = area,
                                        onValueChange = { area = it },
                                        placeholder = { Text("e.g. 1200", fontSize = 14.sp, color = Color.Gray) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = borderColor,
                                            unfocusedBorderColor = borderColor,
                                            focusedContainerColor = fieldBackground,
                                            unfocusedContainerColor = fieldBackground
                                        )
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    InputFieldLabel("FLOOR NUMBER")
                                    OutlinedTextField(
                                        value = floor,
                                        onValueChange = { floor = it },
                                        placeholder = { Text("e.g. 2nd", fontSize = 14.sp, color = Color.Gray) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = borderColor,
                                            unfocusedBorderColor = borderColor,
                                            focusedContainerColor = fieldBackground,
                                            unfocusedContainerColor = fieldBackground
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            InputFieldLabel("FULL ADDRESS")
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                placeholder = { Text("Street, locality, city, pincode", fontSize = 14.sp, color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = borderColor,
                                    unfocusedBorderColor = borderColor,
                                    focusedContainerColor = fieldBackground,
                                    unfocusedContainerColor = fieldBackground
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            InputFieldLabel("DESCRIPTION")
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = { Text("Write something about your property...", fontSize = 14.sp, color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = borderColor,
                                    unfocusedBorderColor = borderColor,
                                    focusedContainerColor = fieldBackground,
                                    unfocusedContainerColor = fieldBackground
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            InputFieldLabel("AMENITIES")
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                amenitiesList.forEach { amenity ->
                                    FilterChip(
                                        selected = selectedAmenities.contains(amenity),
                                        onClick = {
                                            selectedAmenities = if (selectedAmenities.contains(amenity)) {
                                                selectedAmenities - amenity
                                            } else {
                                                selectedAmenities + amenity
                                            }
                                        },
                                        label = { Text(amenity) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = primaryBlue,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                        1 -> { // Photos/Video
                            InputFieldLabel("PROPERTY PHOTOS")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(Color(0xFFF9FAF7), RoundedCornerShape(12.dp))
                                    .drawBehind {
                                        drawRoundRect(
                                            color = Color(0xFFD1D1D1),
                                            cornerRadius = CornerRadius(12.dp.toPx()),
                                            style = Stroke(
                                                width = 1.dp.toPx(),
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                            )
                                        )
                                    }
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFE8F3F1),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = null,
                                                tint = Color(0xFF0F6257),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Tap to upload property photos",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "JPG, PNG · Max 10 photos · 5MB each",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val photos = selectedMedia.filter { uri ->
                                context.contentResolver.getType(uri)?.startsWith("image") == true
                            }
                            
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(photos) { uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(fieldBackground)
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.Black.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(20.dp)
                                                .clickable { selectedMedia = selectedMedia - uri }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                
                                item {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .drawBehind {
                                                drawRoundRect(
                                                    color = Color(0xFFD1D1D1),
                                                    cornerRadius = CornerRadius(12.dp.toPx()),
                                                    style = Stroke(
                                                        width = 1.dp.toPx(),
                                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                                    )
                                                )
                                            }
                                            .background(Color(0xFFF9FAF7), RoundedCornerShape(12.dp))
                                            .clickable {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            InputFieldLabel("VIDEO TOUR (optional)")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(Color(0xFFF9FAF7), RoundedCornerShape(12.dp))
                                    .drawBehind {
                                        drawRoundRect(
                                            color = Color(0xFFD1D1D1),
                                            cornerRadius = CornerRadius(12.dp.toPx()),
                                            style = Stroke(
                                                width = 1.dp.toPx(),
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                            )
                                        )
                                    }
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val video = selectedMedia.find { uri ->
                                    context.contentResolver.getType(uri)?.startsWith("video") == true
                                }
                                
                                if (video == null) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFE8F3F1),
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Videocam,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0F6257),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Upload a video walkthrough",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = "MP4 · Max 50MB",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (imageLoader != null) {
                                            AsyncImage(
                                                model = video,
                                                imageLoader = imageLoader,
                                                contentDescription = null,
                                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray))
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(text = "Video Uploaded", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        IconButton(onClick = { selectedMedia = selectedMedia - video }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                        2 -> { // Pricing
                            InputFieldLabel("MONTHLY RENT (₹)")
                            OutlinedTextField(
                                value = rent,
                                onValueChange = { rent = it },
                                placeholder = { Text("e.g. 25000", fontSize = 14.sp, color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = borderColor,
                                    unfocusedBorderColor = borderColor,
                                    focusedContainerColor = fieldBackground,
                                    unfocusedContainerColor = fieldBackground
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = {
                            if (selectedTab < 2) {
                                selectedTab++
                            } else {
                                val newProperty = Property(
                                    badge = "New",
                                    badgeColorValue = Color(0xFF3B82F6).toArgb(), // Modern Blue for New
                                    imageBgValue = Color(0xFFF7F7F2).toArgb(),
                                    price = "₹$rent",
                                    numericPrice = rent.toIntOrNull() ?: 0,
                                    name = title.ifBlank { "Untitled Property" },
                                    city = address.split(",").lastOrNull()?.trim() ?: "N/A",
                                    areaName = address.split(",").firstOrNull()?.trim() ?: address,
                                    pincode = "",
                                    rating = "0.0",
                                    numericRating = 0f,
                                    reviews = "0",
                                    bhk = selectedBhk,
                                    numericBhk = selectedBhk.split(" ").firstOrNull()?.toIntOrNull() ?: 0,
                                    size = "$area sq ft",
                                    furnishing = "Unfurnished",
                                    type = selectedType,
                                    availability = "Immediate",
                                    amenities = selectedAmenities.toList(),
                                    description = description,
                                    mediaUris = selectedMedia.map { it.toString() },
                                    addedTimestamp = System.currentTimeMillis()
                                )
                                propertyViewModel.addProperty(newProperty)
                                onPublishClick()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                    ) {
                        Text(
                            text = if (selectedTab < 2) "Continue" else "Publish Property",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedTab < 2) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InputFieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun DropDownField(
    selectedOption: String,
    options: List<String>,
    backgroundColor: Color,
    borderColor: Color,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            shape = RoundedCornerShape(10.dp),
            trailingIcon = {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.clickable { expanded = true })
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                focusedContainerColor = backgroundColor,
                unfocusedContainerColor = backgroundColor
            )
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(showBackground = true)
@Composable
fun AddPropertyScreenPreview() {
    MyRentTheme {
        val mockViewModel = PropertyViewModel()
        AddPropertyScreen(
            userName = "Rahul",
            propertyViewModel = mockViewModel
        )
    }
}
