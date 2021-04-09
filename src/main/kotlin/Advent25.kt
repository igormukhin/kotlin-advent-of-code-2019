import kotlin.system.exitProcess

fun main() {
    val input = Utils.readInput("Advent25")
    val intcode = input.split(",").map { s -> s.toLong() }

    val command = StringBuffer()
    val screen = StringBuffer()
    //val brain: Brain = Manual()
    val brain: Brain = Smart()
    Task9.runProgram(intcode, {
        val ch = command.first()
        command.deleteCharAt(0)
        ch.toLong()
    }, { out ->
        screen.append(out.toChar())
        if (screen.contains("== ") && screen.endsWith("\n\n\n")) {
            println(screen)
            brain.handleScreen(screen.toString())
            screen.setLength(0)
        } else if (screen.endsWith("Command?")) {
            println(screen)
            brain.handleScreen(screen.toString())
            screen.setLength(0)
            val cmd = brain.getNextCommand()
            println("Command: $cmd")
            command.append(cmd).append(10.toChar())
        }
    })

    if (screen.isNotEmpty()) {
        println("FINAL OUTPUT:")
        println(screen)
    }
}

private class Room(var name: String = "?", var message: String = "?",
                   var visited: Boolean = false,
                   var doors: MutableMap<Direction, Room> = mutableMapOf(), val items: MutableList<String> = mutableListOf()) {
    override fun toString(): String {
        return "$name: $items ($message)"
    }
}

private interface Brain {
    fun handleScreen(screen: String)
    fun getNextCommand(): String
}

private data class RoomScreen(val name: String, val message: String, val doors: List<Direction>, val items: List<String>) {
    companion object {
        fun parse(screen: String): RoomScreen {
            val lines = screen.lines()

            val nameIdx = lines.indexOfFirst { it.startsWith("== ") }
            val name = lines[nameIdx].substringAfter("== ").substringBeforeLast(" ==")
            val message = lines[nameIdx + 1]

            var doorIdx = 1 + lines.indexOfFirst { it == "Doors here lead:" }
            val doors = mutableListOf<Direction>()
            while (lines[doorIdx].startsWith("- ")) {
                doors.add(Direction.valueOf(lines[doorIdx].substringAfter("- ").toUpperCase()))
                doorIdx++
            }

            var itemIdx = 1 + lines.indexOfFirst { it == "Items here:" }
            val items = mutableListOf<String>()
            while (lines[itemIdx].startsWith("- ")) {
                items.add(lines[itemIdx].substringAfter("- "))
                itemIdx++
            }

            return RoomScreen(name, message, doors, items)
        }
    }
}

private class Smart : Brain {
    companion object {
        const val PRESSURE_FLOOR_ROOM = "Pressure-Sensitive Floor"
        const val SECURITY_CHECKPOINT_ROOM = "Security Checkpoint"
        val NEVER_TAKE_ITEMS = listOf("infinite loop", "photons", "escape pod", "giant electromagnet", "molten lava")
    }

    var phase = Phase.EXPLORE
    val rooms = mutableSetOf<Room>()
    var droidAt = Room()
    val commands = mutableListOf<Command>()
    val holding = mutableSetOf<String>()
    val wearables = mutableListOf<String>()
    var wearablesIterator: Iterator<IntArray>? = null
    val heavierItemSets = mutableSetOf<Set<String>>()
    var tries = 0

    enum class Phase {
        EXPLORE, GET_EMPTY, MAKE_ATTEMPTS
    }

    init {
        rooms.add(droidAt)
    }

    override fun handleScreen(screen: String) {
        when {
            screen.contains("== ") -> {
                val roomScreen = RoomScreen.parse(screen)

                val room = droidAt
                if (!room.visited) {
                    room.name = roomScreen.name
                    room.message = roomScreen.message
                    room.visited = true
                    room.items.addAll(roomScreen.items)
                }

                if (roomScreen.name == PRESSURE_FLOOR_ROOM) {
                    if ("you are ejected back to the checkpoint" in screen) {
                        droidAt = rooms.first { it.name == SECURITY_CHECKPOINT_ROOM }
                    } else {
                        throw RuntimeException("Unexpected screen")
                    }
                }

                roomScreen.doors.forEach { door ->
                    if (door !in room.doors) {
                        val otherRoom =
                            if (roomScreen.name == SECURITY_CHECKPOINT_ROOM)
                                Room(name = PRESSURE_FLOOR_ROOM)
                            else
                                Room()
                        rooms.add(otherRoom)
                        room.doors[door] = otherRoom
                        otherRoom.doors[door.turnAround()] = room
                    }
                }

            }
            screen.contains("You take the ") -> {
                // ok
            }
            screen.contains("You drop the ") -> {
                // ok
            }
            screen.contains("Alert! Droids on this ship are lighter") -> {
                heavierItemSets.add(holding.toSet())
            }
            else -> {
                throw IllegalStateException("!!!UNRECOGNIZED SCREEN!!!")
            }
        }
    }

