package com.shu.ktor

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

class JsonDecoder{
    companion object {
        val instance = JsonDecoder()
        val jsonDecoder: Json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
    }

    inline fun<reified T> decode(json:String): T {
        return jsonDecoder.decodeFromString(json)
    }

    fun<T:Any> decodeString(deserializationStrategy: DeserializationStrategy<T>, json:String): T? {
        return jsonDecoder.decodeFromString(deserializationStrategy, json)
    }

    fun<T:Any> encodeString(serializationStrategy: SerializationStrategy<T>, data: T): String? {
        return jsonDecoder.encodeToString(serializationStrategy, data)
    }
}