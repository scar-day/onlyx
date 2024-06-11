
package net.polix.system.localization
/**
 *
 * AUTHOR: ScarDay
 * CREATED AT: 02/11/2023 | 21:57
 *
 **/


import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import net.polix.system.LOGGER
import org.springframework.stereotype.Service
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.net.URL
import java.net.URLConnection
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@Service
class LocalizationService {
    private val PATTERN = Regex("\\$@?\\w+\\$")

    private val messages = ConcurrentHashMap<String, String>()

    private lateinit var project: String
    private lateinit var repository: String

    fun reloadLocalization() {
        messages.clear()
        parseFromRepository(project, repository)
    }

    var totalLines: Int = 0
    var totalKeys:Int= 0

    fun parseFromRepository(project: String, repository: String) {
        val url = URL("https://api.github.com/repos/$project/$repository/zipball/")

        this.project = project
        this.repository = repository

        totalLines = 0
        totalKeys = 0

        val connection = url.openConnection()

        connection.useCaches = false
        connection.setRequestProperty("User-Agent", "LocalizationService")

        val files = fetchFiles(connection)

        val byLanguage = ConcurrentHashMap<String, String>()

        for ((_, fileContent) in files) {
            if (fileContent.isNullOrEmpty() || fileContent.isBlank()) {
                LOGGER.warn("[LocalizationService] <-> Failed to register, no data.")
                continue
            }

            val lines = fileContent.lines().size
            totalLines += lines
            val root = JsonParser.parseString(fileContent).asJsonObject

            totalKeys += root.keySet().size
            readObject(byLanguage, root)
        }

        totalLines -= files.size

        LOGGER.info("[LocalizationService] <-> Registered lines: $totalLines, keys: $totalKeys")

        reload(byLanguage)
    }

    private fun fetchFiles(connection: URLConnection): Map<String, String> {
        val files = HashMap<String, String>()

        connection.getInputStream().use { inputStream ->
            BufferedInputStream(inputStream, 8192).use { bufferedInputStream ->
                ZipInputStream(bufferedInputStream).use { zipInputStream ->
                    var zipEntry: ZipEntry?

                    val readBuffer = ByteArray(8192)
                    val writeBuffer = ByteArrayOutputStream()

                    while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                        val path = zipEntry!!.name.split("/")

                        if (isValidPath(path)) {
                            continue
                        }

                        var n: Int

                        while (zipInputStream.read(readBuffer).also { n = it } != -1) {
                            writeBuffer.write(readBuffer, 0, n)
                        }

                        val fileContents = writeBuffer.toString()
                        writeBuffer.reset()


                        val filePath = path.drop(1).joinToString(":")

                        files[filePath] = fileContents
                    }
                }
            }
        }

        return files
    }

    private fun isValidPath(path: List<String>): Boolean {
        return path.size < 3 || path[1] != "lang" || !path.last().endsWith(".json")
    }

    private fun readObject(map: MutableMap<String, String>, jsonObject: JsonObject) {
        for ((key, value) in jsonObject.entrySet()) {
            val message = try {
                when {
                    value.isJsonObject -> {
                        val nestedMap = mutableMapOf<String, String>()
                        readObject(nestedMap, value.asJsonObject)
                        nestedMap.toString()
                    }
                    value.isJsonArray -> {
                        val array = value.asJsonArray
                        array.joinToString(", ", "[", "]") { it.toString() }
                    }
                    else -> "\"${value.asString}\""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            map[key] = message ?: "No found key"
        }
    }

    private fun reload(messageMap: Map<String, String>) {
        if (messageMap.isNotEmpty()) {
            val messagesMap = mutableMapOf<String, String>()

            for ((key, value) in messageMap) {
                val loadedMessage = loadMessage(messageMap, value)
                if (loadedMessage != null) {
                    messagesMap[key] = loadedMessage
                }
            }

            messages.clear()
            messages.putAll(messagesMap)
        } else {
            messages.clear()
        }
    }

    private fun loadMessage(messages: Map<String, String>, value: String?): String? {
        if (value == null) {
            return null
        }

        var message = value

        val matcher = PATTERN.find(message)

        while (matcher != null) {
            val rawText = matcher.value
            val text = rawText.substring(1, rawText.length - 1)

            val foundMessage: String = if (text[0] == '@') {
                val environmentName = text.substring(1)
                loadMessage(messages, environmentName).toString()
            } else {
                loadMessage(messages, messages[text] ?: text).toString()
            }

            message = message?.replace(rawText, foundMessage)
        }

        return message
    }

    private fun String.replacePlaceholders(placeholders: Array<String>?): String {
        var replaceMessage = this

        for (i in placeholders?.indices?.step(2)!!) {
            val placeholderKey = placeholders[i]
            val placeholderValue = placeholders[i + 1]

            replaceMessage = replaceMessage.replace(placeholderKey, placeholderValue)
        }

        return replaceMessage
    }

    private fun getLang(languageType: LanguageType?, key: String): String? {
        val languageCode = languageType?.name?.uppercase() ?: "RU"
        val languageMessages = messages[key] ?: return null

        return try {
            val jsonString = if (languageMessages.trimEnd().endsWith("]")) {
                languageMessages.trimEnd() + "}"
            } else {
                languageMessages
            }

            val jsonElement = JsonParser.parseString(jsonString)

            when {
                jsonElement.isJsonObject -> {
                    jsonElement.asJsonObject.get(languageCode)?.let { value ->
                        if (!value.isJsonNull) {
                            when {
                                value.isJsonArray -> {
                                    value.asJsonArray.joinToString("\n") { it.asString }
                                }
                                value.isJsonPrimitive -> value.asString
                                else -> null
                            }
                        } else {
                            null
                        }
                    }
                }
                jsonElement.isJsonArray -> {
                    jsonElement.asJsonArray.joinToString("\n") { it.asString }
                }
                else -> null
            }
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }

    fun getObject(key: String): String? {
        val message = messages[key] ?: return null

        return try {
            val jsonString = message.trimEnd()

            if (jsonString.startsWith("[") && jsonString.endsWith("]")) {
                val jsonElement = JsonParser.parseString(jsonString)
                when {
                    jsonElement.isJsonObject -> jsonElement.asJsonObject.toString()
                    jsonElement.isJsonArray -> jsonElement.asJsonArray.joinToString("\n") { it.asString }
                    jsonElement.isJsonPrimitive -> jsonElement.asString
                    else -> null
                }
            } else {
                jsonString
            }
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun findMessagePlaceholders(lang: LanguageType, key: String, values: Array<String>?): String {
        val language = getLang(lang, key)
        if (language == null) LOGGER.info("[LocalizationService] <-> Ключ $key не найден.")

        return language?.replacePlaceholders(values) ?: key
    }

    fun findMessage(lang: LanguageType, key: String): String? {
        val language = getLang(lang, key)
        if (language == null) LOGGER.info("[LocalizationService] <-> Ключ $key не найден.")
        return getLang(lang, key) ?: key
    }

    fun findMessage(key: String): String? {
        val getMessage = getObject(key)
        if (getMessage == null) LOGGER.info("[LocalizationService] <-> Ключ $key не найден.")

        return getMessage ?: key
    }

    fun findMessagePlaceholders(key: String, values: Array<String>): String {
        val getMessage = getObject(key)
        if (getMessage == null) LOGGER.info("[LocalizationService] <-> Ключ $key не найден.")
        val message = getObject(key)

        return message?.replacePlaceholders(values) ?: key
    }


}
