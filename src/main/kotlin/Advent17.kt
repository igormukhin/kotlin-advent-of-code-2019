import kotlin.math.min

fun main() {
    val input = Utils.readInput("Advent17")
    val program = input.split(",").map { s -> s.toLong() }.toMutableList()

    val map = mutableListOf<MutableList<Char>>()
    var linebreaks = 1
    Task9.runProgram(program, { 0L }, { out ->
        when (out.toInt()) {
            10 -> linebreaks++
            else -> {
                while (linebreaks > 0) {
                    map.add(mutableListOf())
                    linebreaks--
                }
                map.last().add(out.toChar())
            }
        }
    })

    // print
    map.forEach { ln ->
        ln.forEach { ch -> print(ch) }
        println()
    }
    println("trailing linebreaks: $linebreaks")

    // task A
    fun isRobotAt(i: Int, j: Int): Boolean {
        return map[i][j] in arrayOf('<', '>', '^', 'v')
    }

    fun isInRange(i: Int, j: Int) = i < 0 || j < 0 || i >= map.size || j >= map[i].size

    fun isScaffoldAt(i: Int, j: Int): Boolean {
        if (isInRange(i, j)) return false
        return map[i][j] == '#' || isRobotAt(i, j)
    }

    fun isScaffoldAt(p: Point): Boolean = isScaffoldAt(p.x, p.y)

    fun isCrossingAt(i: Int, j: Int): Boolean {
        return isScaffoldAt(i, j)
                && isScaffoldAt(i - 1, j)
                && isScaffoldAt(i + 1, j)
                && isScaffoldAt(i, j - 1)
                && isScaffoldAt(i, j + 1)
    }

    var apSum = 0
    map.forEachIndexed { i, ln ->
        ln.indices.forEach { j ->
            if (isCrossingAt(i, j)) {
                apSum += i * j
            }
        }
    }

    println("A: $apSum")

    // task B
    fun getPositionOfRobot(): Point {
        map.forEachIndexed { i, ln ->
            ln.indices.forEach { j ->
                if (isRobotAt(i, j)) {
                    return Point(i, j)
                }
            }
        }
        throw IllegalStateException()
    }

    val path = mutableListOf<Action>()
    var robot = getPositionOfRobot()
    var dir = directionFrom(map[robot.x][robot.y])

    fun addAction(type: ActionType) {
        if (type == ActionType.MOVE) {
            if (path.isEmpty() || path.last().type != ActionType.MOVE) {
                path.add(Action(type, 1))
            } else {
                path.last().steps++
            }
        } else {
            path.add(Action(type))
        }
    }

    do {
        var actionFound = false
        if (isScaffoldAt(robot.move(dir, 1))) {
            robot = robot.move(dir, 1)
            addAction(ActionType.MOVE)
            actionFound = true
        } else {
            if (isScaffoldAt(robot.move(dir.turnRight(), 1))) {
                dir = dir.turnRight()
                addAction(ActionType.TURN_RIGHT)
                actionFound = true
            } else if (isScaffoldAt(robot.move(dir.turnLeft(), 1))) {
                dir = dir.turnLeft()
                addAction(ActionType.TURN_LEFT)
                actionFound = true
            }
        }
    } while (actionFound)

    println(path)

    var functions = listOf<List<Action>>()
    var main = listOf<Int>()
    v@for (a_start in path.indices) {
        for (a_len in 1..min(path.size - a_start, 10)) {
            if (isInvalidFuncLength(path, a_start, a_len)) continue
            for (b_start in path.indices) {
                if (isInside(a_start, a_len, b_start)) continue
                for (b_len in 1..min(path.size - b_start, 10)) {
                    if (isInside(a_start, a_len, b_start + b_len - 1)) continue
                    if (isInvalidFuncLength(path, b_start, b_len)) continue
                    for (c_start in path.indices) {
                        if (isInside(a_start, a_len, c_start)) continue
                        if (isInside(b_start, b_len, c_start)) continue
                        for (c_len in 1..min(path.size - c_start, 10)) {
                            if (isInside(a_start, a_len, c_start + c_len - 1)) continue
                            if (isInside(b_start, b_len, c_start + c_len - 1)) continue
                            if (isInvalidFuncLength(path, c_start, c_len)) continue

                            functions = listOf(
                                path.subList(a_start, a_start + a_len),
                                path.subList(b_start, b_start + b_len),
                                path.subList(c_start, c_start + c_len)
                            )
                            main = resolveMainRoutine(path, functions)
                            if (main.isNotEmpty()) {
                                break@v
                            }
                        }
                    }
                }
            }
        }
    }

    if (main.isEmpty()) {
        throw RuntimeException("no solution found")
    }

    println("functions: $functions")
    println("main: $main")

    program[0] = 2L
    val codes = mutableListOf<Long>()
    main.joinToString(",") { f -> ('A' + f).toString() }.forEach { ch -> codes.add(ch.toLong()) }
    codes.add(10L)
    functions.forEach { func ->
        funcToStr(func).forEach { ch -> codes.add(ch.toLong()) }
        codes.add(10L)
    }
    codes.add('n'.toLong())
    codes.add(10L)
    println("input: $codes")
    val bResult = Task9.runProgram(program, codes).last()
    println("B: $bResult")
}

private fun isInvalidFuncLength(path: List<Action>, start: Int, len: Int): Boolean {
    if (len < 5) return false
    if (len > 10) return true
    return funcToStr(path.subList(start, start + len)).length > 20
}

private fun funcToStr(func: List<Action>) = func.joinToString(",") { f -> f.toString() }

private fun resolveMainRoutine(path: MutableList<Action>, functions: List<MutableList<Action>>): List<Int> {
    val routine = mutableListOf<Int>()
    var done = false

    fun go(from: Int) {
        if (from == path.size && routine.size <= 10) {
            done = true
        }

        fun applicable(func: List<Action>): Boolean {
            for (j in func.indices) {
                if (from + j >= path.size) return false
                if (func[j] != path[from + j]) return false
            }
            return true
        }

        functions.forEachIndexed { i, func ->
            if (!done && applicable(func)) {
                routine.add(i)
                go(from + func.size)
                if (!done) routine.removeLast()
            }
        }
    }

    go(0)

    return routine
}

private fun isInside(start: Int, len: Int, between: Int): Boolean {
    return (between >= start && between <= (start + len - 1))
}

private enum class ActionType {
    TURN_LEFT, TURN_RIGHT, MOVE
}

private data class Action(val type: ActionType, var steps: Int = 0) {
    override fun toString(): String {
        return when (type) {
            ActionType.TURN_LEFT -> "L"
            ActionType.TURN_RIGHT -> "R"
            ActionType.MOVE -> steps.toString()
        }
    }
}

private fun directionFrom(ch: Char): Direction {
    return when (ch) {
        '^' -> Direction.UP
        'v' -> Direction.DOWN
        '<' -> Direction.LEFT
        '>' -> Direction.RIGHT
        else -> throw IllegalArgumentException()
    }
}
