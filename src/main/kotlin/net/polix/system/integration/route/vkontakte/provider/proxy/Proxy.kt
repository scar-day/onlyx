package net.polix.system.integration.route.vkontakte.provider.proxy

data class Proxy(
    private val ip: String,
    private val port: Int,
    private val user: String,
    private val password: String
) {
    fun getIp(): String {
        return ip
    }

    fun getPort(): Int {
        return port
    }

    fun getUser(): String {
        return user
    }

    fun getPassword(): String {
        return password
    }
}
