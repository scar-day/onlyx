package net.polix.system.utility

private val commandsMap = mutableMapOf<Int, MutableMap<String, Long>>()

fun addCommand(fromId: Int, commandName: String, seconds: Int) {
    commandsMap
        .computeIfAbsent(fromId) { mutableMapOf() }[commandName] = calculateCooldownEndTime(seconds)
}

fun isCommandInCooldownList(fromId: Int, commandName: String): Boolean {
    val userCooldowns = commandsMap[fromId]
    return userCooldowns?.let {
        val cooldownEndTime = it[commandName]
        val isInCooldown = cooldownEndTime != null && cooldownEndTime - System.currentTimeMillis() > 0

        if (!isInCooldown) {
            it.remove(commandName)
        }

        isInCooldown
    } ?: false
}


fun getRemainingCooldown(fromId: Int, commandName: String): Long {
    val userCooldowns = commandsMap[fromId]
    return userCooldowns?.get(commandName)?.let {
        maxOf(0, calculateRemainingCooldown(it))
    } ?: 0
}

fun addCommands(fromId: Int, commandNames: Array<out String>, seconds: Int) {
    val currentTimeMillis = calculateCooldownEndTime(seconds)
    commandsMap
        .computeIfAbsent(fromId) { mutableMapOf() }
        .putAll(commandNames.associateWith { currentTimeMillis })
}

private fun calculateCooldownEndTime(seconds: Int): Long {
    return System.currentTimeMillis() + seconds * 1000
}

private fun calculateRemainingCooldown(cooldownEndTime: Long): Long {
    return maxOf(0, (cooldownEndTime - System.currentTimeMillis()) / 1000)
}
