package com.ciobert.wol.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ciobert.wol.R
import com.ciobert.wol.data.repository.PcRepository
import com.ciobert.wol.domain.usecase.SendWakePacketUseCase
import com.ciobert.wol.worker.ScheduleManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class WakeService : Service() {

    @Inject
    lateinit var sendWakePacketUseCase: SendWakePacketUseCase

    @Inject
    lateinit var scheduleManager: ScheduleManager

    @Inject
    lateinit var pcRepository: PcRepository
    
    @Inject
    lateinit var scheduleRepository: com.ciobert.wol.data.repository.ScheduleRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        private const val TAG = "WakeService"
        private const val NOTIFICATION_ID = 888
        private const val CHANNEL_ID = "wake_service_channel"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeUp:WakeServiceExecution").apply {
            acquire(5 * 60 * 1000L) // 5 minutes max
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pcId = intent?.getStringExtra("pcId") ?: run {
            stopSelf()
            return START_NOT_STICKY
        }
        val scheduleId = intent.getStringExtra("scheduleId")
        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", -1)
        val daysBitmap = intent.getIntExtra("daysBitmap", -1)

        Log.d(TAG, "WakeService started for pcId: $pcId (Schedule: ${scheduleId ?: "Legacy/Unknown"})")

        serviceScope.launch {
            try {
                val result = sendWakePacketUseCase(pcId)
                val pc = pcRepository.getPcById(pcId)

                if (result.isSuccess && pc != null) {
                    Log.d(TAG, "Wake packet(s) dispatched successfully for ${pc.name}")
                    pcRepository.updateLastSeen(pcId, System.currentTimeMillis())
                    showSuccessNotification(pc.name)
                }

                if (hour != -1 && minute != -1 && daysBitmap != -1 && pc != null) {
                    val schedules = scheduleRepository.getSchedulesForPc(pcId).first()
                    val resolvedScheduleId = scheduleId ?: schedules.find { 
                        it.timeHour == hour && 
                        it.timeMinute == minute && 
                        it.daysBitmap == daysBitmap 
                    }?.id
                    
                    if (resolvedScheduleId != null) {
                        scheduleManager.scheduleNextWake(pc, resolvedScheduleId, hour, minute, daysBitmap)
                    } else {
                        Log.w(TAG, "Could not resolve schedule ID for ${pc.name} at $hour:$minute. Alarm not rescheduled.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in WakeService", e)
            } finally {
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }
    
    private suspend fun findScheduleId(pcId: String, hour: Int, minute: Int, daysBitmap: Int): String? {
        return try {

            val schedules = scheduleRepository.getSchedulesForPc(pcId).first()
            schedules.find { 
                it.timeHour == hour && 
                it.timeMinute == minute && 
                it.daysBitmap == daysBitmap 
            }?.id
        } catch (e: Exception) {
            Log.e(TAG, "Error finding schedule ID", e)
            null
        }
    }

    private fun showSuccessNotification(pcName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        val successNotification = NotificationCompat.Builder(this, "wol_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Wake-on-LAN Inviato")
            .setContentText("Pacchetto Magic Packet inviato a $pcName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), successNotification)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sincronizzazione di rete")
            .setContentText("Invio pacchetto Wake-on-LAN in corso...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WOL Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        Log.d(TAG, "WakeService destroyed")
    }
}
