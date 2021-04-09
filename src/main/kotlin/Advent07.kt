import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.max

fun main() {
    val input = Utils.readInput("Advent07")
    val program = input.split(",").map { s -> s.toInt() }

    taskA(program)
    taskB(program)
}

private fun taskA(program: List<Int>) {
    var max = Int.MIN_VALUE
    permutations(5).forEach { max = max(calcA(it, program), max) }
    println("A: $max")
}

private fun calcA(settings: IntArray, program: List<Int>): Int {
    var signal = 0
    settings.forEach { setting ->
        signal = Task5.runProgram(program, listOf(setting, signal)).first()
    }
    return signal
}

private fun taskB(program: List<Int>) {
    var max = Int.MIN_VALUE
    permutations(5).forEach { max = max(calcB(it, program), max) }
    println("B: $max")
}

private fun calcB(settings: IntArray, program: List<Int>): Int {
    var output = 0

    runBlocking {
        val channels = Array<Channel<Int>>(settings.size) { Channel() }
        settings.indices.map { i ->
            launch {
                Task5.runProgram(program,
                    {
                        val n = channels[i].receive()
                        //println("program $i receives $n")
                        n
                    },
                    { n ->
                        //println("program $i outputs $n")
                        val otherIdx = (i + 1) % settings.size
                        channels[otherIdx].send(n)
                    })
                if (i == 0) output = channels[0].receive()
                //println("Program $i finished")
            }
        }

        settings.mapIndexed { idx, setting ->
            channels[idx].send(setting + 5)
            if (idx == 0) {
                channels[0].send(0)
            }
        }
    }

    return output
}


