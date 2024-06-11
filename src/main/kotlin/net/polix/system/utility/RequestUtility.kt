package net.polix.system.utility

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture


val GSON = Gson()


object RequestUtility {

    fun <T> sendRequestAsync(url: String, clazz: Class<T>): CompletableFuture<T> {
        return CompletableFuture.supplyAsync {
            val request = HttpClient.newHttpClient()
                .sendAsync(HttpRequest.newBuilder()
                    .uri(URI.create(url.replace(" ", "%20")))
                    .GET()
                    .header("Accept", "application/json")
                    .build(),
                    HttpResponse.BodyHandlers.ofString())
                .thenApply { response -> response.body() }
                .join()

            return@supplyAsync GSON.fromJson(request, clazz)
        }
    }

    fun <T> sendRequest(url: String, clazz: Class<T>): T {

        val request = HttpClient.newHttpClient()
            .sendAsync(HttpRequest.newBuilder()
                .uri(URI.create(url.replace(" ", "%20")))
                .GET()
                .header("Accept", "application/json")
                .build(),
                HttpResponse.BodyHandlers.ofString())
            .thenApply { response -> response.body() }
            .join()

        return GSON.fromJson(request, clazz)
    }

    fun sendRequestAsync(url: String?): CompletableFuture<JsonObject> {
        val future = CompletableFuture<JsonObject>()

        Thread {
            try {
                val request = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder()
                        .uri(URI(url?.replace(" ", "%20")!!))
                        .GET()
                        .setHeader("Accept", "application/json")
                        .build(),
                        HttpResponse.BodyHandlers.ofString())

                val response = Gson().fromJson(request.body(), JsonObject::class.java)
                future.complete(response)

            } catch (ex: Throwable) {
                ex.printStackTrace()
                future.completeExceptionally(ex)
            }
        }.start()

        return future
    }

 }