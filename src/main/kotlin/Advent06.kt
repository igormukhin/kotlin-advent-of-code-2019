fun main() {
    val input = Utils.readInput("Advent06")
    val orbits = input.lines().map { ln -> ln.split(')')
                              .let { it[0] to it[1] } }

    taskA(orbits)
    taskB(orbits)
}

private fun taskA(orbits: List<Pair<String, String>>) {
    val paths = mutableMapOf<String, Int>()
    paths["COM"] = 0
    val sources = mutableSetOf<String>()
    sources.add("COM")
    val targets = mutableSetOf<String>()

    while (true) {
        orbits.forEach { pair ->
            if (pair.first in sources) {
                paths[pair.second] = 1 + paths[pair.first]!!
                targets.add(pair.second)
            }
        }

        if (targets.isEmpty()) {
            break
        }

        sources.clear()
        sources.addAll(targets)
        targets.clear()
    }

    println(paths)
    println("A: " + paths.values.sum())
}

private fun taskB(orbits: List<Pair<String, String>>) {
    val start = "YOU"
    val end = "SAN"
    val paths = mutableMapOf<String, Int>()
    var length = 0
    paths[start] = length

    while (true) {
        orbits.forEach { (x, y) ->
            if (paths[x] == length && paths[y] == null) {
                paths[y] = length + 1
            } else if (paths[y] == length && paths[x] == null) {
                paths[x] = length + 1
            }
            if (paths[end] != null) {
                println("B: ${paths[end]!! - 2}")
                return
            }
        }
        length++
    }

}