package net.polix.system.integration.route.vkontakte.event.group

import net.polix.system.handler.model.Message
import net.polix.system.integration.route.vkontakte.provider.Provider
import w.eventbus.Event

data class VkGroupNewMessageEvent(
    val message: Message,
    val provider: Provider
) : Event
