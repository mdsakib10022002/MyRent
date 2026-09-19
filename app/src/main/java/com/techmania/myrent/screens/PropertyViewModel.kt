package com.techmania.myrent.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.Transaction
import java.util.Calendar
import java.util.Locale

class PropertyViewModel : ViewModel() {
    private val database by lazy { 
        try { FirebaseDatabase.getInstance().reference } catch (e: Exception) { null } 
    }
    private val auth by lazy { 
        try { FirebaseAuth.getInstance() } catch (e: Exception) { null } 
    }

    private val _properties = mutableStateListOf<Property>()
    val properties: List<Property> = _properties

    private val _enquiries = mutableStateListOf<Enquiry>()
    val enquiries: List<Enquiry> = _enquiries

    val availableMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
    
    var selectedMonth by mutableStateOf(
        Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
            ?.takeIf { availableMonths.contains(it) } ?: "May"
    )
    
    private val monthlyEarningsData = mapOf(
        "Jan" to 85000,
        "Feb" to 92000,
        "Mar" to 78000,
        "Apr" to 110000,
        "May" to 99000,
        "Jun" to 105000
    )

    val currentMonthEarnings: Int get() = _properties.filter { it.badge == "Rented" }.sumOf { it.numericPrice }
    
    val displayEarnings: Int get() {
        val currentMonth = Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        return if (selectedMonth == currentMonth) {
            currentMonthEarnings
        } else {
            monthlyEarningsData[selectedMonth] ?: 0
        }
    }

    val rentedCount: Int get() = _properties.count { it.badge == "Rented" }
    
    val lastMonthEarnings = 88000
    val earningsGrowth: String get() {
        val current = displayEarnings
        if (lastMonthEarnings == 0) return "+0%"
        val growth = ((current - lastMonthEarnings).toFloat() / lastMonthEarnings) * 100
        val sign = if (growth >= 0) "+" else ""
        return "$sign${growth.toInt()}% vs last month"
    }

    val monthlyEarningsHistory = listOf(
        Pair("Jan", 0.6f),
        Pair("Feb", 0.75f),
        Pair("Mar", 0.55f),
        Pair("Apr", 0.95f),
        Pair("May", 0.85f),
        Pair("Jun", 0.90f)
    )

    init {
        seedInitialData()
        seedEnquiries()
        
        try {
            fetchProperties()
            fetchEnquiries()
        } catch (e: Exception) {}
    }

