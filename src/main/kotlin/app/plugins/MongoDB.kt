package app.plugins

import com.mongodb.client.*
import com.mongodb.client.model.*
import app.utils.*
import io.ktor.server.application.*
import org.bson.*

/**
 * Establishes connection with a MongoDB database.
 * */

lateinit var mongoDatabase: MongoDatabase
fun Application.initializeMongoDB(breadcrumb: Breadcrumb) {
    breadcrumb.log("MongoDB initializing...")
    val mongoClient = MongoClients.create(dotenv["MONGODB_CONNECTION_STRING"])
    mongoDatabase = mongoClient.getDatabase(dotenv["MONGODB_DATABASE_NAME"])

    // *CREATE INDEXES HERE* //
    // Users indexes
    val userCollection = mongoDatabase.getCollection("users")
    val userIndexes = listOf(
        IndexModel(Document("username", 1), IndexOptions().unique(true)),
        IndexModel(Document("email", 1), IndexOptions().unique(true)),
        IndexModel(Document("mobile", 1), IndexOptions().unique(true))
    )
    userCollection.createIndexes(userIndexes)

    environment.monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }
}