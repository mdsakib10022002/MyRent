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
import androidx.compose.material.icons.filled.Info
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
import com.google.firebase.database.FirebaseDatabase
import com.techmania.myrent.ui.theme.MyRentTheme

@Composable
fun LandlordLoginScreen(
    onBackClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onSignInSuccess: (String) -> Unit,
    onGoogleSignInClick: () -> Unit = {}
) {
    var emailOrMobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var emailError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Fix render problem by avoiding Firebase initialization in Preview
    val isPreview = LocalInspectionMode.current
    val auth = remember { if (isPreview) null else FirebaseAuth.getInstance() }
    val database = remember { if (isPreview) null else FirebaseDatabase.getInstance().getReference("Landlords") }

    Scaffold(
        containerColor = Color(0xFFF8F9FB),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        // Box used to center content and limit width on large screens for a "perfect fit"
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
                    .widthIn(max = 500.dp) // Optimized for tablets/wide devices
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp)
                ) {
                    // Top Bar
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
                                text = "Landlord",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = TextStyle(
                                    color = Color(0xFF006D39),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Icon
                    Spacer(modifier = Modifier.height(32.dp))
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

                    // Title & Subtitle
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Manage your properties",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 28.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sign in to your landlord account",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF757575))
                    )

                    // Form
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "REGISTERED EMAIL",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emailOrMobile,
                        onValueChange = { 
                            emailOrMobile = it
                            emailError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your email", color = Color(0xFFBDBDBD)) },
                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = Color(0xFF757575)) },
                        shape = RoundedCornerShape(12.dp),
                        isError = emailError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFF006D39),
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
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
                            color = Color.Black,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter password", color = Color(0xFFBDBDBD)) },
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
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFF006D39),
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Forgot password?",
                        modifier = Modifier.align(Alignment.End).clickable { /* TODO */ },
                        style = TextStyle(
                            color = Color(0xFF2E3192),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    // Sign In Button
                    Spacer(modifier = Modifier.height(25.dp))
                    Button(
                        onClick = { 
                            if (emailOrMobile.isEmpty()) {
                                emailError = "Please enter email"
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrMobile).matches()) {
                                emailError = "Invalid email format"
                            } else if (password.isEmpty()) {
                                Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT).show()
                            } else if (!isLoading && auth != null && database != null) {
                                isLoading = true
                                auth.signInWithEmailAndPassword(emailOrMobile, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val uid = auth.currentUser?.uid ?: ""
                                            // Verify if user is a Landlord
                                            database.child(uid).get().addOnSuccessListener { snapshot ->
                                                isLoading = false
                                                if (snapshot.exists()) {
                                                    val user = auth.currentUser
                                                    val displayName = user?.displayName ?: emailOrMobile.split("@").first()
                                                    onSignInSuccess(displayName)
                                                } else {
                                                    auth.signOut()
                                                    Toast.makeText(context, "This account is not registered as a Landlord", Toast.LENGTH_LONG).show()
                                                }
                                            }.addOnFailureListener {
                                                isLoading = false
                                                Toast.makeText(context, "Database error: ${it.message}", Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            isLoading = false
                                            Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
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
                            Text("Sign in as Landlord", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    // Divider
                    Spacer(modifier = Modifier.height(22.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                        Text(
                            text = " or use social login ",
                            style = TextStyle(color = Color(0xFF9E9E9E), fontSize = 12.sp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                    }

                    // Social/OTP Buttons
                    Spacer(modifier = Modifier.height(22.dp))
                    OutlinedButton(
                        onClick = { /* TODO: OTP Login */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Login with OTP on mobile", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onGoogleSignInClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Continue with Google", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }

                    // Verification Box
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFF0F9F4),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF13694C),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Your account is verified by Aadhaar & PAN. Listings go live after review.",
                                color = Color(0xFF13694C),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Footer centered vertically in its box at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val footerText = buildAnnotatedString {
                        append("New landlord? ")
                        withStyle(style = SpanStyle(color = Color(0xFF13694C), fontWeight = FontWeight.SemiBold)) {
                            append("Register & get started")
                        }
                    }
                    Text(
                        text = footerText,
                        modifier = Modifier.clickable { onSignUpClick() },
                        style = TextStyle(fontSize = 15.sp, color = Color.Black)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(showBackground = true)
@Composable
fun LandlordLoginPreview() {
    MyRentTheme {
        LandlordLoginScreen(onBackClick = {}, onSignUpClick = {}, onSignInSuccess = {})
    }
}
