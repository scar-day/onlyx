package net.polix.system.dialog.dialog.impl

import net.polix.system.SimplePolixApplication
import net.polix.system.localization.LocalizationService
import net.polix.system.dialog.BaseDialog
import net.polix.system.dialog.DialogCommandService
import net.polix.system.dialog.dialog.UserCommandDialog
import net.polix.system.handler.model.Message
import net.polix.system.handler.model.ReplyMessage
import net.polix.system.integration.route.Bot
import net.polix.system.module.ModuleService
import net.polix.system.scheduler.SchedulerService
import net.polix.system.user.User
import net.polix.system.user.UserService
import net.polix.system.user.status.StatusService

class UserCommandDialogImpl(
    override val content: String,
    override var command: String?,
    override val message: Message,
    override val isChat: Boolean,
    override val replyMessage: ReplyMessage?,
    override var activeDialog: BaseDialog<UserCommandDialog>?,
    override val moduleService: ModuleService,
    override val userService: UserService,
    override val statusService: StatusService,
    override val localizationService: LocalizationService,
    override val bot: Bot,
    override val user: User,
    override val commandService: DialogCommandService,
    override val schedulerService: SchedulerService,
) : UserCommandDialog {

    override val args: List<String> = content.split(" ")

    override var reply: Boolean = true

    override var responseLocale: String = ""
        set(value) {
            field = value

            val lang = localizationService.findMessage(user.lang, field) ?: field
            bot.sendMessage(lang, message.peerId.toString())
        }

    override var response: String = ""
        set(value) {
            field = value

            bot.sendMessage(field, message.peerId.toString())
        }

    override var responseError: String = ""

    override var responseWarn: String = ""
    override var responseReplyLocale: String = ""
        set(value) {
            field = value

            val lang = localizationService.findMessage(user.lang, value)
            bot.sendReplyMessage(lang.toString(), message)
        }

    private fun String.replacePlaceholders(placeholders: Array<String>): String {
        var replaceMessage = this

        for (i in placeholders.indices step 2) {
            val placeholderKey = placeholders[i]
            val placeholderValue = placeholders[i + 1]

            replaceMessage = replaceMessage.replace(placeholderKey, placeholderValue)
        }

        return replaceMessage
    }

    override var responseReply: String = ""
        set(value) {
            field = value

            bot.sendReplyMessage(value, message)
        }


}