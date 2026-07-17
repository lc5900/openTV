package com.lc5900.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.lc5900.tv.data.ChannelRepository
import com.lc5900.tv.ui.ChannelViewModel
import com.lc5900.tv.ui.OpenTvApp
import com.lc5900.tv.ui.theme.OpenTvTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ChannelViewModel> {
        ChannelViewModel.Factory(ChannelRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenTvTheme {
                OpenTvApp(viewModel = viewModel)
            }
        }
    }
}
