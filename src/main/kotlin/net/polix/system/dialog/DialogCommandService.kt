package net.polix.system.dialog

import net.polix.system.handler.CommandHandler
import org.springframework.stereotype.Service

@Service
class DialogCommandService {
    val commands = mutableSetOf<BaseDialog<*>>()
    val messageHandlers = mutableSetOf<CommandHandler>()

    fun registerHandler(messageHandler: CommandHandler) = this.messageHandlers.add(messageHandler)

    fun registerCommand(command: BaseDialog<*>) {
        this.commands.add(command)
    }

    fun unregisterCommand(command: BaseDialog<*>) {
        commands.remove(command)
    }

    fun getCommand(commandName: String, isConsole: Boolean = false): BaseDialog<*>? {
        val command = commands.filter { it.isConsole == isConsole }
            .find { it.commandNames.contains(commandName) } ?: return null

        return command.clone() as BaseDialog<*>
    }

    fun existsCommand(commandName: String) = getCommand(commandName) != null

}