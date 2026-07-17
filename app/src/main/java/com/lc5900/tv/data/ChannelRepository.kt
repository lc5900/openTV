package com.lc5900.tv.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lc5900.tv.R
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.put

private val Context.channelDataStore by preferencesDataStore(name = "channel_catalog")

class ChannelRepository(private val context: Context) {
    suspend fun loadChannels(): List<TvChannel> {
        val savedCatalog = context.channelDataStore.data.first()[CATALOG_KEY]
        return savedCatalog?.let(::parseChannels) ?: loadBuiltInChannels()
    }

    suspend fun loadSubscriptionUrl(): String =
        context.channelDataStore.data.first()[SUBSCRIPTION_URL_KEY].orEmpty()

    suspend fun saveChannels(channels: List<TvChannel>) {
        require(channels.isNotEmpty()) { "频道列表不能为空" }
        context.channelDataStore.edit { preferences ->
            preferences[CATALOG_KEY] = encodeChannels(channels)
        }
    }

    suspend fun subscribe(subscriptionUrl: String): List<TvChannel> {
        val url = normalizeNetworkUrl(subscriptionUrl) ?: error("请输入有效的 HTTP 或 HTTPS 订阅地址")
        val response = downloadCatalog(url)
        val channels = parseSubscription(response)
        require(channels.isNotEmpty()) { "订阅中没有可播放的频道" }

        context.channelDataStore.edit { preferences ->
            preferences[CATALOG_KEY] = encodeChannels(channels)
            preferences[SUBSCRIPTION_URL_KEY] = url
        }
        return channels
    }

    suspend fun restoreBuiltIn(): List<TvChannel> {
        val channels = loadBuiltInChannels()
        context.channelDataStore.edit { preferences ->
            preferences.remove(CATALOG_KEY)
            preferences.remove(SUBSCRIPTION_URL_KEY)
        }
        return channels
    }

    private fun loadBuiltInChannels(): List<TvChannel> =
        context.resources.openRawResource(R.raw.urls).bufferedReader().use { reader ->
            parseChannels(reader.readText())
        }

    private suspend fun downloadCatalog(url: String): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("Accept", "application/json, text/plain;q=0.9")
            connection.setRequestProperty("User-Agent", "OpenTV/2.0")

            val responseCode = connection.responseCode
            require(responseCode in 200..299) { "订阅请求失败：HTTP $responseCode" }
            connection.inputStream.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    require(total <= MAX_CATALOG_BYTES) { "订阅内容超过 2 MB 限制" }
                    output.write(buffer, 0, count)
                }
                output.toString(Charsets.UTF_8.name())
            }
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private val CATALOG_KEY = stringPreferencesKey("catalog_json")
        private val SUBSCRIPTION_URL_KEY = stringPreferencesKey("subscription_url")
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val READ_TIMEOUT_MS = 15_000
        private const val MAX_CATALOG_BYTES = 2 * 1024 * 1024

        fun normalizeNetworkUrl(value: String): String? = runCatching {
            val uri = URI(value.trim())
            value.trim().takeIf {
                uri.scheme?.lowercase() in setOf("http", "https") && !uri.host.isNullOrBlank()
            }
        }.getOrNull()

        fun parseChannels(json: String): List<TvChannel> =
            Json.parseToJsonElement(json).jsonArray.mapNotNull { element ->
                val item = element.jsonObject
                val id = item["id"]?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
                val name = item["name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val urls = item["urls"]?.jsonArray
                    ?.mapNotNull { it.jsonPrimitive.contentOrNull }
                    ?.map(String::trim)
                    ?.filter { normalizeNetworkUrl(it) != null }
                    ?.distinct()
                    .orEmpty()

                TvChannel(id = id, name = name.trim(), urls = urls)
                    .takeIf { name.isNotBlank() && urls.isNotEmpty() }
            }.distinctBy(TvChannel::id)

        fun parseSubscription(content: String): List<TvChannel> =
            if (content.trimStart().startsWith("[")) parseChannels(content) else parseM3u(content)

        fun parseM3u(content: String): List<TvChannel> {
            val entries = mutableListOf<Pair<String, String>>()
            var pendingName: String? = null

            content.lineSequence().forEach { rawLine ->
                val line = rawLine.trim()
                when {
                    line.startsWith("#EXTINF", ignoreCase = true) -> {
                        pendingName = line.substringAfterLast(',').trim().ifBlank { null }
                    }
                    line.isNotBlank() && !line.startsWith("#") -> {
                        normalizeNetworkUrl(line)?.let { url ->
                            val name = pendingName ?: URI(url).host ?: "未命名频道"
                            entries += name to url
                        }
                        pendingName = null
                    }
                }
            }

            return entries
                .groupBy(keySelector = Pair<String, String>::first, valueTransform = Pair<String, String>::second)
                .entries
                .mapIndexed { index, (name, urls) ->
                    TvChannel(id = index + 1, name = name, urls = urls.distinct())
                }
        }

        fun encodeChannels(channels: List<TvChannel>): String = buildJsonArray {
            channels.forEach { channel ->
                add(buildJsonObject {
                    put("id", channel.id)
                    put("name", channel.name)
                    put("urls", buildJsonArray { channel.urls.forEach { add(JsonPrimitive(it)) } })
                })
            }
        }.toString()
    }
}
