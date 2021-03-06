private enum class MapCell {
    UNKNOWN, EMPTY, WALL
}

private fun Direction.asCode(): Long {
    return when (this) {
        Direction.UP -> 1
        Direction.DOWN -> 2
        Direction.LEFT -> 3
        Direction.RIGHT -> 4
    }
}

private fun Point.moveSpecial(dir: Direction, amount: Int = 1): Point {
    return when (dir) {
        Direction.UP -> Point(x, y - amount)
        Direction.DOWN -> Point(x, y + amount)
        Direction.LEFT -> Point(x - amount, y)
        Direction.RIGHT -> Point(x + amount, y)
    }
}

private class InterruptProgramException(message: String?) : RuntimeException(message)

fun main() {
    val input = Utils.readInput("Advent15")
    val program = input.split(",").map { s -> s.toLong() }

    // Task A
    val map = mutableMapOf<Point, MapCell>()
    var robot = Point()
    var nextRobot = Point()
    map[robot] = MapCell.EMPTY
    var valve = Point(Int.MIN_VALUE, Int.MIN_VALUE)
    val plannedMoves = mutableListOf(Direction.UP)
    try {
        Task9.runProgram(program, {
            val direction = plannedMoves.removeFirst()
            nextRobot = robot.moveSpecial(direction)
            direction.asCode()
        }, { signal ->
            // process signal
            when (signal.toInt()) {
                0 -> {
                    map[nextRobot] = MapCell.WALL
                    plannedMoves.clear()
                }
                1 -> {
                    robot = nextRobot
                    map[robot] = MapCell.EMPTY
                }
                2 -> {
                    robot = nextRobot
                    map[robot] = MapCell.EMPTY
                    valve = robot
                }
            }

            // plan further moves
            if (plannedMoves.isEmpty()) {
                plannedMoves.addAll(planMoves(map, robot))
            }
        })
    } catch (e: InterruptProgramException) {
        printMap(map, valve, robot)

        println("Oxygen valve is at $valve")

        val stepsToValve = Dijkstra.findShortestPath(Point(),
            { p -> p == valve },
            { p -> Direction.values().map { p.moveSpecial(it) }
                .filter { map[it] in listOf(MapCell.EMPTY, MapCell.UNKNOWN) } },
            { _, _ -> 1 }).size - 1
        println("A: $stepsToValve")
    }

    // Task B
    var oxygenPathLength = -1
    Dijkstra.findShortestPath(valve,
        { _ -> false },
        { p -> Direction.values().map { p.moveSpecial(it) }
            .filter { map[it]!! in listOf(MapCell.EMPTY) } },
        { _, _ -> 1 },
        { m -> oxygenPathLength = m.values.maxOrNull()!! })
    println("B: $oxygenPathLength")
}

private fun printMap(map: MutableMap<Point, MapCell>, valve: Point, robot: Point) {
    printPointMap(map, MapCell.UNKNOWN, { p, mc ->
        when {
            p == valve -> "[]"
            p == robot -> "**"
            mc == MapCell.EMPTY -> "  "
            mc == MapCell.WALL -> "XX"
            else -> "--"
        }
    })
}

private fun planMoves(map: Map<Point, MapCell>, robot: Point): List<Direction> {
    val pathToUnknown = Dijkstra.findShortestPath(robot,
        { p -> map[p] ?: MapCell.UNKNOWN == MapCell.UNKNOWN },
        { p -> Direction.values().map { p.moveSpecial(it) }
            .filter { map[it] ?: MapCell.UNKNOWN in listOf(MapCell.EMPTY, MapCell.UNKNOWN) } },
        { _, _ -> 1 })
    if (pathToUnknown.isEmpty()) throw InterruptProgramException("map is fully discovered")

    return (1 until pathToUnknown.size).map { i -> findDirection(pathToUnknown[i - 1], pathToUnknown[i]) }
}

private fun findDirection(from: Point, to: Point): Direction {
    return when {
        to.x > from.x -> Direction.RIGHT
        to.x < from.x -> Direction.LEFT
        to.y > from.y -> Direction.DOWN
        to.y < from.y -> Direction.UP
        else -> throw IllegalArgumentException()
    }
}
