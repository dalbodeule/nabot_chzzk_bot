package space.mori.chzzk_bot.webserver.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import space.mori.chzzk_bot.common.services.CommandService
import space.mori.chzzk_bot.common.services.UserService
import space.mori.chzzk_bot.webserver.UserSession

fun Routing.apiCommandRoutes() {
    route("/commands") {
        get("/{uid}") {
            val uid = call.parameters["uid"]
            val session = call.sessions.get<UserSession>()
            if(uid == null) {
                call.respond(HttpStatusCode.BadRequest, "UID is required")
                return@get
            }
            val user = UserService.getUser(uid)
            if(user == null || user.naverId != session?.id) {
                call.respond(HttpStatusCode.BadRequest, "User does not exist")
                return@get
            }

            val commands = CommandService.getCommands(user)

            call.respond(HttpStatusCode.OK, commands.map {
                CommandsResponseDTO(it.command, it.content, it.failContent)
            })
        }

        put("/{uid}") {
            val uid = call.parameters["uid"]
            val session = call.sessions.get<UserSession>()
            val commandRequest = call.receive<CommandsRequestDTO>()
            if(uid == null) {
                call.respond(HttpStatusCode.BadRequest, "UID is required")
                return@put
            }
            val user = UserService.getUser(uid)
            if(user == null || user.naverId != session?.id) {
                call.respond(HttpStatusCode.BadRequest, "User does not exist")
                return@put
            }

            CommandService.saveCommand(user,
                commandRequest.label,
                commandRequest.content,
                commandRequest.failContent ?: ""
            )
            call.respond(HttpStatusCode.OK)
        }

        post("/{uid}") {
            val uid = call.parameters["uid"]
            val session = call.sessions.get<UserSession>()
            val commandRequest = call.receive<CommandsRequestDTO>()
            if(uid == null) {
                call.respond(HttpStatusCode.BadRequest, "UID is required")
                return@post
            }
            val user = UserService.getUser(uid)
            if(user == null || user.naverId != session?.id) {
                call.respond(HttpStatusCode.BadRequest, "User does not exist")
                return@post
            }

            try {
                CommandService.updateCommand(
                    user,
                    commandRequest.label,
                    commandRequest.content,
                    commandRequest.failContent ?: ""
                )
                call.respond(HttpStatusCode.OK)
            } catch(e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        delete("/{uid}") {
            val uid = call.parameters["uid"]
            val session = call.sessions.get<UserSession>()
            val commandRequest = call.receive<CommandsRequestDTO>()
            if(uid == null) {
                call.respond(HttpStatusCode.BadRequest, "UID is required")
                return@delete
            }
            val user = UserService.getUser(uid)
            if(user == null || user.naverId != session?.id) {
                call.respond(HttpStatusCode.BadRequest, "User does not exist")
                return@delete
            }

            try {
                CommandService.removeCommand(user, commandRequest.label)
                call.respond(HttpStatusCode.OK)
            } catch(e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}


@Serializable
data class CommandsRequestDTO(
    val label: String,
    val content: String,
    val failContent: String?
)

@Serializable
data class CommandsResponseDTO(
    val label: String,
    val content: String,
    val failContent: String?
)
