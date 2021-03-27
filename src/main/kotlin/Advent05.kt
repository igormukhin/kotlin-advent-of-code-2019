import kotlinx.coroutines.runBlocking

fun main() {
    val input = Utils.readInput("Advent05")
    val nums = input.split(",").map { s -> s.toInt() }.toMutableList()

    taskA(nums)
    taskB(nums)
}

private fun taskA(nums: List<Int>) {
    val outputs = Task5.runProgram(nums, listOf(1))
    println("A: ${outputs.last()} ($outputs)")
}

private fun taskB(nums: List<Int>) {
    val outputs = Task5.runProgram(nums, listOf(5))
    println("B: $outputs")
}

object Task5 {
    suspend fun runProgram(program: List<Int>,
                           inputSupplier: suspend () -> Int,
                           outputConsumer: suspend (Int) -> Unit) {
        var pos = 0
        val nums = program.toMutableList()

        fun value(par : Int): Int {
            val mode = mode(nums[pos], par)
            return if (mode == 0) {
                nums[nums[pos + par]]
            } else {
                nums[pos + par]
            }
        }

l@      while (true) {
            when (nums[pos] % 100) {
                99 -> break@l
                1 -> { nums[nums[pos + 3]] = value(1) + value(2); pos += 4 }
                2 -> { nums[nums[pos + 3]] = value(1) * value(2); pos += 4 }
                3 -> { nums[nums[pos + 1]] = inputSupplier(); pos += 2 }
                4 -> { outputConsumer(value(1)); pos += 2 }
                5 -> { if (value(1) != 0) pos = value(2) else pos += 3 }
                6 -> { if (value(1) == 0) pos = value(2) else pos += 3 }
                7 -> { nums[nums[pos + 3]] = if (value(1) < value(2)) 1 else 0; pos += 4 }
                8 -> { nums[nums[pos + 3]] = if (value(1) == value(2)) 1 else 0; pos += 4 }
            }
        }
    }

    fun runProgram(program: List<Int>, inputs: List<Int>): List<Int> {
        val outputs = mutableListOf<Int>()
        var inputIdx = 0
        runBlocking {
            runProgram(program,
                { inputs[inputIdx++] },
                { outputs.add(it) })
        }
        return outputs
    }

    private fun mode(opcode: Int, par: Int): Int = (opcode / 10.pow(1 + par)) % 10
}

