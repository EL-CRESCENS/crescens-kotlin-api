package app.plugins

import app.utils.Breadcrumb
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.DotenvException

lateinit var dotenv: Dotenv
fun initializeDotenv(breadcrumb: Breadcrumb) {
    breadcrumb.log("Dotenv initializing...")
    dotenv = try {
        Dotenv
            .configure()
            .directory("app/resources/.env")
            .load()
    } catch (e: DotenvException) {
        Dotenv
            .configure()
            .directory("src/main/resources/.env")
            .load()
    }
}