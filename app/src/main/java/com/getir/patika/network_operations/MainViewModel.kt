package com.getir.patika.network_operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.getir.patika.network_operations.data.NetworkDataSource
import com.getir.patika.network_operations.data.model.AuthState
import com.getir.patika.network_operations.data.model.MainEvent
import com.getir.patika.network_operations.data.model.UiState
import com.getir.patika.network_operations.data.model.UserLoginDto
import com.getir.patika.network_operations.data.model.UserRegisterDto
import com.getir.patika.network_operations.data.model.userProfileToString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(private val networkDataSource: NetworkDataSource) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val email get() = uiState.value.email

    private val password get() = uiState.value.password

    private val fullName get() = uiState.value.fullName

    private val authState get() = uiState.value.authState

    private val userId get() = uiState.value.userId

    /**
     * Handles various UI events and updates the UI state accordingly.
     *
     * @param event The [MainEvent] representing the user action.
     */
    fun onEvent(event: MainEvent) {
        when (event) {
            MainEvent.GetProfileClicked -> getProfile()
            is MainEvent.OnEmailChanged -> _uiState.update { it.copy(email = event.email) }
            is MainEvent.OnFullNameChanged -> _uiState.update { it.copy(fullName = event.fullName) }
            MainEvent.OnLoginClicked -> onLogin()
            is MainEvent.OnPasswordChanged -> _uiState.update { it.copy(password = event.password) }
            MainEvent.OnRegisterClicked -> onRegister()
        }
    }

    private fun onRegister() {
        _uiState.update { it.copy(authState = AuthState.Loading) }
        val userRegisterDto = UserRegisterDto(fullName, email, password)

        viewModelScope.launch {
            networkDataSource.registerUser(userRegisterDto)
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            authState = AuthState.Error(GENERIC_ERROR)
                        )
                    }
                    throwable.printStackTrace()
                }
                .onSuccess {
                    _uiState.update { state -> state.copy(authState = AuthState.Registered) }
                }
        }
    }

    private fun onLogin() {
        _uiState.update { it.copy(authState = AuthState.Loading) }
        val userLoginDto = UserLoginDto(email, password)

        viewModelScope.launch {
            networkDataSource.loginUser(userLoginDto)
                .onFailure {
                    _uiState.update {
                        it.copy(
                            authState = AuthState.Error(GENERIC_ERROR)
                        )
                    }
                }
                .onSuccess { userId ->
                    _uiState.update { it.copy(authState = AuthState.SignedIn, userId = userId) }
                }
        }
    }

    /**
     * Retrieves the user profile if the user is signed in and has a valid user ID.
     */
    private fun getProfile() {
        if (authState !in listOf(AuthState.SignedIn, AuthState.ProfileRetrieved)
        ) {
            _uiState.update { it.copy(authState = AuthState.Error(SIGN_IN_ERROR)) }
            return
        }
        if (userId == null) {
            _uiState.update { it.copy(authState = AuthState.Error(EMPTY_USER_ID)) }
            return
        }

        _uiState.update { it.copy(authState = AuthState.Loading) }

        viewModelScope.launch {
            networkDataSource.getProfile(userId!!)
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            profileInfo = profile.userProfileToString(),
                            authState = AuthState.ProfileRetrieved
                        )
                    }
                }.onFailure {
                    _uiState.update {
                        it.copy(
                            authState = AuthState.Error(GENERIC_ERROR)
                        )
                    }
                }
        }
    }

    companion object {
        /**
         * Factory for creating [MainViewModel] instances.
         */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val networkDataSource = NetworkDataSource()
                return MainViewModel(networkDataSource) as T
            }
        }

        private const val GENERIC_ERROR = "An error occurred, try again"
        private const val SIGN_IN_ERROR = "You need to sign in first"
        private const val EMPTY_USER_ID = "User ID is empty"
    }
}
