package com.ciobert.wol.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ciobert.wol.util.UpdateManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UpdateDownloadReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var updateManager: UpdateManager
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("UpdateDownloadReceiver", "Received broadcast: ${intent.action}")
        
        if (intent.action == "com.ciobert.wol.ACTION_DOWNLOAD_UPDATE") {
            val url = intent.getStringExtra("download_url")
            val version = intent.getStringExtra("version")
            
            Log.d("UpdateDownloadReceiver", "Download URL: $url, Version: $version")
            
            if (url != null && version != null) {
                updateManager.downloadAndInstall(url, "WakeUp-$version.apk")
            } else {
                Log.e("UpdateDownloadReceiver", "Missing URL or version in intent")
            }
        }
    }
}
