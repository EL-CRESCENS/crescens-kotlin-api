package app.plugins

import app.routes.*
import app.utils.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.initializeRouting(breadcrumb: Breadcrumb) {
    breadcrumb.log("Routing initializing...")
    healthCheck()
    routing {
        route(dotenv["APP_URL_PREFIX"]) {
            authRoute()
//            userRoute()
        }
    }
}
