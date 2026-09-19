package com.techmania.myrent.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

data class Property(
    val id: String = "",
    val badge: String = "Available",
    val badgeColorValue: Int = Color(0xFF4B4EFC).toArgb(),
    val imageBgValue: Int = Color(0xFFF5F5F7).toArgb(),
    val price: String = "₹0",
    val numericPrice: Int = 0,
    val name: String = "",
    val city: String = "",
    val areaName: String = "",
    val pincode: String = "",
    val rating: String = "0.0",
    val numericRating: Float = 0f,
    val reviews: String = "0",
    val bhk: String = "1 BHK",
    val numericBhk: Int = 1,
    val size: String = "",
    val furnishing: String = "Unfurnished",
    val type: String = "Apartment",
    val availability: String = "Immediate",
    val amenities: List<String> = emptyList(),
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val mediaUris: List<String> = emptyList(),
    val isLiked: Boolean = false,
    val landlordId: String = ""
) {
    val badgeColor: Color get() = Color(badgeColorValue)
    val imageBg: Color get() = Color(imageBgValue)
}

data class Enquiry(
    val id: String = "",
    val name: String = "",
    val propertyName: String = "",
    val type: String = "",
    val initials: String = "",
    val avatarBgValue: Int = Color(0xFFE8EAF6).toArgb(),
    val showActionButtons: Boolean = false,
    val showChatButton: Boolean = false
) {
    val avatarBg: Color get() = Color(avatarBgValue)
}

data class FilterParams(
    val propertyTypes: Set<String> = setOf("All"),
    val priceRange: ClosedFloatingPointRange<Float> = 5000f..100000f,
    val bhkConfigs: Set<String> = setOf("1 BHK", "2 BHK", "3 BHK", "4+ BHK"),
    val furnishings: Set<String> = setOf("Any"),
    val availabilities: Set<String> = setOf("Immediate", "Within 15 days", "Within 1 month"),
    val amenities: Set<String> = setOf(),
    val minRating: String = "Any"
)

data class Tenant(
    val uid: String = "",
    val fullName: String = "",
    val mobileNumber: String = "",
    val emailAddress: String = "",
    val password: String = ""
)

data class Landlord(
    val uid: String = "",
    val fullName: String = "",
    val mobile: String = "",
    val city: String = "",
    val email: String = "",
    val password: String = ""
)
