package com.gemini.wol.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gemini.wol.R
import com.gemini.wol.data.repository.PcRepository
import com.gemini.wol.domain.usecase.SendWakePacketUseCase
import com.gemini.wol.worker.ScheduleManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WakeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sendWakePacketUseCase: SendWakePacketUseCase

    @Inject
    lateinit var scheduleManager: ScheduleManager

    @Inject
    lateinit var pcRepository: PcRepository
    
    companion object {
        private const val TAG = "WakeReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pcId = intent.getStringExtra("pcId") ?: return
        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", -1)
        val daysBitmap = intent.getIntExtra("daysBitmap", -1)

        Log.d(TAG, "WakeReceiver triggered for pcId: $pcId at ${System.currentTimeMillis()}")

        val pendingResult = goAsync()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, 
            "WakeUp:WakeReceiverExecute"
        )
        
        // Acquire with timeout - 3 minutes should be plenty for network operation
        wakeLock.acquire(3 * 60 * 1000L)
        Log.d(TAG, "WakeLock acquired")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Send Wake Packet
                val result = sendWakePacketUseCase(pcId)

                val pc = pcRepository.getPcById(pcId)

                if (result.isSuccess && pc != null) {
                    Log.d(TAG, "Wake packet sent successfully to ${pc.name}")
                    
                    // Update Last Seen
                    pcRepository.updateLastSeen(pcId, System.currentTimeMillis())

                    // Show Notification
                    showNotification(context, pc.name)
                } else {
                    Log.w(TAG, "Failed to send wake packet or PC not found")
                }

                // 2. Reschedule Next Occurrence
                if (hour != -1 && minute != -1 && daysBitmap != -1 && pc != null) {
                    scheduleManager.scheduleNextWake(pc, hour, minute, daysBitmap)
                    Log.d(TAG, "Next alarm scheduled for ${pc.name}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in WakeReceiver", e)
            } finally {
                // Ensure WakeLock is released even if an exception occurs
                if (wakeLock.isHeld) {
                    wakeLock.release()
                    Log.d(TAG, "WakeLock released")
                }
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, pcName: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val builder = NotificationCompat.Builder(context, "wol_channel")
            .setSmallIcon(R.mipmap.ic_launcher) // Using launcher icon temporarily if specific icon not available
            .setContentTitle("Scheduled Wake-on-LAN")
            .setContentText("Magic Packet sent to $pcName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
