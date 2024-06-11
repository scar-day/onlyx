package net.polix.system.command

import com.google.gson.JsonObject
import com.vk.api.sdk.objects.messages.Keyboard
import com.vk.api.sdk.objects.messages.KeyboardButton
import com.vk.api.sdk.objects.messages.KeyboardButtonColor
import net.polix.system.LOGGER
import net.polix.system.localization.LanguageType
import net.polix.system.localization.LocalizationService
import net.polix.system.dialog.UserCommand
import net.polix.system.dialog.dialog.UserCommandDialog
import net.polix.system.integration.route.vkontakte.VkBot
import net.polix.system.user.User
import net.polix.system.utility.ForwardMessage
import net.polix.system.utility.createbutton
import net.polix.system.utility.getFlag
import java.util.*

class LangCommand : UserCommand(
    "lang", "язык", "language", "сменитьязык", cooldown = 3, isBeta = true, description = "COMMAND_LANG_DESCRIPTION"
) {

    private fun createButton(localizationService: LocalizationService, user: User, command: String, lang: LanguageType, userLang: LanguageType): KeyboardButton {
        val payload = JsonObject().apply {
            addProperty("command", "$command ${lang.name.uppercase()}")
        }

        val color = if (lang == userLang) KeyboardButtonColor.POSITIVE else KeyboardButtonColor.DEFAULT

        return createbutton("${getFlag(lang.name.uppercase().replace("EN", "US"))} ${localizationService.findMessage(user.lang,lang.key)}", payload, color)
    }

    override fun UserCommandDialog.handleInput() {

            val langAbbreviation = localizationService.findMessage(user.lang,user.lang.key)
            val langCodeUpperCase = user.lang.name.uppercase().replace("EN", "GB")
            val langFlag = getFlag(langCodeUpperCase)

            val languageList = LanguageType.values().toList()

            val buttonChunks = languageList.chunked(2)

            val buttonRows = buttonChunks.map { chunk ->
                chunk.map { createButton(localizationService, user, command.toString(), it, user.lang) }
            }

        LOGGER.info(buttonRows.toString())
            val keyboard = Keyboard().setButtons(buttonRows).setInline(true)

            if (args.size < 2) {
                (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                    .forward(ForwardMessage(message!!))
                    .keyboard(keyboard)
                    .message(
                        localizationService.findMessagePlaceholders(
                            user.lang, "COMMAND_LANG", arrayOf("%language%", "$langAbbreviation $langFlag")
                        )
                    )
                    .peerId(message!!.peerId.toInt())
                    .randomId(Random().nextInt())
                    .dontParseLinks(true)
                    .execute()
            } else {
                val newLanguage = args[1].uppercase()

                val isNewLanguageValid = languageList.any { it.name.equals(newLanguage, ignoreCase = true) }

                if (!isNewLanguageValid) {
                    responseReply = localizationService.findMessage(user.lang, "COMMAND_LANG_NO_CORRECT").toString()
                } else if (user.lang.name.equals(newLanguage, ignoreCase = true)) {
                    responseReply = localizationService.findMessage(user.lang, "COMMAND_LANG_ALREADY").toString()
                } else {
                    user.lang = LanguageType.valueOf(newLanguage)
                    userService.repository.save(user)
                    responseReply = localizationService.findMessagePlaceholders(
                        user.lang,
                        "COMMAND_LANG_CHANGE",
                        arrayOf(
                            "%lang%",
                            localizationService.findMessage(user.lang, user.lang.key).toString(),
                            "%flag%",
                            getFlag(user.lang.name.uppercase().replace("EN", "GB"))
                        )
                    )
                }
        }
    }
}

