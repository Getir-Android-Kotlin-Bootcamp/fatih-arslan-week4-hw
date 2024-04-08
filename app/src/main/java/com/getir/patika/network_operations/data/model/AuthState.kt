package com.getir.patika.network_operations.data.model

sealed class AuthState {
    data object Idle: AuthState()
    data object Loading: AuthState()
    data object SignedIn: AuthState()
    data object Registered: AuthState()
    data object ProfileRetrieved: AuthState()
    data class Error(val message: String): AuthState()
}