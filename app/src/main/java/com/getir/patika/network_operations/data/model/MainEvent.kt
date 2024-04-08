package com.getir.patika.network_operations.data.model

sealed class MainEvent {
    data class OnFullNameChanged(val fullName: String) : MainEvent()
    data class OnEmailChanged(val email: String) : MainEvent()
    data class OnPasswordChanged(val password: String) : MainEvent()
    data object OnRegisterClicked : MainEvent()
    data object OnLoginClicked : MainEvent()
    data object GetProfileClicked : MainEvent()
}
