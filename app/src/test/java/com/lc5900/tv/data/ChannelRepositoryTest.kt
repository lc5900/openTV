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
    fun normalizePlaybackUrl_acceptsRtspButSubscriptionUrlDoesNot() {
        val rtsp = "rtsp://media.example.com/live"

        assertEquals(rtsp, ChannelRepository.normalizePlaybackUrl(rtsp))
        assertNull(ChannelRepository.normalizeNetworkUrl(rtsp))
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

    @Test
    fun parseSubscription_readsIptvMetadataAndResolvesRelativeUrls() {
        val m3u = """
            \uFEFF#EXTM3U
            #EXTINF:-1 tvg-id="news" tvg-name="备用名称" group-title="新闻",国际新闻
            streams/news.m3u8
            #EXTINF:-1 tvg-name="网络摄像头"
            rtsp://camera.example.com/live
        """.trimIndent().replace("\\uFEFF", "\uFEFF")

        val channels = ChannelRepository.parseSubscription(m3u, "https://example.com/catalog/list.m3u")

        assertEquals(2, channels.size)
        assertEquals("国际新闻", channels.first().name)
        assertEquals("新闻", channels.first().group)
        assertEquals("https://example.com/catalog/streams/news.m3u8", channels.first().urls.single())
        assertEquals("rtsp://camera.example.com/live", channels.last().urls.single())
    }
}
