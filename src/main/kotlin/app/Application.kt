@file:Suppress("UNUSED")
package app

import app.plugins.*
import app.utils.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val breadcrumb = Breadcrumb()
    breadcrumb.log("CRESCENS APPLICATION STARTING...")
    initializeDotenv(breadcrumb)
    initializeMongoDB(breadcrumb)
    initializeJwt(breadcrumb)
    initializeRouting(breadcrumb)
    initializeAdministration(breadcrumb)
    breadcrumb.log("CRESCENS APPLICATION IS READY :)")
}