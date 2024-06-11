package net.polix.system.dialog

import com.fasterxml.jackson.annotation.JsonTypeInfo
import net.polix.system.dialog.dialog.CommandDialog
import net.polix.system.dialog.dialog.UserCommandDialog

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "_class"
)

abstract class BaseDialog<T : CommandDialog>(
    val isConsole: Boolean = false, vararg val commandNames: String,
    val description: String, val cooldown: Int,
    val isBeta: Boolean, val permission: String?

) : Command<T>, Cloneable {

    public override fun clone(): Any {
        return super.clone()
    }


}

abstract class UserCommand(
    vararg commandNames: String,
    cooldown: Int = 0,
    isBeta: Boolean = false,
    description: String = "UNKNOWN",
    permission: String? = null
) : BaseDialog<UserCommandDialog>(
    isConsole = false,
    *commandNames,
    description = description,
    isBeta = isBeta,
    cooldown = cooldown,
    permission = permission
)


abstract class ConsoleCommand(
    vararg commandNames: String,
    cooldown: Int = 0,
    isBeta: Boolean = false,
    description: String = "COMMAND_HELP_UNKNOWN_COMMAND"
) : BaseDialog<CommandDialog>(
    isConsole = true, *commandNames,
    cooldown = cooldown,
    description = description,
    isBeta = isBeta,
    permission = null
)

interface Command<T : CommandDialog> {

    fun T.handleInput()

    fun handle(commandDialog: T): Any? {
        commandDialog.handleInput()

        if (commandDialog is UserCommandDialog) {
            return commandDialog.activeDialog
        }

        return null
    }

}

