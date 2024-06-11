package net.polix.system.command.console

import net.polix.system.dialog.ConsoleCommand
import net.polix.system.dialog.dialog.CommandDialog

class HelpCommand : ConsoleCommand(
    "help", "commands", "команды", "помощь"
) {
    override fun CommandDialog.handleInput() {
        val sorted = commandService.commands.sortedBy { it.commandNames.first() }.filter { it.isConsole }

        val builder = StringBuilder()
        for (commands in sorted) {
            val command = commands.commandNames.first()
            val aliases = commands.commandNames.drop(1).joinToString(", ")

            if (aliases.isEmpty()) {
                builder.append("— $command » <нету алиас(а/ов)>\n")
            } else {
                builder.append("— $command » ($aliases)\n")
            }
        }

        response = "\n$builder\n\nВсего команд: ${sorted.size}"
    }
}