fun main() {
    val input = Utils.readInput("Advent02")
    val nums = input.split(",").map { s -> s.toInt() }.toMutableList()

    taskA(nums.toMutableList())
    taskB(nums)
}

private fun taskA(nums: MutableList<Int>) {
    nums[1] = 12
    nums[2] = 2
    run(nums)
    println("A: ${nums[0]}")
}

private fun taskB(initial: List<Int>) {
    l@  for (i in 0..99) {
        for (j in 0..99) {
            val nums = initial.toMutableList()
            nums[1] = i
            nums[2] = j
            run(nums)
            if (nums[0] == 19690720) {
                println("B: ${i * 100 + j}")
                break@l
            }
        }
    }
}

private fun run(nums: MutableList<Int>) {
    var pos = 0
    l@  while (true) {
        when (nums[pos]) {
            99 -> break@l
            1 -> { nums[nums[pos + 3]] = nums[nums[pos + 1]] + nums[nums[pos + 2]]; pos += 4 }
            2 -> { nums[nums[pos + 3]] = nums[nums[pos + 1]] * nums[nums[pos + 2]]; pos += 4 }
        }
    }
}
