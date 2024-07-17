@file:Suppress("propertyName")
package app.models

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

data class Token(
    val _id: String? = null,
    val token: String,
    val ipAddress: String,
    val expTimeInSeconds: Long,
    val createdAt: String = DateTime()
        .toDateTime(DateTimeZone.forID("Asia/Manila"))
        .toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"),
    val updatedAt: String = createdAt
)