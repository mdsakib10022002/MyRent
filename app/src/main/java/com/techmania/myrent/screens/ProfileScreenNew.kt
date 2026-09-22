package com.techmania.myrent.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.techmania.myrent.ui.theme.MyRentTheme

// ─── Data Models moved to Models.kt ──────────────────────────────────────────

enum class ProfileTab { INFO, SETTINGS }

// ─── Colors ──────────────────────────────────────────────────────────────────
val Navy           = Color(0xFF1A1A2E)
val Indigo         = Color(0xFF4F46E5)
val IndigoLight    = Color(0xFFEBEBFF)
val IndigoText     = Color(0xFF4B4EFC)
val GreenBg        = Color(0xFFE6F4EA)
val GreenText      = Color(0xFF13694C)
val AmberBg        = Color(0xFFFFF7E6)
val AmberText      = Color(0xFFB45309)
val BlueBg         = Color(0xFFEBF5FF)
val BlueText       = Color(0xFF1E40AF)
val PinkBg         = Color(0xFFFDF2F8)
val PinkText       = Color(0xFF9D174D)
val RedText        = Color(0xFFDC2626)
val RedBorder      = Color(0xFFFECACA)
val RedBg          = Color(0xFFFEF2F2)
val Surface        = Color(0xFFF8F9FB)
val BorderColor    = Color(0xFFE5E7EB)
val TextPrimary    = Color(0xFF1F2937)
val TextSecond     = Color(0xFF4B5563)
val TextTertiary   = Color(0xFF9CA3AF)

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
                    
                    val fetchedVerified = snapshot.child("isVerified").value as? Boolean ?: false
                    val fetchedRating = (snapshot.child("rating").value as? Number)?.toFloat() ?: 0.0f
                    val fetchedSince = snapshot.child("memberSince").value?.toString() ?: "2024"

                    profileState = profileState.copy(
                        name = fetchedName,
                        email = fetchedEmail,
                        phone = fetchedPhone,
                        city = fetchedCity,
                        isVerified = fetchedVerified,
                        rating = fetchedRating,
                        memberSince = fetchedSince
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
                        isLandlord = isLandlord,
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
                        ProfileTab.SETTINGS -> SettingsPanel(onLogoutClick)
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }

            // Photo Option Bottom Sheet (Simplified with AlertDialog for this template)
            if (showPhotoOptions) {
                ModalBottomSheet(
                    onDismissRequest = { showPhotoOptions = false },
                    containerColor = Color.White,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = BorderColor) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    ) {
                        Text(
                            "PROFILE PHOTO",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        ListItem(
                            headlineContent = { Text("Take Photo", fontWeight = FontWeight.Medium) },
                            leadingContent = { 
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(GreenBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = GreenText, modifier = Modifier.size(20.dp)) 
                                }
                            },
                            modifier = Modifier.clickable { cameraLauncher.launch(null) }
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
fun ProfileHero(profile: UserProfile, isLandlord: Boolean, onEditClick: () -> Unit, onAvatarClick: () -> Unit) {

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
                    icon = if (profile.isVerified) Icons.Outlined.Verified else if (isLandlord) Icons.Outlined.Apartment else Icons.Outlined.Person,
                    label = if (profile.isVerified) "Verified ${if (isLandlord) "landlord" else "tenant"}" else if (isLandlord) "Landlord" else "Tenant",
                    iconTint = if (profile.isVerified) Color(0xFF86EFAC) else Color.White.copy(alpha = 0.7f)
                )
                HeroBadge(
                    icon = Icons.Outlined.Star,
                    label = "${profile.rating} rating",
                    iconTint = Color(0xFFFCD34D)
                )
                HeroBadge(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "Since ${if (profile.memberSince.isNotEmpty()) profile.memberSince else "2024"}",
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
            StatCard(count = num.toString(), label = label, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(count: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                count,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                label,
                fontSize = 11.sp,
                color = TextTertiary,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

// ─────────────────────────────────────────────
//  TABS
// ─────────────────────────────────────────────

@Composable
fun ProfileTabRow(selected: ProfileTab, onSelect: (ProfileTab) -> Unit) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            ProfileTab.entries.forEach { tab ->
                val isSelected = selected == tab
                val label = when(tab) {
                    ProfileTab.INFO     -> "Personal Info"
                    ProfileTab.SETTINGS -> "Settings"
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(tab) }
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) IndigoText else TextTertiary
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(2.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) IndigoText else Color.Transparent)
                    )
                }
            }
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = BorderColor)
}

// ─────────────────────────────────────────────
//  PANELS
// ─────────────────────────────────────────────

