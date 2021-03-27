fun main() {
    val input = Utils.readInput("Advent04")
    val (min, max) = input.split("-").map { it.toInt() }

    var cntA = 0
    var cntB = 0
    for (pw in min..max) {
        if (isValidA(pw)) {
            cntA++
        }
        if (isValidB(pw)) {
            cntB++
        }
    }
    println("A: $cntA")
    println("B: $cntB")
}

private fun isValidA(pw: Int): Boolean {
    val digits = asDigits(pw)
    return hasAdjacentDoubleDigits(digits) && digitsNeverDecrease(digits)
}

private fun isValidB(pw: Int): Boolean {
    val digits = asDigits(pw)
    return hasExactDoubleDigits(digits) && digitsNeverDecrease(digits)
}

fun digitsNeverDecrease(digits: List<Byte>): Boolean {
    for (i in 1 until digits.size) {
        if (digits[i] < digits[i - 1]) {
            return false
        }
    }
    return true
}

fun hasAdjacentDoubleDigits(digits: List<Byte>): Boolean {
    for (i in 1 until digits.size) {
        if (digits[i] == digits[i - 1]) {
            return true
        }
    }
    return false
}

fun hasExactDoubleDigits(digits: List<Byte>): Boolean {
    for (i in 1 until digits.size) {
        if (digits[i] == digits[i - 1]) {
            if (i < digits.size - 1 && digits[i] == digits[i + 1]) {
                continue
            }
            if (i > 1 && digits[i] == digits[i - 2]) {
                continue
            }
            return true
        }
    }
    return false
}

private fun asDigits(num: Int): List<Byte> {
    val digits = mutableListOf<Byte>()
    var running = num
    while (running > 0) {
        digits.add(0, (running % 10).toByte())
        running /= 10
    }
    return digits
}
