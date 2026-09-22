package com.techmania.myrent.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.techmania.myrent.ui.theme.MyRentTheme
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandlordSignUpScreen(
    onBackClick: () -> Unit, 
    onSignInClick: () -> Unit,
    onSignUpSuccess: (String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(true) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    
    // Fix render problem and handle Firebase
    val isPreview = LocalInspectionMode.current
    val auth = remember { if (isPreview) null else FirebaseAuth.getInstance() }
    val database = remember { if (isPreview) null else FirebaseDatabase.getInstance().getReference("Landlords") }

    // City suggestions
    val citySuggestions = listOf(
        "Mumbai", "Delhi", "Bangalore", "Hyderabad", "Ahmedabad", "Chennai", 
        "Kolkata", "Surat", "Pune", "Jaipur", "Lucknow", "Kanpur", 
        "Nagpur", "Indore", "Thane", "Bhopal", "Visakhapatnam", "Patna", 
        "Vadodara", "Ghaziabad", "Ludhiana", "Agra", "Nashik", "Faridabad", 
        "Meerut", "Rajkot", "Varanasi", "Srinagar", "Aurangabad", "Dhanbad", 
        "Amritsar", "Navi Mumbai", "Allahabad", "Ranchi", "Howrah", "Coimbatore", 
        "Jabalpur", "Gwalior", "Vijayawada", "Jodhpur", "Madurai", "Raipur", 
        "Kota", "Guwahati", "Chandigarh", "Solapur", "Hubli-Dharwad", "Bareilly", 
        "Moradabad", "Mysore", "Gurgaon", "Aligarh", "Jalandhar", "Tiruchirappalli", 
        "Bhubaneswar", "Salem", "Mira-Bhayandar", "Warangal", "Guntur", "Bhiwandi", 
        "Saharanpur", "Gorakhpur", "Bikaner", "Amravati", "Noida", "Jamshedpur", 
        "Bhilai", "Cuttack", "Firozabad", "Kochi", "Nellore", "Bhavnagar", 
        "Dehradun", "Durgapur", "Asansol", "Rourkela", "Nanded", "Kolhapur", 
        "Ajmer", "Akola", "Gulbarga", "Jamnagar", "Ujjain", "Loni", "Siliguri", 
        "Jhansi", "Ulhasnagar", "Nellore", "Jammu", "Sangli-Miraj & Kupwad", 
        "Belgaum", "Mangalore", "Ambattur", "Tirunelveli", "Malegaon", "Gaya", 
        "Jalgaon", "Udaipur", "Maheshtala"
    )
    val filteredCities = if (city.isNotEmpty()) {
        citySuggestions.filter { it.contains(city, ignoreCase = true) }.take(5)
    } else {
        emptyList()
    }
    var isCityDropdownExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

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
                    .widthIn(max = 500.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp)
                ) {
                    // Top Bar with Back Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(22.dp),
                                tint = Color.Black
                            )
                        }

                        Surface(
                            color = Color(0xFFE6F4EA),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "New Landlord",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = TextStyle(
                                    color = Color(0xFF006D39),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Landlord Icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFE6F4EA), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = null,
                            tint = Color(0xFF006D39),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Title
                    Text(
                        text = "Create your\nlandlord account",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 28.sp,
                            lineHeight = 36.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "List & manage properties easily",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF757575))
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Form Fields
                    LandlordSignUpField(
                        label = "FULL NAME",
                        placeholder = "Full name",
                        icon = Icons.Outlined.Person,
                        value = fullName,
                        onValueChange = { fullName = it }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            LandlordSignUpField(
                                label = "MOBILE",
                                placeholder = "Mobile",
                                icon = Icons.Outlined.PhoneAndroid,
                                value = mobile,
                                onValueChange = { 
                                    if (it.all { char -> char.isDigit() } && it.length <= 10) {
                                        mobile = it
                                        mobileError = null
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = mobileError != null
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CITY",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF757575),
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ExposedDropdownMenuBox(
                                expanded = isCityDropdownExpanded && filteredCities.isNotEmpty(),
                                onExpandedChange = { isCityDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = city,
                                    onValueChange = { 
                                        city = it 
                                        isCityDropdownExpanded = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
                                    placeholder = { Text("City", color = Color(0xFFBDBDBD)) },
                                    leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color(0xFF757575)) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedBorderColor = Color(0xFF006D39),
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    ),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(
                                    expanded = isCityDropdownExpanded && filteredCities.isNotEmpty(),
                                    onDismissRequest = { isCityDropdownExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    filteredCities.forEach { suggestion ->
                                        DropdownMenuItem(
                                            text = { Text(suggestion) },
                                            onClick = {
                                                city = suggestion
                                                isCityDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LandlordSignUpField(
                        label = "EMAIL ADDRESS",
                        placeholder = "your@email.com",
                        icon = Icons.Outlined.Email,
                        value = email,
                        onValueChange = { 
                            email = it
                            emailError = null
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = emailError != null
                    )
                    if (emailError != null) {
                        Text(text = emailError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "PASSWORD",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575),
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            passwordError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Min. 6 characters", color = Color(0xFFBDBDBD)) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color(0xFF757575)) },
                        trailingIcon = { 
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFF757575),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        isError = passwordError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFF006D39),
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    if (passwordError != null) {
                        Text(text = passwordError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { checked = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF006D39))
                        )
                        val termsText = buildAnnotatedString {
                            append("I agree to the ")
                            withStyle(style = SpanStyle(color = Color(0xFF006D39), fontWeight = FontWeight.SemiBold)) {
                                append("Terms & Conditions")
                            }
                        }
                        Text(text = termsText, fontSize = 12.sp, color = Color(0xFF757575))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { 
                            var hasError = false
                            if (mobile.length != 10) {
                                mobileError = "Enter valid 10-digit mobile"
                                hasError = true
                            }
                            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Enter valid email address"
                                hasError = true
                            }
                            if (password.length < 6) {
                                passwordError = "Password too short"
                                hasError = true
                            }

                            if (!hasError && !isLoading && auth != null && database != null) {
                                isLoading = true
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val user = auth.currentUser
                                            val profileUpdates = UserProfileChangeRequest.Builder()
                                                .setDisplayName(fullName)
                                                .build()
                                            user?.updateProfile(profileUpdates)

                                            val uid = user?.uid ?: ""
                                            val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
                                            val landlord = Landlord(
                                                uid = uid,
                                                fullName = fullName,
                                                mobile = mobile,
                                                city = city,
                                                email = email,
                                                password = password,
                                                isVerified = false,
                                                rating = 0.0f,
                                                memberSince = currentYear
                                            )

                                            database.child(uid).setValue(landlord).addOnCompleteListener { dbTask ->
                                                isLoading = false
                                                if (dbTask.isSuccessful) {
                                                    onSignUpSuccess(fullName.split(" ").first())
                                                } else {
                                                    Toast.makeText(context, "Database failed: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        } else {
                                            isLoading = false
                                            Toast.makeText(context, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF13694C))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Register Account", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val signInText = buildAnnotatedString {
                        append("Already have an account? ")
                        withStyle(style = SpanStyle(color = Color(0xFF13694C), fontWeight = FontWeight.SemiBold)) {
                            append("Sign in")
                        }
                    }
                    Text(
                        text = signInText,
                        modifier = Modifier.clickable { onSignInClick() },
                        style = TextStyle(fontSize = 15.sp, color = Color.Black)
                    )
                }
            }
        }
    }
}

@Composable
fun LandlordSignUpField(
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false
) {
    Text(
        text = label,
        style = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF757575),
            letterSpacing = 0.5.sp
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color(0xFFBDBDBD)) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF757575)) },
        shape = RoundedCornerShape(12.dp),
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedBorderColor = Color(0xFF006D39),
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        ),
        singleLine = true,
        keyboardOptions = keyboardOptions
    )
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(showBackground = true)
@Composable
fun LandlordSignUpPreview() {
    MyRentTheme {
        LandlordSignUpScreen(onBackClick = {}, onSignInClick = {}, onSignUpSuccess = {})
    }
}
