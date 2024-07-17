package app.routes.responses

import app.models.Token
import app.models.User

data class LoginResponse(
    val token: Token,
    val user: User
)
