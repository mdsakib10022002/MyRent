package com.techmania.myrent.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Shared Colors from Profile Screen style
val AppNavy        = Color(0xFF1A1A2E)
val AppBorderColor = Color(0xFFE5E7EB)
val AppTextTertiary= Color(0xFF9CA3AF)

@Composable
fun AppBottomNavigation(
    currentScreen: String,
    isLandlord: Boolean = false,
    onNavClick: (String) -> Unit
) {
    val items = mutableListOf(
        Triple("Home", Icons.Outlined.Home, "home"),
        Triple("Explore", Icons.Outlined.Search, "explore")
    )

    if (isLandlord) {
        items.add(Triple("Listings", Icons.Outlined.GridView, "landlord_dashboard"))
    } else {
        items.add(Triple("Bookings", Icons.Outlined.CalendarMonth, "bookings"))
    }

    items.add(Triple("Profile", Icons.Outlined.Person, "profile"))

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.border(0.5.dp, AppBorderColor, RectangleShape).height(105.dp)
    ) {
        items.forEach { (label, icon, _) ->
            val isSelected = label.equals(currentScreen, ignoreCase = true)
            NavigationBarItem(
                selected = isSelected,
                onClick  = { onNavClick(label) },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = icon, 
                            contentDescription = label,
                            modifier = Modifier.size(24.dp),
                            tint = if (isSelected) AppNavy else AppTextTertiary
                        )
                        if (isSelected) {
                            Spacer(Modifier.height(4.dp))
                            Box(
                                Modifier.size(4.dp)
                                    .clip(CircleShape)
                                    .background(AppNavy)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AppNavy else AppTextTertiary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppBottomNavigationPreview() {
    AppBottomNavigation(
        currentScreen = "Home",
        isLandlord = false,
        onNavClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun AppBottomNavigationLandlordPreview() {
    AppBottomNavigation(
        currentScreen = "Listings",
        isLandlord = true,
        onNavClick = {}
    )
}
