import kotlin.math.abs

fun main() {
    val input = Utils.readInput("Advent12")
    val positions = input.lines().map { ln ->
        val lst = ln.substring(1, ln.length - 1).split(", ")
            .map { pt -> pt.substringAfter("=").toInt() }
        Point3D(lst[0], lst[1], lst[2])
    }

    taskA(positions)
    taskB(positions)
}

private fun taskA(startingAt: List<Point3D>) {
    val moons = startingAt.map { MPoint3D(it.x, it.y, it.z) }
    val speeds = Array(moons.size) { MPoint3D() }.toMutableList()
    var step = 0L
    val stopAfterStep = 1000L

    while (true) {
        step++

        // update velocities
        permuteIndexes(moons.size - 1, 2) { (i, j) ->
            if (moons[i].x < moons[j].x) speeds[i].x++
            if (moons[i].x > moons[j].x) speeds[i].x--
            if (moons[i].y < moons[j].y) speeds[i].y++
            if (moons[i].y > moons[j].y) speeds[i].y--
            if (moons[i].z < moons[j].z) speeds[i].z++
            if (moons[i].z > moons[j].z) speeds[i].z--
        }

        // update positions
        moons.forEachIndexed { i, m -> m.add(speeds[i]) }

        if (step == stopAfterStep) {
            // print
            //println("Step #$step")
            //moons.forEachIndexed { i, m -> println("$m with ${speeds[i]}") }
            //println()
            break
        }
    }

    val energy = energy(moons, speeds)
    println("A: energy = $energy")
}

private fun taskB(startingAt: List<Point3D>) {
    val xSteps = calcStepsToRotation(startingAt) { it.x }
    val ySteps = calcStepsToRotation(startingAt) { it.y }
    val zSteps = calcStepsToRotation(startingAt) { it.z }
    //println("xSteps=$xSteps, ySteps=$ySteps, zSteps=$zSteps")
    val lcm = leastCommonMultiple(listOf(xSteps, ySteps, zSteps))
    println("B: steps = $lcm")
}

private fun calcStepsToRotation(startingAt: List<Point3D>, read: (Point3D) -> Int): Long {
    val initials = startingAt.map { read(it) }
    val moons = initials.toMutableList()
    val speeds = Array(moons.size) { 0 }.toMutableList()
    var step = 0L

    while (true) {
        step++

        // update velocities
        permuteIndexes(moons.size - 1, 2) { (i, j) ->
            if (moons[i] < moons[j]) speeds[i]++
            if (moons[i] > moons[j]) speeds[i]--
        }

        // update positions
        moons.indices.forEach { i -> moons[i] += speeds[i] }

        if (moons == initials && speeds.all { it == 0 }) {
            break
        }
    }

    return step
}

private fun energy(positions: List<MPoint3D>, velocities: List<MPoint3D>) =
    positions.indices.map { i -> positions[i].asum() * velocities[i].asum() }.sum()

private data class MPoint3D(var x: Int = 0, var y: Int = 0, var z: Int = 0) {
    fun add(op: MPoint3D) {
        x += op.x
        y += op.y
        z += op.z
    }

    fun asum() = abs(x) + abs(y) + abs(z)
}
