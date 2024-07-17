@file:Suppress("FunctionName", "UNUSED")
package app.utils

import com.google.gson.*

// JSONString or data class to JSONObject
fun JSONObject(data: Any): JsonObject {
    try {
        if (data::class.isData) {
            return JSONObject(Gson().toJson(data))
        }

        if (data is String && data.isNotEmpty()) {
            val dataTrim = data.trim()
            if (dataTrim.startsWith("{") && dataTrim.endsWith("}")) {
                return Gson().fromJson(data, JsonObject::class.java)
            }
        }

        throw IllegalArgumentException("Invalid JSON string.")
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        throw IllegalArgumentException("Invalid JSON string: ${e.message}")
    }
}

fun JsonObject.optString(key: String, defaultValue: String = ""): String =
    runCatching {
        this.get(key)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString ?: defaultValue
    }.getOrDefault(defaultValue)

fun JsonObject.optBoolean(key: String, defaultValue: Boolean = false): Boolean =
    runCatching {
        this.get(key)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isBoolean }?.asBoolean ?: defaultValue
    }.getOrDefault(defaultValue)

fun JsonObject.optJSONObject(key: String, defaultValue: JsonObject = JsonObject()): JsonObject =
    runCatching {
        this.get(key)?.takeIf { it.isJsonObject }?.asJsonObject ?: defaultValue
    }.getOrDefault(defaultValue)

fun JsonObject.optJSONArray(key: String, defaultValue: JsonArray = JsonArray()): JsonArray =
    runCatching {
        this.get(key)?.takeIf { it.isJsonArray }?.asJsonArray ?: defaultValue
    }.getOrDefault(defaultValue)

fun JsonObject.optDouble(key: String, defaultValue: Double = 0.0): Double =
    runCatching {
        this.get(key)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asDouble ?: defaultValue
    }.getOrDefault(defaultValue)

fun JsonObject.optInt(key: String, defaultValue: Int = 0): Int =
    runCatching {
        this.get(key)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asInt ?: defaultValue
    }.getOrDefault(defaultValue)
