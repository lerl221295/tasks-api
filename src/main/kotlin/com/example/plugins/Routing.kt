package com.example.plugins
import com.example.routes.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
data class Response(
    val ok: Boolean,
    val message: String
)

fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        taskRouting()

        get("test", ::handlerTest)
    }
}

typealias RouteHandler = suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit

//fun getHandlerTest(): RouteHandler {
//    return {
//        call.respond(Response(true, "Hi test World"))
//    }
//}

// fun getHandlerTest(): RouteHandler = { // equivalent to the one before
//    call.respond(Response(true, "Hi test World"))
//}

//val getHandlerTest: () -> RouteHandler = { // equivalent to the one before
//    {
//        call.respond(Response(true, "Hi test World"))
//    }
//}

//val handlerTest: RouteHandler = { // preferred
//    call.respond(Response(true, "Hi test World"))
//}

//fn syntax. it works because:
// suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit == suspend (PipelineContext<Unit, ApplicationCall>, Unit) -> Unit
suspend fun handlerTest(c: PipelineContext<Unit, ApplicationCall>, @Suppress("UNUSED_PARAMETER")b: Unit) {
    c.call.respond(Response(true, "Hi test World"))
}

//val handlerTest = fun PipelineContext<Unit, ApplicationCall>.(_: Unit) {
//    runBlocking {
//        call.respond(Response(true, "Hi test World"))
//    }
//}



