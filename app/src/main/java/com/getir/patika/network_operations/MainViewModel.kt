package com.getir.patika.network_operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.getir.patika.network_operations.data.NetworkDataSource
import com.getir.patika.network_operations.data.model.AuthState
import com.getir.patika.network_operations.data.model.MainEvent
import com.getir.patika.network_operations.data.model.UiState
import com.getir.patika.network_operations.data.model.UserDto
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

    private val email
        get() = uiState.value.email

    private val password
        get() = uiState.value.password

    private val fullName
        get() = uiState.value.fullName

    private val authState
        get() = uiState.value.authState

    private val userId
        get() = uiState.value.userId

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

        UserRegisterDto(
            fullName = fullName,
            email = email,
            password = password
        ).let { userRegisterDto ->
            viewModelScope.launch {
                networkDataSource.registerUser(userRegisterDto)
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                authState = AuthState.Error(
                                    throwable.message ?: "An error occurred"
                                )
                            )
                        }
                        throwable.printStackTrace()
                    }
                    .onSuccess {
                        println(it)
                        _uiState.update { it.copy(authState = AuthState.Registered) }
                    }
            }
        }
    }

    private fun onLogin() {
        _uiState.update { it.copy(authState = AuthState.Loading) }

        UserLoginDto(
            email = email,
            password = password
        ).let { userLoginDto ->
            viewModelScope.launch {
                networkDataSource.loginUser(userLoginDto)
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                authState = AuthState.Error(
                                    throwable.message ?: "An error occurred"
                                )
                            )
                        }
                    }
                    .onSuccess { userId ->
                        _uiState.update { it.copy(authState = AuthState.SignedIn, userId = userId) }
                    }
            }
        }
    }

    private fun getProfile() {
        if (authState != AuthState.SignedIn) {
            _uiState.update { it.copy(authState = AuthState.Error("You need to sign in first")) }
            return
        }
        if (userId == null) {
            _uiState.update { it.copy(authState = AuthState.Error("User ID is empty")) }
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
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            authState = AuthState.Error(
                                throwable.message ?: "An error occurred"
                            )
                        )
                    }
                }
        }
    }



    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val networkDataSource = NetworkDataSource()
                return MainViewModel(networkDataSource) as T
            }
        }
    }
}

