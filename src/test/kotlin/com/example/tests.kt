import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class Obj(val id: String, var tittle: String, var description: String)

fun main(){
//    println("handleIntOperation { plus(2) } -> ${ handleIntOperation { plus(2) }}") // 3
//    println("handleIntOperation(::resta) -> ${handleIntOperation(::resta)}") // -1


    val set = mutableSetOf(1,2,2,3,4)
    println(if (2 in set) "2 in the house" else "Nope")
    val map = mutableMapOf(1 to 1, 2 to 2, 3 to 3)
    println("1 in map ? ${1 in map}")

    val list = mutableListOf(Obj("123", "Obj1", "descriptive"), Obj("124", "Obj2", "descriptive"), Obj("125", "Obj3", "descriptive"))
    val o = list.find { it.id == "124" } ?: throw Exception("Not found")
    o.apply {
        description = "new description"
    }

    //doComplicatedStuff()

    //println("List: ${Json.encodeToString(list)}")

    suspend fun doDelayed(cb: () -> Unit) {
        val r = (1..10).random()
        //println("waiting $r seconds")
        delay(r*1000L)
        print("After $r seconds -> ")
        cb()
    }

    runBlocking {
        list.forEach { launch {
            doDelayed { println("Hi $it") }
        }}
    }
    println("Finished")
}

// TRYING THINGS OUT

typealias IntHandler = Int.() -> Int

fun handleIntOperation(cb: IntHandler): Int {
    return 1.cb()
}

fun resta(a: Int): Int { // USE :: reference to make it work
    return a.minus(2)
}

val f: IntHandler = ::resta

fun doComplicatedStuff() {
    TODO("Lack of knowledge")
}

//val resta: (Int) -> Int = {
//    it.minus(2)
//}
