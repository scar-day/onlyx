package net.polix.system.utility

import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object PastebinUtil {

    private const val BASE_URL = "https://paste.md-5.net"
    private val API_URL = "$BASE_URL/documents"

    fun getUrl(jsonParser: JsonParser, text: String): String? {
        val url = URL(API_URL)

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("User-Agent", "OnlyxBot")
        connection.setRequestProperty("charset", "utf-8")
        connection.setRequestProperty("Content-Type", "text/plain")

        connection.doOutput = true

        connection.outputStream.use { out ->
            out.write(text.toByteArray(StandardCharsets.UTF_8))
        }

        connection.inputStream.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonObject = jsonParser.parse(reader).asJsonObject
            val key = jsonObject.get("key")

            if (key != null) {
                return "$BASE_URL/${key.asString}"
            }
        }

        return null
    }
}
