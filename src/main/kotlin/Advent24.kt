fun main() {
    val input = Utils.readInput("Advent24")
    val initial = input.lines().map { ln -> ln.map { it == '#' }.toBooleanArray() }.toTypedArray()

    taskA(initial)
    taskB(initial)
}

private fun taskA(initial: Array<BooleanArray>) {
    val ratings = mutableSetOf<Int>()
    var map = FlatMap(initial)
    do {
        val rating = map.calcRating()
        if (rating in ratings) {
            println("A: $rating")
            break
        }

        ratings.add(rating)
        map = map.evolve()

        //map.printMap(map)
    } while (true)
}

private class FlatMap(val map: Array<BooleanArray>) {

    fun evolve(): FlatMap {
        fun isBug(i: Int, j: Int): Int {
            return if (i >= 0 && i < map.size && j >= 0 && j < map[i].size && map[i][j]) 1 else 0
        }

        fun countBugs(i: Int, j: Int): Int {
            return isBug(i - 1, j) + isBug(i + 1, j) + isBug(i, j - 1) + isBug(i, j + 1)
        }

        val evolved = Array(map.size) { BooleanArray(map[it].size) }
        map.forEachIndexed { i, ln ->
            ln.forEachIndexed { j, bug ->
                evolved[i][j] = if (bug) {
                    countBugs(i, j) == 1
                } else {
                    countBugs(i, j) in 1..2
                }
            }
        }

        return FlatMap(evolved)
    }

    fun calcRating(): Int {
        var factor = 1
        var rating = 0
        map.forEach { ln ->
            ln.forEach { bug ->
                if (bug) {
                    rating += factor
                }
                factor *= 2
            }
        }
        return rating
    }

    fun printMap() {
        map.forEach { ln ->
            ln.forEach { print(if (it) "#" else ".") }
            println()
        }
        println()
    }

}

private fun taskB(initial: Array<BooleanArray>) {
    var map = RecursiveMap(initial)
    //map.print(0)
    repeat(200) {
        map = map.evolve()
        //map.print(it + 1)
    }
    val bugs = map.countAllBugs()
    println("B: $bugs")
}

private class RecursiveMap(val map: Map<Int, Array<BooleanArray>>) {
    constructor(initial: Array<BooleanArray>) : this(mapOf(0 to initial))

    fun evolve(): RecursiveMap {
        val emptyLevel = Array(5) { BooleanArray(5) }

        fun countOn(levelNum: Int, i: Int, j: Int, dir: Direction): Int {
            if (i in 0..4 && j in 0..4) {
                if (i == 2 && j == 2) {
                    val level = map[levelNum + 1] ?: emptyLevel
                    return when (dir) {
                        Direction.UP -> level.last().sumBy { it.toInt() }
                        Direction.DOWN -> level.first().sumBy { it.toInt() }
                        Direction.LEFT -> level.sumBy { it.last().toInt() }
                        Direction.RIGHT -> level.sumBy { it.first().toInt() }
                    }
                } else {
                    val level = map[levelNum]
                    return if (level != null) {
                        level[i][j].toInt()
                    } else {
                        0
                    }
                }
            } else {
                val level = map[levelNum - 1] ?: emptyLevel
                return when (dir) {
                    Direction.UP -> level[1][2].toInt()
                    Direction.DOWN -> level[3][2].toInt()
                    Direction.LEFT -> level[2][1].toInt()
                    Direction.RIGHT -> level[2][3].toInt()
                }
            }

        }

        fun countAround(levelNum: Int, i: Int, j: Int): Int {
            return countOn(levelNum, i - 1, j, Direction.UP) +
                    countOn(levelNum, i + 1, j, Direction.DOWN) +
                    countOn(levelNum, i, j - 1, Direction.LEFT) +
                    countOn(levelNum, i, j + 1, Direction.RIGHT)
        }

        val evolved = mutableMapOf<Int, Array<BooleanArray>>()

        val levelNums = map.asSequence().filter { (_, k) -> countBugs(k) > 0 }.map { (v, _) -> v }.toList()
        val maxLevel = levelNums.maxOrNull()!!
        val minLevel = levelNums.minOrNull()!!
        ((minLevel - 1)..(maxLevel + 1)).forEach { levelNum ->
            evolved.computeIfAbsent(levelNum) { Array(5) { BooleanArray(5) } }
            val levelMap = map[levelNum] ?: emptyLevel
            levelMap.forEachIndexed { i, ln ->
                ln.forEachIndexed { j, bug ->
                    if (!(i == 2 && j == 2)) {
                        evolved[levelNum]!![i][j] = if (bug) {
                            countAround(levelNum, i, j) == 1
                        } else {
                            countAround(levelNum, i, j) in 1..2
                        }
                    }
                }
            }
        }

        return RecursiveMap(evolved)
    }

    fun countBugs(level: Array<BooleanArray>): Int {
        return level.map { ln -> ln.sumBy { it.toInt() } }.sum()
    }

    fun countAllBugs(): Int {
        return map.values.map { level -> countBugs(level) }.sum()
    }

    fun print(minute: Int) {
        println("")
        println("======== Minute: $minute")
        val levelNums = map.asSequence().filter { (_, k) -> countBugs(k) > 0 }.map { (v, _) -> v }.toList()
        val maxLevel = levelNums.maxOrNull()!!
        val minLevel = levelNums.minOrNull()!!
        (minLevel..maxLevel).forEach { levelNum ->
            println("Depth $levelNum:")
            val level = map[levelNum]!!
            FlatMap(level).printMap()
        }
    }

}

private fun Boolean.toInt(): Int = if (this) 1 else 0