package com.lc5900.tv.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChannelRepositoryTest {
    @Test
    fun parseChannels_ignoresEntriesWithoutPlayableUrls() {
        val json = """
            [
              {"id":1,"name":"新闻","urls":["https://example.com/live.m3u8"]},
              {"id":2,"name":"无效频道","urls":[]}
            ]
        """.trimIndent()

        val channels = ChannelRepository.parseChannels(json)

        assertEquals(1, channels.size)
        assertEquals("新闻", channels.single().name)
    }

    @Test
    fun encodeChannels_roundTripsManualSources() {
        val channels = listOf(
            TvChannel(7, "测试频道", listOf("https://example.com/one.m3u8", "http://example.com/two.m3u8")),
        )

        assertEquals(channels, ChannelRepository.parseChannels(ChannelRepository.encodeChannels(channels)))
    }

    @Test
    fun normalizeNetworkUrl_rejectsUnsupportedSchemes() {
        assertNull(ChannelRepository.normalizeNetworkUrl("file:///data/local.json"))
        assertNull(ChannelRepository.normalizeNetworkUrl("not a url"))
        assertEquals(
            "https://example.com/channels.json",
            ChannelRepository.normalizeNetworkUrl(" https://example.com/channels.json "),
        )
    }

    @Test
    fun parseSubscription_mergesM3uEntriesWithTheSameName() {
        val m3u = """
            #EXTM3U
            #EXTINF:-1 group-title="央视",CCTV-1
            https://one.example/live.m3u8
            #EXTINF:-1,CCTV-1
            http://two.example/live.m3u8
            #EXTINF:-1,CCTV-2
            https://three.example/live.m3u8
        """.trimIndent()

        val channels = ChannelRepository.parseSubscription(m3u)

        assertEquals(2, channels.size)
        assertEquals(2, channels.first().urls.size)
        assertEquals("CCTV-2", channels.last().name)
    }
}
