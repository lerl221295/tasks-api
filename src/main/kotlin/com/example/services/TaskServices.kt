package com.example.services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import io.ktor.http.content.*

suspend fun uploadTaskImage(taskId: Int, file: PartData.FileItem): String {
    val bucketName = "lerl221295-tasks-images" // TODO: get this from config (env)
    val s3Client = S3Client.fromEnvironment()

    val originalName = file.originalFileName ?: throw Exception("this is bad")
    val extension = originalName.substring(originalName.lastIndexOf(".") + 1)
    val fileName = "task-$taskId-image.$extension"

    val fileStreamByes = ByteStream.fromBytes(file.streamProvider().readBytes())
    val request = PutObjectRequest {
        bucket = bucketName
        key = fileName
        body = fileStreamByes
        contentType = "image/$extension"
    }

    s3Client.use {
        it.putObject(request)
    }

    return "https://$bucketName.s3.amazonaws.com/$fileName"
}