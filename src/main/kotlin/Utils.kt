import kotlin.math.max
import kotlin.math.min

object Utils {
    fun readInput(dataSetName : String) : String {
        val resource = this::class.java.getResource("/$dataSetName.txt")!!
        return resource.readText()
    }
}

data class Point(val x: Int = 0, val y: Int = 0) {
    fun turnClockwise(): Point = Point(-y, x)
    fun turnCounterClockwise(): Point = Point(y, -x)
    operator fun plus(op: Point): Point = Point(x + op.x, y + op.y)
}

data class Point3D(val x: Int = 0, val y: Int = 0, val z: Int = 0) {
    operator fun plus(op: Point3D): Point3D = Point3D(x + op.x, y + op.y, z + op.z)
}

enum class Direction {
    NORTH, SOUTH, WEST, EAST;

    fun turnRight(): Direction {
        return when (this) {
            NORTH -> EAST
            SOUTH -> WEST
            WEST -> NORTH
            EAST -> SOUTH
        }
    }

    fun turnLeft(): Direction {
        return when (this) {
            NORTH -> WEST
            SOUTH -> EAST
            WEST -> SOUTH
            EAST -> NORTH
        }
    }
}

fun Int.pow(power: Int): Int {
    return when {
        power < 0 -> throw IllegalArgumentException("negative power not supported")
        power == 0 -> 1
        power == 1 -> this
        else -> (2..power).fold(this) { r, _ -> r * this }
    }
}

/**
 * Permutes numbers from 0 to maxIndex (inclusive) and executes the block for each permutation.
 */
fun permuteIndexes(maxIndex: Int, block: (IntArray) -> Unit) {
    permuteIndexes(maxIndex, maxIndex + 1, block)
}

/**
 * Permutes numbers from 0 to maxIndex (inclusive) in N-places and executes the block for each permutation.
 */
fun permuteIndexes(maxIndex: Int, places: Int, block: (IntArray) -> Unit) {
    assert(places <= maxIndex + 1) { "too many places" }
    assert(places > 0) { "too few places" }
    val current = IntArray(places)
    val used = BooleanArray(maxIndex + 1)
    permuteIndexes(0, maxIndex, current, used, block)
}

private fun permuteIndexes(currentIndex: Int, maxIndex: Int, current: IntArray,
                           used: BooleanArray, block: (IntArray) -> Unit) {
    for (i in 0..maxIndex) {
        if (used[i]) continue
        used[i] = true
        current[currentIndex] = i
        if (currentIndex == current.size - 1) {
            block(current)
        } else {
            permuteIndexes(currentIndex + 1, maxIndex, current, used, block)
        }
        used[i] = false
    }
}

fun greatestCommonDivisor(pa: Long, pb: Long): Long {
    var a = pa
    var b = pb
    while (b > 0) {
        val temp = b
        b = a % b
        a = temp
    }
    return a
}

fun greatestCommonDivisor(nums: List<Long>): Long {
    return nums.reduce { acc, n -> greatestCommonDivisor(acc, n) }
}

fun leastCommonMultiple(a: Long, b: Long): Long {
    return a * (b / greatestCommonDivisor(a, b))
}

fun leastCommonMultiple(nums: List<Long>): Long {
    return nums.reduce { acc, n -> leastCommonMultiple(acc, n) }
}

fun <T> printPointMap(map: Map<Point, T>, defaultValue: T, block: (Point, T) -> String) {
    var top = Int.MAX_VALUE
    var left = Int.MAX_VALUE
    var bottom = Int.MIN_VALUE
    var right = Int.MIN_VALUE
    map.forEach { (p, _) ->
        top = min(p.y, top)
        left = min(p.x, left)
        bottom = max(p.y, bottom)
        right = max(p.x, right)
    }

    println("Map from top=$top, left=$left to bottom=$bottom, right=$right")
    for (y in top..bottom) {
        for (x in left..right) {
            val point = Point(x, y)
            print(block(point, map[point] ?: defaultValue))
        }
        println()
    }
}

fun LongRange.binarySearch(comparator: (Long) -> Int,
        notFoundHandler: ((boundary1: Long, boundary2: Long) -> Long)? = null): Long {
    var min = this.first
    var max = this.last

    do {
        val mid = min + ((max - min) / 2)
        val comp = comparator(mid)
        when {
            comp > 0 -> max = mid - 1
            comp < 0 -> min = mid + 1
            else -> return mid
        }

        if (min >= max) {
            if (notFoundHandler == null) {
                throw IllegalArgumentException("Element not found")
            } else {
                return notFoundHandler(min, max)
            }
        }
    } while (true)
}


// Travelling Salesman Problem algorithm helpers
object SalesmanHelpers {

    /**
     * C - coordinate
     * Costs is currently Int. Initial cost is 0. Costs are summed.
     *
     * onTargetNotFound called with costs map
     *
     * @return the resulting path includes start and target
     */
    fun <C> findShortestPath(
        start: C,
        targetTester: (pos: C) -> Boolean,
        validMovesResolver: (pos: C) -> List<C>,
        moveCostResolver: (from: C, to: C) -> Int,
        onTargetNotFound: ((costs: Map<C, Int>) -> Unit)? = null
    ): List<C> {
        val costs = mutableMapOf<C, Int>()
        val paths = mutableMapOf<C, C>() // key - point, value - how we got there
        var updated = setOf(start)
        costs[start] = 0
        do {
            val newlyUpdated = mutableSetOf<C>()
            updated.forEach { coordinate ->
                val cost = costs[coordinate]!!
                validMovesResolver(coordinate).forEach { move ->
                    val newCost = cost + moveCostResolver(coordinate, move)
                    if (costs[move] == null || costs[move]!! > newCost) {
                        costs[move] = newCost
                        paths[move] = coordinate
                        newlyUpdated.add(move)
                    }

                    if (targetTester(move)) {
                        return backtrackPath(paths, start, move)
                    }
                }
            }
            updated = newlyUpdated
        } while (updated.isNotEmpty())

        // not found
        onTargetNotFound?.let { it(costs) }
        return listOf()
    }

    /**
     * Find the path from start to target
     */
    private fun <C> backtrackPath(origins: Map<C, C>, start: C, target: C): List<C> {
        val steps = mutableListOf(target)
        var pos = target
        do {
            pos = origins[pos]!!
            steps.add(pos)
        } while (steps.last() != start)
        return steps.toList().reversed()
    }

    /**
     * Find positions of multiple targets (with costs)
     *
     * @return the map of positions of found targets with costs
     */
    fun <C> findTargets(
        start: C,
        targetTester: (pos: C) -> Boolean,
        validMovesResolver: (pos: C) -> List<C>,
        moveCostResolver: (from: C, to: C) -> Int
    ): Map<C, Int> {
        val costs = mutableMapOf<C, Int>()
        var updated = setOf(start)
        costs[start] = 0
        val targets = mutableSetOf<C>()
        do {
            val newlyUpdated = mutableSetOf<C>()
            updated.forEach { coordinate ->
                val cost = costs[coordinate]!!
                validMovesResolver(coordinate).forEach { move ->
                    val newCost = cost + moveCostResolver(coordinate, move)
                    if (costs[move] == null || costs[move]!! > newCost) {
                        costs[move] = newCost
                        newlyUpdated.add(move)
                    }

                    if (targetTester(move)) {
                        targets.add(move)
                    }
                }
            }
            updated = newlyUpdated
        } while (updated.isNotEmpty())

        // not found
        return targets.associateWith { c -> costs[c]!! }
    }

}
