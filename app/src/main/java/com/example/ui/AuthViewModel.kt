package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.MindfulApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val email: String) : AuthUiState
    data class Error(val errorMessage: String) : AuthUiState
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    init {
        // Observe auth state changes on init
        val currentAuth = auth
        if (currentAuth != null) {
            try {
                currentAuth.addAuthStateListener { firebaseAuth ->
                    _currentUserEmail.value = firebaseAuth.currentUser?.email
                    if (firebaseAuth.currentUser != null) {
                        _uiState.value = AuthUiState.Success(firebaseAuth.currentUser?.email ?: "")
                    } else {
                        if (_uiState.value is AuthUiState.Success) {
                            _uiState.value = AuthUiState.Idle
                        }
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                setupGuestModeFallback()
            }
        } else {
            setupGuestModeFallback()
        }
    }

    private fun setupGuestModeFallback() {
        _currentUserEmail.value = "offline.guest@mindfulmeter.com"
        _uiState.value = AuthUiState.Success("offline.guest@mindfulmeter.com")
    }

    fun loginWithEmail(email: String, javaPass: String) {
        if (email.isBlank() || javaPass.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty")
            return
        }
        _uiState.value = AuthUiState.Loading
        
        val currentAuth = auth
        if (currentAuth == null) {
            // Local sandbox mock login
            _currentUserEmail.value = email
            _uiState.value = AuthUiState.Success(email)
            return
        }

        viewModelScope.launch {
            try {
                currentAuth.signInWithEmailAndPassword(email, javaPass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _currentUserEmail.value = email
                            _uiState.value = AuthUiState.Success(email)
                        } else {
                            _uiState.value = AuthUiState.Error(task.exception?.localizedMessage ?: "Login failed")
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Unknown compilation/runtime issue")
            }
        }
    }

    fun signUpWithEmail(email: String, javaPass: String) {
        if (email.isBlank() || javaPass.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty")
            return
        }
        if (javaPass.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
            return
        }
        _uiState.value = AuthUiState.Loading

        val currentAuth = auth
        if (currentAuth == null) {
            // Local sandbox mock signup
            _currentUserEmail.value = email
            _uiState.value = AuthUiState.Success(email)
            return
        }

        viewModelScope.launch {
            try {
                currentAuth.createUserWithEmailAndPassword(email, javaPass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _currentUserEmail.value = email
                            _uiState.value = AuthUiState.Success(email)
                        } else {
                            _uiState.value = AuthUiState.Error(task.exception?.localizedMessage ?: "Signup failed")
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun loginWithGoogleToken(idToken: String) {
        _uiState.value = AuthUiState.Loading
        val currentAuth = auth
        if (currentAuth == null) {
            _uiState.value = AuthUiState.Success("google.sandbox@mindfulmeter.com")
            return
        }

        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            currentAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = currentAuth.currentUser
                        _currentUserEmail.value = user?.email
                        _uiState.value = AuthUiState.Success(user?.email ?: "Google Account")
                    } else {
                        _uiState.value = AuthUiState.Error(task.exception?.localizedMessage ?: "Google Sign-In failed")
                    }
                }
        } catch (t: Throwable) {
            _uiState.value = AuthUiState.Error(t.localizedMessage ?: "Google Sign-In initialization failed")
        }
    }

    fun loginWithGoogleMock(mockEmail: String) {
        _uiState.value = AuthUiState.Loading
        val currentAuth = auth
        if (currentAuth == null) {
            _currentUserEmail.value = mockEmail
            _uiState.value = AuthUiState.Success(mockEmail)
            return
        }

        viewModelScope.launch {
            try {
                currentAuth.signInWithEmailAndPassword(mockEmail, "GoogleMockPassword123")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _currentUserEmail.value = mockEmail
                            _uiState.value = AuthUiState.Success(mockEmail)
                        } else {
                            currentAuth.createUserWithEmailAndPassword(mockEmail, "GoogleMockPassword123")
                                .addOnCompleteListener { signupTask ->
                                    if (signupTask.isSuccessful) {
                                        _currentUserEmail.value = mockEmail
                                        _uiState.value = AuthUiState.Success(mockEmail)
                                    } else {
                                        _uiState.value = AuthUiState.Error(signupTask.exception?.localizedMessage ?: "Mock Google initialization failed")
                                    }
                                }
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Connection error")
            }
        }
    }

    fun signOut() {
        auth?.signOut()
        _currentUserEmail.value = null
        _uiState.value = AuthUiState.Idle
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AuthViewModel()
            }
        }
    }
}
