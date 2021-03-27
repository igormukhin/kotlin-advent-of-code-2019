import kotlin.math.abs

fun main() {
    val input = Utils.readInput("Advent16")
    //val input = "69317163492948606335995924319873"
    val originals = input.map { it.toString().toInt() }
    val basePattern = arrayOf(0, 1, 0, -1)

    val factors = input.indices.map { i ->
        input.indices.map { j -> calcFactor(basePattern, i, j) }
    }
    //println(factors)

    val phases = 100
    var nums = originals.toIntArray()
    for (p in 1..phases) {
        val newNums = IntArray(originals.size) { 0 }
        for (ln in input.indices) {
            //println("p=$p")
            //nums.forEachIndexed { i, n -> println("$n * ${factors[ln][i]}") }
            newNums[ln] = abs(nums.mapIndexed { i, n -> n * factors[ln][i] }.sum()) % 10
        }
        nums = newNums
    }
    val result = nums.map { it.toString() }.take(8).joinToString(separator = "")
    println("A: $result")
}

fun calcFactor(basePattern: Array<Int>, line: Int, pos: Int): Int {
    val repeats = line + 1
    var shift = 1
    return basePattern[((pos + shift) / repeats) % basePattern.size]
}