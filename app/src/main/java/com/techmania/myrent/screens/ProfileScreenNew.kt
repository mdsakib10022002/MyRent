package com.techmania.myrent.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// ─────────────────────────────────────────────
//  DATA MODELS
// ─────────────────────────────────────────────

data class UserProfile(
    val name: String = "Rahul Sharma",
    val email: String = "rahul.sharma@email.com",
    val phone: String = "+91 98765 43210",
    val city: String = "New Delhi, India",
    val occupation: String = "Software Engineer",
    val rating: Float = 4.9f,
    val memberSince: String = "2024",
    val propertiesViewed: Int = 3,
    val activeBookings: Int = 2,
    val leasesEnded: Int = 1,
    val profileImage: Any? = null // Can be Uri or Bitmap or String URL
)

data class BookingItem(
    val name: String,
    val location: String,
    val price: String,
    val status: BookingStatus,
    val colorSeed: Int
)

enum class ProfileTab { INFO, BOOKINGS, SETTINGS }

// ─────────────────────────────────────────────
//  COLORS
// ─────────────────────────────────────────────
private val Navy        = Color(0xFF1A1A2E)
private val Indigo      = Color(0xFF4F46E5)
private val IndigoLight = Color(0xFFEDE9FE)
private val IndigoText  = Color(0xFF3730A3)
private val GreenBg     = Color(0xFFDCFCE7)
private val GreenText   = Color(0xFF15803D)
private val AmberBg     = Color(0xFFFEF3C7)
private val AmberText   = Color(0xFFB45309)
private val BlueBg      = Color(0xFFDBEAFE)
private val BlueText    = Color(0xFF1D4ED8)
private val PinkBg      = Color(0xFFFCE7F3)
private val PinkText    = Color(0xFFBE185D)
private val RedText     = Color(0xFFDC2626)
private val RedBorder   = Color(0xFFFCA5A5)
private val RedBg       = Color(0xFFFEF2F2)
private val Surface     = Color(0xFFF9FAFB)
private val BorderColor = Color(0xFFE5E7EB)
private val TextPrimary = Color(0xFF111827)
private val TextSecond  = Color(0xFF6B7280)
private val TextTertiary= Color(0xFF9CA3AF)

