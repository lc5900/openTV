package com.lc5900.tv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lc5900.tv.data.ChannelRepository
import com.lc5900.tv.data.TvChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChannelUiState(
    val channels: List<TvChannel> = emptyList(),
    val query: String = "",
    val subscriptionUrl: String = "",
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val settingsMessage: String? = null,
) {
    val visibleChannels: List<TvChannel>
        get() = channels.filter { it.name.contains(query.trim(), ignoreCase = true) }
}

class ChannelViewModel(private val repository: ChannelRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ChannelUiState())
    val uiState: StateFlow<ChannelUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun updateSubscriptionUrl(url: String) {
        _uiState.update { it.copy(subscriptionUrl = url, settingsMessage = null) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                repository.loadChannels() to repository.loadSubscriptionUrl()
            }.onSuccess { (channels, subscriptionUrl) ->
                _uiState.update {
                    it.copy(
                        channels = channels,
                        subscriptionUrl = subscriptionUrl,
                        isLoading = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "频道数据读取失败")
                }
            }
        }
    }

    fun syncSubscription() {
        val url = _uiState.value.subscriptionUrl
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, settingsMessage = null) }
            runCatching { repository.subscribe(url) }
                .onSuccess { channels ->
                    _uiState.update {
                        it.copy(
                            channels = channels,
                            isSyncing = false,
                            settingsMessage = "订阅成功，已更新 ${channels.size} 个频道",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isSyncing = false, settingsMessage = error.message ?: "订阅失败")
                    }
                }
        }
    }

    fun restoreBuiltInCatalog() {
        viewModelScope.launch {
            runCatching { repository.restoreBuiltIn() }
                .onSuccess { channels ->
                    _uiState.update {
                        it.copy(
                            channels = channels,
                            subscriptionUrl = "",
                            settingsMessage = "已恢复内置频道列表",
                        )
                    }
                }
                .onFailure(::showSettingsError)
        }
    }

    fun addSource(channelId: Int, url: String) {
        changeSources(channelId) { sources ->
            val normalized = ChannelRepository.normalizeNetworkUrl(url)
                ?: error("请输入有效的 HTTP 或 HTTPS 播放地址")
            require(normalized !in sources) { "该播放源已存在" }
            sources + normalized
        }
    }

    fun updateSource(channelId: Int, sourceIndex: Int, url: String) {
        changeSources(channelId) { sources ->
            val normalized = ChannelRepository.normalizeNetworkUrl(url)
                ?: error("请输入有效的 HTTP 或 HTTPS 播放地址")
            require(normalized !in sources.filterIndexed { index, _ -> index != sourceIndex }) {
                "该播放源已存在"
            }
            sources.toMutableList().apply { this[sourceIndex] = normalized }
        }
    }

    fun removeSource(channelId: Int, sourceIndex: Int) {
        changeSources(channelId) { sources ->
            require(sources.size > 1) { "每个频道至少需要保留一个播放源" }
            sources.toMutableList().apply { removeAt(sourceIndex) }
        }
    }

    fun clearSettingsMessage() {
        _uiState.update { it.copy(settingsMessage = null) }
    }

    private fun changeSources(channelId: Int, transform: (List<String>) -> List<String>) {
        viewModelScope.launch {
            runCatching {
                val updated = _uiState.value.channels.map { channel ->
                    if (channel.id == channelId) channel.copy(urls = transform(channel.urls)) else channel
                }
                repository.saveChannels(updated)
                updated
            }.onSuccess { channels ->
                _uiState.update { it.copy(channels = channels, settingsMessage = "播放源已保存") }
            }.onFailure(::showSettingsError)
        }
    }

    private fun showSettingsError(error: Throwable) {
        _uiState.update { it.copy(settingsMessage = error.message ?: "操作失败") }
    }

    class Factory(private val repository: ChannelRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ChannelViewModel(repository) as T
    }
}
