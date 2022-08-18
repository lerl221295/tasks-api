package com.example.models
import kotlinx.serialization.Serializable

@Serializable
data class Task(var id: Int, var tittle: String, var description: String, var priority: Int)

var lastId = 0

val taskStorage = mutableListOf( // just some mock data to always start with something
    Task(id = ++lastId, tittle = "Go shopping", description = "get the necessary food :)", priority = 1),
    Task(id = ++lastId, tittle = "Cook dinner", description = "Make something delicious :)", priority = 1),
    Task(id = ++lastId, tittle = "Load bills", description = "Load bills on the monthly report :)", priority = 1),
)
