import kotlin.math.min

fun main() {
    val input = Utils.readInput("Advent18")

    val walls = mutableListOf<MutableList<Boolean>>()
    val keys = mutableMapOf<Char, Point>()
    val doors = mutableMapOf<Char, Point>()
    var me = Point()

    input.lines().forEachIndexed { i, ln ->
        if (i + 1 > walls.size) walls.add(mutableListOf())
        ln.forEachIndexed { j, ch ->
            val row = walls.last()
            row.add(false)
            when (ch) {
                '#' -> row[j] = true
                '@' -> me = Point(i, j)
                in 'a'..'z' -> keys[ch] = Point(i, j)
                in 'A'..'Z' -> doors[ch.toLowerCase()] = Point(i, j)
            }
        }
    }

    taskA(keys, doors, walls, me)
    taskB(keys, doors, walls, me)

}

private fun taskA(
    keys: MutableMap<Char, Point>,
    doors: MutableMap<Char, Point>,
    walls: MutableList<MutableList<Boolean>>,
    me: Point
) {
    val leastSteps = solve(keys, doors, walls, listOf(me))
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
    val optimizationMap = mutableMapOf<Pair<Set<Char>, List<Point>>, Int>()

    fun go(robots: List<Point>, steps: Int) {
        if (keysTaken.size == keys.size) {
            leastSteps = min(leastSteps, steps)
            return
        }

        robots.forEachIndexed { robotIndex, robot ->
            // find all reachable not taken keys
            val reachableKeys = Dijkstra.findTargets(robot,
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

            fun optimize(p: Point, cost: Int): Boolean {
                val newRobots = robots.toMutableList()
                newRobots[robotIndex] = p
                val mapKey = keysTaken.toMutableSet() to newRobots
                val value = optimizationMap[mapKey]
                return if (value == null || value > steps + cost) {
                    optimizationMap[mapKey] = steps + cost
                    true
                } else {
                    false
                }
            }

            val optimizedKeys = reachableKeys.filter { (k, v) -> optimize(k, v) }

            // iterate over them
            optimizedKeys.forEach { (p, cost) ->
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
