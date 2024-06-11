package net.polix.system.command

import com.google.gson.JsonObject
import com.vk.api.sdk.client.Lang
import com.vk.api.sdk.objects.messages.Keyboard
import com.vk.api.sdk.objects.messages.KeyboardButtonColor
import net.polix.system.dialog.UserCommand
import net.polix.system.dialog.dialog.UserCommandDialog
import net.polix.system.integration.IntegrationType
import net.polix.system.integration.route.vkontakte.VkBot
import net.polix.system.utility.ForwardMessage
import net.polix.system.utility.createbutton
import net.polix.system.utility.getConversationMessageId
import kotlin.random.Random

/**
 *
 * AUTHOR: ScarDay
 * CREATED AT: 11/11/2023 | 22:19
 *
 **/

class AdminCommand : UserCommand(
         "admin", "админ", description = "COMMAND_ADMIN_DESCRIPTION", permission = "admin.access"
) {
    override fun UserCommandDialog.handleInput() {

        if (args.size == 1) {
            responseReply = "Правильное использование данной команды:\n— /$command infostatus <status> » Отдает информацию о статусе\n— /$command listusers <status> » Отдает список пользователей данного статуса"
            return
        }

        when (args[1].lowercase()) {
            "infostatus", "инфостатус" -> {
                if (!user.has("command.admin.infostatus")) {
                    responseReply = localizationService.findMessagePlaceholders(
                        user.lang,
                        "NO_PERMISSION",
                        arrayOf("%permission%", "command.mcadmin.changealias")
                    )
                    return
                }

                if (args.size < 2) {
                    responseReply = "⛔Вы не указали аргументы для данной подкоманды."
                    return
                }

                val statuss = statusService.getStatus(args[2])

                if (statuss == null) {
                    responseReply = "Статус не был найден"
                    return
                }

                val builder = StringBuilder()

                if (statuss.permissions.isNotEmpty()) {
                    builder.append("Возможности статуса:")
                    for (stat in statuss.permissions) {
                        builder
                            .append("\n").append("- $stat")
                    }
                }
                responseReply = "ℹ\uFE0FИнформация о статусе: \n" +
                        "\n" +
                        "Отображаемое имя: ${statuss.displayName} (${statuss.id})\n" +
                        "Приоритет статуса: ${statuss.priority}\n" +
                        "" +
                        builder
            }

            "listusers" -> {
                if (!user.has("command.admin.listusers")) {
                    responseReply = localizationService.findMessagePlaceholders(
                        user.lang,
                        "NO_PERMISSION",
                        arrayOf("%permission%", "command.admin.listusers")
                    )
                    return
                }

                if (args.size < 3) {
                    responseReply = "⛔️Вы не указали статус!"
                    return
                }

                if (bot is VkBot) {

                    val loading = (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                        .peerIds(message!!.peerId.toInt())
                        .forward(ForwardMessage(message!!))
                        .message("Загрузка...")
                        .dontParseLinks(true)
                        .randomId(Random.nextInt())
                        .disableMentions(true)
                        .executeAsString()

                    val status = statusService.getStatus(args[2]) ?: run {
                        (bot as VkBot).vkApiClient.messages().edit((bot as VkBot).groupActor, message!!.peerId.toInt())
                            .keepForwardMessages(true)
                            .message("Статус не был найден!")
                            .dontParseLinks(true)
                            .conversationMessageId(getConversationMessageId(loading))
                            .execute()
                        return
                    }

                    val usersWithStatus = userService.repository.getUsersByStatus(status)

                    val userChunks = usersWithStatus.chunked(20)

                    val currentPage = args.getOrNull(3)?.toIntOrNull() ?: 1

                    if (currentPage !in 1..userChunks.size) {
                        (bot as VkBot).vkApiClient.messages().edit((bot as VkBot).groupActor, message!!.peerId.toInt())
                            .keepForwardMessages(true)
                            .message("Некорректный номер страницы.")
                            .dontParseLinks(true)
                            .conversationMessageId(getConversationMessageId(loading))
                            .execute()
                        return
                    }

                    val currentChunk = userChunks.getOrNull(currentPage - 1)

                    if (currentChunk.isNullOrEmpty()) {
                        (bot as VkBot).vkApiClient.messages().edit((bot as VkBot).groupActor, message!!.peerId.toInt())
                            .keepForwardMessages(true)
                            .message("На странице $currentPage нет пользователей.")
                            .dontParseLinks(true)
                            .keepSnippets(true)
                            .conversationMessageId(getConversationMessageId(loading))
                            .execute()
                        return
                    }

                    val userIds =
                        currentChunk.filter { it.type == IntegrationType.VK }.map { it.id.toString() }.toTypedArray()
                    val usersResponse = (bot as VkBot).vkApiClient.users().get((bot as VkBot).groupActor)
                        .userIds(*userIds)
                        .lang(Lang.valueOf(user.lang.name.uppercase()))
                        .execute()

                    val usersInfo = usersResponse.map { user ->
                        "- [id${user.id}|${user.firstName} ${user.lastName}]"
                    }

                    val usersList = usersInfo.joinToString("\n")

                    val showKeyboard = userChunks.size > 1

                    val messageText = if (currentChunk.size <= 2) {
                        "ℹ️\uFE0FВот все пользователи с данным статусом (страница $currentPage):\n$usersList"
                    } else {
                        "ℹ️\uFE0FВот все пользователи с данным статусом (страница $currentPage):\n$usersList"
                    }

                    val messageBuilder =
                        (bot as VkBot).vkApiClient.messages().edit((bot as VkBot).groupActor, message!!.peerId.toInt())
                            .keepForwardMessages(true)
                            .message(messageText)
                            .dontParseLinks(true)
                            .conversationMessageId(getConversationMessageId(loading))

                    if (showKeyboard) {
                        messageBuilder.keyboard(createKeyboard(currentPage, userChunks.size, args).toString())
                    }

                    messageBuilder.execute()

                }
            }
        }
    }



    private fun createKeyboard(currentPage: Int, totalPageCount: Int, args: List<String>): Keyboard {
        val keyboardButtons = (1..totalPageCount).map { page ->
            val payload = JsonObject()
            payload.addProperty("command", "admin listusers ${args.getOrNull(2) ?: ""} $page")

            val label = page.toString()
            val isCurrentPage = page == currentPage

            val knopki = createbutton(label, payload, if (isCurrentPage) KeyboardButtonColor.POSITIVE else KeyboardButtonColor.DEFAULT)

            knopki
        }

        val buttonsInRow = 4
        val keyboardRows = keyboardButtons.chunked(buttonsInRow)

        return Keyboard().setButtons((keyboardRows)).setInline(true)
    }


}