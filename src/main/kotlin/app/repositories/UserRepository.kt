package app.repositories

import app.plugins.*
import app.models.User
import app.utils.toDataClass
import app.utils.toDocument
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.*
import org.bson.Document
import org.bson.types.ObjectId

class UserRepository {
    private val userCollection = mongoDatabase.getCollection("users")

    // Create new user
    suspend fun createUser(user: User): User = withContext(Dispatchers.IO) {
        try {
            val userDoc = user.toDocument()
            userCollection.insertOne(userDoc)
            userDoc["_id"]?.let { findUserByProperty("_id", it) }
                ?: throw Throwable("Creating user failed.")
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    // Find a user
    suspend fun findUserByProperty(property: String, value: Any): User? = withContext(Dispatchers.IO) {
        try {
            userCollection.find(Document(property, value)).firstOrNull()?.toDataClass<User>()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    // Update a user
    suspend fun updateUserById(id: String, updateUserFields: Map<String, Any>) = withContext(Dispatchers.IO) {
        try {
            // Sample
            // val updateUserFields = mapOf(
            //    "username" to "newUsername",
            //    "email" to "newEmail@example.com",
            //    "mobile" to "+1234567890"
            // )

            val filter = Filters.eq("_id", ObjectId(id))
            val updates = updateUserFields.map { entry ->
                Updates.set(entry.key, entry.value)
            }
            userCollection.updateOne(filter, Updates.combine(updates))

            val updatedUser = userCollection.find(filter).firstOrNull()
                ?: throw Throwable("Updating user failed.")
            updatedUser.toDataClass<User>()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }
}