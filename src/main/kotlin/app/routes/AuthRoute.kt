package app.routes

import app.routes.requests.*
import app.routes.responses.*
import app.services.*
import app.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.routing.*

fun Route.authRoute() {
    val authService = AuthService()

    route("/auth") {
        post("/signup") {
            val breadcrumb = Breadcrumb()
            breadcrumb.log("[AUTH_ROUTE]: START /auth/sign_up")

            try {
                val reqBody = SignUpRequest.fromJson(call.getBodyAsJson())
                breadcrumb.log("[AUTH_ROUTE]: REQUEST BODY: $reqBody")

                val user = authService.signup(breadcrumb, reqBody)
                val response = Response(
                    success = true,
                    message = "Successfully created user.",
                    data = user.toSafeResponse()
                )
                call.result(response, HttpStatusCode.Created)
            } catch (e: Throwable) {
                val response = Response(
                    success = false,
                    message = "Failed to sign up. ${e.message}",
                    data = null
                )
                call.result(response, HttpStatusCode.BadRequest)
            }

            breadcrumb.log("[AUTH_ROUTE]: END /auth/sign_up")
        }

        post("/login") {
            val breadcrumb = Breadcrumb()
            breadcrumb.log("[AUTH_ROUTE]: START /auth/login")

            try {
                // Get the IP address of the client
                val ipAddress = call.request.origin.remoteHost
                breadcrumb.log("[AUTH_ROUTE]: CLIENT IP: $ipAddress")

                breadcrumb.log("[AUTH_ROUTE]: Getting request body...")
                val reqBody = LoginRequest.fromJson(call.getBodyAsJson())

                val loginResponse = authService.login(breadcrumb, reqBody, ipAddress)

                call.response.cookies.append(
                    Cookie(
                        name = "Authorization",
                        value = loginResponse.token._id.toString(),
                        httpOnly = true,
                        maxAge = 86400, // 24hrs
                        secure = true
                    )
                )

                val response = Response(
                    success = true,
                    message = "Successfully log in.",
                    data = loginResponse.user.toSafeResponse()
                )
                call.result(response, HttpStatusCode.Accepted)
            } catch (e: Throwable) {
                val response = Response(
                    success = false,
                    message = "Failed to log in. ${e.message}",
                    data = null
                )
                call.result(response, HttpStatusCode.Unauthorized)
            }

            breadcrumb.log("[AUTH_ROUTE]: END /auth/login")
        }

        authenticate {
            get("/authenticate") {
                val breadcrumb = Breadcrumb()
                breadcrumb.log("[AUTH_ROUTE]: START /auth/authenticate")

                try {
                    // Required for authenticating request
                    // Validate the principal produced by the authentication, can return the user's data if needed
                    val user = validatePrincipal(breadcrumb, call)

                    val response = Response(
                        success = true,
                        message = "Successfully authenticated.",
                        data = user.toSafeResponse()
                    )
                    call.result(response, HttpStatusCode.OK)
                } catch (e: Throwable) {
                    val response = Response(
                        success = false,
                        message = "Failed to authenticate. ${e.message}",
                        data = null
                    )
                    call.result(response, HttpStatusCode.Unauthorized)
                }

                breadcrumb.log("[AUTH_ROUTE]: END /auth/authenticate")
            }
        }

        authenticate {
            post("/logout") {
                val breadcrumb = Breadcrumb()
                breadcrumb.log("[AUTH_ROUTE]: START /auth/logout")

                try {
                    // Required for authenticating request
                    // Validate the principal produced by the authentication, can return the user's data if needed
                    val user = validatePrincipal(breadcrumb, call)

                    authService.logout(breadcrumb, call, user)

                    val response = Response(
                        success = true,
                        message = "Successfully log out.",
                        data = null
                    )
                    call.result(response, HttpStatusCode.OK)
                } catch (e: Throwable) {
                    val response = Response(
                        success = false,
                        message = "Failed to log out. ${e.message}",
                        data = null
                    )
                    call.result(response, HttpStatusCode.BadRequest)
                }

                breadcrumb.log("[AUTH_ROUTE]: END /auth/logout")
            }
        }
    }
}