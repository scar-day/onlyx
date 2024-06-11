package net.polix.system.utility

import net.polix.system.utility.data.Patterns

/**
 *
 * AUTHOR: ScarDay
 * CREATED AT: 06/11/2023 | 22:09
 *
 **/
object PatternUtility {
    fun getAddress(text: String?): String? {
        if (text == null) return null
        return extractDomain(text) ?: extractIp(text)
    }

    private fun extractDomain(text: String): String? {
        val matcher = Patterns.DOMAIN_NAME.matcher(text)
        return if (matcher.find()) matcher.group() else null
    }

    private fun extractIp(text: String): String? {
        val matcher = Patterns.IP_ADDRESS.matcher(text)
        return if (matcher.find()) matcher.group() else null
    }
}