package com.getir.patika.network_operations.data.model

data class UiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val authState: AuthState = AuthState.Idle,
    val userId: String? = null,
    val profileInfo: String = ""
)
