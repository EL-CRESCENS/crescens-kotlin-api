package app.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.bson.Document


// Extension function to get request body string as JSON
suspend fun ApplicationCall.getBodyAsJson(): JsonObject {
    try {
        return Gson().fromJson(this.receiveText(), JsonObject::class.java)
    } catch (e: Throwable) {
        e.printStackTrace()
        throw Throwable("Invalid or missing request body.")
    }
}

// Extension function for data class to return route with json format
suspend fun <T : Any> ApplicationCall.result(data: T, statusCode: HttpStatusCode) {
    val functionName = object{}.javaClass.enclosingMethod.name
    if (!data::class.isData) {
        throw IllegalArgumentException("This function $functionName() can only be used with data classes.")
    }
    respondText(
        text = Gson().toJson(data),
        contentType = ContentType.Application.Json,
        status = statusCode
    )
}

// Extension function to convert data class to a Document for mongodb
inline fun <reified T : Any> T.toDocument(): Document {
    val functionName = object{}.javaClass.enclosingMethod.name
    if (!T::class.isData) {
        throw IllegalArgumentException("This function $functionName() can only be used with data classes.")
    }
    val json = Gson().toJson(this)
    return Document.parse(json)
}

// Extension function to convert Document to a specified data class
inline fun <reified T : Any> Document.toDataClass(): T {
    val functionName = object{}.javaClass.enclosingMethod.name
    if (!T::class.isData) {
        throw IllegalArgumentException("This function $functionName() can only be used with data classes.")
    }
    // Convert ObjectId to String if needed
    if (this.containsKey("_id")) {
        this["_id"] = this["_id"].toString()
    }
    return Gson().fromJson(this.toJson(), T::class.java)
}