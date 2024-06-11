package net.polix.system.command

import net.polix.system.localization.LanguageType
import net.polix.system.dialog.UserCommand
import net.polix.system.dialog.dialog.UserCommandDialog
import net.polix.system.startTime
import net.polix.system.utility.getTimeLeft
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.management.ManagementFactory
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.system.exitProcess


class CoreCommand : UserCommand(
        "core", "coordinator", "ядро",  description = "COMMAND_CORE_DESCRIPTION", permission = "core.access"
)
{
    override fun UserCommandDialog.handleInput() {
        if (args.size == 1) {
            responseReply = "Правильное использование данной подкоманды:\n— /$command lang » Отдает информацию о локализации\n— /$command stop » Останавливает координатор\n— /$command system » Информация о системе"
            return
        }

        when (args[1].lowercase()) {
            "lang", "локализация" -> {
                if (!user.has("command.core.lang")) {
                    responseReply = localizationService.findMessagePlaceholders(user.lang, "NO_PERMISSION", arrayOf("%permission%", "command.core.lang"))
                    return
                }
                responseReply = "Всего ключей: ${localizationService.totalKeys}\n" +
                        "Всего строк: ${localizationService.totalLines}\n" +
                        "Всего языков: ${LanguageType.values().size}\n" +
                        "Доступные языки: ${LanguageType.values().joinToString(", ")}"
            }
            "stop", "выключить" -> {
                if (!user.has("command.core.stop")) {
                    responseReply = localizationService.findMessagePlaceholders(user.lang, "NO_PERMISSION", arrayOf("%permission%", "command.core.stop"))
                    return
                }
                responseReply = "Выключаюсь..."
                exitProcess(-1)
            }

            "peer" -> {
                responseReply = "ℹ\uFE0FPeerId этого чата: ${message!!.peerId}"
            }

            "system", "система" -> {
                if (!user.has("command.core.memory")) {
                    responseReply = localizationService.findMessagePlaceholders(user.lang, "NO_PERMISSION", arrayOf("%permission%", "command.core.memory"))
                    return
                }

                val runtime = Runtime.getRuntime()

                val processor = runtime.availableProcessors()

                val system = System.getProperty("os.name")
                val systemVersion = System.getProperty("os.version")

                val allInMemory = runtime.maxMemory() / 1048576L
                val totalMemory = runtime.totalMemory() / 1048576L
                val freeMemory = runtime.freeMemory() / 1048576L
                val memoryUsed = totalMemory - freeMemory

                val percentUsedMeory = String.format("%.2f", getUsageMemory())

                val uptimeServer = getServerUptime()
                val uptimeProcess = getTimeLeft((System.currentTimeMillis() - startTime) / 1000L)

                val osBean = ManagementFactory.getOperatingSystemMXBean()
                val memoryUsage = osBean.systemLoadAverage * 10

                val usedProccessor = String.format("%.2f", memoryUsage)

                responseReply = "ℹ\uFE0FИнформация о системе:\n" +
                        "— Ядер: $processor шт.\n" +
                        "— Система: $system, $systemVersion\n" +
                        "— Используется процессор: $usedProccessor%\n" +
                        "— Используется ОЗУ: $percentUsedMeory%\n" +
                        "— Процессор: ${getProcessorModel()}\n" +
                        "\n" +
                        "ℹ\uFE0FАптайм системы:\n" +
                        "— Аптайм сервера: $uptimeServer\n" +
                        "— Аптайм процесса: $uptimeProcess\n" +
                        "\n" +
                        "ℹ\uFE0FИспользование ОЗУ:\n" +
                        "— Используется: $memoryUsed мб.\n" +
                        "— Свободно: $freeMemory мб.\n" +
                        "— Выделено: $totalMemory мб.\n" +
                        "— Всего: $allInMemory мб."
            }

            "reload" -> {
                if (!user.has("command.core.reload")) {
                    responseReply = localizationService.findMessagePlaceholders(user.lang, "NO_PERMISSION", arrayOf("%permission%", "command.core.reload"))
                    return
                }
                localizationService.reloadLocalization()
                responseReply = "Успешно!\nПолучено ключей: ${localizationService.totalKeys}\nПолучено строк: ${localizationService.totalLines}"
            }

            else -> {
                responseReply = "Вы ввели не верную подкоманду."
            }
        }
    }

    private fun getProcessorModel(): String {
        val cpuInfoFile = File("/proc/cpuinfo")
        val cpuInfoLines = cpuInfoFile.readLines()

        var processorModel = ""
        for (line in cpuInfoLines) {
            if (line.startsWith("model name")) {
                processorModel = line.substringAfter(":").trim()
                break
            }
        }

        if (processorModel.isEmpty()) {
            return "Не удалось получить процессор из /proc/cpuinfo"
        }

        return processorModel
    }

    private fun getUsageMemory(): Double {
        var totalMemory: Long
        var usedMemory: Long

        val memoryInfoProcess = Runtime.getRuntime().exec("free -m")
        val memoryReader = BufferedReader(InputStreamReader(memoryInfoProcess.inputStream))

        memoryReader.useLines { lines ->
            lines.forEachIndexed { index, line ->
                if (index == 1) {
                    val parts = line.trim().split("\\s+".toRegex())
                    totalMemory = parts[1].toLong()
                    usedMemory = parts[2].toLong()

                    return usedMemory.toDouble() / totalMemory * 100
                }
            }
        }

        return 0.0
    }


    private fun getServerUptime(): String {
        val uptimeFile = File("/proc/uptime")
        val content = uptimeFile.readText()
        val uptimeSeconds = content.split(" ")[0].toDouble()

        val uptimeDuration = Duration.ofSeconds(uptimeSeconds.toLong())
        val uptimeLocalDateTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC).plus(uptimeDuration)

        val builder = StringBuilder()
        val days = uptimeDuration.toDays()

        val hours = uptimeLocalDateTime.hour
        val minutes = uptimeLocalDateTime.minute
        val seconds = uptimeLocalDateTime.second

        if (days > 0) builder.append("$days дн. ")
        if (hours > 0) builder.append("$hours ч. ")
        if (minutes > 0) builder.append("$minutes мин. ")

        builder.append("$seconds сек.")

        return builder.toString()
    }
}