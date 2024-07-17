package app.routes.requests

import com.google.gson.JsonObject
import app.utils.optString

data class SignUpRequest(
    val username: String,
    val password: String,
    val email: String,
    val mobile: String,
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): SignUpRequest {
            try {
                val username = jsonObject.optString("username")
                val password = jsonObject.optString("password")
                val email = jsonObject.optString("email")
                val mobile = jsonObject.optString("mobile")

                require(username.isNotEmpty()) {
                    throw IllegalArgumentException("Invalid or missing value: username")
                }
                require(password.isNotEmpty()) {
                    throw IllegalArgumentException("Invalid or missing value: password")
                }
                require(email.isNotEmpty()) {
                    throw IllegalArgumentException("Invalid or missing value: email")
                }
                require(mobile.isNotEmpty()) {
                    throw IllegalArgumentException("Invalid or missing value: mobile")
                }

                return SignUpRequest(username, password, email, mobile)
            } catch (e: IllegalArgumentException) {
                throw e
            } catch (e: Throwable) {
                throw Throwable("Internal server error: ${e.message}")
            }
        }
    }
}

