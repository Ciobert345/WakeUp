package com.ciobert.wol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.ciobert.wol.ui.theme.WakeOnLanSchedulerTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.enableEdgeToEdge

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: com.ciobert.wol.ui.viewmodel.MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val splashScreen = installSplashScreen()
        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }
        
        enableEdgeToEdge()

        // Check for update intent (after super.onCreate so viewModel is initialized)
        if (intent?.action == "ACTION_UPDATE_DOWNLOAD") {
            val url = intent.getStringExtra("download_url")
            val version = intent.getStringExtra("version")
            if (url != null && version != null) {
                viewModel.startDownload(url, version)
            }
        }

        // Post a delayed action to hide the splash screen
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            keepSplash = false
        }, 800)
        
        
        setContent {
            val currentTheme = viewModel.currentTheme.collectAsState(initial = com.ciobert.wol.data.repository.AppTheme.SYSTEM).value
            val accentColor = viewModel.accentColor.collectAsState(initial = 0xFF6200EE.toInt()).value
            
            // Disable dynamic color to prefer our custom accent
            WakeOnLanSchedulerTheme(
                appTheme = currentTheme,
                accentColor = accentColor,
                dynamicColor = false 
            ) {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                   com.ciobert.wol.ui.navigation.WolNavigation()
                }
            }
        }
    }
}
