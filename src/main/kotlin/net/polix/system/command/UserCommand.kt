package net.polix.system.command

import com.vk.api.sdk.client.Lang
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import net.polix.system.localization.LanguageType
import net.polix.system.dialog.UserCommand
import net.polix.system.dialog.dialog.UserCommandDialog
import net.polix.system.integration.IntegrationType
import net.polix.system.integration.route.vkontakte.VkBot
import net.polix.system.utility.ForwardMessage
import net.polix.system.utility.getConversationMessageId
import net.polix.system.utility.getFlag
import kotlin.random.Random

/**
 *
 * AUTHOR: ScarDay
 * CREATED AT: 18/11/2023 | 21:47
 *
 **/
class UserCommand : UserCommand(
        "user", "юзер", description = "COMMAND_USER_DESCRIPTION", permission = "admin.access", isBeta = false, cooldown = 0
) {
    override fun UserCommandDialog.handleInput() {

        if (args.size <= 2) {
            responseReply = "Правильное использование команды:\n— /$command <mention> info - Отдает информацию о пользователе.\n— /$command <mention> status » Отдаст подробную информацию об установке статуса."
            return
        }

        val getId = bot.getId(args[1])

        if (getId < 0) {
            responseReply = "⛔Проверять статус у групп нельзя!"
            return
        }

        if (!userService.repository.existsById(getId)) {
            responseReply = "⛔Данного пользователя не существует :/"
            return
        }

            when (args[2].lowercase()) {
                "info" -> {
                    if (!user.has("command.user.info")) {
                        responseReply = localizationService.findMessagePlaceholders(
                            user.lang,
                            "NO_PERMISSION",
                            arrayOf("%permission%", "command.user.info")
                        )
                        return
                    }

                    val User = userService.createUser(getId, IntegrationType.VK)

                    val name = (bot as VkBot).vkApiClient.users().get((bot as VkBot).groupActor).userIds(User.id.toString())
                        .lang(Lang.valueOf(user.lang.name.uppercase())).execute()

                    val firstNames = name.map { u ->
                        "[id${u.id}|${u.firstName}]"
                    }

                    val firstList = firstNames.joinToString("\n")


                    val permissionsText = if (User.permissions.isEmpty()) {
                        localizationService.findMessage(user.lang, "NO_BLOCKED").toString()
                    } else {
                        if ("*" in User.permissions) {
                            localizationService.findMessage(user.lang, "ALL_PERMISSIONS").toString()
                        } else {
                            User.permissions.joinToString("") { "\n» $it " }
                        }
                    }

                    (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                        .forward(ForwardMessage(message!!))
                        .message(
                            localizationService.findMessagePlaceholders(
                                user.lang, "COMMAND_USER_RESULT",
                                arrayOf(
                                    "%name%",
                                    firstList,
                                    "%from_id%",
                                    User.id.toString(),
                                    "%status_id%",
                                    User.status.id,
                                    "%status_displayName%",
                                    User.status.displayName,
                                    "%lang%",
                                    "${localizationService.findMessage(user.lang, User.lang.key)} ${getFlag(User.lang.name.uppercase().replace("EN", "GB"))}",
                                    "%blocked%",
                                    if (getBlackListUsers((bot as VkBot).groupActor, (bot as VkBot).vkApiClient,
                                            100,
                                            User.id.toInt()
                                        )
                                    ) localizationService.findMessage(user.lang, "BLOCKED")
                                        .toString() else localizationService.findMessage(user.lang, "NO_BLOCKED")
                                        .toString(),
                                    "%permissions%",
                                    permissionsText
                                )
                            )
                        )
                        .lang(Lang.valueOf(user.lang.name.uppercase()))
                        .randomId(Random.nextInt())
                        .peerId(message!!.peerId.toInt())
                        .disableMentions(false)
                        .execute()
                }

                "status" -> {
                    if (!user.has("command.user.status")) {
                        responseReply = localizationService.findMessagePlaceholders(
                            user.lang,
                            "NO_PERMISSION",
                            arrayOf("%permission%", "command.user.status")
                        )
                        return
                    }

                    if (args.size < 4) {
                        responseReply = "ℹ\uFE0FПравильное использование данной подкоманды:\n" +
                                "\n" +
                                "/$command <mention> status set <status> » Устанавливает статус пользователю.\n" +
                                "/$command <mention> status remove » Устанавливает стандартный статус пользователю."

                        return
                    }

                    when (args[3].lowercase()) {
                        "set" -> {
                            if (!user.has("command.user.status.set")) {
                                responseReply = localizationService.findMessagePlaceholders(
                                    user.lang,
                                    "NO_PERMISSION",
                                    arrayOf("%permission%", "command.user.status.set")
                                )
                                return
                            }

                            val loading = (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                                .forward(ForwardMessage(message!!))
                                .message(localizationService.findMessage(user.lang, "LOADING"))
                                .randomId(Random.nextInt())
                                .peerIds(message!!.peerId.toInt())
                                .executeAsString()

                            val existsStatus = statusService.existsStatus(args[4])

                            if (!existsStatus) {
                                (bot as VkBot).vkApiClient.messages()
                                    .edit((bot as VkBot).groupActor, message!!.peerId.toInt())
                                    .message("\uD83D\uDEABВы ввели не верный статус.")
                                    .conversationMessageId(getConversationMessageId(loading))
                                    .keepForwardMessages(true)
                                    .execute()
                                return
                            }

                            val status = statusService.getStatus(args[4])

                            val getIdUser = userService.createUser(getId, IntegrationType.VK)

                            val displayName = status?.displayName ?: "<не удалось получить название роли>"
                            getIdUser.status = status!!
                            userService.repository.save(getIdUser)

                            (bot as VkBot).vkApiClient.messages()
                                .edit((bot as VkBot).groupActor, message!!.peerId.toInt())
                                .message("✅Изменил статус пользователя на $displayName")
                                .conversationMessageId(getConversationMessageId(loading))
                                .keepForwardMessages(true)
                                .execute()
                        }


                        "remove" -> {
                            if (!user.has("command.user.status.remove")) {
                                responseReply = localizationService.findMessagePlaceholders(
                                    user.lang,
                                    "NO_PERMISSION",
                                    arrayOf("%permission%", "command.user.status.set")
                                )
                                return
                            }

                            val loading = (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                                .forward(ForwardMessage(message!!))
                                .message(localizationService.findMessage(user.lang, "LOADING"))
                                .randomId(Random.nextInt())
                                .peerIds(message!!.peerId.toInt())
                                .executeAsString()

                            val status = statusService.getStatus("user")

                            val getIdUser = userService.createUser(getId, IntegrationType.VK)

                            if (getIdUser.status.id == status!!.id) {
                                (bot as VkBot).vkApiClient.messages()
                                    .edit((bot as VkBot).groupActor, message!!.peerId.toInt())
                                    .message("✅У пользователя итак данный статус.")
                                    .conversationMessageId(getConversationMessageId(loading))
                                    .keepForwardMessages(true)
                                    .execute()
                                return
                            }

                            getIdUser.status = status
                            userService.repository.save(getIdUser)

                            (bot as VkBot).vkApiClient.messages()
                                .edit((bot as VkBot).groupActor, message!!.peerId.toInt())
                                .message("✅Успешно сброшен статус пользователю.")
                                .conversationMessageId(getConversationMessageId(loading))
                                .keepForwardMessages(true)
                                .execute()
                        }

                        else -> {
                            responseReply = "Вы ввели не верную подкоманду."
                            responseReply = "ℹ\uFE0FПравильное использование данной подкоманды:\n" +
                                    "\n" +
                                    "/$command <mention> status set <status> » Устанавливает статус пользователю.\n" +
                                    "/$command <mention> status remove » Устанавливает стандартный статус пользователю."

                        }
                    }
                }

                "lang" -> {
                    if (!user.has("command.user.status.lang")) {
                        responseReply = localizationService.findMessagePlaceholders(
                            user.lang,
                            "NO_PERMISSION",
                            arrayOf("%permission%", "command.user.status.lang")
                        )
                        return
                    }

                    if (args.size < 4) {
                        responseReply = "Вы не указали язык"
                        return
                    }

                    val User = userService.createUser(getId, IntegrationType.VK)

                    val languageList = LanguageType.values().toList()

                    val isNewLanguageValid = languageList.any { it.name.equals(args[3], ignoreCase = true) }

                    if (!isNewLanguageValid) {
                        responseReply = "Вы ввели не верный язык!"
                        return
                    }

                    val newLanguage = args[3].uppercase()

                    val LangInfo = LanguageType.valueOf(newLanguage)

                    if (User.lang == LangInfo) {
                        responseReply = "У пользователя итак установлен этот язык."
                        return
                    }

                    User.lang = LangInfo
                    userService.repository.saveAndFlush(User)

                    responseReply = "Успешно установил язык [id${User.id}|Пользователю] на ${localizationService.findMessage(user.lang, LangInfo.key)}"
                }

                else -> {
                    responseReply = "Вы указали не верную подкоманду."
                }
            }
    }

    private fun getBlackListUsers(actor: GroupActor, methods: VkApiClient, count: Int, id: Int): Boolean {
        val blacklist = methods.groups().getBanned(actor, actor.id.toString().replace("-", "").toInt())
            .count(count)
            .execute().items

        if (blacklist .isNotEmpty()) {
            val blacklistItem = blacklist[0]

            return blacklistItem.profile.id == id
        }

        return false
    }
    
}