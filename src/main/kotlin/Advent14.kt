import kotlin.math.max
import kotlin.math.min

fun main() {
    val input = Utils.readInput("Advent14")
    val reactions = input.lines().associate { ln ->
        val (comps, res) = ln.split(" => ")
        Pair(parseQuantity(res), comps.split(", ").map { parseQuantity(it) })
    }
    val outputChemical = "FUEL"
    val inputChemical = "ORE"

    val chemicals = reactions.keys.map { it.chemical }.toList() + inputChemical

    fun calcOreRequired(fuelRequired: Long): Long {
        val requests = Array(chemicals.size) { 0L }
        requests[chemicals.indexOf(outputChemical)] = fuelRequired

        do {
            var produced = false

            for (i in requests.indices) {
                if (chemicals[i] == inputChemical) continue
                if (requests[i] <= 0) continue

                val output = reactions.keys.first { it.chemical == chemicals[i] }
                val receipt = reactions[output]!!
                val times = requests[i] / output.amount + if (requests[i] % output.amount > 0) 1 else 0

                produced = true
                requests[i] -= times * output.amount
                receipt.forEach { ingredient ->
                    requests[chemicals.indexOf(ingredient.chemical)] += times * ingredient.amount
                }
            }

        } while (produced)

        return requests[chemicals.indexOf(inputChemical)]
    }

    // A
    val oreRequiredFor1 = calcOreRequired(1)
    println("A: $oreRequiredFor1")

    // B
    val trillion = 1000000000000
    val min = trillion / oreRequiredFor1
    val max = min * 1000 // 1000 is a magic number, you may have to increase it if you get -1 as answer

    val bestHit = (min..max).binarySearch({ n ->
        val r = calcOreRequired(n)
        when {
            r > trillion -> 1
            r < trillion -> -1
            else -> 0
        }
    }, { pmin, pmax ->
        when {
            calcOreRequired(max(pmin, pmax)) <= trillion -> max(pmin, pmax)
            calcOreRequired(min(pmin, pmax)) <= trillion -> min(pmin, pmax)
            else -> -1
        }
    })

    println("B: $bestHit")

}

private fun parseQuantity(str: String): Quantity {
    val (amount, chemical) = str.split(" ")
    return Quantity(amount.toInt(), chemical)
}

data class Quantity(val amount: Int, val chemical: String)
