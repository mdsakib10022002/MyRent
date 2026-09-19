package com.techmania.myrent.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.techmania.myrent.ui.theme.MyRentTheme

@Composable
fun TenantSignUpScreen(
    onBackClick: () -> Unit,
    onSignInClick: () -> Unit,
    onSignUpSuccess: (String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(true) }

    var mobileError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Fix render problem and handle Firebase
    val isPreview = LocalInspectionMode.current
    val auth = remember { if (isPreview) null else FirebaseAuth.getInstance() }
    val database = remember { if (isPreview) null else FirebaseDatabase.getInstance().getReference("Tenants") }

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
                // Main Content
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
                            color = Color(0xFFEBEBFF),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "New Tenant",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = TextStyle(
                                    color = Color(0xFF4B4EFC),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    
                    // Tenant Icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFEBEBFF), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = Color(0xFF4B4EFC),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title Section
                    Text(
                        text = "Create your account",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 28.sp,
                            lineHeight = 36.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Start renting in minutes",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF757575))
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // Form Fields
                    SignUpField(
                        label = "FULL NAME",
                        placeholder = "Rahul Sharma",
                        icon = Icons.Outlined.Person,
                        value = fullName,
                        onValueChange = { fullName = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SignUpField(
                        label = "MOBILE NUMBER",
                        placeholder = "Mobile",
                        icon = Icons.Default.Phone,
                        value = mobileNumber,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 10) {
                                mobileNumber = it
                                mobileError = null
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = mobileError != null
                    )
                    if (mobileError != null) {
                        Text(text = mobileError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SignUpField(
                        label = "EMAIL ADDRESS",
                        placeholder = "your@email.com",
                        icon = Icons.Outlined.Email,
                        value = emailAddress,
                        onValueChange = {
                            emailAddress = it
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
                            focusedBorderColor = Color(0xFF4B4EFC),
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

                    // Checkbox row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { checked = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4B4EFC))
                        )
                        val termsText = buildAnnotatedString {
                            append("I agree to ")
                            withStyle(style = SpanStyle(color = Color(0xFF4B4EFC), fontWeight = FontWeight.SemiBold)) {
                                append("Terms of Service")
                            }
                            append(" & ")
                            withStyle(style = SpanStyle(color = Color(0xFF4B4EFC), fontWeight = FontWeight.SemiBold)) {
                                append("Privacy Policy")
                            }
                        }
                        Text(text = termsText, fontSize = 12.sp, color = Color(0xFF757575))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Create Account Button
                    Button(
                        onClick = {
                            var hasError = false
                            if (mobileNumber.length != 10) {
                                mobileError = "Mobile number must be 10 digits"
                                hasError = true
                            }
                            if (emailAddress.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
                                emailError = "Please enter a valid email address"
                                hasError = true
                            }
                            if (password.length < 6) {
                                passwordError = "Password must be at least 6 characters"
                                hasError = true
                            }

                            if (!hasError && !isLoading && auth != null && database != null) {
                                isLoading = true
                                auth.createUserWithEmailAndPassword(emailAddress, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val user = auth.currentUser
                                            val profileUpdates = userProfileChangeRequest {
                                                displayName = fullName
                                            }
                                            user?.updateProfile(profileUpdates)

                                            val uid = user?.uid ?: ""
                                            val tenant = Tenant(uid, fullName, mobileNumber, emailAddress, password)

                                            database.child(uid).setValue(tenant).addOnCompleteListener { dbTask ->
                                                isLoading = false
                                                if (dbTask.isSuccessful) {
                                                    val firstName = fullName.trim().split(" ").firstOrNull() ?: fullName
                                                    onSignUpSuccess(firstName)
                                                } else {
                                                    Toast.makeText(context, "Data saving failed: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B4EFC))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Fixed Footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val signInFooterText = buildAnnotatedString {
                        append("Already registered? ")
                        withStyle(style = SpanStyle(color = Color(0xFF4B4EFC), fontWeight = FontWeight.SemiBold)) {
                            append("Sign in")
                        }
                    }
                    Text(
                        text = signInFooterText,
                        modifier = Modifier.clickable { onSignInClick() },
                        style = TextStyle(fontSize = 15.sp, color = Color.Black)
                    )
                }
            }
        }
    }
}

@Composable
fun SignUpField(
    label: String,
    placeholder: String,
    icon: ImageVector,
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
            focusedBorderColor = Color(0xFF4B4EFC),
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
fun TenantSignUpPreview() {
    MyRentTheme {
        TenantSignUpScreen(onBackClick = {}, onSignInClick = {}, onSignUpSuccess = {})
    }
}
