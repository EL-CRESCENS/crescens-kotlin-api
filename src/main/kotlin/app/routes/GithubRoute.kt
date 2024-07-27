package app.routes

import app.plugins.dotenv
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.githubRoute() {

    route("/github") {
        post("/webhook") {
            try {
                val body = call.receive<Map<String, Any>>()

                // Check if the push event is on the master branch
                val ref = body["ref"] as? String ?: throw Throwable("Ref is null")

                val eventType = when (ref) {
                    "refs/heads/master" -> "trigger-master-workflow"
                    "refs/heads/master-dev" -> "trigger-master-dev-workflow"
                    else -> ""
                }

                if (eventType.isNotEmpty()) {
                    val client = HttpClient()
                    val response = client.post(
                        "https://api.github.com/repos/${dotenv["ORG_NAME"]}/${dotenv["GITHUB_ACTIONS_REPO_NAME"]}/dispatches"
                    ) {
                        headers {
                            append(HttpHeaders.Authorization, "token ${dotenv["GH_PAT"]}")
                            append(HttpHeaders.UserAgent, "Webhook-Service")
                        }
                        contentType(ContentType.Application.Json)
                        setBody(mapOf("event_type" to eventType))
                    }

                    // Log response status if needed
                    println("GitHub API response: ${response.status}")

                    call.respond(HttpStatusCode.OK, "Webhook received and processed")
                } else {
                    throw Throwable("Empty eventType.")
                }
            } catch (e : Throwable) {
                call.respond(HttpStatusCode.InternalServerError, "Error processing webhook: ${e.message}")
            }

        }
    }
}
