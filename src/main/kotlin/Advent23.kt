import java.lang.RuntimeException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.system.exitProcess

fun main() {
    val input = Utils.readInput("Advent23")
    val intcode = input.split(",").map { s -> s.toLong() }

    val size = 50
    val lock = ReentrantLock()
    val inputQueues = Array(size) { mutableListOf(it.toLong()) }
    val emptyInputQueueCounters = Array(size) { 0 }
    val outputQueues = Array(size) { mutableListOf<Long>() }
    var natMemory = Pair(-1L, -1L)
    var aSolved = false

    fun <T> withLock(block: () -> T): T {
        try {
            lock.lock()
            return block()
        } finally {
            lock.unlock()
        }
    }

    fun withLock(block: () -> Unit) {
        try {
            lock.lock()
            block()
        } finally {
            lock.unlock()
        }
    }

    (0 until size).map { addr -> thread {
        Task9.runProgram(intcode, {
           withLock<Long> {
               if (inputQueues[addr].isEmpty()) {
                   emptyInputQueueCounters[addr]++
                   -1L
               } else {
                   emptyInputQueueCounters[addr] = 0
                   try {
                       inputQueues[addr].removeFirst()
                   } catch (e: Exception) {
                       throw RuntimeException("$addr", e)
                   }
               }
           }
        }, { out ->
            withLock {
                outputQueues[addr].add(out)
                if (outputQueues[addr].size == 3) {
                    val (dest, x, y) = outputQueues[addr]
                    outputQueues[addr].clear()

                    if (dest == 255L) {
                        if (!aSolved) {
                            aSolved = true
                            println("A: $y")
                        }
                        natMemory = Pair(x, y)
                    } else {
                        val inputQueue = inputQueues[dest.toInt()]
                        inputQueue.add(x)
                        inputQueue.add(y)
                    }
                }
            }
        })
    } }

    var deliveredY: Long? = null
    thread {
        do {
            withLock {
                if (natMemory.first != -1L && emptyInputQueueCounters.all { it > 1000 }) {
                    if (deliveredY == natMemory.second) {
                        println("B: $deliveredY")
                        exitProcess(0)
                    }

                    //println("NAT sends $natMemory")
                    inputQueues[0].add(natMemory.first)
                    inputQueues[0].add(natMemory.second)
                    deliveredY = natMemory.second

                    emptyInputQueueCounters.fill(0)
                }
            }

        } while (true)
    }

}
