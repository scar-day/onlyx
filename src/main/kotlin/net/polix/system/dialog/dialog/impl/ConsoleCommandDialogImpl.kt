package net.polix.system.dialog.dialog.impl

import net.polix.system.LOGGER
import net.polix.system.SimplePolixApplication
import net.polix.system.localization.LocalizationService
import net.polix.system.dialog.DialogCommandService
import net.polix.system.dialog.dialog.CommandDialog
import net.polix.system.module.ModuleService
import net.polix.system.scheduler.SchedulerService
import net.polix.system.user.UserService
import net.polix.system.user.status.StatusService

class ConsoleCommandDialogImpl(
    override val content: String,
    override var command: String?,
    override val moduleService: ModuleService,
    override val userService: UserService,
    override val statusService: StatusService,
    override val localizationService: LocalizationService,
    override val commandService: DialogCommandService,
    override val schedulerService: SchedulerService,
) : CommandDialog {
    override val args: List<String> = content.split(" ")


    override var responseError: String = ""
        set(value) {
            field = value

            LOGGER.error(value)
        }

    override var responseWarn: String = ""
        set(value) {
            field = value

            LOGGER.warn(value)
        }
    override var response: String = ""
        set(value) {
            field = value
            LOGGER.info(value)
        }

}
