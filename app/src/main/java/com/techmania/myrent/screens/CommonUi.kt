package com.techmania.myrent.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// Shared Colors from Profile Screen style
val AppNavy        = Color(0xFF1A1A2E)
val AppBorderColor = Color(0xFFE5E7EB)
val AppTextTertiary= Color(0xFF9CA3AF)

@Composable
fun AutoResizingText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    minFontSize: TextUnit = 8.sp
) {
    var resizedFontSize by remember { mutableStateOf(style.fontSize) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        modifier = modifier.drawBehind {
            if (readyToDraw) drawRect(Color.Transparent)
        },
        fontWeight = fontWeight,
        textAlign = textAlign,
        fontSize = resizedFontSize,
        softWrap = false,
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Clip,
        onTextLayout = { result ->
            if (result.didOverflowWidth || result.didOverflowHeight) {
                if (resizedFontSize.value > minFontSize.value) {
                    resizedFontSize = (resizedFontSize.value * 0.95f).sp
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleVisitDialog(
    propertyName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Date Picker, 2: Time Picker
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    if (step == 1) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = { step = 2 },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text("Next", fontWeight = FontWeight.Bold, color = Color(0xFF4B4EFC))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(state = datePickerState)
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Visit Time", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("When would you like to visit $propertyName?", fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(24.dp))
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val dateString = datePickerState.selectedDateMillis?.let { sdf.format(Date(it)) } ?: ""
                        val timeString = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                        onConfirm(dateString, timeString)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { step = 1 }) {
                    Text("Back", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White
        )
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
