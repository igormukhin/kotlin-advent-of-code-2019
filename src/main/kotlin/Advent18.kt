import kotlin.math.min

fun main() {
    val input = Utils.readInput("Advent18")

    val walls = mutableListOf<MutableList<Boolean>>()
    val keys = mutableMapOf<Char, Point>()
    val doors = mutableMapOf<Char, Point>()
    var meStart = Point()

    input.lines().forEachIndexed { i, ln ->
        if (i + 1 > walls.size) walls.add(mutableListOf())
        ln.forEachIndexed { j, ch ->
            val row = walls.last()
            row.add(false)
            when (ch) {
                '#' -> row[j] = true
                '@' -> meStart = Point(i, j)
                in 'a'..'z' -> keys[ch] = Point(i, j)
                in 'A'..'Z' -> doors[ch.toLowerCase()] = Point(i, j)
            }
        }
    }

    taskA(keys, doors, walls, meStart)
    taskB(keys, doors, walls, meStart)

}

private fun taskA(
    keys: MutableMap<Char, Point>,
    doors: MutableMap<Char, Point>,
    walls: MutableList<MutableList<Boolean>>,
    meStart: Point
) {
    val leastSteps = solve(keys, doors, walls, listOf(meStart))
    println("A: $leastSteps")
}

private fun taskB(
    keys: MutableMap<Char, Point>,
    doors: MutableMap<Char, Point>,
    walls: MutableList<MutableList<Boolean>>,
    me: Point
) {
    val robots = mutableListOf<Point>()
    robots.add(Point(me.x + 1, me.y + 1))
    robots.add(Point(me.x - 1, me.y - 1))
    robots.add(Point(me.x + 1, me.y - 1))
    robots.add(Point(me.x - 1, me.y + 1))

    walls[me.x][me.y] = true
    walls[me.x + 1][me.y] = true
    walls[me.x - 1][me.y] = true
    walls[me.x][me.y + 1] = true
    walls[me.x][me.y - 1] = true

    val leastSteps = solve(keys, doors, walls, robots)
    println("A: $leastSteps")
}

private fun solve(
    keys: MutableMap<Char, Point>,
    doors: MutableMap<Char, Point>,
    walls: MutableList<MutableList<Boolean>>,
    robotsInitial: List<Point>
): Int {
    val pointToKeys = keys.entries.associate { (k, v) -> v to k }
    val pointToDoors = doors.entries.associate { (k, v) -> v to k }
    val keysTaken = mutableSetOf<Char>()
    var leastSteps = Int.MAX_VALUE
    var solutions = 0
    val optimizationMap = mutableMapOf<Pair<Set<Char>, Char>, Int>()

    fun go(robots: List<Point>, steps: Int) {
        if (keysTaken.size == keys.size) {
            leastSteps = min(leastSteps, steps)
            solutions++
            if (solutions % 10000 == 0) println("solutions: $solutions, leastSteps: $leastSteps")
            return
        }

        fun optimize(p: Point, cost: Int): Boolean {
            val mapKey = keysTaken.toMutableSet() to pointToKeys[p]!!
            val value = optimizationMap[mapKey]
            return if (value == null || value > steps + cost) {
                optimizationMap[mapKey] = steps + cost
                true
            } else {
                false
            }
        }

        robots.forEachIndexed { robotIndex, robot ->
            // find all reachable not taken keys
            val reachableKeys = SalesmanHelpers.findTargets(robot,
                { p -> pointToKeys.contains(p) && !keysTaken.contains(pointToKeys[p]) },
                { sp ->
                    if (pointToKeys.contains(sp) && !keysTaken.contains(pointToKeys[sp])) {
                        emptyList()
                    } else {
                        Direction.values().map { dir -> sp.move(dir) }.filter { p ->
                            p.x >= 0 && p.y >= 0 && p.x < walls.size && p.y < walls[p.x].size
                                    && !walls[p.x][p.y]
                                    && !(pointToDoors.contains(p) && !keysTaken.contains(pointToDoors[p]))
                        }
                    }
                },
                { _, _ -> 1 }
            )

            val sortedKeys = reachableKeys
                .filter { (k, v) -> optimize(k, v) }
                .map { (k, v) -> k to v }
                .sortedBy { it.second }

            // iterate over them
            sortedKeys.forEach { (p, cost) ->
                val key = pointToKeys[p]!!
                keysTaken.add(key)
                val newRobots = robots.toMutableList()
                newRobots[robotIndex] = p
                go(newRobots, steps + cost)
                keysTaken.remove(key)
            }
        }
    }

    go(robotsInitial, 0)
    return leastSteps
}

private fun Point.move(dir: Direction, amount: Int = 1): Point {
    return when (dir) {
        Direction.NORTH -> Point(x - amount, y)
        Direction.SOUTH -> Point(x + amount, y)
        Direction.WEST -> Point(x, y - amount)
        Direction.EAST -> Point(x, y + amount)
    }
}