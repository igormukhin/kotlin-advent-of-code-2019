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

    /**
     * for x growing to south.
     * for y growing to east.
     */
    fun move(dir: Direction, amount: Int = 1): Point {
        return when (dir) {
            Direction.UP -> Point(x - amount, y)
            Direction.DOWN -> Point(x + amount, y)
            Direction.LEFT -> Point(x, y - amount)
            Direction.RIGHT -> Point(x, y + amount)
        }
    }

    fun directionTo(to: Point): Direction {
        return when {
            to.y == this.y && to.x > this.x -> Direction.DOWN
            to.y == this.y && to.x < this.x -> Direction.UP
            to.x == this.x && to.y > this.y -> Direction.RIGHT
            to.x == this.x && to.y < this.y -> Direction.LEFT
            else -> throw IllegalArgumentException()
        }
    }

    override fun toString(): String = "[$x; $y]"
}

data class Point3D(val x: Int = 0, val y: Int = 0, val z: Int = 0) {
    operator fun plus(op: Point3D): Point3D = Point3D(x + op.x, y + op.y, z + op.z)
    override fun toString(): String = "[$x; $y; $z]"
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT;

    fun turnRight(): Direction {
        return when (this) {
            UP -> RIGHT
            DOWN -> LEFT
            LEFT -> UP
            RIGHT -> DOWN
        }
    }

    fun turnLeft(): Direction {
        return when (this) {
            UP -> LEFT
            DOWN -> RIGHT
            LEFT -> DOWN
            RIGHT -> UP
        }
    }

    fun turnAround(): Direction {
        return when (this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
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

fun permutations(indexes: Int, places: Int = indexes): Iterable<IntArray> {
    return Permuter(indexes, places)
}

private class Permuter(val indexes: Int, val places: Int) : Iterable<IntArray> {
    init {
        assert(indexes > 0)
        assert(places > 0)
        assert(places <= indexes)
    }

    override fun iterator(): Iterator<IntArray> = OurIterator()

    private inner class OurIterator: Iterator<IntArray> {
        var next: IntArray? = null

        init {
            next = IntArray(places) { it }
        }

        private fun resolveNext() {
            val array = next!!
            for (i in (places - 1) downTo 0) {
                do {
                    array[i]++
                    if (array[i] >= indexes) break
                    if (array.indexOf(array[i]) == i) {
                        for (j in (i + 1) until places) {
                            array[j] = 0
                            while (array.indexOf(array[j]) != j) {
                                array[j]++
                            }
                        }
                        return
                    }
                } while (true)
            }
            next = null
        }

        override fun hasNext(): Boolean {
            return next != null
        }

        override fun next(): IntArray {
            if (next == null) throw NoSuchElementException()
            return next!!.copyOf().also { resolveNext() }
        }
    }
}

fun combinations(indexes: Int, minPlaces: Int, maxPlaces: Int = minPlaces) : Iterable<IntArray> {
    return Combinator(indexes, minPlaces, maxPlaces)
}

private class Combinator(val indexes: Int, val minPlaces: Int, val maxPlaces: Int) : Iterable<IntArray> {
    init {
        assert(indexes > 0)
        assert(minPlaces > 0)
        assert(maxPlaces >= minPlaces)
        assert(maxPlaces <= indexes)
    }

    override fun iterator(): Iterator<IntArray> = OurIterator()

    private inner class OurIterator: Iterator<IntArray> {
        var places = minPlaces - 1
        var next: IntArray? = null

        init {
            resolveNext()
        }

        private fun resolveNext() {
            fun initFor(n: Int) {
                places = n
                next = IntArray(n) { it }
            }

            // very first combination
            if (places < minPlaces) {
                initFor(minPlaces)
                return
            }

            // next combination
            val array = next!!
            for (i in array.lastIndex downTo 0) {
                val maxAtI = indexes - places + i
                if (array[i] < maxAtI) {
                    array[i]++
                    for (j in (i + 1) until places) {
                        array[j] = array[j - 1] + 1
                    }
                    return
                }
            }

            // next number of places
            if (places < maxPlaces) {
                initFor(places + 1)
                return
            }

            // no more combinations
            next = null
        }

        override fun hasNext(): Boolean {
            return next != null
        }

        override fun next(): IntArray {
            if (next == null) throw NoSuchElementException()
            return next!!.copyOf().also { resolveNext() }
        }
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


object Dijkstra {

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
        moveCostResolver: (from: C, to: C) -> Int = { _, _ -> 1 },
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
