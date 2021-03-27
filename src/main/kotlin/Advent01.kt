fun main() {
    val input = Utils.readInput("Advent01").lines()
    val nums = input.map { s -> s.toInt() }
    println(nums.map { n -> n / 3 - 2 }.sum())

    println(nums.map { n -> calcB(n) }.sum())
}

private fun calcB(n: Int): Int {
    var sum = 0
    var runner = n
    while (true) {
        val f = runner / 3 - 2
        if (f < 1) break
        sum += f
        runner = f
    }
    return sum
}
