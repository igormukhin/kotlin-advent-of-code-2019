fun main() {
    val input = Utils.readInput("Advent21")
    val intcode = input.split(",").map { s -> s.toLong() }

    run {
        val program = listOf(
            "NOT C J", // if C is empty, jump

            "NOT A T", // if next spot is empty, always jump
            "OR T J",

            "AND D J", // if landing spot is empty, dont jump

            "WALK"
        )

        val output = Task9.runProgram(intcode, program.joinToString(separator = "") { it + 10.toChar() }
            .map { it.toLong() })
        println("A: ")
        output.forEach { n -> if (n < 255) print(n.toChar()) else println(n) }
    }

    run {
        val program = listOf(
            "NOT C J", // if C is empty and either E or H is a brick, jump
            "NOT E T",
            "NOT T T",
            "OR H T",
            "AND T J",

            "NOT B T", // if B and E are empty, jump
            "NOT T T",
            "OR E T",
            "NOT T T",
            "OR T J",

            "NOT A T", // if next spot is empty, always jump
            "OR T J",

            "AND D J", // if landing spot is empty, never jump

            "RUN"
        )

        val output = Task9.runProgram(intcode, program.joinToString(separator = "") { it + 10.toChar() }
            .map { it.toLong() })
        println("B: ")
        output.forEach { n -> if (n < 255) print(n.toChar()) else println(n) }
    }
}
