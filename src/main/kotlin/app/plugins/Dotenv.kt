package app.plugins

import app.utils.Breadcrumb
import io.github.cdimascio.dotenv.Dotenv

lateinit var dotenv: Dotenv
fun initializeDotenv(breadcrumb: Breadcrumb) {
    breadcrumb.log("Dotenv initializing...")
    dotenv = Dotenv
        .configure()
        .directory("src/main/resources/.env") // Specify the correct directory
        .load()
}