// ─────────────────────────────────────────────
//  ROOT SCREEN
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenNew(
    profile: UserProfile = UserProfile(),
    isLandlord: Boolean = false,
    onEditClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onNavClick: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(ProfileTab.INFO) }
    var showPhotoOptions by remember { mutableStateOf(false) }

    val isPreview = LocalInspectionMode.current
    val auth = remember { if (isPreview) null else FirebaseAuth.getInstance() }
    val database = remember { if (isPreview) null else FirebaseDatabase.getInstance() }

    var profileState by remember { mutableStateOf(profile) }
    var isLoading by remember { mutableStateOf(!isPreview) }

    LaunchedEffect(auth?.currentUser?.uid) {
        val uid = auth?.currentUser?.uid
        if (uid != null && database != null) {
            val dbPath = if (isLandlord) "Landlords" else "Tenants"
            database.getReference(dbPath).child(uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val fetchedName = snapshot.child("fullName").value?.toString() ?: profileState.name
                    val fetchedEmail = snapshot.child(if (isLandlord) "email" else "emailAddress").value?.toString() 
                        ?: snapshot.child("email").value?.toString() ?: profileState.email
                    val fetchedPhone = snapshot.child(if (isLandlord) "mobile" else "mobileNumber").value?.toString() 
                        ?: snapshot.child("mobile").value?.toString() ?: profileState.phone
                    val fetchedCity = snapshot.child("city").value?.toString() ?: profileState.city
                    
                    profileState = profileState.copy(
                        name = fetchedName,
                        email = fetchedEmail,
                        phone = fetchedPhone,
                        city = fetchedCity
                    )
                }
                isLoading = false
            }.addOnFailureListener {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }
    
    // State to hold the current profile image (could be updated via gallery/camera)
    var currentImage by remember { mutableStateOf<Any?>(profile.profileImage) }

    // Launcher for Gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImage = uri
        }
        showPhotoOptions = false
    }

    // Launcher for Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            currentImage = bitmap
        }
        showPhotoOptions = false
    }

    val bookings = listOf(
        BookingItem("Sunshine Apartments","Dwarka Sector 10, Delhi","₹22,000", BookingStatus.ACTIVE, 0),
        BookingItem("Green View Villa","Vasant Kunj, Delhi","₹45,000", BookingStatus.VISIT_SET, 1),
        BookingItem("Laxmi Nagar Studio","East Delhi","₹12,000", BookingStatus.COMPLETED, 2)
    )

    Scaffold(
        containerColor = Surface,
        bottomBar = { 
            AppBottomNavigation(
                currentScreen = "Profile",
                isLandlord = isLandlord,
                onNavClick = onNavClick
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Indigo)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── Hero banner
                    ProfileHero(
                        profile = profileState.copy(profileImage = currentImage), 
                        onEditClick = onEditClick,
                        onAvatarClick = { showPhotoOptions = true }
                    )

                    // ── Stat cards
                    StatCardsRow(profileState)

                    // ── Tabs
                    ProfileTabRow(selected = selectedTab, onSelect = { selectedTab = it })

                    // ── Panel content
                    when (selectedTab) {
                        ProfileTab.INFO     -> InfoPanel(profileState, onLogoutClick)
                        ProfileTab.BOOKINGS -> BookingsPanel(bookings)
                        ProfileTab.SETTINGS -> SettingsPanel(onLogoutClick)
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }

            if (showPhotoOptions) {
                ModalBottomSheet(
                    onDismissRequest = { showPhotoOptions = false },
                    sheetState = rememberModalBottomSheetState(),
                    containerColor = Color.White,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp, top = 8.dp)
                    ) {
                        Text(
                            text = "Change Profile Photo",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        ListItem(
                            headlineContent = { Text("Take Photo", fontWeight = FontWeight.Medium) },
                            leadingContent = { 
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(IndigoLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = Indigo, modifier = Modifier.size(20.dp)) 
                                }
                            },
                            modifier = Modifier.clickable { cameraLauncher.launch() }
                        )
                        
                        ListItem(
                            headlineContent = { Text("Choose from Gallery", fontWeight = FontWeight.Medium) },
                            leadingContent = { 
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(BlueBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Image, contentDescription = null, tint = BlueText, modifier = Modifier.size(20.dp)) 
                                }
                            },
                            modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  HERO BANNER
// ─────────────────────────────────────────────

@Composable
fun ProfileHero(profile: UserProfile, onEditClick: () -> Unit, onAvatarClick: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Navy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "My Profile",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedButton(
                    onClick = onEditClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.12f),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(0.dp, Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("Edit profile", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(20.dp))

            // avatar
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onAvatarClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.profileImage != null) {
                        AsyncImage(
                            model = profile.profileImage,
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = profile.name
                                .split(" ")
                                .filter { it.isNotBlank() }
                                .take(2)
                                .joinToString("") { it.first().uppercase() },
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Indigo)
                        .border(2.dp, Navy, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.PhotoCamera,
                        contentDescription = "Change photo",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                profile.name,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(3.dp))
            Text(
                profile.email,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(14.dp))

            // badges row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroBadge(
                    icon = Icons.Outlined.CheckCircle,
                    label = "Verified tenant",
                    iconTint = Color(0xFF86EFAC)
                )
                HeroBadge(
                    icon = Icons.Outlined.Star,
                    label = "${profile.rating} rating",
                    iconTint = Color(0xFFFCD34D)
                )
                HeroBadge(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "Since ${profile.memberSince}",
                    iconTint = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun HeroBadge(icon: ImageVector, label: String, iconTint: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(13.dp))
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────
//  STAT CARDS
// ─────────────────────────────────────────────

@Composable
fun StatCardsRow(profile: UserProfile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(
            profile.propertiesViewed to "Properties\nviewed",
            profile.activeBookings   to "Active\nbookings",
            profile.leasesEnded      to "Leases\nended"
        ).forEach { (num, label) ->
            StatCard(number = num.toString(), label = label, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(number: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            number,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 11.sp,
            color = TextSecond,
            lineHeight = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────
//  TAB ROW
// ─────────────────────────────────────────────

@Composable
fun ProfileTabRow(selected: ProfileTab, onSelect: (ProfileTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(
                width = 0.5.dp,
                color = BorderColor,
                shape = RectangleShape
            )
    ) {
        ProfileTab.entries.forEach { tab ->
            val isOn = tab == selected
            val indicatorColor by animateColorAsState(
                if (isOn) Indigo else Color.Transparent,
                animationSpec = tween(200),
                label = "tab_indicator"
            )
            val textColor by animateColorAsState(
                if (isOn) Indigo else TextSecond,
                animationSpec = tween(200),
                label = "tab_text"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(tab) }
                    .drawBehind {
                        // bottom indicator line
                        drawRect(
                            color = indicatorColor,
                            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - 2.5f),
                            size = androidx.compose.ui.geometry.Size(size.width, 2.5f)
                        )
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    tab.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  INFO PANEL
// ─────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InfoPanel(profile: UserProfile, onLogout: () -> Unit) {
    val isPreview = LocalInspectionMode.current
    val auth = if (isPreview) null else FirebaseAuth.getInstance()
    Column {
        // Personal details
        SectionHeader("Personal details")
        InfoCard {
            InfoRow(icon = Icons.Outlined.Person,      iconBg = IndigoLight, iconTint = IndigoText, label = "Full name",       value = profile.name)
            InfoRowDivider()
            InfoRow(icon = Icons.Outlined.Phone,       iconBg = BlueBg,     iconTint = BlueText,   label = "Mobile number",   value = profile.phone)
            InfoRowDivider()
            InfoRow(icon = Icons.Outlined.Mail,        iconBg = PinkBg,     iconTint = PinkText,   label = "Email address",   value = profile.email)
            InfoRowDivider()
            InfoRow(icon = Icons.Outlined.LocationOn,  iconBg = GreenBg,    iconTint = GreenText,  label = "Current city",    value = profile.city)
            InfoRowDivider()
            InfoRow(icon = Icons.Outlined.Work,        iconBg = AmberBg,    iconTint = AmberText,  label = "Occupation",      value = profile.occupation)
        }

        // Rental preferences
        SectionHeader("Rental preferences")
        InfoCard {
            InfoRow(icon = Icons.Outlined.Apartment,   iconBg = IndigoLight, iconTint = IndigoText, label = "Property type",  value = "Apartment, Studio")
            InfoRowDivider()
            InfoRow(icon = Icons.Outlined.CurrencyRupee, iconBg = BlueBg,   iconTint = BlueText,   label = "Budget range",   value = "₹10,000 – ₹35,000 / mo")
            InfoRowDivider()
            // amenity chips
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text("AMENITIES PREFERRED", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecond, letterSpacing = 0.07.sp)
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    listOf("Parking" to true, "WiFi" to true, "AC" to true, "Gym" to false, "Lift" to true, "Pool" to false)
                        .forEach { (label, active) ->
                            AmenityChip(label = label, active = active)
                        }
                }
            }
        }

        // Documents
        SectionHeader("Documents")
        InfoCard {
            DocumentRow(label = "Aadhaar card", status = "Verified", statusColor = GreenText, statusBg = GreenBg, icon = Icons.Outlined.Badge)
            InfoRowDivider()
            DocumentRow(label = "PAN card",     status = "Verified", statusColor = GreenText, statusBg = GreenBg, icon = Icons.Outlined.CreditCard)
            InfoRowDivider()
            DocumentRow(label = "Salary slip / ITR", status = "Pending", statusColor = AmberText, statusBg = AmberBg, icon = Icons.Outlined.Description)
        }
        Spacer(Modifier.height(8.dp))
        // Logout Button
        Button(
            onClick = {
                auth?.signOut()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE))
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", color = Color.Red, fontWeight = FontWeight.Bold)
        }


    }
}

// ─────────────────────────────────────────────
//  BOOKINGS PANEL
// ─────────────────────────────────────────────

@Composable
fun BookingsPanel(bookings: List<BookingItem>) {
    val active = bookings.filter { it.status != BookingStatus.COMPLETED && it.status != BookingStatus.CANCELLED }
    val past   = bookings.filter { it.status == BookingStatus.COMPLETED || it.status == BookingStatus.CANCELLED }

    if (active.isNotEmpty()) {
        SectionHeader("Active bookings")
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            active.forEach { BookingCard(it) }
        }
    }

    if (past.isNotEmpty()) {
        SectionHeader("Past bookings")
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .alpha(0.6f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            past.forEach { BookingCard(it) }
        }
    }
}

@Composable
fun BookingCard(item: BookingItem) {
    val thumbColors = listOf(
        Color(0xFFDCE8F8) to Color(0xFF6680AA),
        Color(0xFFD4EEE0) to Color(0xFF5A9E7A),
        Color(0xFFFCE7F3) to Color(0xFFC08868)
    )
    val (thumbBg, houseColor) = thumbColors[item.colorSeed % thumbColors.size]

    val (statusBg, statusColor, statusText) = when (item.status) {
        BookingStatus.ACTIVE    -> Triple(GreenBg,   GreenText,  "Confirmed")
        BookingStatus.VISIT_SET -> Triple(AmberBg,   AmberText,  "Visit scheduled")
        BookingStatus.PENDING   -> Triple(AmberBg,   AmberText,  "Pending")
        BookingStatus.COMPLETED -> Triple(Surface,   TextSecond, "Lease ended")
        BookingStatus.CANCELLED -> Triple(Surface,   TextSecond, "Cancelled")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // thumbnail
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(thumbBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Home,
                contentDescription = null,
                tint = houseColor.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.location, fontSize = 11.5.sp, color = TextSecond, maxLines = 1)
            Spacer(Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(statusBg)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(statusText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(item.price, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("/month", fontSize = 11.sp, color = TextSecond)
        }
    }
}

// ─────────────────────────────────────────────
//  SETTINGS PANEL
// ─────────────────────────────────────────────

@Composable
fun SettingsPanel(onLogoutClick: () -> Unit) {
    // Account section
    SectionHeader("Account")
    SettingsCard {
        SettingsRow(
            icon = Icons.Outlined.Lock,
            iconBg = IndigoLight, iconTint = IndigoText,
            title = "Change password",
            subtitle = "Update your login password",
            action = { Icon(Icons.Outlined.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(18.dp)) }
        )
        InfoRowDivider()
        var twoFaOn by remember { mutableStateOf(true) }
        SettingsRow(
            icon = Icons.Outlined.Shield,
            iconBg = BlueBg, iconTint = BlueText,
            title = "Two-factor auth",
            subtitle = "Extra layer of security",
            action = { NestToggle(checked = twoFaOn, onToggle = { twoFaOn = it }) }
        )
        InfoRowDivider()
        SettingsRow(
            icon = Icons.Outlined.Badge,
            iconBg = GreenBg, iconTint = GreenText,
            title = "KYC verification",
            subtitle = "Identity documents",
            action = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(GreenBg)
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) { Text("Verified", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GreenText) }
            }
        )
    }

    // Notifications section
    SectionHeader("Notifications")
    SettingsCard {
        var pushOn  by remember { mutableStateOf(true) }
        var emailOn by remember { mutableStateOf(false) }
        var smsOn   by remember { mutableStateOf(true) }

        SettingsRow(
            icon = Icons.Outlined.Notifications,
            iconBg = AmberBg, iconTint = AmberText,
            title = "Push notifications",
            subtitle = "New listings, rent reminders",
            action = { NestToggle(checked = pushOn, onToggle = { pushOn = it }) }
        )
        InfoRowDivider()
        SettingsRow(
            icon = Icons.Outlined.Mail,
            iconBg = PinkBg, iconTint = PinkText,
            title = "Email alerts",
            subtitle = "Booking confirmations",
            action = { NestToggle(checked = emailOn, onToggle = { emailOn = it }) }
        )
        InfoRowDivider()
        SettingsRow(
            icon = Icons.AutoMirrored.Outlined.Message,
            iconBg = BlueBg, iconTint = BlueText,
            title = "SMS alerts",
            subtitle = "OTP and transaction SMS",
            action = { NestToggle(checked = smsOn, onToggle = { smsOn = it }) }
        )
    }

    // Support section
    SectionHeader("Support")
    SettingsCard {
        SettingsRow(
            icon = Icons.Outlined.Headset,
            iconBg = IndigoLight, iconTint = IndigoText,
            title = "Help & support",
            subtitle = "FAQs and chat with us",
            action = { Icon(Icons.Outlined.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(18.dp)) }
        )
        InfoRowDivider()
        SettingsRow(
            icon = Icons.Outlined.Star,
            iconBg = Surface, iconTint = TextSecond,
            title = "Rate the app",
            subtitle = "Share your feedback",
            action = { Icon(Icons.Outlined.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(18.dp)) }
        )
        InfoRowDivider()
        SettingsRow(
            icon = Icons.Outlined.Description,
            iconBg = Surface, iconTint = TextSecond,
            title = "Terms & privacy",
            subtitle = "Legal information",
            action = { Icon(Icons.Outlined.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(18.dp)) }
        )
    }

    // Logout button
    Box(modifier = Modifier.padding(16.dp)) {
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = RedBg,
                contentColor   = RedText
            ),
            border = BorderStroke(0.5.dp, RedBorder),
            shape  = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 13.dp)
        ) {
            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Log out", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─────────────────────────────────────────────
//  REUSABLE SMALL COMPOSABLES
// ─────────────────────────────────────────────

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 10.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecond,
        letterSpacing = 0.07.sp
    )
}

@Composable
fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp)),
        content = content
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) = InfoCard(content)

@Composable
fun InfoRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 14.dp),
        thickness = 0.5.dp,
        color = BorderColor
    )
}

