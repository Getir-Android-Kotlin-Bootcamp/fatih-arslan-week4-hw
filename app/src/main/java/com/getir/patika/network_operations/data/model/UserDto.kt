package com.getir.patika.network_operations.data.model

data class UserDto(
    val id: Int,
    val userId: String,
    val fullName: String,
    val email: String,
    val password: String,
    val phoneNumber: String? = null,
    val occupation: String? = null,
    val employer: String? = null,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

fun UserDto.userProfileToString(): String = buildString {
    appendLine("User ID: $userId")
    appendLine("Full Name: $fullName")
    appendLine("Email: $email")
    appendLine("Phone Number: $phoneNumber")
    appendLine("Occupation: $occupation")
    appendLine("Employer: $employer")
    appendLine("Country: $country")
    appendLine("Latitude: $latitude")
    appendLine("Longitude: $longitude")
}
