fun main() {
    val input = Utils.readInput("Advent13")
    val program = input.split(",").map { s -> s.toLong() }

    taskA(program)
    taskB(program)
}

private fun taskA(program: List<Long>) {
    val (screen, _) = runGame(program)
    val blocks = screen.values.count { it == 2L }
    println("A: $blocks")
}

private fun taskB(program: List<Long>) {
    val modified = program.toMutableList()
    modified[0] = 2L
    val (_, score) = runGame(modified)
    println("B: $score")
}

@Suppress("unused")
private fun runGame(program: List<Long>): GameResult {
    val screen = mutableMapOf<Point, Long>()
    val outputs = mutableListOf<Long>()
    var score = 0L
    //var presses = 0

    Task9.runProgram(
        program,
        {
            /*
            println()
            println("Key presses = $presses")
            presses++

            printPointMap(screen, 0L) { _, v ->
                when (v.toInt()) {
                    1 -> "W"
                    2 -> "x"
                    3 -> "-"
                    4 -> "o"
                    else -> " "
                }
            }
            */

            val ballPos = screen.entries.filter { it.value == 4L }.map { it.key }.first()
            val paddleX = screen.entries.filter { it.value == 3L }.map { it.key }.first().x
            var key = 0

            // AI xD
            if (paddleX < ballPos.x) { // ball moves to the right
                key = 1
            } else if (paddleX > ballPos.x) { // ball moves to the left
                key = -1
            }

            //println("Move is $key")
            key.toLong()

            //keyControl()
        },
        {
            outputs.add(it)
            if (outputs.size == 3) {
                val (x, y, v) = outputs
                if (x == -1L && y == 0L) {
                    score = v
                } else {
                    screen[Point(x.toInt(), y.toInt())] = v
                }
                outputs.clear()
            }
        })

    return GameResult(screen, score)
}

private fun keyControl(): Long {
    print("Press A for move left, S to stay and D for move right: ")
    var key: Char
    while (true) {
        key = waitKeyPress().toLowerCase()
        if (key in listOf('a', 's', 'd')) break
    }

    return when (key) {
        'a' -> -1
        'd' -> 1
        else -> 0
    }
}

private data class GameResult(val screen: Map<Point, Long>, val score: Long)

private fun waitKeyPress(): Char {
    return System.`in`.read().toChar()
}