package com.example.models
import kotlinx.serialization.Serializable

@Serializable
data class Task(var id: Int, var tittle: String, var description: String, var priority: Int)

var lastId = 0

val taskStorage = mutableListOf(
    Task(id = ++lastId, tittle = "Amar a mi esposo", description = "hacerle el amor muchas veces", priority = 1),
    Task(id = ++lastId, tittle = "Amar a mi esposo", description = "hacerle el amor muchas veces", priority = 1),
    Task(id = ++lastId, tittle = "Amar a mi esposo", description = "hacerle el amor muchas veces", priority = 1),
)
