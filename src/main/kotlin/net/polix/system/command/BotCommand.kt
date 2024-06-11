package net.polix.system.command

import com.google.gson.JsonObject
import com.vk.api.sdk.objects.messages.Keyboard
import com.vk.api.sdk.objects.messages.KeyboardButtonColor
import net.polix.system.dialog.UserCommand
import net.polix.system.dialog.dialog.UserCommandDialog
import net.polix.system.integration.IntegrationType
import net.polix.system.integration.route.vkontakte.VkBot
import net.polix.system.startTime
import net.polix.system.utility.ForwardMessage
import net.polix.system.utility.createbutton
import net.polix.system.utility.getTimeLeft
import java.util.*
import kotlin.random.Random


/**
 *
 * AUTHOR: ScarDay
 * CREATED AT: 15/11/2023 | 20:45
 *
 **/
class BotCommand : UserCommand(
        "bot", "бот", cooldown = 3, description = "COMMAND_BOT_DESCRIPTION"
) {
    override fun UserCommandDialog.handleInput() {
        val versionProperties = Properties()
        versionProperties.load(this.javaClass.getResourceAsStream("/info.properties"))

        val projectVersion = versionProperties

        val keyboard: Keyboard = if (user.has("core.access") && user.has("command.core.stop")) {
            val payload = JsonObject().apply {
                addProperty("command", "core stop")
            }

            val restart = createbutton(localizationService.findMessage(user.lang, "COMMAND_BOT_INLINE_RESTART").toString(), payload, KeyboardButtonColor.DEFAULT)
            val row1 = listOf(restart)
            Keyboard().setInline(true).setButtons(listOf(row1))
        } else {
            Keyboard().setInline(true).setButtons(listOf())
        }

            (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                .forward(ForwardMessage(message!!))
                .message(
                    localizationService.findMessagePlaceholders(
                        user.lang,
                        "COMMAND_BOT_INFO",
                        arrayOf(
                            "%version%",
                            projectVersion.getProperty("version").toString(),
                            "%commands%",
                            commandService.commands.filter { !it.isConsole }.size.toString(),
                            "%users%",
                            userService.repository.countAllByType(IntegrationType.VK).toString(),
                            "%uptime%",
                            getTimeLeft((System.currentTimeMillis() - startTime) / 1000L, localizationService, user),
                            "%module_counts%",
                            moduleService.getModules().size.toString(),
                            "%scheduler_counts%",
                            schedulerService.schedulerMap.size.toString()
                        )
                    )
                )
                .peerId(message!!.peerId.toInt())
                .randomId(Random.nextInt())
                .keyboard(keyboard)
                .execute()


    }

}