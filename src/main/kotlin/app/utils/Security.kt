package app.utils

import app.models.Token
import app.models.User
import app.plugins.dotenv
import app.repositories.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import org.bson.types.ObjectId

val authRepository = AuthRepository()
val userRepository = UserRepository()

suspend fun validatePrincipal(breadcrumb: Breadcrumb, call: ApplicationCall): User {
    breadcrumb.log("[SECURITY]: Validating principal...")

    var userTokenId = ""
    var userToken = ""

    try {
        // Get ipAddress and principal
        val ipAddress = call.request.origin.remoteHost
        val principal = call.principal<JWTPrincipal>()

        // Extract tokenId from JWTPrincipal
        val tokenId = principal?.payload?.getClaim("tokenId")?.asString() ?: throw Throwable("Unauthorized")

        // Find the token data in database
        val token = authRepository.findToken(tokenId) ?: throw Throwable("Unauthorized")
        userTokenId = token._id.toString()
        userToken = token.token

        // Validate if the token is use in the same ipAddress
        if (ipAddress != token.ipAddress) throw Throwable("Unauthorized")

        // Verify and decode the token from the token data from database
        val decodedJWT = jwtVerify(token)

        // Extract userId from the token and find user data in database
        val userId = decodedJWT.getClaim("_id").asString()
        val user = userRepository.findUserByProperty("_id", ObjectId(userId)) ?: throw Throwable("Unauthorized")
        return user
    } catch (e: JWTVerificationException) {
        // If the token has expired
        if (e.message?.contains("The Token has expired") == true) {
            return refreshToken(breadcrumb, userToken, userTokenId)
        } else {
            breadcrumb.log("[SECURITY]: ERROR validatePrincipal: ${e.message}")
            throw e
        }
    } catch (e: Throwable) {
        breadcrumb.log("[SECURITY]: ERROR validatePrincipal: ${e.message}")
        throw e
    }
}

suspend fun refreshToken(breadcrumb: Breadcrumb, userToken: String, userTokenId: String): User {
    breadcrumb.log("[SECURITY]: Refreshing token...")

    try {
        // Decode the expired token to get claims
        val decodedJWT = JWT.decode(userToken)

        // Extract userId from the expired token
        val userId = decodedJWT.getClaim("_id").asString()

        // Refresh the token
        val expInSeconds = dotenv["JWT_EXPIRATION"].toInt()
        val currentTimeInSeconds = System.currentTimeMillis() / 1000
        val expTimeInSeconds = currentTimeInSeconds + expInSeconds
        val newJwtToken = JWT.create()
            .withAudience(dotenv["JWT_AUDIENCE"])
            .withIssuer(dotenv["JWT_ISSUER"])
            .withClaim("_id", userId)
            .withClaim("exp", expTimeInSeconds)
            .sign(Algorithm.HMAC256(dotenv["JWT_SECRET"]))

        // Update token in database
        authRepository.updateToken(userTokenId, newJwtToken, expTimeInSeconds.toString())

        // Retrieve the user
        val user = userRepository.findUserByProperty("_id", ObjectId(userId)) ?: throw Throwable("Unauthorized")
        return user
    } catch (e: Throwable) {
        breadcrumb.log("[SECURITY]: ERROR Refreshing token: ${e.message}")
        throw e
    }
}

fun createUserJwtToken(user: User, expTimeInSeconds: Long): String {
    try {
        return JWT.create()
            .withAudience(dotenv["JWT_AUDIENCE"])
            .withIssuer(dotenv["JWT_ISSUER"])
            .withClaim("_id", user._id)
            .withClaim("exp", expTimeInSeconds)
            .sign(Algorithm.HMAC256(dotenv["JWT_SECRET"]))
    } catch (e: Throwable) {
        throw e
    }
}

fun createJwtToken(string: String): String {
    try {
        val currentTimeInSeconds = System.currentTimeMillis() / 1000
        val expTimeInSeconds = currentTimeInSeconds + 86400 // 24hrs

        return JWT.create()
            .withAudience(dotenv["JWT_AUDIENCE"])
            .withIssuer(dotenv["JWT_ISSUER"])
            .withClaim("tokenId", string)
            .withClaim("exp", expTimeInSeconds)
            .sign(Algorithm.HMAC256(dotenv["JWT_SECRET"]))
    } catch (e: Throwable) {
        throw e
    }
}

fun jwtVerify(token: Token): DecodedJWT {
    try {
        val jwtVerifier = JWT
            .require(Algorithm.HMAC256(dotenv["JWT_SECRET"]))
            .withAudience(dotenv["JWT_AUDIENCE"])
            .withIssuer(dotenv["JWT_ISSUER"])
            .build()
        return jwtVerifier.verify(token.token)
    } catch (e: Throwable) {
        throw e
    }
}