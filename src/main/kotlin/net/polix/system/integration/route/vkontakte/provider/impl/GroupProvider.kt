package net.polix.system.integration.route.vkontakte.provider.impl

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vk.api.sdk.actions.LongPoll
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.exceptions.ApiAuthException
import com.vk.api.sdk.exceptions.ClientException
import com.vk.api.sdk.exceptions.LongPollServerKeyExpiredException
import com.vk.api.sdk.httpclient.HttpTransportClient
import net.polix.system.LOGGER
import net.polix.system.event.EventService
import net.polix.system.handler.model.*
import net.polix.system.integration.IntegrationType
import net.polix.system.integration.route.vkontakte.event.group.VkGroupNewMessageEvent
import net.polix.system.integration.route.vkontakte.provider.Provider
import net.polix.system.integration.route.vkontakte.provider.ProviderType
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


class GroupProvider(
    private val id: Int,
    private val token: String,
    private val eventService: EventService
) : Provider {
    override fun getMethods(): VkApiClient = VkApiClient(HttpTransportClient())
    override fun getActor(): GroupActor = GroupActor(id, token)



//    @Bean
//    fun vkApiClient(enableProxy: Boolean, httpClient: CloseableHttpClient?): VkApiClient {
//        if (enableProxy) {
//            val transportClient: TransportClient = HttpTransportClient.getInstance()
//            val httpClientField = transportClient::class.java.getDeclaredField("httpClient")
//            httpClientField.isAccessible = true
//            httpClientField.set(null, httpClient)
//
//            return VkApiClient(transportClient)
//        }
//
//        return VkApiClient(HttpTransportClient())
//    }
//
//
//    @Bean
//    fun httpClient(proxyHost: String, proxyPort: Int, username: String?, password: String?): CloseableHttpClient? {
//        val credentialsProvider = BasicCredentialsProvider()
//        credentialsProvider.setCredentials(
//            AuthScope(proxyHost, proxyPort),
//            UsernamePasswordCredentials(username, password)
//        )
//
//        val context = HttpClientContext.create()
//        context.credentialsProvider = credentialsProvider
//
//        val sslContext = SSLContexts.custom()
//            .loadTrustMaterial(null, TrustSelfSignedStrategy())
//            .build()
//
//        val socksProxy = HttpHost(proxyHost, proxyPort)
//        val routePlanner = DefaultProxyRoutePlanner(socksProxy)
//
//        val requestConfig = RequestConfig.custom()
//            .setProxy(socksProxy)
//            .build()
//
//        val connectionManager = PoolingHttpClientConnectionManager()
//        connectionManager.maxTotal = 300
//        connectionManager.defaultMaxPerRoute = 300
//
//        val clientBuilder = HttpClients.custom()
//            .setSSLContext(sslContext)
//            .setRoutePlanner(routePlanner)
//            .setDefaultRequestConfig(requestConfig)
//            .setDefaultCredentialsProvider(credentialsProvider)
//            .setConnectionManager(connectionManager)
//
//        val cookieStore = BasicCookieStore()
//        clientBuilder.setDefaultCookieStore(cookieStore)
//        clientBuilder.setUserAgent("Java VK SDK/0.4.3")
//
//        return clientBuilder.build()
//    }


    override fun getEvents() {
        Executors.newSingleThreadExecutor().execute {
            LOGGER.info("[EventService - VK] <-> Попытка подключиться к LongPoll")
            try {
                while (true) {
                    try {
                        val serverInfo = getMethods().groups().getLongPollServer(getActor(), getActor().groupId).execute()
                        val longPoll = LongPoll(getMethods())

                        val server = serverInfo.server
                        var ts = serverInfo.ts
                        val key = serverInfo.key
                        LOGGER.info("[EventService - VK] <-> Подключился к LongPoll")
                        while (true) {
                            val response = longPoll.getEvents(server, key, ts).waitTime(25).execute()
                            ts = response.ts

                            response.updates.forEach { e ->
                                if (e.has("type") && e.has("object")) {
                                    val rawObject = e.getAsJsonObject("object")
                                    val type = e.get("type").asString

                                    if (rawObject != null) {
                                        when (type) {
                                            "message_new" -> {
                                                onMessageNew(rawObject)
                                            }

                                            else -> return@forEach
                                        }
                                    }

                                }
                            }
                        }
                    } catch (e: LongPollServerKeyExpiredException) {
                        getMethods().groups().getLongPollServer(getActor(), getActor().groupId).execute().key

                        LOGGER.warn("[EventService - VK] <-> Сменил данные о сервере LongPoll!")
                    } catch (e: ApiAuthException) {
                        LOGGER.warn("[EventService - VK] <-> Вы указали не верный access_token токен, завершаю процесс..")
                        exitProcess(-1)
                    } catch (e: ClientException) {
                        LOGGER.warn("[EventService - VK] <-> Произошла ошибка, повторное подключение к VK-API через 30 сек:")
                        LOGGER.error(e.toString())
                        TimeUnit.SECONDS.sleep(30)
                    }
                }
            } catch (e: Exception) {
                LOGGER.info(e.stackTraceToString())
            }
        }
    }

    private fun onMessageNew(message: JsonObject) {
        if (message.has("message")) {

            val messageData = message.get("message").asJsonObject

            val fromId = messageData.get("from_id").asInt
            val text = messageData.get("text").asString
            val peerId = messageData.get("peer_id").asInt
            val messageId = messageData.get("conversation_message_id").asInt

            val payload = messageData.getAsJsonPrimitive("payload")?.asString ?: ""

            val replyInfo: JsonObject? = messageData.getAsJsonObject("reply_message")
            val (replymessageId, replyMessageFromId, replymessagetext) = replyInfo?.let {
                Triple(it.get("conversation_message_id").asInt, it.get("from_id").asInt, it.get("text").asString)
            } ?: Triple(null, null, null)

            val fwdMessage: JsonArray? = messageData.getAsJsonArray("fwd_messages")
            val (fwdMessageId, fwdMessageFromId, fwdMessagemessagetext) = fwdMessage?.let {
                if (it.size() > 0) {
                    val firstMessage = it[0].asJsonObject
                    Triple(
                        if (firstMessage.has("id")) {
                            firstMessage.get("id").asInt
                        } else {
                            0
                        },
                        firstMessage.get("from_id").asInt,
                        firstMessage.get("text").asString
                    )
                } else {
                    Triple(null, null, null)
                }
            } ?: Triple(null, null, null)

            val attachments: JsonArray? = messageData.getAsJsonArray("attachments")
            val photoData: photo = attachments?.let {
                if (it.size() > 0) {
                    val firstAttachment = it[0].asJsonObject
                    val photo = firstAttachment.getAsJsonObject("photo")

                    val sizesList: ArrayList<photoSizes> = ArrayList()

                    if (photo != null) {
                        val sizes = photo.getAsJsonArray("sizes")

                        sizes.forEach { size ->
                            val sizeObject = size.asJsonObject
                            val height = sizeObject.get("height").asInt
                            val width = sizeObject.get("width").asInt
                            val type = sizeObject.get("type").asString
                            val url = sizeObject.get("url").asString

                            val photoSize = photoSizes(height, type, width, url)
                            sizesList.add(photoSize)
                        }

                        photo(
                            photo.get("date").asInt,
                            photo.get("owner_id").asInt,
                            photo.getAsJsonPrimitive("album_id").asInt,
                            sizesList
                        )
                    } else {
                        photo(null, null, null, null)
                    }
                } else {

                    photo(null, null, null, null)
                }
            } ?: photo(null, null, null, null)

            val messageModel = Message(
                IntegrationType.VK, text, fromId.toLong(), peerId.toLong(), messageId, payload,
                ReplyMessage(replymessageId, replyMessageFromId, replymessagetext),
                FwdMessages(fwdMessageId, fwdMessageFromId, fwdMessagemessagetext),
                Attachments(photo = photo(photoData.album_id, photoData.date, photoData.owner_id, photoData.sizes))
            )

            eventService.dispatchEvent(VkGroupNewMessageEvent(messageModel, this))
        }
    }


    override fun getProviderType(): ProviderType = ProviderType.GROUP


}