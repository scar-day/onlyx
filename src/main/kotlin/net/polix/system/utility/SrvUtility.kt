package net.polix.system.utility

import net.polix.system.LOGGER
import org.xbill.DNS.*
import java.net.InetAddress

fun resolveIp(ip: String): String {
    try {
        val srvRecords = Lookup("_minecraft._tcp.$ip", Type.SRV)
        val srvResult = srvRecords.run()

        if (srvResult?.isNotEmpty() == true) {
            val srvRecord = srvResult[0] as SRVRecord
            val targetDomain = srvRecord.target.toString().replaceAfterLast(".", "")
            val target = targetDomain
            val srvPort = srvRecord.port
            val numericIp = InetAddress.getByName(target).hostAddress
            return "$numericIp:$srvPort"
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    try {
        val aRecords = Lookup(ip, Type.A)
        val aResult = aRecords.run()

        return if (aResult?.isNotEmpty() == true) {
            val aRecord = aResult[0] as ARecord
            aRecord.rdataToString()
        } else {
            ip
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return "<не удалось получить>"
}

fun resolveSrv(ip: String, port: Int): Pair<String, Int> {
    try {
        val lookup = Lookup("_minecraft._tcp.$ip", Type.SRV)
        val records: Array<Record>? = lookup.run()

        if (records != null && lookup.result == Lookup.SUCCESSFUL) {
            for (record in records) {
                if (record is SRVRecord) {
                    val portAddress = record.port
                    val address = record.target.toString().dropLast(1)
                    return Pair(address, portAddress)
                }
            }
        }
    } catch (e: Exception) {
        LOGGER.info(e.stackTraceToString())
    }

    return Pair(ip, port)
}
