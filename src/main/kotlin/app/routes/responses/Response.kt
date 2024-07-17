package app.routes.responses

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

data class Response<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    private val timestamp: String = DateTime()
        .toDateTime(DateTimeZone.forID("Asia/Manila"))
        .toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
)
