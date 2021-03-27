
fun main() {
    val input = Utils.readInput("Advent11")
    val program = input.split(",").map { s -> s.toLong() }

    taskA(program)
    taskB(program)
}

private fun taskA(program: List<Long>) {
    val map = mutableMapOf<Point, Long>()
    runRobot(program, map)
    println("A: ${map.size}")
}

private fun taskB(program: List<Long>) {
    val map = mutableMapOf<Point, Long>()
    map[Point(0, 0)] = 1
    runRobot(program, map)

    println("B:")
    printPointMap(map, 0L) { _, v -> if (v == 1L) "XX" else "  " }
    println()
}

private fun runRobot(program: List<Long>, map: MutableMap<Point, Long>) {
    var robotPoint = Point(0, 0)
    var robotDir = Point(0, -1)
    var painting = true
    //var moves = 0

    Task9.runProgram(
        program,
        {
            //println("Step #${moves++}")
            //println("Reading map at $robotPoint = ${map[robotPoint] ?: 0L}")
            map[robotPoint] ?: 0L
        },
        {
            if (painting) {
                map[robotPoint] = it
                //println("Paint $robotPoint in $it")
            } else {
                robotDir = if (it == 1L) robotDir.turnClockwise() else robotDir.turnCounterClockwise()
                robotPoint += robotDir
                //println("Turn $it to $robotDir and move to $robotPoint")
            }
            painting = !painting
        })
}
