package net.polix.system.dialog.dialog

import net.polix.system.SimplePolixApplication
import net.polix.system.localization.LocalizationService
import net.polix.system.dialog.BaseDialog
import net.polix.system.dialog.DialogCommandService
import net.polix.system.handler.model.Message
import net.polix.system.handler.model.ReplyMessage
import net.polix.system.integration.route.Bot
import net.polix.system.module.ModuleService
import net.polix.system.scheduler.SchedulerService
import net.polix.system.user.User
import net.polix.system.user.UserService
import net.polix.system.user.status.StatusService

interface CommandDialog {
    
    val content: String
    val args: List<String>
    var command: String?
    val moduleService: ModuleService
    val userService: UserService
    val statusService: StatusService
    val commandService: DialogCommandService
    val localizationService: LocalizationService
    val schedulerService: SchedulerService
    var response: String
    var responseWarn: String
    var responseError: String
}

interface UserCommandDialog : CommandDialog {

    val message: Message?
    val isChat: Boolean
    val replyMessage: ReplyMessage?
    val user: User
    var responseReply: String
    val bot: Bot

    var activeDialog: BaseDialog<UserCommandDialog>?
    var reply: Boolean

    var responseReplyLocale: String

    var responseLocale: String
}