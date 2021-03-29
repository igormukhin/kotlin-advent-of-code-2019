import kotlin.math.abs

fun main() {
    val input = Utils.readInput("Advent16")
    val nums = input.map { it.toString().toInt() }

    // task A
    Task16(nums, 1, 100).taskA()

    // task B
    Task16(nums, 10000, 100).taskB()
}

private class Task16(val input: List<Int>, inputRepeats: Int, val phases: Int) {
    val inputSize = input.size * inputRepeats
    val cache = Array(phases) { Array(inputSize) { -1 } }
    val basePattern = arrayOf(0, 1, 0, -1)

    fun taskA() {
        val factors = input.indices.map { i ->
            input.indices.map { j -> calcFactor(basePattern, i, j) }
        }

        var nums = input.toIntArray()
        for (p in 1..phases) {
            val newNums = IntArray(input.size) { 0 }
            for (ln in input.indices) {
                newNums[ln] = abs(nums.mapIndexed { i, n -> n * factors[ln][i] }.sum()) % 10
            }
            nums = newNums
        }
        val result = nums.map { it.toString() }.take(8).joinToString(separator = "")
        println("A: $result")
    }

    private fun calcFactor(basePattern: Array<Int>, line: Int, pos: Int): Int {
        val repeats = line + 1
        val shift = 1
        return basePattern[((pos + shift) / repeats) % basePattern.size]
    }

    fun taskB() {
        val shift = input.take(7).joinToString(separator = "") { it.toString() }.toInt()
        if (shift < inputSize / 2) {
            throw IllegalArgumentException("provided shift does not allow supported optimization")
        }

        val nums = generateSequence { input }.take(10_000).flatten().drop(shift).toMutableList()
        repeat(phases) {
            var value = 0
            for (i in nums.indices) {
                value += nums[nums.lastIndex - i]
                nums[nums.lastIndex - i] = value
            }
            nums.replaceAll { it % 10 }
        }

        val result = nums.map { it.toString() }.take(8).joinToString(separator = "")
        println("B: $result")
    }
}
