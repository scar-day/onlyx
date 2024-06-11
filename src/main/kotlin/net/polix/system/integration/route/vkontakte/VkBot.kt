package net.polix.system.integration.route.vkontakte

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import net.polix.system.LOGGER
import net.polix.system.handler.model.Message
import net.polix.system.integration.route.Bot
import net.polix.system.utility.ForwardMessage
import java.io.File
import java.util.*

class VkBot(
    val groupActor: GroupActor
) : Bot {

    val vkApiClient = VkApiClient(HttpTransportClient())

    override fun sendMessage(text: String, chatId: String) {
        vkApiClient.messages().send(groupActor)
            .randomId(Random().nextInt())
            .message(text)
            .peerId(chatId.toInt())
            .execute()
    }

    override fun sendReplyMessage(text: String, message: Message) {

        vkApiClient.messages().send(groupActor)
            .message(text)
            .randomId(Random().nextInt())
            .peerId(message.peerId.toInt())
            .forward(ForwardMessage(message))
            .disableMentions(true)
            .dontParseLinks(true)
            .execute()
    }

    override fun sendPeerMesasge(chatId: String, text: String) {
        vkApiClient.messages().send(groupActor)
            .message(text)
            .randomId(Random().nextInt())
            .peerId(chatId.toInt())
            .execute()
    }

    override fun getId(name: String): Long {
        var id: Long = 0

        if(name.startsWith("https://vk.com")) {
            val userId = name.replace("https://vk.com/", "")
            try {
                val response = vkApiClient.users().get(groupActor)
                    .userIds(userId)
                    .execute()[0]
                return response.id.toLong()
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        }

        try {
            val nameParts = name.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (nameParts.isNotEmpty()) {
                val userIdString = nameParts[0].replace("[id", "").replace("[club", "-")
                id = userIdString.toLong()
            }
        } catch (e: Exception) {
            println(e)
        }
        return id
    }

    override fun getAllId(name: String): Long {
        var id = 0L

        try {
            val nameParts = name.split("|")
            id = if (nameParts.size == 2) {
                nameParts[0].replace("[club", "-").replace("[id", "").toLong()
            } else {
                0
            }
        } catch (e: NumberFormatException) {
            LOGGER.error("Ошибка преобразования строки в Long: $e")
        } catch (e: IllegalArgumentException) {
            LOGGER.error(e.message)
        }

        return id
    }

    override fun getValue(message: Message, args: List<String>): String? {
        val fwdMessages = message.fwdMessages
        val replyMessage = message.replyMessage

        if (args.isEmpty() && fwdMessages?.text?.isEmpty() == true && (replyMessage?.text?.isEmpty() == true)) {
            return null
        }

        val text = when {
            fwdMessages?.text?.isNotEmpty() == true -> fwdMessages.text
            replyMessage?.text?.isNotEmpty() == true -> replyMessage.text
            else -> args.joinToString(" ")
        }

        return text.replace("@all", "[@]all")
            .replace("*all", "[*]all")
    }

    override fun sendReplyMapMessage(text: String, message: Message, lat: Double, lon: Double) {

    }

    override fun sendPhoto(chatId: Long, photo: File) {

    }


}