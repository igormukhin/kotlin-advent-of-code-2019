fun main() {
    val input = Utils.readInput("Advent09")
    val program = input.split(",").map { s -> s.toLong() }.toMutableList()

    println("A: " + Task9.runProgram(program, listOf(1)))
    println("B: " + Task9.runProgram(program, listOf(2)))
}

object Task9 {

    fun runProgram(program: List<Long>, inputs: List<Long>): List<Long> {
        var inputIdx = 0
        val outputs = mutableListOf<Long>()
        runProgram(program, { inputs[inputIdx++] }, { outputs.add(it) })
        return outputs
    }

    fun runProgram(program: List<Long>,
                           inputSupplier: () -> Long,
                           outputConsumer: (Long) -> Unit) {
        var pos = 0L
        var relBase = 0L
        val mem: MutableMap<Long, Long> = program.mapIndexed { i, v -> i.toLong() to v }.associate { it }
                .toMutableMap()

        fun readAt(addr: Long): Long = mem[addr] ?: 0
        fun write(par: Int, value: Long) {
            fun writeAt(addr: Long, value: Long) { mem[addr] = value }
            when (mode(readAt(pos), par)) {
                0, 1 -> writeAt(readAt(pos + par), value)
                2 -> writeAt(relBase + readAt(pos + par), value)
                else -> throw IllegalArgumentException()
            }
        }

        fun value(par : Int): Long {
            return when (mode(readAt(pos), par)) {
                0 -> readAt(readAt(pos + par))
                1 -> readAt(pos + par)
                2 -> readAt(relBase + readAt(pos + par))
                else -> throw IllegalArgumentException()
            }
        }

        l@ while (true) {
            val opcode = readAt(pos)
            when (command(opcode)) {
                1 -> { write(3, value(1) + value(2)); pos += 4 }
                2 -> { write(3, value(1) * value(2)); pos += 4 }
                3 -> { write(1, inputSupplier()); pos += 2 }
                4 -> { outputConsumer(value(1)); pos += 2 }
                5 -> { if (value(1) != 0L) pos = value(2) else pos += 3 }
                6 -> { if (value(1) == 0L) pos = value(2) else pos += 3 }
                7 -> { write(3, if (value(1) < value(2)) 1 else 0); pos += 4 }
                8 -> { write(3, if (value(1) == value(2)) 1 else 0); pos += 4 }
                9 -> { relBase += value(1); pos += 2 }
                99 -> break@l
            }
        }
    }

    private fun command(opcode: Long) = (opcode % 100).toInt()
    private fun mode(opcode: Long, par: Int): Int = (opcode.toInt() / 10.pow(1 + par)) % 10

}

