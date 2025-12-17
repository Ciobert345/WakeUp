package com.gemini.wol.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gemini.wol.domain.usecase.SendWakePacketUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class WakeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sendWakePacketUseCase: SendWakePacketUseCase,
    private val scheduleManager: com.gemini.wol.worker.ScheduleManager,
    private val scheduleRepository: com.gemini.wol.data.repository.ScheduleRepository,
    private val pcRepository: com.gemini.wol.data.repository.PcRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val pcId = inputData.getString("pcId") ?: return@withContext Result.failure()
        
        // 1. Send Wake Packet
        val result = sendWakePacketUseCase(pcId)

        val pc = pcRepository.getPcById(pcId)

        if (result.isSuccess && pc != null) {
            // Update Last Seen
            pcRepository.updateLastSeen(pcId, System.currentTimeMillis())
            
            // Show Notification
            showNotification(applicationContext, pc.name)
        }

        // 2. Reschedule Next Occurrence!
        val hour = inputData.getInt("hour", -1)
        val minute = inputData.getInt("minute", -1)
        val daysBitmap = inputData.getInt("daysBitmap", -1)

        if (hour != -1 && minute != -1 && daysBitmap != -1 && pc != null) {
             scheduleManager.scheduleNextWake(pc, hour, minute, daysBitmap)
        }

        if (result.isSuccess) {
             Result.success()
        } else {
             Result.success() 
        }
    }

    private fun showNotification(context: Context, pcName: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val builder = androidx.core.app.NotificationCompat.Builder(context, "wol_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Use app icon
            .setContentTitle("Scheduled Wake-on-LAN")
            .setContentText("Magic Packet sent to $pcName")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override suspend fun getForegroundInfo(): androidx.work.ForegroundInfo {
        val pcId = inputData.getString("pcId") ?: "Unknown"
        // We can try to fetch name or just use generic.
        // Since we are in suspend function, we *could* fetch name from repo, but for ForegroundInfo speed is key.
        val notificationId = pcId.hashCode()
        val notification = androidx.core.app.NotificationCompat.Builder(applicationContext, "wol_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Wake-on-LAN")
            .setContentText("Executing scheduled wake-up...")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .build()
            
        return androidx.work.ForegroundInfo(notificationId, notification)
    }
}
