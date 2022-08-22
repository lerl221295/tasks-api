package com.example.routes
import aws.sdk.kotlin.runtime.auth.credentials.ProfileCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.model.S3Exception
import aws.smithy.kotlin.runtime.content.ByteStream
import com.example.models.Task
import com.example.models.lastId
import com.example.models.taskStorage
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


suspend fun uploadImage(taskId: Int, file: ByteArray): String {
    val s3Client = S3Client.fromEnvironment() {
        credentialsProvider = ProfileCredentialsProvider(profileName = "personal")
        region = "us-east-1"
    }

//    val metadataVal = mutableMapOf<String, String>()
//    metadataVal["myVal"] = "test"

    val fileName = "task-$taskId-image.jpg"

    val request = PutObjectRequest {
        bucket = "lerl221295-tasks-images"
        key = fileName
//        metadata = metadataVal
        body = ByteStream.fromBytes(file)
    }

    try {
        val response = s3Client.putObject(request)
        println("Tag information is ${response.eTag}")
        s3Client.close()
    } catch (e: S3Exception) {
        println("ERROR (S3Exception): ${e.sdkErrorMetadata.errorMessage}")
    } catch(e: Exception) {
        println("IDK")
    }



    return fileName
}

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

        post("{id?}/image") {
            var fileDescription = ""

            val id = call.parameters["id"]?.toInt() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val task = taskStorage.find { it.id == id } ?: return@post call.respond(HttpStatusCode.NotFound, ErrorResponse("No task with id $id"))

            val multipartData = call.receiveMultipart()

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        fileDescription = part.value
                    }

                    is PartData.FileItem -> {
//                        fileName = part.originalFileName as String
                        val fileName = uploadImage(id, part.streamProvider().readBytes())
                        task.apply {
                            imageUrl = fileName
                        }
//                        File("uploads/$fileName").writeBytes(fileBytes)
                    }

                    else -> {}
                }
            }

            call.respond(HttpStatusCode.OK, SuccessResponse("Image saved successfully"))
        }
    }
}


