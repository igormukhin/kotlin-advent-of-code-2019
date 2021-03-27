import kotlin.math.*

fun main() {
    val input = Utils.readInput("Advent10")
    val map = input.lines().map { ln -> ln.toCharArray().map { ch -> ch == '#' } }

    val base = taskA(map)

    //println("Base is at (${base.first}, ${base.second})")
    taskB(map, base)
}

private fun taskB(map: List<List<Boolean>>, base: Pair<Int, Int>) {
    val initialLastRad = PI + 0.0000001
    val emptyStar = -1 to -1
    val starsToKill = countStars(map) - 1
    val resultNum = 200
    var resultStar = emptyStar

    var lastRad = initialLastRad
    val mmap = map.map { it.toMutableList() }.toMutableList()

    var n = 0
    while (n < starsToKill) {
        n++

        var closestRad = -4.0
        var closestStar = emptyStar

        forEveryStar(mmap) { x, y ->
            if (isVisible(mmap, base.first, base.second, x, y)) {
                val thisRad = angleRad(base.first, base.second, x, y)
                if (thisRad < lastRad && thisRad > closestRad) {
                    closestRad = thisRad
                    closestStar = x to y
                }
            }
        }

        if (closestStar == emptyStar) {
            lastRad = initialLastRad
            n--
            continue
        }

        lastRad = closestRad
        //println("Hit ${n}: (${closestStar.first}, ${closestStar.second})")
        if (resultNum == n) resultStar = closestStar
        mmap[closestStar.second][closestStar.first] = false
    }

    println("B: ${resultNum}th hit was on (${resultStar.first}, ${resultStar.second}) = " +
            "${resultStar.first * 100 + resultStar.second}")
}

private fun angleRad(sx: Int, sy: Int, x: Int, y: Int): Double {
    var rad = atan2(sy.toDouble() - y, x.toDouble() - sx)
    // rotate 90 degrees, so PI is at the top
    rad += (PI / 2)
    if (rad > PI) rad -= 2 * PI
    return rad
}

private fun taskA(map: List<List<Boolean>>): Pair<Int, Int> {
    var bestPoint = -1 to -1
    var bestResult = Int.MIN_VALUE
    forEveryStar(map) { x, y ->
        countVisibilityFor(map, x, y).let {
            if (it > bestResult) {
                bestResult = it
                bestPoint = x to y
            }
        }
    }
    println("A: $bestResult")
    return bestPoint
}

private inline fun forEveryPos(map: List<List<Boolean>>, block: (Int, Int) -> Unit) {
    for (x in map[0].indices) {
        for (y in map.indices) {
            block(x, y)
        }
    }
}

private inline fun forEveryStar(map: List<List<Boolean>>, block: (Int, Int) -> Unit) {
    forEveryPos(map) { x, y -> if (map[y][x]) block(x, y) }
}

private fun countStars(map: List<List<Boolean>>): Int {
    var cnt = 0
    forEveryStar(map) { _, _ -> cnt++ }
    return cnt
}

private fun countVisibilityFor(map: List<List<Boolean>>, sx: Int, sy: Int): Int {
    var cnt = 0
    forEveryStar(map) { x, y -> if (isVisible(map, sx, sy, x, y)) cnt++ }
    return cnt
}

private fun isVisible(map: List<List<Boolean>>, sx: Int, sy: Int, x: Int, y: Int): Boolean {
    if (sx == x && sy == y) {
        return false
    }

    forEveryStar(map) { i, j -> if (isInLineOfSight(sx, sy, x, y, i, j)) return false }

    return true
}

private fun isInLineOfSight(x1: Int, y1: Int, x2: Int, y2: Int, px: Int, py: Int): Boolean {
    return when {
        x1 == px && y1 == py -> false
        x2 == px && y2 == py -> false
        x1 == x2 -> {
            x1 == px && min(y1, y2) < py && max(y1, y2) > py
        }
        y1 == y2 -> {
            y1 == py && min(x1, x2) < px && max(x1, x2) > px
        }
        else -> {
            val dx1 = x2.toDouble() - x1
            val dy1 = y2.toDouble() - y1
            val px1 = px.toDouble() - x1
            val py1 = py.toDouble() - y1
            (sign(dx1) == sign(px1)
                    && sign(dy1) == sign(py1)
                    && abs(abs(dx1/dy1) - abs(px1/py1)) < 0.000001)
                    && (dx1*dx1 + dy1*dy1) > (px1*px1 + py1*py1)
        }
    }
}