    override fun getNextCommand(): String {
        var command: String
        do {
            command = when (phase) {
                Phase.EXPLORE -> getNextExploreCommand()
                Phase.GET_EMPTY -> getNextGetEmptyCommand()
                Phase.MAKE_ATTEMPTS -> getNextMakeAttemptCommand()
            }
        } while (command.isEmpty())
        return command
    }

    private fun getNextMakeAttemptCommand(): String {
        if (commands.isEmpty()) {
            if (droidAt.name == PRESSURE_FLOOR_ROOM) {
                throw IllegalStateException("Found solution? Holding $holding")
            } else {
                if (wearablesIterator == null) {
                    wearablesIterator = combinations(wearables.size, 1, wearables.size).iterator()
                }

                wearablesIterator?.let { iter ->
                    while (iter.hasNext()) {
                        val itemIndexes = iter.next()
                        val itemSet = itemIndexes.map { wearables[it] }.toSet()
                        if (heavierItemSets.any { itemSet in it }) {
                            continue
                        }

                        wearables.forEachIndexed { idx, item ->
                            if (item in holding && idx !in itemIndexes) {
                                holding.remove(item)
                                commands.add(Command(Command.Type.DROP, item = item))
                            } else if (item !in holding && idx in itemIndexes) {
                                holding.add(item)
                                commands.add(Command(Command.Type.TAKE, item = item))
                            }
                        }
                        break
                    }
                    if (!iter.hasNext()) {
                        throw RuntimeException("No solution found")
                    }
                }

                commands.add(Command(Command.Type.MOVE, rooms.first { it.name == PRESSURE_FLOOR_ROOM }))
                tries++
                println("Prepared try #$tries holding $holding")
            }
        }

        return pullNextCommand()
    }

    private fun getNextGetEmptyCommand(): String {
        if (droidAt.name != SECURITY_CHECKPOINT_ROOM) {
            if (commands.isEmpty()) {
                addPathTo({ room -> room.name == PRESSURE_FLOOR_ROOM }, { true })
            }
            return pullNextCommand()
        }

        return if (holding.isNotEmpty()) {
            val item = holding.first()
            holding.remove(item)
            wearables.add(item)
            "drop $item"
        } else {
            phase = Phase.MAKE_ATTEMPTS
            ""
        }
    }

    private fun getNextExploreCommand(): String {
        val picks = droidAt.items.filter { it !in NEVER_TAKE_ITEMS }
        if (picks.isNotEmpty()) {
            val item = picks.first()
            droidAt.items.remove(item)
            holding.add(item)
            return "take $item"
        }

        if (commands.isEmpty()) {
            if (!addPathTo({ room -> !room.visited && room.name != PRESSURE_FLOOR_ROOM }, { it.name != PRESSURE_FLOOR_ROOM })) {
                println("Total rooms: ${rooms.size}")
                phase = Phase.GET_EMPTY
                return ""
            }
        }

        return pullNextCommand()
    }

    private fun addPathTo(target: (pos: Room) -> Boolean, roomFilter: (pos: Room) -> Boolean): Boolean {
        val path = Dijkstra.findShortestPath(droidAt,
            target,
            { room -> room.doors.values.filter(roomFilter) })
        if (path.isEmpty()) {
            return false
        }

        commands.addAll(path.drop(1).map { Command(Command.Type.MOVE, it) })
        return true
    }

    private fun pullNextCommand(): String {
        val command = commands.removeAt(0)
        return when (command.type) {
            Command.Type.MOVE -> {
                val dir = droidAt.doors.entries.first { (_, rm) -> rm == command.room }.key
                droidAt = command.room!!
                dir.toString().toLowerCase()
            }
            Command.Type.TAKE -> {
                "take ${command.item}"
            }
            Command.Type.DROP -> {
                "drop ${command.item}"
            }
        }
    }
}

private class Manual : Brain {
    override fun handleScreen(screen: String) {
    }

    override fun getNextCommand(): String {
        return readLine()!!
    }
}

private data class Command(val type: Type, val room: Room? = null, val item: String? = null) {
    enum class Type { MOVE, TAKE, DROP }
}