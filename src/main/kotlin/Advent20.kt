private enum class Brick {
    WALLS, PASS, NOTHING
}

private data class Portal(val name: String, val entry: Point, val exit: Point, val outer: Boolean)

fun main() {
    val input = Utils.readInput("Advent20").lines()

    // parse map and letters
    val map = mutableMapOf<Point, Brick>()
    val letters = mutableMapOf<Point, Char>()
    val height = input.size
    val width = input.map { it.length }.maxOrNull()!!
    input.forEachIndexed { i, ln ->
        ln.forEachIndexed { j, ch ->
            when (ch) {
                '#' -> map[Point(i, j)] = Brick.WALLS
                '.' -> map[Point(i, j)] = Brick.PASS
                in 'A'..'Z' -> letters[Point(i, j)] = ch
            }
        }
    }

    // find portals
    fun isOuter(p: Point): Boolean {
        return p.x < 2 || p.y < 2 || p.x >= height - 2 || p.y >= width - 2
    }
    val portals = mutableListOf<Portal>()
    letters.forEach { (p, letter) ->
        val outer = isOuter(Point(p.x, p.y))
        if (letters[Point(p.x, p.y + 1)] in 'A'..'Z') {
            val name = letter.toString() + letters[Point(p.x, p.y + 1)]
            if (map[Point(p.x, p.y + 2)] == Brick.PASS) {
                portals.add(Portal(name, Point(p.x, p.y + 1), Point(p.x, p.y + 2), outer))
            } else {
                portals.add(Portal(name, Point(p.x, p.y), Point(p.x, p.y - 1), outer))
            }
        } else if (letters[Point(p.x + 1, p.y)] in 'A'..'Z') {
            val name = letter.toString() + letters[Point(p.x + 1, p.y)]
            if (map[Point(p.x + 2, p.y)] == Brick.PASS) {
                portals.add(Portal(name, Point(p.x + 1, p.y), Point(p.x + 2, p.y), outer))
            } else {
                portals.add(Portal(name, Point(p.x, p.y), Point(p.x - 1, p.y), outer))
            }
        }
    }

    fun findPortal(name: String? = null, entry: Point? = null, exit: Point? = null,
                   notPortal: Portal? = null, outer: Boolean? = null): Portal? {
        return portals
            .asSequence()
            .filter { p -> if (name == null) true else p.name == name }
            .filter { p -> if (entry == null) true else p.entry == entry }
            .filter { p -> if (exit == null) true else p.exit == exit }
            .filter { p -> if (notPortal == null) true else p != notPortal }
            .filter { p -> if (outer == null) true else p.outer == outer }
            .firstOrNull()
    }

    val entryPortal = findPortal("AA")!!
    val exitPortal = findPortal("ZZ")!!
    portals.remove(entryPortal)
    portals.remove(exitPortal)

    // Task A
    run {
        val path = Dijkstra.findShortestPath(entryPortal.exit,
            { p -> p == exitPortal.exit },
            { p ->
                Direction.values().map { dir ->
                    val moved = p.move(dir, 1)
                    val portal = findPortal(entry = moved)
                    if (portal == null) moved else findPortal(name = portal.name, notPortal = portal)!!.exit
                }.filter { moved ->
                    map[moved] == Brick.PASS
                }
            },
            { _, _ -> 1 })
        println("A: steps = ${path.size - 1}")
    }

    // Task B
    //   AA, ZZ only work on level 0
    //   inner portals send down to level + 1
    //   outer portals send up to level - 1
    run {
        val path = Dijkstra.findShortestPath(entryPortal.exit.onLevel(0),
            { p -> p == exitPortal.exit.onLevel(0) },
            { p ->
                Direction.values().map { dir ->
                    val moved = p.move(dir)
                    val portal = findPortal(entry = moved.toPoint())
                    if (portal == null) {
                        moved
                    } else {
                        val other = findPortal(name = portal.name, notPortal = portal)!!
                        other.exit.onLevel(moved.z + if(portal.outer) -1 else 1)
                    }
                }.filter { moved ->
                    map[moved.toPoint()] == Brick.PASS && moved.z >= 0
                }
            },
            { _, _ -> 1 })
        println("B: steps = ${path.size - 1}")
    }
}

private fun Point.onLevel(level: Int): Point3D = Point3D(x, y, level)
private fun Point3D.toPoint(): Point = Point(x, y)
private fun Point3D.move(dir: Direction): Point3D = toPoint().move(dir).onLevel(z)
