package net.polix.system.integration.route.vkontakte

import net.polix.system.event.listener.EventListener
import net.polix.system.integration.IntegrationType
import net.polix.system.integration.route.Route
import net.polix.system.integration.route.vkontakte.provider.Provider

class VkRoute(
    private val provider: Provider
) : Route, EventListener {
    override fun getType(): IntegrationType = IntegrationType.VK

    override fun handleEvents() {
        provider.getEvents()
    }
}