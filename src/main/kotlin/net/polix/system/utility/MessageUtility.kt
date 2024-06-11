package net.polix.system.utility

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.vk.api.sdk.objects.messages.*
import net.polix.system.handler.model.Message


fun ForwardMessage(message: Message?): Forward {
    return Forward()
        .setConversationMessageIds(listOf(message!!.messageId))
        .setPeerId(message.peerId.toInt())
        .setIsReply(true)
}

fun getConversationMessageId(message: String?): Int {
    val jsonObject = JsonParser.parseString(message).asJsonObject
    val responseArray = jsonObject.getAsJsonArray("response")
    if (responseArray.size() > 0) {
        val responseObject = responseArray[0].asJsonObject
        return responseObject.get("conversation_message_id").asInt
    }
    return 0
}

fun createbutton(label: String, payload: JsonObject, colorButton: KeyboardButtonColor): KeyboardButton {
    val action = KeyboardButtonAction()
    action.label = label
    action.payload = payload.toString()
    action.type = TemplateActionTypeNames.TEXT

    return KeyboardButton().setAction(action).setColor(colorButton)
}

fun extractIP(input: String?): String? {
    val pattern = Regex("\\b(?:\\d{1,3}\\.){3}\\d{1,3}(?::\\d{1,5})?\\b|(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(?::\\d{1,5})?\\b|([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]{1,4})")
    val matchResult = pattern.find(input ?: "")
    val result = matchResult?.value

    if (result != null && !result.contains(":")) {
        return "$result:25565"
    }

    return result
}

fun extractTwoIP(input: String?): Pair<String?, String?> {
    val pattern = Regex("\\b(?:\\d{1,3}\\.){3}\\d{1,3}(?::\\d{1,5})?\\b|(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(?::\\d{1,5})?\\b|([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]{1,4})")
    val matchResults = pattern.findAll(input ?: "")
    val results = matchResults.map { it.value }.toList()

    if (results.size >= 2) {
        var firstIP = results[0]
        var secondIP = results[1]

        if (!firstIP.contains(":")) {
            firstIP = "$firstIP:25565"
        }

        if (!secondIP.contains(":")) {
            secondIP = "$secondIP:25565"
        }

        return Pair(firstIP, secondIP)
    }

    return Pair(null, null)
}

