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
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
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

    private val _bookings = mutableStateListOf<Booking>()
    val bookings: List<Booking> = _bookings

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
        try {
            fetchProperties()
            fetchEnquiries()
            fetchBookings()
        } catch (e: Exception) {}
    }

    private fun fetchProperties() {
        database?.child("properties")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                val snapshotIds = mutableSetOf<String>()
                val currentTime = System.currentTimeMillis()
                val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L

                for (child in snapshot.children) {
                    val property = child.getValue(Property::class.java) ?: continue
                    var propertyWithId = property.copy(id = child.key ?: "")
                    
                    // Auto-transition logic: "New" -> "Available" after 7 days
                    if (propertyWithId.badge == "New" && 
                        propertyWithId.addedTimestamp > 0 && 
                        (currentTime - propertyWithId.addedTimestamp) > sevenDaysMs) {
                        
                        // Update in Firebase
                        child.ref.updateChildren(mapOf(
                            "badge" to "Available",
                            "badgeColorValue" to Color(0xFF22C55E).toArgb()
                        ))
                        // Update local copy immediately for UI snappiness
                        propertyWithId = propertyWithId.copy(
                            badge = "Available",
                            badgeColorValue = Color(0xFF22C55E).toArgb()
                        )
                    }

                    snapshotIds.add(propertyWithId.id)
                    val index = _properties.indexOfFirst { it.id == propertyWithId.id }
                    if (index != -1) {
                        if (_properties[index] != propertyWithId) _properties[index] = propertyWithId
                    } else {
                        _properties.add(propertyWithId)
                    }
                }
                _properties.removeIf { it.id.isNotEmpty() && !it.id.startsWith("seed_") && it.id !in snapshotIds }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchEnquiries() {
        database?.child("enquiries")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _enquiries.clear()
                for (child in snapshot.children) {
                    val enquiry = child.getValue(Enquiry::class.java) ?: continue
                    _enquiries.add(enquiry.copy(id = child.key ?: ""))
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchBookings() {
        database?.child("bookings")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _bookings.clear()
                for (child in snapshot.children) {
                    val booking = child.getValue(Booking::class.java) ?: continue
                    _bookings.add(booking.copy(id = child.key ?: ""))
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun scheduleVisit(property: Property, date: String, time: String) {
        val user = auth?.currentUser ?: return
        val enquiryId = database?.child("enquiries")?.push()?.key ?: return
        val bookingId = database?.child("bookings")?.push()?.key ?: return

        val enquiry = Enquiry(
            id = enquiryId,
            name = user.displayName ?: user.email?.split("@")?.first() ?: "Tenant",
            propertyName = property.name,
            propertyId = property.id,
            type = "Visit request",
            initials = (user.displayName ?: "T").take(1).uppercase(),
            tenantId = user.uid,
            landlordId = property.landlordId,
            visitDate = date,
            visitTime = time,
            status = "Pending"
        )

        val booking = Booking(
            id = bookingId,
            propertyId = property.id,
            propertyName = property.name,
            location = "${property.areaName}, ${property.city}",
            bhk = property.bhk,
            price = property.price,
            status = "Pending",
            tenantId = user.uid,
            landlordId = property.landlordId,
            visitDate = "$date · $time",
            mediaUri = property.mediaUris.firstOrNull() ?: "",
            imageBgValue = property.imageBgValue
        )

        database?.child("enquiries")?.child(enquiryId)?.setValue(enquiry)
        database?.child("bookings")?.child(bookingId)?.setValue(booking)
    }

    fun rescheduleVisit(booking: Booking, date: String, time: String) {
        val user = auth?.currentUser ?: return
        val enquiryId = database?.child("enquiries")?.push()?.key ?: return
        
        val enquiry = Enquiry(
            id = enquiryId,
            name = user.displayName ?: user.email?.split("@")?.first() ?: "Tenant",
            propertyName = booking.propertyName,
            propertyId = booking.propertyId,
            type = "Visit request (Reschedule)",
            initials = (user.displayName ?: "T").take(1).uppercase(),
            tenantId = user.uid,
            landlordId = booking.landlordId,
            visitDate = date,
            visitTime = time,
            status = "Pending"
        )

        database?.child("enquiries")?.child(enquiryId)?.setValue(enquiry)
        database?.child("bookings")?.child(booking.id)?.updateChildren(mapOf(
            "status" to "Pending",
            "visitDate" to "$date · $time"
        ))
    }

    fun requestToRent(property: Property) {
        val user = auth?.currentUser ?: return
        val enquiryId = database?.child("enquiries")?.push()?.key ?: return
        val bookingId = database?.child("bookings")?.push()?.key ?: return
        
        val enquiry = Enquiry(
            id = enquiryId,
            name = user.displayName ?: user.email?.split("@")?.first() ?: "Tenant",
            propertyName = property.name,
            propertyId = property.id,
            type = "Rent request",
            initials = (user.displayName ?: "T").take(1).uppercase(),
            tenantId = user.uid,
            landlordId = property.landlordId,
            status = "Pending"
        )

        val booking = Booking(
            id = bookingId,
            propertyId = property.id,
            propertyName = property.name,
            location = "${property.areaName}, ${property.city}",
            bhk = property.bhk,
            price = property.price,
            status = "Pending",
            tenantId = user.uid,
            landlordId = property.landlordId,
            mediaUri = property.mediaUris.firstOrNull() ?: "",
            imageBgValue = property.imageBgValue
        )

        database?.child("enquiries")?.child(enquiryId)?.setValue(enquiry)
        database?.child("bookings")?.child(bookingId)?.setValue(booking)
    }

    fun respondToEnquiry(enquiry: Enquiry, accept: Boolean) {
        if (enquiry.id.isEmpty()) return
        val status = if (accept) "Accepted" else "Declined"
        database?.child("enquiries")?.child(enquiry.id)?.child("status")?.setValue(status)

        if (accept) {
            if (enquiry.type.contains("Rent request")) {
                // Update Property to Rented globally
                database?.child("properties")?.child(enquiry.propertyId)?.updateChildren(mapOf(
                    "badge" to "Rented",
                    "badgeColorValue" to Color(0xFFEF4444).toArgb(),
                    "availability" to "Occupied"
                ))

                // Update Booking status to Active
                database?.child("bookings")?.orderByChild("propertyId")?.equalTo(enquiry.propertyId)
                    ?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (child in snapshot.children) {
                                val booking = child.getValue(Booking::class.java)
                                if (booking?.tenantId == enquiry.tenantId) {
                                    child.ref.child("status").setValue("Active")
                                    child.ref.child("since").setValue("Since " + SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(java.util.Date()))
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            } else if (enquiry.type.contains("Visit request")) {
                // Update existing Booking status to "Visit set"
                database?.child("bookings")?.orderByChild("propertyId")?.equalTo(enquiry.propertyId)
                    ?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (child in snapshot.children) {
                                val booking = child.getValue(Booking::class.java)
                                if (booking?.tenantId == enquiry.tenantId) {
                                    child.ref.updateChildren(mapOf(
                                        "status" to "Visit set",
                                        "visitDate" to (enquiry.visitDate + " · " + enquiry.visitTime)
                                    ))
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
        }
    }

    fun addProperty(property: Property) {
        val id = database?.child("properties")?.push()?.key ?: return
        val landlordId = auth?.currentUser?.uid ?: ""
        database?.child("properties")?.child(id)?.setValue(property.copy(id = id, landlordId = landlordId))
    }

    fun rateProperty(propertyId: String, newUserRating: Float) {
        if (propertyId.isEmpty()) return
        val propertyRef = database?.child("properties")?.child(propertyId) ?: return
        propertyRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val p = mutableData.getValue(Property::class.java)
                    ?: return Transaction.success(mutableData)

                val currentReviews = p.reviews.toIntOrNull() ?: 0
                val currentAvg = p.numericRating

                val newReviewsCount = currentReviews + 1
                val newAvg = ((currentAvg * currentReviews) + newUserRating) / newReviewsCount
                val roundedAvg = (Math.round(newAvg * 10.0) / 10.0).toFloat()

                mutableData.child("numericRating").value = roundedAvg
                mutableData.child("rating").value = String.format(Locale.getDefault(), "%.1f", roundedAvg)
                mutableData.child("reviews").value = newReviewsCount.toString()

                return Transaction.success(mutableData)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {}
        })
    }

    fun toggleLike(property: Property) {
        if (property.id.isEmpty()) return
        database?.child("properties")?.child(property.id)?.child("isLiked")?.setValue(!property.isLiked)
    }

    fun cancelBooking(bookingId: String) {
        if (bookingId.isEmpty()) return
        database?.child("bookings")?.child(bookingId)?.child("status")?.setValue("Cancelled")
    }

    fun removeProperty(property: Property) {
        if (property.id.isNotEmpty()) {
            database?.child("properties")?.child(property.id)?.removeValue()
        }
    }
}
