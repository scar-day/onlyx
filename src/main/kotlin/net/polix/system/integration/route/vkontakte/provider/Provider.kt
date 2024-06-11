package net.polix.system.integration.route.vkontakte.provider

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor

interface Provider {

    fun getMethods(): VkApiClient

    fun getActor(): GroupActor

    fun getEvents()

    fun getProviderType(): ProviderType
}