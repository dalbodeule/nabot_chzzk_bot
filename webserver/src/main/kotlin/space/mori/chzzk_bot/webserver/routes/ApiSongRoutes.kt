package space.mori.chzzk_bot.webserver.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import space.mori.chzzk_bot.common.models.SongList
import space.mori.chzzk_bot.common.services.SongListService
import space.mori.chzzk_bot.common.services.UserService

@Serializable
data class SongsDTO(
    val url: String,
    val name: String,
    val author: String,
    val time: Int,
    val reqName: String
)

fun SongList.toDTO(): SongsDTO = SongsDTO(
    this.url,
    this.name,
    this.author,
    this.time,
    this.reqName
)

fun Routing.apiSongRoutes() {
    route("/songs/{uid}") {
        get {
            val uid = call.parameters["uid"]
            val user = uid?.let { it1 -> UserService.getUser(it1) }
            if (user == null) {
                call.respondText("No user found", status = HttpStatusCode.NotFound)
                return@get
            }

            val songs = SongListService.getSong(user)
            call.respond(HttpStatusCode.OK, songs.map { it.toDTO() })
        }
    }
    route("/songs") {
        get {
            call.respondText("Require UID", status= HttpStatusCode.BadRequest)
        }
    }
}