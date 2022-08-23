package com.example.routes

import com.example.models.Task
import com.example.models.lastId
import com.example.models.taskStorage
import com.example.services.uploadTaskImage
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val errorMessage: String)

@Serializable
data class SuccessResponse(val message: String)

fun Route.taskRouting() {
    route("/tasks") {
        get {
            if (taskStorage.isNotEmpty()) {
                return@get call.respond(taskStorage)
            }
            return@get call.respond(SuccessResponse("No tasks found"))
        }
        get("{id?}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing id"))

            val task = taskStorage.find { it.id == id.toInt() } ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("No task with id $id"))

            call.respond(task)
        }

        post {
            val task = call.receive<Task>()
            task.id = ++lastId
            taskStorage.add(task)
            call.respond(HttpStatusCode.Created, task)
        }

        patch {
            val updateTaskData = call.receive<Task>()
            taskStorage.find { it.id == updateTaskData.id }?.let {
                it.apply {
                    tittle = updateTaskData.tittle
                    description =  updateTaskData.description
                    priority = updateTaskData.priority
                }
                call.respond(HttpStatusCode.OK, it)
            } ?: call.respond(HttpStatusCode.NotFound, ErrorResponse("No task with id $updateTaskData.id"))
        }

        delete("{id?}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (taskStorage.removeIf { it.id == id.toInt() }) {
                call.respond(HttpStatusCode.OK, SuccessResponse("Removed successfully"))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("No task with id $id"))
            }
        }

        put("{id?}/image") {
            val id = call.parameters["id"]?.toInt() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val task = taskStorage.find { it.id == id } ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("No task with id $id"))

            val multipartData = call.receiveMultipart()
            val imageFormData = multipartData.readPart() as PartData.FileItem
            val fileUrl = uploadTaskImage(id, imageFormData)

            task.apply {
                imageUrl = fileUrl
            }

            call.respond(HttpStatusCode.OK, SuccessResponse("Image saved successfully"))
        }
    }
}


