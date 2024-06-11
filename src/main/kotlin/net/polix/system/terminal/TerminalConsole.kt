package net.polix.system.terminal

import jakarta.annotation.PostConstruct
import net.minecrell.terminalconsole.SimpleTerminalConsole
import net.polix.system.LOGGER
import net.polix.system.SimplePolixApplication
import net.polix.system.localization.LocalizationService
import net.polix.system.dialog.BaseDialog
import net.polix.system.dialog.DialogCommandService
import net.polix.system.dialog.dialog.CommandDialog
import net.polix.system.dialog.dialog.impl.ConsoleCommandDialogImpl
import net.polix.system.event.EventService
import net.polix.system.module.ModuleService
import net.polix.system.scheduler.SchedulerService
import net.polix.system.user.UserService
import net.polix.system.user.status.StatusService
import org.springframework.stereotype.Component

@Component
class TerminalConsole(
    val commandService: DialogCommandService,
    val localizationService: LocalizationService,
    val userService: UserService,
    val statusService: StatusService,
    val moduleService: ModuleService,
    val eventService: EventService,
    val schedulerService: SchedulerService,
) : SimpleTerminalConsole() {

    @PostConstruct
    fun initialize() {
        Thread {
            start()
        }.start()
    }

    override fun isRunning(): Boolean = true

    override fun runCommand(content: String) {
        val command = commandService.getCommand(content.split(" ")[0].lowercase(), isConsole = true)

        if (command == null) {
            LOGGER.error("Command not found.")
            return
        }

        if (!command.isConsole) {
            LOGGER.error("This command only works for users!")

            return
        }

        val commandDialog = ConsoleCommandDialogImpl(
            content = content,
            command = null,
            userService = userService,
            statusService = statusService,
            moduleService = moduleService,
            localizationService = localizationService,
            commandService = commandService,
            schedulerService = schedulerService,
        )

        commandDialog.command = content

        (command as BaseDialog<CommandDialog>).handle(commandDialog)
    }

    override fun shutdown() {
        Runtime.getRuntime().exit(-1)
    }

}
