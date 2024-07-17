@file:Suppress("propertyName")
package app.models

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

data class User(
    val _id: String? = null,
    val username: String,
    val password: String,
    val email: String,
    val mobile: String,
    val active: Boolean,
    val emailVerified: Boolean,
    val mobileVerified: Boolean,
    val tokenList: MutableList<String> = mutableListOf(),
    val createdAt: String = DateTime()
        .toDateTime(DateTimeZone.forID("Asia/Manila"))
        .toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"),
    val updatedAt: String = createdAt
) {
    fun toSafeResponse(): UserSafeResponse {
        return UserSafeResponse(
            username,
            email,
            mobile,
            active,
            emailVerified,
            mobileVerified,
            createdAt
        )
    }
}

data class UserSafeResponse(
    val username: String,
    val email: String,
    val mobile: String,
    val active: Boolean,
    val emailVerified: Boolean,
    val mobileVerified: Boolean,
    val createdAt: String
)