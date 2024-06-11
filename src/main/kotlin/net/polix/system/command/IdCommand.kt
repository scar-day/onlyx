package net.polix.system.command

import net.polix.system.dialog.UserCommand
import net.polix.system.dialog.dialog.UserCommandDialog
import net.polix.system.integration.route.vkontakte.VkBot


/**
 *
 * AUTHOR: ScarDay
 * CREATED AT: 26/11/2023 | 19:06
 *
 **/
class IdCommand : UserCommand(
    "id", "vk", "айди", "ид", description = "COMMAND_ID_DESCRIPTION"
) {
    override fun UserCommandDialog.handleInput() {

        if (bot is VkBot) {

            if (message!!.replyMessage?.fromId != null) {
                responseReply = localizationService.findMessagePlaceholders(
                    user.lang, "COMMAND_ID",
                    arrayOf("%id%", message!!.replyMessage?.fromId.toString())
                )
                return
            }

            if (args.size < 2) {
                responseReply = localizationService.findMessagePlaceholders(
                    user.lang, "COMMAND_ID",
                    arrayOf("%id%", message!!.fromId.toString())
                )
                return
            }

            if (bot.getAllId(args[1]).toInt() == -1) {
                responseReplyLocale = "COMMAND_ID_NO_CORRECT"
                return
            }

            responseReply = localizationService.findMessagePlaceholders(
                user.lang, "COMMAND_ID",
                arrayOf("%id%", bot.getAllId(args[1]).toString())
            )
        }
    }
}