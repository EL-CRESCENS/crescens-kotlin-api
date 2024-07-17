package app.routes.requests

import app.utils.optString
import com.google.gson.JsonObject

data class LoginRequest(
    val username: String? = null,
    val mobile: String? = null,
    val email: String? = null,
    val password: String
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): LoginRequest {
            try {
                val password = jsonObject.optString("password")
                val username = jsonObject.optString("username").takeIf { it.isNotEmpty() }
                val email = jsonObject.optString("email").takeIf { it.isNotEmpty() }
                val mobile = jsonObject.optString("mobile").takeIf { it.isNotEmpty() }

                require(password.isNotEmpty()) {
                    throw IllegalArgumentException("Invalid or missing value: password")
                }

                val providedIdentifiers = listOfNotNull(username, mobile, email)
                require(providedIdentifiers.size == 1) {
                    throw IllegalArgumentException("Either only one of username, mobile, or email must be provided")
                }

                return LoginRequest(username, mobile, email, password)
            } catch (e: IllegalArgumentException) {
                throw e
            } catch (e: Throwable) {
                throw Throwable("Internal server error: ${e.message}")
            }
        }
    }
}