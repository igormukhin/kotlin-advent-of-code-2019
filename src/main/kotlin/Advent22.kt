import java.math.BigInteger.ONE
import java.math.BigInteger
import java.math.BigInteger.ZERO

private enum class DealType {
    NEW, CUT, INC
}

private data class Deal(val type: DealType, val num: Int)

private fun parseDeal(line: String): Deal {
    return when {
        line.startsWith("cut ") -> Deal(DealType.CUT, line.substringAfterLast(" ").toInt())
        line.startsWith("deal with increment ") -> Deal(DealType.INC, line.substringAfterLast(" ").toInt())
        line == "deal into new stack" -> Deal(DealType.NEW, 0)
        else -> throw IllegalArgumentException(line)
    }
}

fun main() {
    val input = Utils.readInput("Advent22").lines()
    val deals = input.map(::parseDeal)

    // Task A
    run {
        val stackSize = 10007L
        val cardOfInterest = 2019L

        val position = calcPosition(stackSize, deals, cardOfInterest)
        println("A: card $cardOfInterest landed on position $position")
    }

    // Task B
    run {
        val stackSize = 119315717514047.toBigInteger()
        val shuffles = 101741582076661.toBigInteger()
        val finalPosOfInterest = 2020
        val two = 2.toBigInteger()

        // solution copied from https://todd.ginsberg.com/post/advent-of-code/2019/day22/
        // how the math works described here https://codeforces.com/blog/entry/72593
        fun modularArithmeticVersion(find: BigInteger): BigInteger {
            val memory = arrayOf(ONE, ZERO)
            deals.asReversed().forEach { deal ->
                when (deal.type) {
                    DealType.CUT ->
                        memory[1] += deal.num.toBigInteger()
                    DealType.INC ->
                        deal.num.toBigInteger().modPow(stackSize - two, stackSize).also {
                            memory[0] *= it
                            memory[1] *= it
                        }
                    DealType.NEW -> {
                        memory[0] = memory[0].negate()
                        memory[1] = (memory[1].inc()).negate()
                    }
                }
                memory[0] %= stackSize
                memory[1] %= stackSize
            }
            val power = memory[0].modPow(shuffles, stackSize)
            return ((power * find) +
                    ((memory[1] * (power + stackSize.dec())) *
                            ((memory[0].dec()).modPow(stackSize - two, stackSize))))
                .mod(stackSize)
        }

        val result = modularArithmeticVersion(2020.toBigInteger())
        println("B: $result")

    }
}

fun equalHalves(positions: MutableList<Long>): Boolean {
    val half = positions.size / 2
    for (i in 0 until half) {
        if (positions[i] != positions[half + i]) {
            return false
        }
    }
    return true
}

private fun calcPosition(
    stackSize: Long,
    deals: List<Deal>,
    cardOfInterest: Long
): Long {
    var position = cardOfInterest
    deals.forEach { deal ->
        when (deal.type) {
            DealType.NEW -> position = stackSize - position - 1
            DealType.CUT -> {
                val shift = if (deal.num >= 0) deal.num.toLong() else stackSize + deal.num
                if (position < shift) {
                    position = position + stackSize - shift
                } else {
                    position -= shift
                }
            }
            DealType.INC -> position = (position * deal.num) % stackSize
        }
    }
    return position
}
