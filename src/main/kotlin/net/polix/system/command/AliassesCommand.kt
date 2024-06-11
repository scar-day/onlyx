package net.polix.system.command

import net.polix.system.dialog.UserCommand
import net.polix.system.dialog.dialog.UserCommandDialog

class AliassesCommand : UserCommand(
    "aliases", "алиасы", cooldown = 3, description = "COMMAND_ALIASES_DESCRIPTION"
) {
    override fun UserCommandDialog.handleInput() {
        val builder = StringBuilder()

        val sorted = commandService.commands
            .filter { !it.isConsole }
            .filter { it.permission == null || user.has(it.permission.toString()) }
            .sortedWith(compareBy { it.commandNames.first() })

        for (commands in sorted) {
            val name = commands.commandNames.first()
            val aliases = commands.commandNames.drop(1).joinToString(", ")

            builder.append("— $name » ($aliases)\n")
        }

        responseReply = "${localizationService.findMessage(user.lang, "COMMAND_ALIASES")}:\n$builder\n"
    }

}