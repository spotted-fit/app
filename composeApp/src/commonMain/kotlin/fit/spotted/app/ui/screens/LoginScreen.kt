package fit.spotted.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mmk.kmpnotifier.notification.NotifierManager
import fit.spotted.app.api.ApiProvider
import kotlinx.coroutines.launch

/**
 * Screen that allows users to log in or create a new account.
 *
 * @param onLogin Callback to be invoked when the user successfully logs in, passing the username
 */
class LoginScreen(
    private val onLogin: () -> Unit = {}
) : Screen {
    // API client
    private val apiClient = ApiProvider.getApiClient()

    @Composable
    override fun Content() {
        var isLogin by remember { mutableStateOf(true) }
        // State for loading and error handling
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Coroutine scope for API calls
        val coroutineScope = rememberCoroutineScope()
        if (apiClient.isLoggedIn()) {
            onLogin()
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App title
            Text(
                text = "Spotted",
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Toggle between login and signup
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TabRow(
                    selectedTabIndex = if (isLogin) 0 else 1,
                    modifier = Modifier.width(300.dp)
                ) {
                    Tab(
                        selected = isLogin,
                        onClick = { isLogin = true },
                        text = { Text("Login") }
                    )
                    Tab(
                        selected = !isLogin,
                        onClick = { isLogin = false },
                        text = { Text("Sign Up") }
                    )
                }
            }

            // Display error message if any
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Form content based on selected tab
            if (isLogin) {
                LoginForm(
                    isLoading = isLoading,
                    onLoginSubmit = { password, username ->
                        errorMessage = null
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                val firebaseToken = NotifierManager.getPushNotifier().getToken()
                                val response = apiClient.login(password, username, firebaseToken)
                                if (response.result == "ok") {
                                    onLogin()
                                } else {
                                    errorMessage = response.message ?: "Login failed"
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "An error occurred"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            } else {
                SignupForm(
                    isLoading = isLoading,
                    onSignupSubmit = { email, password, username ->
                        errorMessage = null
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                val firebaseToken = NotifierManager.getPushNotifier().getToken()
                                val response = apiClient.register(email, password, username, firebaseToken)
                                if (response.result == "ok") {
                                    onLogin()
                                } else {
                                    errorMessage = response.message ?: "Registration failed"
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "An error occurred"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun LoginForm(
        isLoading: Boolean = false,
        onLoginSubmit: (password: String, username: String) -> Unit = { _, _ -> }
    ) {
        var password by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Username"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Login button
            Button(
                onClick = {
                    onLoginSubmit(password, username)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && password.isNotBlank() && username.isNotBlank()
            ) {
                Text("Login")
            }
        }
    }

    @Composable
    private fun SignupForm(
        isLoading: Boolean = false,
        onSignupSubmit: (email: String, password: String, username: String) -> Unit = { _, _, _ -> }
    ) {
        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Username"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // Confirm password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Confirm Password"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sign up button
            Button(
                onClick = {
                    // Validate passwords match
                    if (password == confirmPassword) {
                        onSignupSubmit(email, password, username)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && password == confirmPassword &&
                        email.isNotBlank() && password.isNotBlank() && username.isNotBlank()
            ) {
                Text("Create Account")
            }
        }
    }
}
