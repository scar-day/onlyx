package net.polix.system.command

import com.google.gson.JsonObject
import com.vk.api.sdk.objects.messages.Keyboard
import com.vk.api.sdk.objects.messages.KeyboardButtonColor
import net.polix.system.LOGGER
import net.polix.system.localization.LocalizationService
import net.polix.system.dialog.UserCommand
import net.polix.system.dialog.dialog.UserCommandDialog
import net.polix.system.integration.route.vkontakte.VkBot
import net.polix.system.user.User
import net.polix.system.utility.ForwardMessage
import net.polix.system.utility.createbutton
import kotlin.random.Random

class HelpCommand : UserCommand(
    "help", "commands", "команды", cooldown = 3, description = "COMMAND_HELP_DESCRIPTION"
) {
    override fun UserCommandDialog.handleInput() {

        val sorted = commandService.commands
            .filter { !it.isConsole }
            .filter { it.permission == null || user.has(it.permission.toString()) }
            .sortedWith(compareBy { it.commandNames.first() })

        val commands = sorted.joinToString("\n") { commands ->
            val name = commands.commandNames.first()
            val description = localizationService.findMessage(user.lang, commands.description)
            val beta = if (commands.isBeta) "(β)" else ""
            "— $name » $description $beta"
        }

        if (bot is VkBot) {


            (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                .message(
                    "${localizationService.findMessage(user.lang, "COMMAND_HELP")}:\n" + commands
                )
                .peerId(message!!.peerId.toInt())
                .randomId(Random.nextInt())
                .forward(ForwardMessage(message!!))
                .keyboard(createkeybard(localizationService, user))
                .execute()
        }


    }

    private fun createkeybard(localizationService: LocalizationService, user: User): Keyboard {
        val payload = JsonObject()
        payload.addProperty("command", "aliases")
        val alias = createbutton(localizationService.findMessage(user.lang, "COMMAND_HELP_INLINE_ALIASES").toString(), payload, KeyboardButtonColor.DEFAULT)
        LOGGER.info(alias.toString())
        val row = listOf(alias)
        return Keyboard() .setButtons(listOf(row)).setInline(true)
    }
}