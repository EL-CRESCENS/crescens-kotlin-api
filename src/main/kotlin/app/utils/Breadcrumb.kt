package app.utils

import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class Breadcrumb {
    private val log = KotlinLogging.logger {}

    private var interval: DateTime = DateTime()
        .toDateTime(DateTimeZone.forID("Asia/Manila"))

    private var id = getRandomString()

    fun log(msg: String): String {
        try {
            val currentTime = DateTime().toDateTime(DateTimeZone.forID("Asia/Manila"))
            val diffInMillis = currentTime.millis - interval.millis
            log.info { "$id ${diffInMillis}ms - $msg" }
            interval = currentTime
            return id
        } catch (e: Throwable) {
            e.printStackTrace()
            return id
        }
    }

    private fun getRandomString(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..12)
            .map { allowedChars.random() }
            .joinToString("")
    }
}