@Composable
fun InfoRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label.uppercase(), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = TextSecond, letterSpacing = 0.07.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun DocumentRow(
    label: String,
    status: String,
    statusColor: Color,
    statusBg: Color,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AmberBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AmberText, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label.uppercase(), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = TextSecond, letterSpacing = 0.07.sp)
            Spacer(Modifier.height(2.dp))
            Text(status, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = statusColor)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(statusBg)
                .padding(horizontal = 9.dp, vertical = 3.dp)
        ) {
            Text(
                if (status == "Verified") "✓" else "Upload",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title,    fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(subtitle, fontSize = 11.5.sp, color = TextSecond)
        }
        action()
    }
}

@Composable
fun AmenityChip(label: String, active: Boolean) {
    val bg     = if (active) IndigoLight else Surface
    val border = if (active) Color(0xFFC4B5FD) else BorderColor
    val text   = if (active) IndigoText else TextSecond

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(0.5.dp, border, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = text)
    }
}

@Composable
fun NestToggle(checked: Boolean, onToggle: (Boolean) -> Unit) {
    val bg by animateColorAsState(
        targetValue = if (checked) Indigo else BorderColor,
        animationSpec = tween(200),
        label = "toggle_bg"
    )
    Box(
        modifier = Modifier
            .size(width = 38.dp, height = 22.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(bg)
            .clickable { onToggle(!checked) },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

// ─────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ProfileScreenNewPreview() {
    MaterialTheme {
        ProfileScreenNew()
    }
}
