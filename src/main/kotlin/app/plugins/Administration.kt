package app.plugins

import app.utils.Breadcrumb
import io.ktor.server.application.*
import io.ktor.server.engine.*

fun Application.initializeAdministration(breadcrumb: Breadcrumb) {
    breadcrumb.log("Administration initializing...")
    install(ShutDownUrl.ApplicationCallPlugin) {
        // The URL that will be intercepted (you can also use the application.conf's ktor.deployment.shutdown.url key)
        shutDownUrl = dotenv["APP_SHUTDOWN_URL"]
        // A function that will be executed to get the exit code of the process
        exitCodeSupplier = { 0 } // ApplicationCall.() -> Int
    }
}