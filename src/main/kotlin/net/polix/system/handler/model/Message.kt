package net.polix.system.handler.model

import net.polix.system.integration.IntegrationType

data class Message(
    val type: IntegrationType,
    val text: String,
    val fromId: Long,
    val peerId: Long,
    val messageId: Int,
    val payload: String?,
    val replyMessage: ReplyMessage? = null,
    val fwdMessages: FwdMessages? = null,
    val attachments: Attachments? = null
)

data class ReplyMessage(
    val messageId: Int? = null,
    val fromId: Int? = null,
    val text: String? = null
)

data class FwdMessages(
    val messageId: Int? = null,
    val fromId: Int? = null,
    val text: String? = null
)

data class Attachments(
    val photo: photo? = null
)

data class photo (
    val album_id: Int? = null,
    val date: Int? = null,
    val owner_id: Int? = null,
    val sizes: ArrayList<photoSizes>? = null
)

data class photoSizes(
    val height: Int? = null,
    val type: String? = null,
    val width: Int? = null,
    val url: String? = null
)
