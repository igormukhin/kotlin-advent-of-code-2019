fun main() {
    val input = Utils.readInput("Advent19")
    val program = input.split(",").map { s -> s.toLong() }

    val smallSize = 50
    val map = Array(smallSize) { Array(smallSize) { false } }
    for (i in map.indices) {
        for (j in map[i].indices) {
            map[i][j] = isBeamAt(program, Point(i, j))
            print(if (map[i][j]) '#' else '.')
        }
        println()
    }

    val sum = map.sumBy { ln -> ln.sumBy { if (it) 1 else 0 } }
    println("A: $sum")

    // Task B
    val shipSize = 100
    var rightEdge = Point(map.indices.indexOfFirst { map[it][25] }, 25)
    //println("rightEdge = $rightEdge")
    var leftEdge = Point(map.indices.indexOfLast { map[it][25] }, 25)
    //println("leftEdge = $leftEdge")
    val leftEdges = mutableMapOf(leftEdge.x to leftEdge.y)
    do {
        rightEdge = findNextEdge(program, rightEdge, 1)
        //println("rightEdge = $rightEdge")

        val ship = Point(rightEdge.x, rightEdge.y - shipSize + 1)
        val corner = Point(ship.x + shipSize - 1, ship.y)

        while (leftEdge.x < corner.x || leftEdge.y < corner.y) {
            leftEdge = findNextEdge(program, leftEdge, -1)
            //println("leftEdge = $leftEdge")

            leftEdges[leftEdge.x] = leftEdge.y
        }

        if (corner.y > shipSize * 2 && leftEdges[corner.x]!! <= corner.y) {
            println("B: ship = $ship, result = ${ship.y * 10000 + ship.x}")
            break
        }
    } while (true)
}

private fun isBeamAt(program: List<Long>, p: Point) =
    Task9.runProgram(program, listOf(p.y.toLong(), p.x.toLong())).first() == 1L

fun findNextEdge(program: List<Long>, edge: Point, side: Int): Point {
    var current = Point(edge.x + 1, edge.y - 1)
    do {
        val next = Point(current.x, current.y + 1)
        val beam = isBeamAt(program, next)
        if (side == 1 && !beam) {
            return current
        } else if (side == -1 && beam) {
            return next
        }
        current = next
    } while (true)
}
