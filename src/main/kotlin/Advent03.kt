import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun main() {
    val input = Utils.readInput("Advent03").lines()

    val line1 = Line(input[0])
    val line2 = Line(input[1])
    var shortest = Int.MAX_VALUE
    var nearest = Int.MAX_VALUE
    line1.segments.forEach { segment ->
        segment.forEach { x, y ->
            if (!(x == 0 && y == 0) && line2.contains(x, y)) {
                shortest = min(shortest, abs(x) + abs(y))
                nearest = min(nearest, line1.stepsTo(x, y) + line2.stepsTo(x, y))
            }
        }
    }
    println("A: $shortest")
    println("B: $nearest")
}

private class Line(path: String) {
    val segments: List<Segment>

    init {
        var x = 0
        var y = 0
        segments = path.split(",").map {
            val seg = Segment.parse(x, y, it)
            x = seg.x2
            y = seg.y2
            seg
        }
    }

    fun contains(x: Int, y: Int): Boolean = segments.any { it.contains(x, y) }

    fun stepsTo(x: Int, y: Int): Int {
        var steps = 0
        segments.forEach { segment ->
            segment.dropFirst().forEach { sx, sy ->
                steps++
                if (x == sx && y == sy) {
                    return steps
                }
            }
        }
        throw IllegalArgumentException("Not found")
    }
}

private data class Segment(val x1: Int, val y1: Int, val x2: Int, val y2: Int) {

    companion object {
        fun parse(x: Int, y: Int, token: String): Segment {
            val len = token.substring(1).toInt()
            return when (token[0]) {
                'D' -> Segment(x, y, x, y - len)
                'U' -> Segment(x, y, x, y + len)
                'L' -> Segment(x, y, x - len, y)
                'R' -> Segment(x, y, x + len, y)
                else -> throw IllegalArgumentException("Illegal direction: ${token[0]}")
            }
        }
    }

    init {
        if (x1 != x2 && y1 != y2) {
            throw IllegalArgumentException("Should be horizontal or vertical")
        }
    }

    fun contains(x: Int, y: Int): Boolean {
        return if (x1 == x2) {
            x1 == x && y >= min(y1, y2) && y <= max(y1, y2)
        } else {
            y1 == y && x > min(x1, x2) && x <= max(x1, x2)
        }
    }

    inline fun forEach(block: (Int, Int) -> Unit) {
        if (x1 == x2) {
            for (y in min(y1, y2)..max(y1, y2)) {
                block(x1, y)
            }
        } else {
            for (x in min(x1, x2)..max(x1, x2)) {
                block(x, y1)
            }
        }
    }

    fun dropFirst(): Segment {
        return if (x1 == x2) {
            when {
                y1 == y2 -> throw IllegalStateException("Already a point")
                y1 < y2 -> Segment(x1, y1 + 1, x2, y2)
                else -> Segment(x1, y1 - 1, x2, y2)
            }
        } else {
            when {
                x1 == x2 -> throw IllegalStateException("Already a point")
                x1 < x2 -> Segment(x1 + 1, y1, x2, y2)
                else -> Segment(x1 - 1, y1, x2, y2)
            }
        }
    }
}