    private fun fetchProperties() {
        database?.child("properties")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                
                val snapshotIds = mutableSetOf<String>()
                
                for (child in snapshot.children) {
                    val property = child.getValue(Property::class.java) ?: continue
                    val propertyWithId = property.copy(id = child.key ?: "")
                    snapshotIds.add(propertyWithId.id)
                    
                    val index = _properties.indexOfFirst { it.id == propertyWithId.id }
                    if (index != -1) {
                        if (_properties[index] != propertyWithId) {
                            _properties[index] = propertyWithId
                        }
                    } else {
                        _properties.add(propertyWithId)
                    }
                }
                
                // Remove properties that are no longer in Firebase (excluding local seeds)
                _properties.removeIf { it.id.isNotEmpty() && !it.id.startsWith("seed_") && it.id !in snapshotIds }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchEnquiries() {
        database?.child("enquiries")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                val snapshotIds = mutableSetOf<String>()
                for (child in snapshot.children) {
                    val enquiry = child.getValue(Enquiry::class.java) ?: continue
                    val enquiryWithId = enquiry.copy(id = child.key ?: "")
                    snapshotIds.add(enquiryWithId.id)
                    
                    val index = _enquiries.indexOfFirst { it.id == enquiryWithId.id }
                    if (index != -1) {
                        if (_enquiries[index] != enquiryWithId) {
                            _enquiries[index] = enquiryWithId
                        }
                    } else {
                        _enquiries.add(enquiryWithId)
                    }
                }
                _enquiries.removeIf { it.id.isNotEmpty() && !it.id.startsWith("e_seed_") && it.id !in snapshotIds }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun seedInitialData() {
        val initialProperties = listOf(
            Property(
                id = "seed_1",
                badge = "Rented", badgeColorValue = Color(0xFFFF0000).toArgb(),
                price = "₹22,000", numericPrice = 22000, name = "Sunshine Apartments", city = "Delhi",
                areaName = "Dwarka Sector 10", pincode = "110075", rating = "4.8", numericRating = 4.8f,
                reviews = "24", bhk = "2 BHK", numericBhk = 2, size = "850 sq ft", furnishing = "Fully Furnished",
                type = "Apartment", availability = "Occupied", amenities = listOf("Parking", "WiFi", "AC", "Lift"),
                latitude = 28.5823, longitude = 77.0500
            ),
            Property(
                id = "seed_2",
                badge = "Available", badgeColorValue = Color(0xFF4B4EFC).toArgb(),
                price = "₹45,000", numericPrice = 45000, name = "Royal Villa", city = "Mumbai",
                areaName = "Bandra West", pincode = "400050", rating = "4.9", numericRating = 4.9f,
                reviews = "12", bhk = "3 BHK", numericBhk = 3, size = "1800 sq ft", furnishing = "Semi-Furnished",
                type = "Villa", availability = "Within 15 days", amenities = listOf("Parking", "Gym", "Pool", "Security"),
                latitude = 19.0596, longitude = 72.8295
            )
        )
        
        if (_properties.isEmpty()) {
            _properties.addAll(initialProperties)
        }
    }

    private fun seedEnquiries() {
        val initialEnquiries = listOf(
            Enquiry(id = "e_seed_1", name = "Amit Mehta", propertyName = "Green View Villa", type = "Visit request", initials = "AM", avatarBgValue = Color(0xFFE8EAF6).toArgb(), showActionButtons = true),
            Enquiry(id = "e_seed_2", name = "Priya Sharma", propertyName = "Lotus Studio", type = "Rent enquiry", initials = "PS", avatarBgValue = Color(0xFFFFF3E0).toArgb(), showChatButton = true)
        )
        if (_enquiries.isEmpty()) {
            _enquiries.addAll(initialEnquiries)
        }
    }

    fun rateProperty(propertyId: String, newUserRating: Float) {
        if (propertyId.isEmpty()) return
        
        // Handle local seed data
        if (propertyId.startsWith("seed_")) {
            val index = _properties.indexOfFirst { it.id == propertyId }
            if (index != -1) {
                val p = _properties[index]
                val currentReviews = p.reviews.toIntOrNull() ?: 0
                val newReviewsCount = currentReviews + 1
                val newAvg = ((p.numericRating * currentReviews) + newUserRating) / newReviewsCount
                val roundedAvg = (Math.round(newAvg * 10.0) / 10.0).toFloat()
                
                _properties[index] = p.copy(
                    numericRating = roundedAvg,
                    rating = String.format("%.1f", roundedAvg),
                    reviews = newReviewsCount.toString()
                )
            }
            return
        }

        val propertyRef = database?.child("properties")?.child(propertyId) ?: return

        propertyRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: com.google.firebase.database.MutableData): Transaction.Result {
                val p = mutableData.getValue(Property::class.java)
                    ?: return Transaction.success(mutableData)

                val currentReviews = p.reviews.toIntOrNull() ?: 0
                val currentAvg = p.numericRating

                val newReviewsCount = currentReviews + 1
                val newAvg = ((currentAvg * currentReviews) + newUserRating) / newReviewsCount
                
                val roundedAvg = (Math.round(newAvg * 10.0) / 10.0).toFloat()

                mutableData.child("numericRating").value = roundedAvg
                mutableData.child("rating").value = String.format("%.1f", roundedAvg)
                mutableData.child("reviews").value = newReviewsCount.toString()

                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {}
        })
    }

    fun addProperty(property: Property) {
        val id = database?.child("properties")?.push()?.key ?: return
        val landlordId = auth?.currentUser?.uid ?: ""
        database?.child("properties")?.child(id)?.setValue(property.copy(id = id, landlordId = landlordId))
    }

    fun removeProperty(property: Property) {
        if (property.id.isNotEmpty()) {
            database?.child("properties")?.child(property.id)?.removeValue()
        }
    }

    fun togglePropertyStatus(property: Property) {
        if (property.id.isEmpty()) return
        val currentStatus = property.badge
        val newStatus = if (currentStatus == "Rented") "Available" else "Rented"
        val newColor = if (newStatus == "Rented") Color(0xFFFF0000) else Color(0xFF4B4EFC)
        val newAvailability = if (newStatus == "Rented") "Occupied" else "Immediate"
        
        database?.child("properties")?.child(property.id)?.updateChildren(mapOf(
            "badge" to newStatus,
            "badgeColorValue" to newColor.toArgb(),
            "availability" to newAvailability
        ))
    }

    fun toggleLike(property: Property) {
        if (property.id.isEmpty() || property.id.startsWith("seed_")) {
            val index = _properties.indexOfFirst { it.id == property.id }
            if (index != -1) {
                _properties[index] = _properties[index].copy(isLiked = !property.isLiked)
            }
            return
        }
        
        database?.child("properties")?.child(property.id)?.child("isLiked")?.setValue(!property.isLiked)
    }
}
