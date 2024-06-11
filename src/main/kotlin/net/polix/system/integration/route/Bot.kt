package net.polix.system.integration.route

import net.polix.system.handler.model.Message
import java.io.File

interface Bot {

    fun sendMessage(text: String, chatId: String)

    fun sendReplyMessage(text: String, message: Message)

    fun sendPeerMesasge(chatId: String, text: String)

    fun getId(name: String): Long

    fun getAllId(name: String): Long

    fun getValue(message: Message, args: List<String>): String?

    fun sendReplyMapMessage(text: String, message: Message, lat: Double, lon: Double)

    fun sendPhoto(chatId: Long, photo: File)
}
