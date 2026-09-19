package com.techmania.myrent

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.techmania.myrent.screens.*
import com.techmania.myrent.ui.theme.MyRentTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window,false)
        setContent {
            MyRentTheme {
                MainNavigation()
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val isPreview = LocalInspectionMode.current
    val navController = rememberNavController()
    val auth = remember { 
        try { FirebaseAuth.getInstance() } catch (e: Exception) { null } 
    }
    val context = LocalContext.current
    
    var currentUserName by remember { 
        mutableStateOf(auth?.currentUser?.displayName ?: auth?.currentUser?.email?.split("@")?.first() ?: "User") 
    }
    var isLandlord by remember { mutableStateOf(false) }

    val propertyViewModel: PropertyViewModel = viewModel()

    // Google Sign-In Logic
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account, auth, isLandlord) { name ->
                currentUserName = name
                if (isLandlord) {
                    navController.navigate("landlord_dashboard") {
                        popUpTo("choose_role") { inclusive = true }
                    }
                } else {
                    navController.navigate("bookings") {
                        popUpTo("choose_role") { inclusive = true }
                    }
                }
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (auth?.currentUser != null) "home" else "splash",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("splash") {
            SplashScreen(onNext = {
                navController.navigate("choose_role") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("choose_role") {
            ChooseRoleScreen(
                onTenantClick = {
                    isLandlord = false
                    navController.navigate("tenant_login")
                },
                onLandlordClick = {
                    isLandlord = true
                    navController.navigate("landlord_login")
                },
                onSignInClick = {
                    navController.navigate("tenant_login")
                }
            )
        }
        composable("tenant_login") {
            TenantLoginScreen(
                onBackClick = { navController.popBackStack() },
                onSignUpClick = { navController.navigate("tenant_signup") },
                onSignInSuccess = { name ->
                    currentUserName = name
                    isLandlord = false
                    navController.navigate("bookings") {
                        popUpTo("choose_role") { inclusive = true }
                    }
                },
                onGoogleSignInClick = {
                    isLandlord = false
                    launcher.launch(googleSignInClient.signInIntent)
                }
            )
        }
        composable("tenant_signup") {
            TenantSignUpScreen(
                onBackClick = { navController.popBackStack() },
                onSignInClick = { navController.popBackStack() },
                onSignUpSuccess = { _ ->
                    auth?.signOut() // Ensure user is signed out so they must log in
                    Toast.makeText(context, "Registration successful! Please sign in.", Toast.LENGTH_LONG).show()
                    navController.navigate("tenant_login") {
                        popUpTo("tenant_signup") { inclusive = true }
                    }
                }
            )
        }
        composable("landlord_login") {
            LandlordLoginScreen(
                onBackClick = { navController.popBackStack() },
                onSignUpClick = { navController.navigate("landlord_signup") },
                onSignInSuccess = { name ->
                    currentUserName = name
                    isLandlord = true
                    navController.navigate("landlord_dashboard") {
                        popUpTo("choose_role") { inclusive = true }
                    }
                },
                onGoogleSignInClick = {
                    isLandlord = true
                    launcher.launch(googleSignInClient.signInIntent)
                }
            )
        }
        composable("landlord_signup") {
            LandlordSignUpScreen(
                onBackClick = { navController.popBackStack() },
                onSignInClick = { navController.popBackStack() },
                onSignUpSuccess = { _ ->
                    auth?.signOut() // Ensure user is signed out so they must log in
                    Toast.makeText(context, "Registration successful! Please sign in.", Toast.LENGTH_LONG).show()
                    navController.navigate("landlord_login") {
                        popUpTo("landlord_signup") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                userName = currentUserName,
                isLandlord = isLandlord,
                propertyViewModel = propertyViewModel,
                onPropertyClick = { propertyName ->
                    navController.navigate("property_detail/$propertyName")
                },
                onExploreClick = {
                    navController.navigate("explore") {
                        launchSingleTop = true
                    }
                },
                onListingsClick = {
                    navController.navigate("landlord_dashboard") {
                        launchSingleTop = true
                    }
                },
                onBookingsClick = {
                    navController.navigate("bookings") {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate("profile") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("explore") {
            ExploreScreen(
                userName = currentUserName,
                isLandlord = isLandlord,
                propertyViewModel = propertyViewModel,
                onHomeClick = {
                    navController.navigate("home") {
                        launchSingleTop = true
                    }
                },
                onPropertyClick = { propertyName ->
                    navController.navigate("property_detail/$propertyName")
                },
                onListingsClick = {
                    navController.navigate("landlord_dashboard") {
                        launchSingleTop = true
                    }
                },
                onBookingsClick = {
                    navController.navigate("bookings") {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate("profile") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("landlord_dashboard") {
            LandlordDashboardScreen(
                userName = currentUserName,
                isLandlord = isLandlord,
                propertyViewModel = propertyViewModel,
                onHomeClick = {
                    navController.navigate("home") {
                        launchSingleTop = true
                    }
                },
                onExploreClick = {
                    navController.navigate("explore") {
                        launchSingleTop = true
                    }
                },
                onAddNewClick = {
                    navController.navigate("add_property")
                },
                onProfileClick = {
                    navController.navigate("profile") {
                        launchSingleTop = true
                    }
                },
                onBookingsClick = {
                    navController.navigate("bookings") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("add_property") {
            AddPropertyScreen(
                userName = currentUserName,
                propertyViewModel = propertyViewModel,
                onBackClick = { navController.popBackStack() },
                onPublishClick = { navController.popBackStack() }
            )
        }
        composable(
            "property_detail/{propertyName}",
            arguments = listOf(navArgument("propertyName") { type = NavType.StringType })
        ) { backStackEntry ->
            val propertyName = backStackEntry.arguments?.getString("propertyName") ?: ""
            PropertyDetailScreen(
                onBackClick = { navController.popBackStack() },
                propertyName = propertyName,
                propertyViewModel = propertyViewModel
            )
        }
        composable("bookings") {
            MyBookingsScreen(
                isLandlord = isLandlord,
                onHomeClick = {
                    navController.navigate("home") {
                        launchSingleTop = true
                    }
                },
                onExploreClick = {
                    navController.navigate("explore") {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate("profile") {
                        launchSingleTop = true
                    }
                },
                onListingsClick = {
                    navController.navigate("landlord_dashboard") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("profile") {
            ProfileScreenNew(
                profile = UserProfile(name = currentUserName, email = auth?.currentUser?.email ?: "No email"),
                isLandlord = isLandlord,
                onLogoutClick = {
                    auth?.signOut()
                    googleSignInClient.signOut()
                    navController.navigate("choose_role") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavClick = { destination ->
                    when (destination) {
                        "Home" -> navController.navigate("home") { launchSingleTop = true }
                        "Explore" -> navController.navigate("explore") { launchSingleTop = true }
                        "Bookings" -> navController.navigate("bookings") { launchSingleTop = true }
                        "Listings" -> navController.navigate("landlord_dashboard") { launchSingleTop = true }
                    }
                }
            )
        }
    }
}

private fun firebaseAuthWithGoogle(
    account: GoogleSignInAccount,
    auth: FirebaseAuth?,
    isLandlord: Boolean,
    onSuccess: (String) -> Unit
) {
    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
    auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val user = auth.currentUser
            val uid = user?.uid ?: ""
            val name = user?.displayName ?: account.displayName ?: "User"
            val email = user?.email ?: account.email ?: ""
            
            val dbPath = if (isLandlord) "Landlords" else "Tenants"
            val userRef = FirebaseDatabase.getInstance().getReference(dbPath).child(uid)
            
            // Check if user exists in the specific role's database, if not create entry
            userRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    val userData = mapOf(
                        "name" to name,
                        "email" to email,
                        "role" to dbPath.dropLast(1) // Landlord or Tenant
                    )
                    userRef.setValue(userData)
                }
                onSuccess(name)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainNavigationPreview() {
    MyRentTheme {
        MainNavigation()
    }
}

@Composable
fun SplashScreen(onNext: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val isPreview = LocalInspectionMode.current
    
    LaunchedEffect(Unit) {
        if (!isPreview) {
            alpha.animateTo(1f, animationSpec = tween(1500))
            delay(1000)
            onNext()
        } else {
            alpha.snapTo(1f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .alpha(alpha.value)
                    .background(Color(0xFF191C1E), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            @Suppress("DEPRECATION")
            Text(
                text = "MyRent",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}

@Composable
fun ChooseRoleScreen(
    onTenantClick: () -> Unit,
    onLandlordClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF191C1E), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to MyRent",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Find your home or list your property",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Black
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        RoleCard(
            title = "I'm a Tenant",
            subtitle = "Looking for a property to rent",
            icon = Icons.Default.Person,
            iconBackground = Color(0xFFEBEBFF),
            iconColor = Color(0xFF4B4EFC),
            onClick = onTenantClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        RoleCard(
            title = "I'm a Landlord",
            subtitle = "List and manage my properties",
            icon = Icons.Outlined.Home,
            iconBackground = Color(0xFFE6F4EA),
            iconColor = Color(0xFF006D39),
            onClick = onLandlordClick
        )
    }
}

@Composable
fun RoleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBackground: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(iconBackground, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF757575)
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFBDBDBD)
            )
        }
    }
}
