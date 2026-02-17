package com.ciobert.wol.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ciobert.wol.R
import com.ciobert.wol.data.repository.PcRepository
import com.ciobert.wol.domain.usecase.SendWakePacketUseCase
import com.ciobert.wol.worker.ScheduleManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WakeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WakeReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pcId = intent.getStringExtra("pcId") ?: return
        
        Log.d(TAG, "WakeReceiver triggered for pcId: $pcId. Starting WakeService...")

        val serviceIntent = Intent(context, com.ciobert.wol.service.WakeService::class.java).apply {
            putExtras(intent)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
