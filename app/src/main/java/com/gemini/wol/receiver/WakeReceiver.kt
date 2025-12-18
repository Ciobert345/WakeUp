package com.gemini.wol.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PowerManager
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

    override fun onReceive(context: Context, intent: Intent) {
        val pcId = intent.getStringExtra("pcId") ?: return
        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", -1)
        val daysBitmap = intent.getIntExtra("daysBitmap", -1)

        val pendingResult = goAsync()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeUp:WakeReceiverExecute")
        wakeLock.acquire(3 * 60 * 1000L) // 3 minutes timeout

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Send Wake Packet
                val result = sendWakePacketUseCase(pcId)

                val pc = pcRepository.getPcById(pcId)

                if (result.isSuccess && pc != null) {
                    // Update Last Seen
                    pcRepository.updateLastSeen(pcId, System.currentTimeMillis())

                    // Show Notification
                    showNotification(context, pc.name)
                }

                // 2. Reschedule Next Occurrence
                if (hour != -1 && minute != -1 && daysBitmap != -1 && pc != null) {
                    scheduleManager.scheduleNextWake(pc, hour, minute, daysBitmap)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                wakeLock.release()
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
