package com.getir.patika.network_operations.data

import com.getir.patika.network_operations.data.model.UserDto
import com.getir.patika.network_operations.data.model.UserLoginDto
import com.getir.patika.network_operations.data.model.UserRegisterDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class NetworkDataSource {
    suspend fun registerUser(registerDto: UserRegisterDto): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            val jsonBody = JSONObject().apply {
                put("fullName", registerDto.fullName)
                put("email", registerDto.email)
                put("password", registerDto.password)
            }.toString()
            performPostRequest(jsonBody, "register")
        }
    }

    suspend fun loginUser(loginDto: UserLoginDto): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            val jsonBody = JSONObject().apply {
                put("email", loginDto.email)
                put("password", loginDto.password)
            }.toString()
            performPostRequest(jsonBody, "login")
        }
    }

    suspend fun getProfile(userId: String): Result<UserDto> = runCatching {
        withContext(Dispatchers.IO) {
            performGetRequest("profile/$userId").let {
                parseJsonToUserDto(it)
            }
        }
    }

    private fun performGetRequest(endpoint: String): String {
        val url = URL(BASE_URL + endpoint)
        var response: String

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")

            response = inputStream.bufferedReader().use { it.readText() }
        }

        return response
    }

    private fun performPostRequest(
        jsonBody: String,
        endpoint: String
    ): String {
        var response: String
        val url = URL(BASE_URL + endpoint)

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            doOutput = true

            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                writer.write(jsonBody)
                writer.flush()
            }

            response = inputStream.bufferedReader().use { it.readText() }
        }

        return response
    }

    private fun parseJsonToUserDto(json: String): UserDto {
        val jsonObject = JSONObject(json)
        return UserDto(
            id = jsonObject.getInt("id"),
            userId = jsonObject.getString("userId"),
            fullName = jsonObject.getString("fullName"),
            email = jsonObject.getString("email"),
            password = jsonObject.getString("password"),
            phoneNumber = jsonObject.optString("phoneNumber"),
            occupation = jsonObject.optString("occupation"),
            employer = jsonObject.optString("employer"),
            country = jsonObject.optString("country"),
            latitude = jsonObject.optDouble("latitude"),
            longitude = jsonObject.optDouble("longitude")
        )
    }

    private companion object {
        const val BASE_URL = "https://espresso-food-delivery-backend-cc3e106e2d34.herokuapp.com/"
    }
}
