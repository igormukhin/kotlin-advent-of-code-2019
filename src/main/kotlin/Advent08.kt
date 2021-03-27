
fun main() {
    val input = Utils.readInput("Advent08")
    val image = Image(25, 6, input)

    taskA(image)
    taskB(image)
}

private fun taskA(image: Image) {
    val layerIdx = (0 until image.layerCount).minByOrNull { image.countOnLayer(it, '0') }!!
    val product = image.countOnLayer(layerIdx, '1') * image.countOnLayer(layerIdx, '2')
    println("A: $product")
}

private fun taskB(image: Image) {
    val merged = image.layerData(0).toCharArray()
    for (i in 1 until image.layerCount) {
        val layer = image.layerData(i)
        layer.forEachIndexed { idx, ch ->
            if (merged[idx] == '2') merged[idx] = ch
        }
    }

    println("B:")
    merged.toList().chunked(image.width).forEach { line ->
        println(String(line.map { if (it == '1') 'X' else ' ' }.toCharArray()))
    }
}

private class Image(val width: Int, val height: Int, val data: String) {
    val size = width * height
    val layerCount = data.length / size

    fun layerData(idx: Int): String {
        return data.substring(idx * size, (idx + 1) * size)
    }

    fun countOnLayer(layerIdx: Int, ch: Char): Int {
        return layerData(layerIdx).count { it == ch }
    }
}
