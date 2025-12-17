package com.gemini.wol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gemini.wol.ui.theme.WakeOnLanSchedulerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: com.gemini.wol.ui.viewmodel.MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val currentTheme = viewModel.currentTheme.collectAsState(initial = com.gemini.wol.data.repository.AppTheme.SYSTEM).value
            val accentColor = viewModel.accentColor.collectAsState(initial = 0xFF6200EE.toInt()).value
            
            // Disable dynamic color to prefer our custom accent
            WakeOnLanSchedulerTheme(
                appTheme = currentTheme,
                accentColor = accentColor,
                dynamicColor = false 
            ) {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                   com.gemini.wol.ui.navigation.WolNavigation()
                }
            }
        }
    }
}
