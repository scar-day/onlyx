package net.polix.system.handler

import net.polix.system.integration.route.vkontakte.event.group.VkGroupNewMessageEvent

interface CommandHandler {

    fun handleMessage(event: VkGroupNewMessageEvent)

}