@Composable
fun InfoPanel(profile: UserProfile, onLogout: () -> Unit) {
    val isPreview = LocalInspectionMode.current
    val auth = if (isPreview) null else FirebaseAuth.getInstance()
    Column {
        // Personal details
        SectionHeader("Personal details")
        InfoCard {
            InfoRow(icon = Icons.Outlined.Person,      iconBg = IndigoLight, iconTint = IndigoText, label = "FULL NAME",       value = profile.name)
            InfoRowDivider()
            InfoRow(icon = Icons.Outlined.Phone,       iconBg = BlueBg,     iconTint = BlueText,   label = "MOBILE NUMBER",   value = profile.phone)
            InfoRowDivider()
            InfoRow(icon = Icons.Outlined.Mail,        iconBg = PinkBg,     iconTint = PinkText,   label = "EMAIL ADDRESS",   value = profile.email)
            InfoRowDivider()
            InfoRow(icon = Icons.Outlined.LocationOn,  iconBg = GreenBg,    iconTint = GreenText,  label = "CURRENT CITY",    value = profile.city)
            InfoRowDivider()
            InfoRow(icon = Icons.Outlined.Work,        iconBg = AmberBg,    iconTint = AmberText,  label = "OCCUPATION",      value = profile.occupation)
        }

        // Rental preferences
        SectionHeader("Rental preferences")
        InfoCard {
            InfoRow(icon = Icons.Outlined.Apartment,   iconBg = IndigoLight, iconTint = IndigoText, label = "PROPERTY TYPE",  value = "Apartment, Studio")
            InfoRowDivider()
            InfoRow(icon = Icons.Outlined.CurrencyRupee, iconBg = BlueBg,   iconTint = BlueText,   label = "BUDGET RANGE",   value = "₹10,000 – ₹35,000 / mo")
            InfoRowDivider()
            // amenity chips
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text("AMENITIES PREFERRED", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecond, letterSpacing = 0.07.sp)
                Spacer(Modifier.height(8.dp))
                @OptIn(ExperimentalLayoutApi::class)
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
            DocumentRow(label = "AADHAAR CARD", status = if (profile.isVerified) "Verified" else "Pending", statusColor = if (profile.isVerified) GreenText else AmberText, statusBg = if (profile.isVerified) GreenBg else AmberBg, icon = Icons.Outlined.Badge)
            InfoRowDivider()
            DocumentRow(label = "PAN CARD",     status = if (profile.isVerified) "Verified" else "Pending", statusColor = if (profile.isVerified) GreenText else AmberText, statusBg = if (profile.isVerified) GreenBg else AmberBg, icon = Icons.Outlined.CreditCard)
            InfoRowDivider()
            DocumentRow(label = "SALARY SLIP / ITR", status = "Pending", statusColor = AmberText, statusBg = AmberBg, icon = Icons.Outlined.Description)
        }
        Spacer(Modifier.height(16.dp))
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


@Composable
fun SettingsPanel(onLogout: () -> Unit) {
    var notificationsOn by remember { mutableStateOf(true) }
    var twoFaOn by remember { mutableStateOf(false) }

    Column {
        SectionHeader("Account settings")
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.Notifications,
                iconBg = IndigoLight,
                iconTint = IndigoText,
                title = "Push notifications",
                subtitle = "Alerts about rent & visits",
                action = { NestToggle(checked = notificationsOn, onToggle = { notificationsOn = it }) }
            )
            InfoRowDivider()
            SettingsRow(
                icon = Icons.Outlined.Security,
                iconBg = BlueBg,
                iconTint = BlueText,
                title = "Two-factor auth",
                subtitle = "Secure your account",
                action = { NestToggle(checked = twoFaOn, onToggle = { twoFaOn = it }) }
            )
            InfoRowDivider()
            SettingsRow(
                icon = Icons.Outlined.Language,
                iconBg = PinkBg,
                iconTint = PinkText,
                title = "Language",
                subtitle = "English (US)",
                action = { Icon(Icons.Default.ChevronRight, null, tint = TextTertiary) }
            )
        }

        SectionHeader("Support")
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.HelpOutline,
                iconBg = GreenBg,
                iconTint = GreenText,
                title = "Help center",
                subtitle = "FAQs & documentation",
                action = { Icon(Icons.Default.ChevronRight, null, tint = TextTertiary) }
            )
            InfoRowDivider()
            SettingsRow(
                icon = Icons.Outlined.Description,
                iconBg = AmberBg,
                iconTint = AmberText,
                title = "Terms of service",
                subtitle = "Identity documents",
                action = { Icon(Icons.Default.ChevronRight, null, tint = TextTertiary) }
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout from all devices", color = RedText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ─────────────────────────────────────────────
//  REUSABLES
// ─────────────────────────────────────────────

@Composable
fun SectionHeader(title: String) {
    Text(
        title.uppercase(),
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextTertiary,
        letterSpacing = 0.8.sp
    )
}

@Composable
fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(0.5.dp, BorderColor)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) = InfoCard(content)

@Composable
fun InfoRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 14.dp),
        thickness = 0.5.dp,
        color = BorderColor.copy(alpha = 0.6f)
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
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
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
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(IndigoLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = IndigoText, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        
        Surface(
            color = statusBg,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                status,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
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
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 11.sp, color = TextTertiary)
        }
        action()
    }
}

@Composable
fun AmenityChip(label: String, active: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (active) IndigoLight else Color(0xFFF3F4F6),
        border = if (active) BorderStroke(0.5.dp, IndigoText.copy(alpha = 0.3f)) else null
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            color = if (active) IndigoText else TextTertiary
        )
    }
}

@Composable
fun NestToggle(checked: Boolean, onToggle: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onToggle,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Indigo,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFE5E7EB),
            uncheckedBorderColor = Color.Transparent
        )
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ProfileScreenNewPreview() {
    MyRentTheme {
        ProfileScreenNew()
    }
}
