package app.plugins

import app.repositories.AuthRepository
import app.utils.Breadcrumb
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.initializeJwt(breadcrumb: Breadcrumb) {
    breadcrumb.log("JWT initializing...")
    val authRepository = AuthRepository()
    val jwtAudience = dotenv["JWT_AUDIENCE"]
    val jwtIssuer = dotenv["JWT_ISSUER"]
    val jwtRealm = dotenv["JWT_REALM"]
    val jwtSecret = dotenv["JWT_SECRET"]

    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                val tokenId = credential.payload.getClaim("tokenId").asString()

                if (
                    tokenId != null &&
                    credential.payload.audience.contains(jwtAudience) &&
                    credential.payload.issuer.contains(jwtIssuer)
                ) {
                    authRepository.findToken(tokenId) ?: throw Throwable("Invalid tokenId.")
                    JWTPrincipal(credential.payload)
                }  else null
            }
        }
    }
}