package app.routes

import app.plugins.dotenv
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.velocity.*
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

fun Application.healthCheck() {
    install(Velocity) {
        setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath")
        setProperty("classpath.resource.loader.class", ClasspathResourceLoader::class.java.name)
    }
    routing {
        route(dotenv["APP_URL_PREFIX"]) {
            // welcome-note
            get {
                call.respond(VelocityContent("templates/index.vl", mapOf()))
            }
            get("/healthcheck") {
                call.respondText("I'm healthy, are you? Looks like you need a life check. :)")
            }
        }
    }
}