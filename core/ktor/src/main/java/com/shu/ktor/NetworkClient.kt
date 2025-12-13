package com.shu.ktor

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType


class NetworkClient() {
    val httpClient: HttpClient = createHttpClient()

    suspend inline fun <reified T> request(path: String): Result<T> {
        return try {
            val data = httpClient.get(path)
            print(data)
            val result = data.body<T>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend inline fun <reified T> request(
        path: String,
        method: Method,
        body: Any? = null,
    ): Result<T> {
        val url = "$path"
        return try {
            val data = when (method) {
                Method.GET -> httpClient.get(url) {
                    NetworkConfiguration.header.forEach {
                        header(it.key, it.value)
                    }
                }
                Method.POST -> httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                    NetworkConfiguration.header.forEach {
                        header(it.key, it.value)
                    }
                }
                Method.PUT -> httpClient.put(path) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
                Method.DELETE -> httpClient.delete(path) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            }
            val result = data.body<T>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}