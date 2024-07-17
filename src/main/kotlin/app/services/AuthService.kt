package app.services

import app.utils.*
import app.models.*
import app.plugins.dotenv
import app.repositories.*
import app.routes.requests.*
import app.routes.responses.LoginResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

class AuthService {
    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()

    suspend fun signup(breadcrumb: Breadcrumb, reqBody: SignUpRequest): User {
        breadcrumb.log("[AUTH_SERVICE]: START signup")

        try {
            val findUserByUsername = userRepository.findUserByProperty("username", reqBody.username)
            if (findUserByUsername != null) throw Throwable("username already exist")

            val findUserByEmail = userRepository.findUserByProperty("email", reqBody.email)
            if (findUserByEmail != null) throw Throwable("email already exist")

            val findUserByMobile = userRepository.findUserByProperty("mobile", reqBody.mobile)
            if (findUserByMobile != null) throw Throwable("email already exist")

            val hashPassword = hash(reqBody.password)

            val user = User(
                username = reqBody.username,
                password = hashPassword,
                email = reqBody.email,
                mobile = reqBody.mobile,
                active = true,
                emailVerified = false,
                mobileVerified = false
            )

            val newUser = userRepository.createUser(user)

            breadcrumb.log("[AUTH_SERVICE]: SUCCESS signup: newUser - $newUser")
            return newUser
        } catch (e: Throwable) {
            breadcrumb.log("[AUTH_SERVICE]: ERROR signup: ${e.message}")
            throw e
        }
    }

    suspend fun login(breadcrumb: Breadcrumb, reqBody: LoginRequest, ipAddress: String): LoginResponse {
        breadcrumb.log("[AUTH_SERVICE]: START login")

        try {
            var identifier = ""
            var user: User? = null

            when {
                reqBody.username != null -> {
                    // Handle login with username
                    breadcrumb.log("[AUTH_SERVICE]: Logging in with username: ${reqBody.username}")
                    identifier = "username"
                    user = userRepository.findUserByProperty("username", reqBody.username)
                }
                reqBody.mobile != null -> {
                    // Handle login with mobile
                    breadcrumb.log("[AUTH_SERVICE]: Logging in with mobile: ${reqBody.mobile}")
                    identifier = "mobile"
                    user = userRepository.findUserByProperty("mobile", reqBody.mobile)
                }
                reqBody.email != null -> {
                    // Handle login with email
                    breadcrumb.log("[AUTH_SERVICE]: Logging in with email: ${reqBody.email}")
                    identifier = "email"
                    user = userRepository.findUserByProperty("email", reqBody.email)
                }
            }

            // Check if user exist
            if (user == null) throw Throwable("Invalid $identifier or password.")

            // Check if password matched
            val passMatched = compare(reqBody.password, user.password)
            if (!passMatched) throw Throwable("Invalid $identifier or password.")

            // Create token
            // Development token expires in 24hr (86400 seconds)
            // Production token expires in 30min (1800 seconds)
            val expInSeconds = dotenv["JWT_EXPIRATION"].toInt()
            val currentTimeInSeconds = System.currentTimeMillis() / 1000
            val expTimeInSeconds = currentTimeInSeconds + expInSeconds
            val jwtToken = createUserJwtToken(user, expTimeInSeconds)
            val tokenId: String

            val existingUserToken = user.tokenList.find { it.split(":")[0] == ipAddress }
            val existingToken = if (existingUserToken != null) {
                authRepository.findToken(existingUserToken.split(":")[1])
            } else null

            if (existingUserToken != null && existingToken != null) {
                // Update token
                val updatedToken = authRepository.updateToken(
                    existingToken._id.toString(),
                    jwtToken,
                    expTimeInSeconds.toString()
                )
                tokenId = updatedToken._id.toString()
            } else {
                // Save token
                val token = Token(
                    token = jwtToken,
                    ipAddress = ipAddress,
                    expTimeInSeconds =  expTimeInSeconds
                )
                val savedToken = authRepository.saveToken(token)
                tokenId = savedToken._id.toString()

                // Add the new token to the user
                user.tokenList.remove(existingUserToken)
                user.tokenList.add("$ipAddress:${savedToken._id}")
                val updateUserFields = mapOf("tokenList" to user.tokenList)
                userRepository.updateUserById(user._id.toString(), updateUserFields)
            }

            val tokenWithJWTId = Token(
                _id = createJwtToken(tokenId),
                token = jwtToken,
                ipAddress = ipAddress,
                expTimeInSeconds =  expTimeInSeconds
            )

            breadcrumb.log("[AUTH_SERVICE]: SUCCESS login: token = $tokenWithJWTId")

            val response = LoginResponse(
                token = tokenWithJWTId,
                user = user
            )

            return response
        } catch (e: Throwable) {
            breadcrumb.log("[AUTH_SERVICE]: ERROR login: ${e.message}")
            throw e
        }
    }

    suspend fun logout(breadcrumb: Breadcrumb, call: ApplicationCall, user: User) {
        breadcrumb.log("[AUTH_SERVICE]: START logout")

        try {
            // Extract tokenId from JWTPrincipal
            val principal = call.principal<JWTPrincipal>()
            val tokenId = principal?.payload?.getClaim("tokenId")?.asString() ?: throw Throwable("Unauthorized")
            // Check and delete token in database
            val foundToken = authRepository.findToken(tokenId)
            if (foundToken != null) authRepository.deleteToken(tokenId)

            // Remove token in user's token list
            val existingUserToken = user.tokenList.find { it.split(":")[1] == tokenId }
            if (existingUserToken !== null) {
                user.tokenList.remove(existingUserToken)
                val updateUserFields = mapOf("tokenList" to user.tokenList)
                userRepository.updateUserById(user._id.toString(), updateUserFields)
            }
            breadcrumb.log("[AUTH_SERVICE]: SUCCESS logout")
        } catch (e: Throwable) {
            breadcrumb.log("[AUTH_SERVICE]: ERROR logout: ${e.message}")
            throw e
        }
    }
}