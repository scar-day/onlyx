package net.polix.system.integration.route.vkontakte.provider.proxy

import com.vk.api.sdk.httpclient.HttpTransportClient
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.DefaultProxyRoutePlanner
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager


class HttpTransportClientWithProxy(
    retryAttemptsNetworkErrorCount: Int,
    retryAttemptsInvalidStatusCount: Int,
    proxy: Proxy,
) :
    HttpTransportClient() {
    constructor(proxy: Proxy) : this(3, 3, proxy)

    init {
        this.retryAttemptsNetworkErrorCount = retryAttemptsNetworkErrorCount
        this.retryAttemptsInvalidStatusCount = retryAttemptsInvalidStatusCount
        val cookieStore = BasicCookieStore()
        val requestConfig =
            RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(5000).setConnectionRequestTimeout(5000)
                .setCookieSpec("standard").build()
        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.maxTotal = 300
        connectionManager.defaultMaxPerRoute = 300


        val proxyAuth = BasicCredentialsProvider()
        proxyAuth.setCredentials(
            AuthScope(proxy.getIp(), proxy.getPort()),
            UsernamePasswordCredentials(proxy.getUser(), proxy.getPassword())
        )

        httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .setDefaultCookieStore(cookieStore)
            .setDefaultCredentialsProvider(proxyAuth)
            .setRoutePlanner(DefaultProxyRoutePlanner(HttpHost(proxy.getIp(), proxy.getPort())))
            .setUserAgent("Java VK SDK/1.0")
            .build()
    }
}