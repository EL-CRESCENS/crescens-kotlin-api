package app.repositories

import app.models.*
import app.plugins.*
import app.utils.toDataClass
import app.utils.toDocument
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.*
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class AuthRepository {
    private val tokenCollection = mongoDatabase.getCollection("token")

    // Save token in database
    suspend fun saveToken(token: Token): Token = withContext(Dispatchers.IO) {
        try {
            val tokenDoc = token.toDocument()
            tokenCollection.insertOne(tokenDoc)

            val filter = Filters.eq("_id", tokenDoc["_id"])
            val savedTokenDoc = tokenCollection.find(filter).firstOrNull()
                ?: throw Throwable("Saving token failed.")
            savedTokenDoc.toDataClass<Token>()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    // Update token in database
    suspend fun updateToken(id: String, jwtToken: String, expTimeInSeconds: String): Token = withContext(Dispatchers.IO) {
        try {
            val dateNow = DateTime()
                .toDateTime(DateTimeZone.forID("Asia/Manila"))
                .toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
            val filter = Filters.eq("_id", ObjectId(id))
            tokenCollection.updateOne(
                filter,
                Updates.combine(
                    Updates.set("token", jwtToken),
                    Updates.set("expTimeInSeconds", expTimeInSeconds),
                    Updates.set("updatedAt", dateNow)
                )
            )

            val savedTokenDoc = tokenCollection.find(filter).firstOrNull()
                ?: throw Throwable("Updating token failed.")
            savedTokenDoc.toDataClass<Token>()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    // Find token in database
    suspend fun findToken(id: String): Token? = withContext(Dispatchers.IO) {
        try {
            tokenCollection.find(Filters.eq("_id", ObjectId(id))).firstOrNull()
                ?.toDataClass<Token>()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    // Delete token in database
    suspend fun deleteToken(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            tokenCollection.deleteOne(Filters.eq("_id", ObjectId(id)))
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

}