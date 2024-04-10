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

/**
 * Data source for handling network operations related to user authentication and profile management.
 */
class NetworkDataSource {
    /**
     * Registers a new user with the provided registration details.
     *
     * @param registerDto The data transfer object containing the user's registration details.
     * @return A [Result] containing the user ID as a [String] if successful, or an exception if an error occurs.
     */
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

    /**
     * Logs in a user with the provided login details.
     *
     * @param loginDto The data transfer object containing the user's login details.
     * @return A [Result] containing the user ID as a [String] if successful, or an exception if an error occurs.
     */
    suspend fun loginUser(loginDto: UserLoginDto): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            val jsonBody = JSONObject().apply {
                put("email", loginDto.email)
                put("password", loginDto.password)
            }.toString()
            performPostRequest(jsonBody, "login")
        }
    }

    /**
     * Retrieves the profile of a user with the given user ID.
     *
     * @param userId The ID of the user whose profile is to be retrieved.
     * @return A [Result] containing the [UserDto] if successful, or an exception if an error occurs.
     */
    suspend fun getProfile(userId: String): Result<UserDto> = runCatching {
        withContext(Dispatchers.IO) {
            parseJsonToUserDto(performGetRequest("profile/$userId"))
        }
    }

    /**
     * Performs a GET request to the specified endpoint.
     *
     * @param endpoint The endpoint to which the GET request will be sent.
     * @return The response from the server as a [String].
     */
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

    /**
     * Performs a POST request to the specified endpoint with the given JSON body.
     *
     * @param jsonBody The JSON body to be sent with the POST request.
     * @param endpoint The endpoint to which the POST request will be sent.
     * @return The response from the server as a [String].
     */
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

    /**
     * Parses the given JSON string into a [UserDto].
     *
     * @param json The JSON string to be parsed.
     * @return The [UserDto] parsed from the JSON string.
     */
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
