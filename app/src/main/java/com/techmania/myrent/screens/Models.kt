package com.techmania.myrent.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

data class Property(
    val id: String = "",
    val badge: String = "Available",
    val badgeColorValue: Int = Color(0xFF22C55E).toArgb(),
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
    val landlordId: String = "",
    val addedTimestamp: Long = 0L
) {
    val badgeColor: Color get() = Color(badgeColorValue)
    val imageBg: Color get() = Color(imageBgValue)
}

data class Enquiry(
    val id: String = "",
    val name: String = "",
    val propertyName: String = "",
    val propertyId: String = "",
    val type: String = "", // "Visit request" or "Rent request"
    val initials: String = "",
    val avatarBgValue: Int = Color(0xFFE8EAF6).toArgb(),
    val tenantId: String = "",
    val landlordId: String = "",
    val status: String = "Pending", // "Pending", "Accepted", "Declined"
    val visitDate: String = "",
    val visitTime: String = "",
    val showActionButtons: Boolean = true,
    val showChatButton: Boolean = false
) {
    val avatarBg: Color get() = Color(avatarBgValue)
}

enum class BookingStatus(val label: String, val color: Color) {
    ACTIVE("Active", Color(0xFF22C55E)),
    VISIT_SET("Visit set", Color(0xFF3B82F6)),
    PENDING("Pending", Color(0xFFF59E0B)),
    COMPLETED("Completed", Color(0xFF6B7280)),
    CANCELLED("Cancelled", Color(0xFFEF4444))
}

data class Booking(
    val id: String = "",
    val propertyId: String = "",
    val propertyName: String = "",
    val location: String = "",
    val bhk: String = "",
    val price: String = "",
    val status: String = "Pending", // "Pending", "Active", "Completed", "Cancelled", "Visit set"
    val tenantId: String = "",
    val landlordId: String = "",
    val visitDate: String = "",
    val since: String = "",
    val mediaUri: String = "",
    val imageBgValue: Int = Color(0xFFC8D3F5).toArgb()
) {
    val imageBg: Color get() = Color(imageBgValue)
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

data class UserProfile(
    val uid: String = "",
    val name: String = "Rahul Sharma",
    val email: String = "rahul.sharma@email.com",
    val phone: String = "+91 98765 43210",
    val city: String = "New Delhi, India",
    val occupation: String = "Software Engineer",
    val rating: Float = 0.0f,
    val memberSince: String = "2024",
    val propertiesViewed: Int = 0,
    val activeBookings: Int = 0,
    val leasesEnded: Int = 0,
    val role: String = "",
    val profileImage: Any? = null,
    val isVerified: Boolean = false
)

data class Tenant(
    val uid: String = "",
    val fullName: String = "",
    val mobileNumber: String = "",
    val emailAddress: String = "",
    val password: String = "",
    val isVerified: Boolean = false,
    val rating: Float = 0.0f,
    val memberSince: String = ""
)

data class Landlord(
    val uid: String = "",
    val fullName: String = "",
    val mobile: String = "",
    val city: String = "",
    val email: String = "",
    val password: String = "",
    val isVerified: Boolean = false,
    val rating: Float = 0.0f,
    val memberSince: String = ""
)
